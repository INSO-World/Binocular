'use strict';

import path from 'node:path';
import { discoverDependencySources } from './dependencySources.js';
import { packageNameFromNodeModulesPath } from './packageLockDependencies.js';

const DECLARATIONS = ['dependencies', 'optionalDependencies', 'peerDependencies'];

function parse(text) {
  if (text && typeof text === 'object' && !Buffer.isBuffer(text)) return text;
  try {
    return JSON.parse(String(text || ''));
  } catch {
    return null;
  }
}

function namesFrom(info, fields) {
  return new Set(fields.flatMap((field) => Object.keys(info?.[field] || {})));
}

function legacyPackages(dependencies, parent = '', result = {}) {
  for (const [name, info] of Object.entries(dependencies || {})) {
    const location = parent ? `${parent}/node_modules/${name}` : `node_modules/${name}`;
    result[location] = {
      ...info,
      dependencies: { ...info.requires, ...info.dependencies },
    };
    legacyPackages(info.dependencies, location, result);
  }
  return result;
}

function resolveLocation(packages, directory, name) {
  let current = directory || '.';
  while (current) {
    const location = path.posix.join(current, 'node_modules', name);
    if (Object.hasOwn(packages, location)) return location;
    const parent = path.posix.dirname(current);
    if (parent === current) return null;
    current = parent;
  }
  return null;
}

export function directParentsByPackage(lockInput, manifestInput) {
  const lock = parse(lockInput);
  if (!lock) return new Map();
  const manifest = parse(manifestInput);
  const packages = lock.packages || legacyPackages(lock.dependencies);
  const rootNames = namesFrom(packages[''], [...DECLARATIONS, 'devDependencies']);
  if (!rootNames.size) for (const name of namesFrom(manifest, [...DECLARATIONS, 'devDependencies'])) rootNames.add(name);
  if (!lock.packages && !manifest) for (const name of Object.keys(lock.dependencies || {})) rootNames.add(name);
  const roots = new Set([...rootNames].map((name) => resolveLocation(packages, '', name)).filter(Boolean));

  for (const info of Object.values(packages)) {
    if (!info?.link || !info.resolved) continue;
    const workspace = String(info.resolved).replace(/^(?:file:)?(?:\.\/)?/, '');
    for (const name of namesFrom(packages[workspace], [...DECLARATIONS, 'devDependencies'])) {
      const location = resolveLocation(packages, workspace, name);
      if (location) roots.add(location);
    }
  }

  const parents = new Map();
  const edges = new Map();
  function describe(location) {
    if (edges.has(location)) return edges.get(location);
    const entry = packages[location];
    const linked = entry?.link && entry.resolved ? String(entry.resolved).replace(/^(?:file:)?(?:\.\/)?/, '') : null;
    const info = (linked && packages[linked]) || entry;
    const name = info?.name || packageNameFromNodeModulesPath(location);
    const children = [...namesFrom(info, DECLARATIONS)]
      .map((child) => resolveLocation(packages, linked || location, child))
      .filter(Boolean);
    const node = { name, version: info?.version, children };
    edges.set(location, node);
    return node;
  }

  for (const root of roots) {
    const directName = describe(root).name;
    if (!directName) continue;
    const visited = new Set([root]);
    const pending = [...describe(root).children];
    while (pending.length) {
      const location = pending.pop();
      if (visited.has(location)) continue;
      visited.add(location);
      if (roots.has(location)) continue;
      const { name, version, children } = describe(location);
      if (name && version) {
        const identity = JSON.stringify([name, version]);
        if (!parents.has(identity)) parents.set(identity, new Set());
        parents.get(identity).add(directName);
      }
      for (const child of children) pending.push(child);
    }
  }
  return new Map([...parents].map(([identity, names]) => [identity, [...names].sort()]));
}

export async function collectDirectAttribution(repo, branch) {
  const sources = await discoverDependencySources(repo, branch);
  const commits = await repo.getFirstParentCommits(branch);
  const relevant = new Set(
    (
      await repo.getFirstParentFilesCommits(
        branch,
        sources.flatMap(({ lockPath, manifestPath }) => [lockPath, manifestPath]),
      )
    ).map(({ oid }) => oid),
  );
  const out = new Map();
  const lastByComponent = new Map();
  for (let sequence = 0; sequence < commits.length; sequence++) {
    const commit = commits[sequence];
    if (!relevant.has(commit.oid)) continue;
    const parents = new Map();
    for (const source of sources) {
      const lock = await repo.readFileAtCommit(source.lockPath, commit.oid).catch(() => null);
      const manifest = await repo.readFileAtCommit(source.manifestPath, commit.oid).catch(() => null);
      const previous = lastByComponent.get(source.component);
      const map =
        previous && previous.lock === lock && previous.manifest === manifest ? previous.map : directParentsByPackage(lock, manifest);
      lastByComponent.set(source.component, { lock, manifest, map });
      parents.set(source.component, map);
    }
    out.set(commit.oid, { sequence, timestamp: Number(commit.commit.committer.timestamp), parents });
  }
  return out;
}
