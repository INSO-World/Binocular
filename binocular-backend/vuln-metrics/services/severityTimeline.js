'use strict';

import { vulnerabilityInstanceKey } from './dependencyIdentity.js';

export const SEVERITY_KEYS = Object.freeze(['CRITICAL', 'HIGH', 'MODERATE', 'LOW', 'MALICIOUS', 'UNKNOWN']);

export function normalizeSeverity(value) {
  const severity = String(value || '')
    .trim()
    .toUpperCase();
  if (severity === 'MEDIUM') return 'MODERATE';
  return SEVERITY_KEYS.includes(severity) ? severity : 'UNKNOWN';
}

function hasSequence(event) {
  return event?.sequence != null && Number.isFinite(Number(event.sequence));
}

function orderedEvents(events) {
  const allHaveSequence = events.every(hasSequence);

  return [...events].sort((left, right) => {
    if (allHaveSequence) {
      const sequenceDifference = Number(left.sequence) - Number(right.sequence);
      if (sequenceDifference) return sequenceDifference;
    }

    const timestampDifference = (Number(left?.timestamp) || 0) - (Number(right?.timestamp) || 0);
    if (timestampDifference) return timestampDifference;
    return String(left?.commitHash || '').localeCompare(String(right?.commitHash || ''));
  });
}

function belongsToBranch(event, branch) {
  return !event?.branchName || event.branchName === branch;
}

function snapshotCounts(counts) {
  return {
    criticalOpen: counts.CRITICAL,
    highOpen: counts.HIGH,
    moderateOpen: counts.MODERATE,
    lowOpen: counts.LOW,
    maliciousOpen: counts.MALICIOUS,
    unknownOpen: counts.UNKNOWN,
  };
}

export function calculateSeverityTimeline({ events, triples, branch = 'main', createdAt } = {}) {
  const branchEvents = Array.isArray(events) ? events.filter((event) => belongsToBranch(event, branch)) : [];
  const sortedEvents = orderedEvents(branchEvents);
  if (!sortedEvents.length) return { snapshots: [], diagnostics: { commitsProcessed: 0 } };

  const generatedAt = createdAt || new Date().toISOString();
  const generatedTime = Date.parse(generatedAt);
  const commitMetadata = new Map();
  let effectiveTime = -Infinity;

  for (const event of sortedEvents) {
    const commitHash = String(event?.commitHash || '').trim();
    if (!commitHash || commitMetadata.has(commitHash)) continue;

    const timestamp = Number(event?.timestamp) * 1000;
    const commitTime = event?.timestamp != null && Number.isFinite(timestamp) ? timestamp : generatedTime;
    // A descendant's dependency state cannot become effective before its parent.
    effectiveTime = Math.max(effectiveTime, commitTime);
    commitMetadata.set(commitHash, {
      commitHash,
      time: effectiveTime,
      sequence: hasSequence(event) ? Number(event.sequence) : null,
    });
  }

  const transitionsByCommit = new Map();
  const pendingByTime = new Map();
  let triplesSeen = 0;

  for (const triple of Array.isArray(triples) ? triples : []) {
    triplesSeen++;
    const commitHash = String(triple?.event?.commitHash || '').trim();
    const library = String(triple?.event?.library || '').trim();
    const vulnerabilityId = String(triple?.vuln?.vulnId || '').trim();
    const relation = String(triple?.conn?.relation || '').toUpperCase();
    if (!commitMetadata.has(commitHash) || !belongsToBranch(triple?.event, branch) || !library || !vulnerabilityId) continue;
    if (relation !== 'AFFECTS' && relation !== 'FIXES') continue;

    const key = vulnerabilityInstanceKey(triple.event, vulnerabilityId, library);
    if (!transitionsByCommit.has(commitHash)) transitionsByCommit.set(commitHash, { affects: new Map(), fixes: new Set() });
    const transitions = transitionsByCommit.get(commitHash);
    if (relation === 'FIXES') {
      transitions.fixes.add(key);
    } else {
      const publishedAt = Date.parse(triple.vuln.publishedAt);
      transitions.affects.set(key, { severity: normalizeSeverity(triple.vuln.severity), publishedAt });
      if (publishedAt > commitMetadata.get(commitHash).time && !pendingByTime.has(publishedAt)) {
        pendingByTime.set(publishedAt, new Map());
      }
    }
  }

  const counts = Object.fromEntries(SEVERITY_KEYS.map((severity) => [severity, 0]));
  const openVulnerabilities = new Map();
  const pendingTimes = [...pendingByTime.keys()].sort((left, right) => left - right);
  const pendingByKey = new Map();
  let nextDisclosure = 0;
  let affectsApplied = 0;
  let fixesApplied = 0;
  let severityChanges = 0;

  function openVulnerability(key, severity) {
    const existingSeverity = openVulnerabilities.get(key);
    if (existingSeverity) {
      if (existingSeverity !== severity) {
        counts[existingSeverity]--;
        counts[severity]++;
        openVulnerabilities.set(key, severity);
        severityChanges++;
      }
      return;
    }

    openVulnerabilities.set(key, severity);
    counts[severity]++;
  }

  function closeVulnerability(key) {
    const severity = openVulnerabilities.get(key);
    if (!severity) return;

    counts[severity]--;
    openVulnerabilities.delete(key);
  }

  const snapshots = [];

  function appendSnapshot({ commitHash, sequence }, time, disclosureSnapshot) {
    snapshots.push({
      branch,
      commitHash,
      sequence,
      date: new Date(time).toISOString(),
      ...snapshotCounts(counts),
      totalOpen: openVulnerabilities.size,
      createdAt: generatedAt,
      ...(disclosureSnapshot ? { disclosureSnapshot: true } : {}),
    });
  }

  function activateDisclosures(time) {
    const pending = pendingByTime.get(time);
    for (const [key, severity] of pending) {
      pendingByKey.delete(key);
      openVulnerability(key, severity);
    }
    nextDisclosure++;
    return pending.size > 0;
  }

  function cancelPendingDisclosure(key) {
    pendingByTime.get(pendingByKey.get(key))?.delete(key);
    pendingByKey.delete(key);
  }

  let previousCommit;
  for (const metadata of commitMetadata.values()) {
    while (pendingTimes[nextDisclosure] < metadata.time) {
      const time = pendingTimes[nextDisclosure];
      if (activateDisclosures(time)) appendSnapshot(previousCommit, time, true);
    }

    const transitions = transitionsByCommit.get(metadata.commitHash);
    for (const key of transitions?.fixes || []) {
      cancelPendingDisclosure(key);
      closeVulnerability(key);
      fixesApplied++;
    }
    for (const [key, { severity, publishedAt }] of transitions?.affects || []) {
      cancelPendingDisclosure(key);
      if (publishedAt > metadata.time) {
        pendingByTime.get(publishedAt).set(key, severity);
        pendingByKey.set(key, publishedAt);
      } else {
        openVulnerability(key, severity);
      }
      affectsApplied++;
    }
    if (pendingTimes[nextDisclosure] === metadata.time) activateDisclosures(metadata.time);
    appendSnapshot(metadata, metadata.time, false);
    previousCommit = metadata;
  }

  while (pendingTimes[nextDisclosure] <= generatedTime) {
    const time = pendingTimes[nextDisclosure];
    if (activateDisclosures(time)) appendSnapshot(previousCommit, time, true);
  }

  return {
    snapshots,
    diagnostics: {
      commitsProcessed: commitMetadata.size,
      triplesSeen,
      affectsApplied,
      fixesApplied,
      severityChanges,
      openNow: openVulnerabilities.size,
      countsNow: { ...counts },
    },
  };
}
