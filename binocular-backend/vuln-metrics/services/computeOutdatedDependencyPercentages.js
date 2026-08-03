'use strict';

import debug from 'debug';

import OutdatedDependencySnapshot from '../../models/metrics/OutdatedDependencySnapshot.js';
import { getNpmPackageTimes, preloadNpmTimesForPackages } from './npmRegistryTimes.js';
import { calculateOutdatedDependencyTimeline } from './outdatedDependencyTimeline.js';
import { collectBranchDirectDependencyHistory } from './dependencySources.js';

const log = debug('vuln-metrics:outdated-dependencies');

export async function computeOutdatedDependencyPercentages(repo, currentBranch) {
  console.log('[OUTDATED][START] collecting branch dependency histories');

  const remoteBranches = await repo.getAllBranchesRemote().catch(() => []);
  const branchNames = [...new Set([currentBranch, ...remoteBranches].map((branch) => String(branch || '').trim()).filter(Boolean))]
    .filter((branch) => branch !== 'HEAD')
    .sort();
  const sourceCache = new Map();
  const branchHistories = [];
  const packageNames = new Set();
  const uniqueCommits = new Set();
  const lockPaths = new Set();
  let branchesSkipped = 0;

  console.log(`[OUTDATED][SCAN] branches=${branchNames.length} files=**/package-lock.json`);

  for (let branchIndex = 0; branchIndex < branchNames.length; branchIndex++) {
    const branch = branchNames[branchIndex];
    try {
      const history = await collectBranchDirectDependencyHistory(repo, branch, sourceCache);
      for (const source of history.sources) lockPaths.add(source.lockPath);
      for (const commit of history.commits) {
        uniqueCommits.add(commit.commitHash);
        for (const dependency of Object.values(commit.dependencies || {})) {
          if (dependency?.name) packageNames.add(dependency.name);
        }
      }
      if (history.commits.length) branchHistories.push({ branch, commits: history.commits });
    } catch (error) {
      branchesSkipped++;
      log(`skipping branch=${branch}: ${error?.message || String(error)}`);
    }
    if ((branchIndex + 1) % 10 === 0 || branchIndex + 1 === branchNames.length) {
      const progress = `${branchIndex + 1}/${branchNames.length}`;
      console.log(`[OUTDATED][SCAN] branches=${progress} uniqueLockCommits=${uniqueCommits.size} packages=${packageNames.size}`);
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
      lockPaths: [...lockPaths].sort(),
      branchesSkipped,
      ...diagnostics,
    })}`,
  );
  return snapshots;
}

export default computeOutdatedDependencyPercentages;
