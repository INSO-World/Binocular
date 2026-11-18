'use strict';

import semver from 'semver';
import { isVersionAffected } from './vulnerabilityRanges.js';

function normalizedVersion(value) {
  return typeof value === 'string' ? semver.valid(value.trim()) : null;
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

export function calculateOutdatedDependencyTimeline({
  branchHistories,
  packageTimesByName,
  advisoriesByPackage = new Map(),
  createdAt,
} = {}) {
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
        const dependencies = [];
        let outdatedCount = 0;
        let evaluatedCount = 0;

        for (const [identity, dependency] of Object.entries(commit?.dependencies || {})) {
          dependenciesSeen++;
          const name = dependency?.name || identity;
          const installedVersion = normalizedVersion(dependency?.version);
          const latestVersion = latestStableVersionAt(releaseTimes.get(name), timestamp);
          let freshness = 'UNKNOWN';
          if (installedVersion && latestVersion) {
            evaluatedCount++;
            dependenciesEvaluated++;
            freshness = semver.lt(installedVersion, latestVersion) ? 'OUTDATED' : 'CURRENT';
            if (freshness === 'OUTDATED') outdatedCount++;
          }
          const advisories = advisoriesByPackage.get(name) || [];
          let knownVulnerabilities = null;
          if (installedVersion) {
            knownVulnerabilities = advisories
              .filter((advisory) => Date.parse(advisory.publishedAt) <= timestamp * 1000 && isVersionAffected(installedVersion, advisory))
              .map((advisory) => advisory.vulnId)
              .sort();
          }
          dependencies.push({
            library: name,
            component: dependency?.component || 'root',
            installedVersion: dependency?.version || null,
            latestVersion,
            freshness,
            knownVulnerabilities,
          });
        }

        const totalCount = dependencies.length;
        evaluation = {
          outdatedPercentage: evaluatedCount ? (outdatedCount / evaluatedCount) * 100 : null,
          outdatedCount,
          evaluatedCount,
          totalCount,
          unknownCount: Math.max(0, totalCount - evaluatedCount),
          dependencies,
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
