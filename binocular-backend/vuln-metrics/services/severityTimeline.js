'use strict';

export const SEVERITY_KEYS = Object.freeze(['CRITICAL', 'HIGH', 'MODERATE', 'LOW', 'MALICIOUS', 'UNKNOWN']);

export function normalizeSeverity(value) {
  const severity = String(value || '')
    .trim()
    .toUpperCase();
  if (severity === 'MEDIUM') return 'MODERATE';
  return SEVERITY_KEYS.includes(severity) ? severity : 'UNKNOWN';
}

function openKey(library, vulnerabilityId) {
  return `${library}||${vulnerabilityId}`;
}

function orderedEvents(events) {
  const input = Array.isArray(events) ? events : [];
  const allHaveSequence = input.every((event) => Number.isFinite(Number(event?.sequence)));

  return [...input].sort((left, right) => {
    if (allHaveSequence) {
      const sequenceDifference = Number(left.sequence) - Number(right.sequence);
      if (sequenceDifference) return sequenceDifference;
    }

    const timestampDifference = (Number(left?.timestamp) || 0) - (Number(right?.timestamp) || 0);
    if (timestampDifference) return timestampDifference;
    return String(left?.commitHash || '').localeCompare(String(right?.commitHash || ''));
  });
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
  const sortedEvents = orderedEvents(events);
  if (!sortedEvents.length) return { snapshots: [], diagnostics: { commitsProcessed: 0 } };

  const generatedAt = createdAt || new Date().toISOString();
  const commitOrder = [];
  const commitMetadata = new Map();

  for (const event of sortedEvents) {
    const commitHash = String(event?.commitHash || '').trim();
    if (!commitHash || commitMetadata.has(commitHash)) continue;

    commitOrder.push(commitHash);
    commitMetadata.set(commitHash, {
      timestamp: Number(event?.timestamp) || 0,
      sequence: Number.isFinite(Number(event?.sequence)) ? Number(event.sequence) : null,
    });
  }

  const transitionsByCommit = new Map();
  let triplesSeen = 0;

  for (const triple of Array.isArray(triples) ? triples : []) {
    triplesSeen++;
    const commitHash = String(triple?.event?.commitHash || '').trim();
    const library = String(triple?.event?.library || '').trim();
    const vulnerabilityId = String(triple?.vuln?.vulnId || '').trim();
    const relation = String(triple?.conn?.relation || '').toUpperCase();
    if (!commitHash || !library || !vulnerabilityId) continue;
    if (relation !== 'AFFECTS' && relation !== 'FIXES') continue;

    if (!transitionsByCommit.has(commitHash)) transitionsByCommit.set(commitHash, new Map());
    const libraries = transitionsByCommit.get(commitHash);
    if (!libraries.has(library)) libraries.set(library, { affects: new Map(), fixes: new Set() });

    const transitions = libraries.get(library);
    if (relation === 'AFFECTS') transitions.affects.set(vulnerabilityId, normalizeSeverity(triple?.vuln?.severity));
    else transitions.fixes.add(vulnerabilityId);
  }

  const counts = Object.fromEntries(SEVERITY_KEYS.map((severity) => [severity, 0]));
  const openVulnerabilities = new Map();
  let affectsApplied = 0;
  let fixesApplied = 0;
  let severityChanges = 0;

  function openVulnerability(library, vulnerabilityId, severity) {
    const key = openKey(library, vulnerabilityId);
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

  function closeVulnerability(library, vulnerabilityId) {
    const key = openKey(library, vulnerabilityId);
    const severity = openVulnerabilities.get(key);
    if (!severity) return;

    counts[severity]--;
    openVulnerabilities.delete(key);
  }

  const snapshots = [];

  for (const commitHash of commitOrder) {
    const libraryTransitions = transitionsByCommit.get(commitHash);
    for (const [library, transitions] of libraryTransitions || []) {
      for (const vulnerabilityId of transitions.fixes) {
        closeVulnerability(library, vulnerabilityId);
        fixesApplied++;
      }
      for (const [vulnerabilityId, severity] of transitions.affects) {
        openVulnerability(library, vulnerabilityId, severity);
        affectsApplied++;
      }
    }

    const metadata = commitMetadata.get(commitHash);
    snapshots.push({
      branch,
      commitHash,
      sequence: metadata?.sequence ?? null,
      date: metadata?.timestamp ? new Date(metadata.timestamp * 1000).toISOString() : generatedAt,
      ...snapshotCounts(counts),
      totalOpen: openVulnerabilities.size,
      createdAt: generatedAt,
    });
  }

  return {
    snapshots,
    diagnostics: {
      commitsProcessed: commitOrder.length,
      triplesSeen,
      affectsApplied,
      fixesApplied,
      severityChanges,
      openNow: openVulnerabilities.size,
      countsNow: { ...counts },
    },
  };
}
