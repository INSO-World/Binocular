'use strict';

import debug from 'debug';

import OutdatedDependencySnapshot from '../../models/metrics/OutdatedDependencySnapshot.js';
import { extractDependencies } from './packageLockDependencies.js';
import { getNpmPackageTimes, preloadNpmTimesForPackages } from './npmRegistryTimes.js';
import { calculateOutdatedDependencyTimeline } from './outdatedDependencyTimeline.js';

const log = debug('vuln-metrics:outdated-dependencies');

async function readDependencies(repo, commitHash) {
  let lock;
  try {
    lock = await repo.readFileAtCommit('package-lock.json', commitHash);
  } catch {
    return null;
  }

  let manifest = null;
  try {
    manifest = await repo.readFileAtCommit('package.json', commitHash);
  } catch {
    // package-lock v2/v3 contains the root dependency declarations.
  }

  const dependencies = extractDependencies(lock, manifest);
  return Object.fromEntries(Object.entries(dependencies).filter(([, dependency]) => dependency?.direct));
}

export async function computeOutdatedDependencyPercentages(repo, currentBranch) {
  console.log('[OUTDATED][START] collecting branch dependency histories');

  const remoteBranches = await repo.getAllBranchesRemote().catch(() => []);
  const branchNames = [...new Set([currentBranch, ...remoteBranches].map((branch) => String(branch || '').trim()).filter(Boolean))]
    .filter((branch) => branch !== 'HEAD')
    .sort();
  const commitCache = new Map();
  const branchHistories = [];
  const packageNames = new Set();
  let branchesSkipped = 0;

  console.log(`[OUTDATED][SCAN] branches=${branchNames.length} file=package-lock.json`);

  for (let branchIndex = 0; branchIndex < branchNames.length; branchIndex++) {
    const branch = branchNames[branchIndex];
    let commits;
    try {
      commits = await repo.getFirstParentFileCommits(branch, 'package-lock.json');
    } catch (error) {
      branchesSkipped++;
      log(`skipping branch=${branch}: ${error?.message || String(error)}`);
      continue;
    }

    const history = [];
    for (let sequence = 0; sequence < commits.length; sequence++) {
      const commit = commits[sequence];
      const commitHash = commit?.oid;
      if (!commitHash) continue;

      if (!commitCache.has(commitHash)) {
        const dependencies = await readDependencies(repo, commitHash);
        commitCache.set(commitHash, {
          commitHash,
          timestamp: Number(commit?.commit?.committer?.timestamp),
          dependencies,
        });
      }

      const cached = commitCache.get(commitHash);
      if (!cached?.dependencies || !Number.isFinite(cached.timestamp)) continue;
      for (const packageName of Object.keys(cached.dependencies)) {
        packageNames.add(packageName);
      }
      history.push({ ...cached, sequence });
    }

    if (history.length) branchHistories.push({ branch, commits: history });
    if ((branchIndex + 1) % 10 === 0 || branchIndex + 1 === branchNames.length) {
      const progress = `${branchIndex + 1}/${branchNames.length}`;
      console.log(`[OUTDATED][SCAN] branches=${progress} uniqueLockCommits=${commitCache.size} packages=${packageNames.size}`);
    }
  }

  console.log(`[OUTDATED][REGISTRY] loading release histories for packages=${packageNames.size}`);
  await preloadNpmTimesForPackages([...packageNames], {
    concurrency: 8,
    onProgress: ({ completed, total }) => {
      if (completed % 100 === 0 || completed === total) {
        console.log(`[OUTDATED][REGISTRY] packages=${completed}/${total}`);
      }
    },
  });
  const packageTimesByName = new Map();
  for (const packageName of packageNames) {
    packageTimesByName.set(packageName, await getNpmPackageTimes(packageName));
  }

  const { snapshots, diagnostics } = calculateOutdatedDependencyTimeline({
    branchHistories,
    packageTimesByName,
  });
  console.log(`[OUTDATED][PERSIST] snapshots=${snapshots.length}`);
  for (const branch of branchNames) {
    await OutdatedDependencySnapshot.removeByBranch(branch);
  }
  for (const snapshot of snapshots) {
    await OutdatedDependencySnapshot.persist(snapshot);
  }

  console.log(
    `[OUTDATED][SUMMARY] ${JSON.stringify({
      rows: snapshots.length,
      packages: packageNames.size,
      branchesSkipped,
      ...diagnostics,
    })}`,
  );
  return snapshots;
}

export default computeOutdatedDependencyPercentages;
