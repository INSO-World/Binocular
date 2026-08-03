'use strict';

import debug from 'debug';

import LicenseComplianceSnapshot from '../../models/metrics/LicenseComplianceSnapshot.js';
import { getNpmPackageLicenses, preloadNpmTimesForPackages } from './npmRegistryTimes.js';
import { calculateLicenseComplianceTimeline } from './licenseComplianceTimeline.js';
import { collectBranchDirectDependencyHistory } from './dependencySources.js';

const log = debug('vuln-metrics:license-compliance');

export async function computeLicenseComplianceSnapshots(repo, currentBranch) {
  console.log('[LICENSE][START] collecting direct dependency histories');

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

  console.log(`[LICENSE][SCAN] branches=${branchNames.length} files=**/package-lock.json`);
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
      console.log(`[LICENSE][SCAN] branches=${progress} uniqueLockCommits=${uniqueCommits.size} packages=${packageNames.size}`);
    }
  }

  console.log(`[LICENSE][REGISTRY] loading license metadata for packages=${packageNames.size}`);
  await preloadNpmTimesForPackages([...packageNames], {
    concurrency: 8,
    onProgress: ({ completed, total }) => {
      if (completed % 100 === 0 || completed === total) console.log(`[LICENSE][REGISTRY] packages=${completed}/${total}`);
    },
  });

  const packageLicensesByName = new Map();
  for (const packageName of packageNames) {
    packageLicensesByName.set(packageName, await getNpmPackageLicenses(packageName));
  }

  const { snapshots, diagnostics } = calculateLicenseComplianceTimeline({ branchHistories, packageLicensesByName });
  console.log(`[LICENSE][PERSIST] snapshots=${snapshots.length}`);
  for (const branch of branchNames) await LicenseComplianceSnapshot.removeByBranch(branch);
  for (const snapshot of snapshots) await LicenseComplianceSnapshot.persist(snapshot);

  console.log(
    `[LICENSE][SUMMARY] ${JSON.stringify({
      rows: snapshots.length,
      packages: packageNames.size,
      lockPaths: [...lockPaths].sort(),
      branchesSkipped,
      ...diagnostics,
    })}`,
  );
  return snapshots;
}

export default computeLicenseComplianceSnapshots;
