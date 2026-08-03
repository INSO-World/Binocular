'use strict';

import debug from 'debug';

import LicenseComplianceSnapshot from '../../models/metrics/LicenseComplianceSnapshot.js';
import { extractDependencies } from './packageLockDependencies.js';
import { getNpmPackageLicenses, preloadNpmTimesForPackages } from './npmRegistryTimes.js';
import { calculateLicenseComplianceTimeline } from './licenseComplianceTimeline.js';

const log = debug('vuln-metrics:license-compliance');

async function readDirectDependencies(repo, commitHash) {
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
    // package-lock v2/v3 contains root and workspace declarations.
  }

  const dependencies = extractDependencies(lock, manifest);
  return Object.fromEntries(Object.entries(dependencies).filter(([, dependency]) => dependency?.direct));
}

export async function computeLicenseComplianceSnapshots(repo, currentBranch) {
  console.log('[LICENSE][START] collecting direct dependency histories');

  const remoteBranches = await repo.getAllBranchesRemote().catch(() => []);
  const branchNames = [...new Set([currentBranch, ...remoteBranches].map((branch) => String(branch || '').trim()).filter(Boolean))]
    .filter((branch) => branch !== 'HEAD')
    .sort();
  const commitCache = new Map();
  const branchHistories = [];
  const packageNames = new Set();
  let branchesSkipped = 0;

  console.log(`[LICENSE][SCAN] branches=${branchNames.length} file=package-lock.json`);
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
        const dependencies = await readDirectDependencies(repo, commitHash);
        commitCache.set(commitHash, {
          commitHash,
          timestamp: Number(commit?.commit?.committer?.timestamp),
          dependencies,
        });
      }

      const cached = commitCache.get(commitHash);
      if (!cached?.dependencies || !Number.isFinite(cached.timestamp)) continue;
      for (const packageName of Object.keys(cached.dependencies)) packageNames.add(packageName);
      history.push({ ...cached, sequence });
    }

    if (history.length) branchHistories.push({ branch, commits: history });
    if ((branchIndex + 1) % 10 === 0 || branchIndex + 1 === branchNames.length) {
      const progress = `${branchIndex + 1}/${branchNames.length}`;
      console.log(`[LICENSE][SCAN] branches=${progress} uniqueLockCommits=${commitCache.size} packages=${packageNames.size}`);
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
      branchesSkipped,
      ...diagnostics,
    })}`,
  );
  return snapshots;
}

export default computeLicenseComplianceSnapshots;
