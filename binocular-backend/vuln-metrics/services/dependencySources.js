'use strict';

import path from 'path';

import { extractDependencies } from './packageLockDependencies.js';

export function componentFromLockPath(lockPath) {
  const directory = path.posix.dirname(String(lockPath || '').replaceAll('\\', '/'));
  return directory === '.' ? 'root' : directory;
}

export function dependencySourceFromLockPath(lockPath) {
  const normalizedLockPath = String(lockPath || '')
    .replaceAll('\\', '/')
    .replace(/^\.\//, '');
  const directory = path.posix.dirname(normalizedLockPath);
  return {
    component: componentFromLockPath(normalizedLockPath),
    lockPath: normalizedLockPath,
    manifestPath: directory === '.' ? 'package.json' : `${directory}/package.json`,
  };
}

export async function discoverDependencySources(repo, branch) {
  const files = new Set();
  for (const method of ['getFilePathsForBranch', 'getFilePathsForBranchRemote']) {
    if (typeof repo?.[method] !== 'function') continue;
    try {
      for (const filepath of await repo[method](branch)) files.add(String(filepath || '').replaceAll('\\', '/'));
    } catch {
      // The branch may exist only locally or only on the configured remote.
    }
  }

  const lockPaths = [...files]
    .filter(
      (filepath) => (filepath === 'package-lock.json' || filepath.endsWith('/package-lock.json')) && !filepath.includes('node_modules/'),
    )
    .sort();
  if (!lockPaths.length) lockPaths.push('package-lock.json');
  return lockPaths.map(dependencySourceFromLockPath);
}

export async function readDependencySourceAtCommit(repo, source, commitHash, cache = new Map()) {
  const key = `${source.lockPath}\0${commitHash}`;
  if (cache.has(key)) return cache.get(key);

  let lock = null;
  try {
    lock = await repo.readFileAtCommit(source.lockPath, commitHash);
  } catch {
    const missing = { ...source, exists: false, dependencies: {} };
    cache.set(key, missing);
    return missing;
  }

  let manifest = null;
  try {
    manifest = await repo.readFileAtCommit(source.manifestPath, commitHash);
  } catch {
    // npm lockfiles v2/v3 normally embed declaration metadata.
  }

  const result = { ...source, exists: true, dependencies: extractDependencies(lock, manifest) };
  cache.set(key, result);
  return result;
}

function mergeDirectDependencies(sourceStates) {
  const dependencies = {};
  for (const sourceState of sourceStates) {
    for (const [name, dependency] of Object.entries(sourceState.dependencies || {})) {
      if (!dependency?.direct) continue;
      const identity = `${sourceState.component}\0${name}`;
      dependencies[identity] = { ...dependency, name, component: sourceState.component };
    }
  }
  return dependencies;
}

export async function collectBranchDirectDependencyHistory(repo, branch, cache = new Map()) {
  const sources = await discoverDependencySources(repo, branch);
  let commits;
  if (typeof repo.getFirstParentFilesCommits === 'function') {
    commits = await repo.getFirstParentFilesCommits(
      branch,
      sources.map((source) => source.lockPath),
    );
  } else {
    const byHash = new Map();
    for (const source of sources) {
      for (const commit of await repo.getFirstParentFileCommits(branch, source.lockPath)) byHash.set(commit.oid, commit);
    }
    commits = [...byHash.values()].sort(
      (left, right) => Number(left?.commit?.committer?.timestamp) - Number(right?.commit?.committer?.timestamp),
    );
  }

  const history = [];
  for (let sequence = 0; sequence < commits.length; sequence++) {
    const commit = commits[sequence];
    if (!commit?.oid) continue;
    const states = [];
    for (const source of sources) states.push(await readDependencySourceAtCommit(repo, source, commit.oid, cache));
    history.push({
      commitHash: commit.oid,
      timestamp: Number(commit?.commit?.committer?.timestamp),
      sequence,
      dependencies: mergeDirectDependencies(states.filter((state) => state.exists)),
    });
  }

  return { branch, sources, commits: history };
}
