'use strict';

import { dependencyIdentity } from './dependencyIdentity.js';

function dependencyTypeForEvent(event) {
  const value = String(event?.dependencyType || '').toUpperCase();
  if (value === 'DIRECT' || value === 'TRANSITIVE' || value === 'ABSENT') return value;
  if (typeof event?.direct === 'boolean') return event.direct ? 'DIRECT' : 'TRANSITIVE';
  return 'TRANSITIVE';
}

function openKey(identity, vulnerabilityId) {
  return `${identity}||${vulnerabilityId}`;
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

export function calculateDirectTransitiveTimeline({ events, triples, branch = 'main', createdAt } = {}) {
  const sortedEvents = orderedEvents(events);
  if (!sortedEvents.length) return { snapshots: [], diagnostics: { commitsProcessed: 0 } };

  const generatedAt = createdAt || new Date().toISOString();
  const commitOrder = [];
  const seenCommits = new Set();
  const commitMetadata = new Map();
  const commitLibraryTypes = new Map();

  for (const event of sortedEvents) {
    const commitHash = String(event?.commitHash || '');
    const library = String(event?.library || '').trim();
    if (!commitHash || !library) continue;

    if (!seenCommits.has(commitHash)) {
      seenCommits.add(commitHash);
      commitOrder.push(commitHash);
      commitMetadata.set(commitHash, {
        timestamp: Number(event?.timestamp) || 0,
        sequence: Number.isFinite(Number(event?.sequence)) ? Number(event.sequence) : null,
      });
    }

    if (!commitLibraryTypes.has(commitHash)) commitLibraryTypes.set(commitHash, new Map());
    commitLibraryTypes.get(commitHash).set(dependencyIdentity(event, library), dependencyTypeForEvent(event));
  }

  const transitionsByCommit = new Map();
  let triplesSeen = 0;

  for (const triple of Array.isArray(triples) ? triples : []) {
    triplesSeen++;
    const commitHash = String(triple?.event?.commitHash || '');
    const library = String(triple?.event?.library || '').trim();
    const vulnerabilityId = String(triple?.vuln?.vulnId || '').trim();
    const relation = String(triple?.conn?.relation || '').toUpperCase();
    if (!commitHash || !library || !vulnerabilityId) continue;
    if (relation !== 'AFFECTS' && relation !== 'FIXES') continue;

    const identity = dependencyIdentity(triple?.event, library);
    if (!transitionsByCommit.has(commitHash)) transitionsByCommit.set(commitHash, new Map());
    const libraries = transitionsByCommit.get(commitHash);
    if (!libraries.has(identity)) libraries.set(identity, { affects: new Set(), fixes: new Set() });

    const transitions = libraries.get(identity);
    if (relation === 'AFFECTS') transitions.affects.add(vulnerabilityId);
    else transitions.fixes.add(vulnerabilityId);
  }

  const libraryTypes = new Map();
  const openVulnerabilities = new Map();
  const openKeysByLibrary = new Map();
  let directOpen = 0;
  let transitiveOpen = 0;
  let affectsApplied = 0;
  let fixesApplied = 0;
  let reclassifications = 0;

  function reclassifyLibrary(identity, nextType) {
    const keys = openKeysByLibrary.get(identity);
    if (!keys) return;

    for (const key of keys) {
      const record = openVulnerabilities.get(key);
      if (!record || record.dependencyType === nextType) continue;

      if (record.dependencyType === 'DIRECT') directOpen--;
      if (record.dependencyType === 'TRANSITIVE') transitiveOpen--;
      if (nextType === 'DIRECT') directOpen++;
      if (nextType === 'TRANSITIVE') transitiveOpen++;
      record.dependencyType = nextType;
      reclassifications++;
    }
  }

  function openVulnerability(identity, vulnerabilityId, dependencyType) {
    const key = openKey(identity, vulnerabilityId);
    if (openVulnerabilities.has(key)) return;

    openVulnerabilities.set(key, { identity, vulnerabilityId, dependencyType });
    if (!openKeysByLibrary.has(identity)) openKeysByLibrary.set(identity, new Set());
    openKeysByLibrary.get(identity).add(key);
    if (dependencyType === 'DIRECT') directOpen++;
    if (dependencyType === 'TRANSITIVE') transitiveOpen++;
  }

  function closeVulnerability(identity, vulnerabilityId) {
    const key = openKey(identity, vulnerabilityId);
    const record = openVulnerabilities.get(key);
    if (!record) return;

    if (record.dependencyType === 'DIRECT') directOpen--;
    if (record.dependencyType === 'TRANSITIVE') transitiveOpen--;
    openVulnerabilities.delete(key);

    const keys = openKeysByLibrary.get(identity);
    if (!keys) return;
    keys.delete(key);
    if (!keys.size) openKeysByLibrary.delete(identity);
  }

  const snapshots = [];

  for (const commitHash of commitOrder) {
    const libraryTypeChanges = commitLibraryTypes.get(commitHash);
    for (const [identity, nextType] of libraryTypeChanges || []) {
      const previousType = libraryTypes.get(identity);
      libraryTypes.set(identity, nextType);
      if (previousType && previousType !== nextType) reclassifyLibrary(identity, nextType);
    }

    const libraryTransitions = transitionsByCommit.get(commitHash);
    for (const [identity, transitions] of libraryTransitions || []) {
      const currentType = libraryTypes.get(identity) || 'TRANSITIVE';

      for (const vulnerabilityId of transitions.fixes) {
        closeVulnerability(identity, vulnerabilityId);
        fixesApplied++;
      }
      for (const vulnerabilityId of transitions.affects) {
        openVulnerability(identity, vulnerabilityId, currentType);
        affectsApplied++;
      }
    }

    const metadata = commitMetadata.get(commitHash);
    snapshots.push({
      branch,
      commitHash,
      sequence: metadata?.sequence ?? null,
      date: metadata?.timestamp ? new Date(metadata.timestamp * 1000).toISOString() : generatedAt,
      directOpen,
      transitiveOpen,
      totalOpen: directOpen + transitiveOpen,
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
      reclassifications,
      openNow: openVulnerabilities.size,
      directNow: directOpen,
      transitiveNow: transitiveOpen,
    },
  };
}
