'use strict';

const ROOT_DEPENDENCY_FIELDS = ['dependencies', 'devDependencies', 'optionalDependencies', 'peerDependencies'];

export function dependencyType(version, isDirect) {
  if (version === null || version === undefined) return 'ABSENT';
  return isDirect ? 'DIRECT' : 'TRANSITIVE';
}

function parseJson(value) {
  if (value && typeof value === 'object') return value;
  if (typeof value !== 'string' || !value.trim()) return null;

  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
}

function declaredDependencyNames(source) {
  const names = new Set();
  if (!source || typeof source !== 'object') return names;

  for (const field of ROOT_DEPENDENCY_FIELDS) {
    const dependencies = source[field];
    if (!dependencies || typeof dependencies !== 'object') continue;
    for (const name of Object.keys(dependencies)) names.add(name);
  }

  return names;
}

function workspacePatterns(rootPackage) {
  const workspaces = rootPackage?.workspaces;
  if (Array.isArray(workspaces)) return workspaces;
  if (Array.isArray(workspaces?.packages)) return workspaces.packages;
  return [];
}

function workspacePatternMatches(packagePath, pattern) {
  const normalized = String(pattern || '')
    .replace(/^\.\//, '')
    .replace(/\/$/, '');
  if (!normalized) return false;

  const globstar = '__GLOBSTAR__';
  const expression = normalized
    .replace(/[.+^${}()|[\]\\]/g, '\\$&')
    .replace(/\*\*/g, globstar)
    .replace(/\*/g, '[^/]*')
    .replace(new RegExp(globstar, 'g'), '.*');
  return new RegExp(`^${expression}$`).test(packagePath);
}

function declaredDependencyNamesFromPackages(packages) {
  const names = new Set();
  const declarationPaths = new Set(['']);
  const patterns = workspacePatterns(packages?.['']);

  for (const packageInfo of Object.values(packages || {})) {
    if (packageInfo?.link && packageInfo?.resolved) {
      declarationPaths.add(
        String(packageInfo.resolved)
          .replace(/^file:/, '')
          .replace(/^\.\//, ''),
      );
    }
  }

  for (const packagePath of Object.keys(packages || {})) {
    if (patterns.some((pattern) => workspacePatternMatches(packagePath, pattern))) declarationPaths.add(packagePath);
  }

  for (const [packagePath, packageInfo] of Object.entries(packages || {})) {
    if (!declarationPaths.has(packagePath)) continue;
    for (const name of declaredDependencyNames(packageInfo)) names.add(name);
  }
  return names;
}

export function packageNameFromNodeModulesPath(packagePath) {
  const marker = 'node_modules/';
  const path = String(packagePath || '');
  const markerIndex = path.lastIndexOf(marker);
  if (markerIndex < 0) return null;

  const parts = path
    .slice(markerIndex + marker.length)
    .split('/')
    .filter(Boolean);
  if (!parts.length) return null;
  if (!parts[0].startsWith('@')) return parts[0];
  return parts.length >= 2 ? `${parts[0]}/${parts[1]}` : null;
}

function installDepth(packagePath) {
  return String(packagePath || '').split('node_modules/').length - 1;
}

function chooseCandidate(candidates, name, candidate) {
  const existing = candidates.get(name);
  if (!existing) {
    candidates.set(name, candidate);
    return;
  }

  if (candidate.direct !== existing.direct) {
    if (candidate.direct) candidates.set(name, candidate);
    return;
  }

  if (candidate.depth < existing.depth) {
    candidates.set(name, candidate);
    return;
  }

  if (candidate.depth === existing.depth && candidate.path.localeCompare(existing.path) < 0) {
    candidates.set(name, candidate);
  }
}

function finalizeCandidates(candidates) {
  return Object.fromEntries(
    [...candidates.entries()]
      .sort(([left], [right]) => left.localeCompare(right))
      .map(([name, candidate]) => {
        const dependency = { version: candidate.version, direct: candidate.direct };
        if (candidate.license !== null && candidate.license !== undefined) dependency.license = candidate.license;
        return [name, dependency];
      }),
  );
}

function dependenciesFromPackages(lock, directNames) {
  const candidates = new Map();
  const packages = lock.packages;

  for (const [packagePath, packageInfo] of Object.entries(packages)) {
    if (!packagePath || !packagePath.includes('node_modules/')) continue;

    const installedName = packageNameFromNodeModulesPath(packagePath);
    if (!installedName) continue;

    const linkedInfo = packageInfo?.link && packageInfo?.resolved ? packages[packageInfo.resolved] : null;
    const resolvedInfo = linkedInfo || packageInfo;
    const version = resolvedInfo?.version || null;
    if (!version) continue;

    const name = resolvedInfo?.name || installedName;
    const depth = installDepth(packagePath);
    const direct = depth === 1 && directNames.has(installedName);

    chooseCandidate(candidates, name, {
      version,
      direct,
      depth,
      path: packagePath,
      license: resolvedInfo?.license ?? packageInfo?.license ?? null,
    });
  }

  return finalizeCandidates(candidates);
}

function dependenciesFromLegacyTree(lock, directNames) {
  const candidates = new Map();

  function walk(dependencies, depth, parentPath) {
    for (const [name, info] of Object.entries(dependencies || {})) {
      const path = `${parentPath}/node_modules/${name}`;
      const version = info?.version || null;

      if (version) {
        chooseCandidate(candidates, name, {
          version,
          direct: depth === 0 && directNames.has(name),
          depth: depth + 1,
          path,
          license: info?.license ?? null,
        });
      }

      walk(info?.dependencies, depth + 1, path);
    }
  }

  walk(lock.dependencies, 0, '');
  return finalizeCandidates(candidates);
}

export function extractDependencies(packageLock, packageManifest = null) {
  const lock = parseJson(packageLock);
  if (!lock) return {};

  const manifest = parseJson(packageManifest);
  let directNames = declaredDependencyNamesFromPackages(lock?.packages);

  if (!directNames.size) directNames = declaredDependencyNames(manifest);

  if (lock.packages && typeof lock.packages === 'object') {
    return dependenciesFromPackages(lock, directNames);
  }

  // A v1 lockfile does not contain the root manifest. The caller should pass
  // package.json; retaining this fallback keeps older call sites best-effort.
  if (!directNames.size) directNames = new Set(Object.keys(lock.dependencies || {}));
  return dependenciesFromLegacyTree(lock, directNames);
}

export function diffDependencies(previousDependencies, currentDependencies) {
  const previous = previousDependencies || {};
  const current = currentDependencies || {};
  const changes = [];

  for (const [name, currentInfo] of Object.entries(current)) {
    const previousInfo = previous[name];

    if (!previousInfo) {
      changes.push({
        name,
        oldVersion: null,
        newVersion: currentInfo.version,
        wasDependencyType: dependencyType(null, false),
        dependencyType: dependencyType(currentInfo.version, currentInfo.direct),
      });
      continue;
    }

    if (previousInfo.version !== currentInfo.version || previousInfo.direct !== currentInfo.direct) {
      changes.push({
        name,
        oldVersion: previousInfo.version,
        newVersion: currentInfo.version,
        wasDependencyType: dependencyType(previousInfo.version, previousInfo.direct),
        dependencyType: dependencyType(currentInfo.version, currentInfo.direct),
      });
    }
  }

  for (const [name, previousInfo] of Object.entries(previous)) {
    if (name in current) continue;

    changes.push({
      name,
      oldVersion: previousInfo.version,
      newVersion: null,
      wasDependencyType: dependencyType(previousInfo.version, previousInfo.direct),
      dependencyType: dependencyType(null, false),
    });
  }

  return changes;
}
