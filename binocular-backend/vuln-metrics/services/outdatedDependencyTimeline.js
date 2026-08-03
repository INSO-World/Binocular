'use strict';

import semver from 'semver';

function normalizedVersion(value) {
  const exact = semver.valid(String(value || '').trim());
  if (exact) return exact;

  const coerced = semver.coerce(String(value || '').trim());
  return coerced && semver.valid(coerced) ? coerced.version : null;
}

export function latestStableVersionAt(packageTimes, timestamp) {
  const snapshotTime = Number(timestamp) * 1000;
  if (!(packageTimes instanceof Map) || !Number.isFinite(snapshotTime)) {
    return null;
  }

  let latest = null;
  for (const [version, publishedAt] of packageTimes) {
    const normalized = semver.valid(String(version || '').trim());
    const releaseTime = new Date(publishedAt).getTime();
    if (!normalized || semver.prerelease(normalized) || !Number.isFinite(releaseTime) || releaseTime > snapshotTime) {
      continue;
    }
    if (!latest || semver.gt(normalized, latest)) latest = normalized;
  }

  return latest;
}

export function calculateOutdatedDependencyTimeline({ branchHistories, packageTimesByName, createdAt } = {}) {
  const histories = Array.isArray(branchHistories) ? branchHistories : [];
  const releaseTimes = packageTimesByName instanceof Map ? packageTimesByName : new Map();
  const generatedAt = createdAt || new Date().toISOString();
  const snapshots = [];
  const evaluationByCommit = new Map();
  let dependenciesSeen = 0;
  let dependenciesEvaluated = 0;

  for (const history of histories) {
    const branch = String(history?.branch || '').trim();
    if (!branch) continue;

    for (const commit of Array.isArray(history?.commits) ? history.commits : []) {
      const commitHash = String(commit?.commitHash || '').trim();
      const timestamp = Number(commit?.timestamp);
      if (!commitHash || !Number.isFinite(timestamp)) continue;

      let evaluation = evaluationByCommit.get(commitHash);
      if (!evaluation) {
        const dependencies = Object.entries(commit?.dependencies || {});
        let outdatedCount = 0;
        let evaluatedCount = 0;

        for (const [identity, dependency] of dependencies) {
          dependenciesSeen++;
          const name = dependency?.name || identity;
          const installedVersion = normalizedVersion(dependency?.version);
          const latestVersion = latestStableVersionAt(releaseTimes.get(name), timestamp);
          if (!installedVersion || !latestVersion) continue;

          evaluatedCount++;
          dependenciesEvaluated++;
          if (semver.lt(installedVersion, latestVersion)) outdatedCount++;
        }

        const totalCount = dependencies.length;
        evaluation = {
          outdatedPercentage: evaluatedCount ? (outdatedCount / evaluatedCount) * 100 : null,
          outdatedCount,
          evaluatedCount,
          totalCount,
          unknownCount: Math.max(0, totalCount - evaluatedCount),
        };
        evaluationByCommit.set(commitHash, evaluation);
      }

      snapshots.push({
        branch,
        commitHash,
        sequence: Number.isFinite(Number(commit?.sequence)) ? Number(commit.sequence) : null,
        date: new Date(timestamp * 1000).toISOString(),
        ...evaluation,
        createdAt: generatedAt,
      });
    }
  }

  return {
    snapshots,
    diagnostics: {
      branchesProcessed: histories.length,
      commitsProcessed: snapshots.length,
      uniqueCommitsEvaluated: evaluationByCommit.size,
      dependenciesSeen,
      dependenciesEvaluated,
    },
  };
}
