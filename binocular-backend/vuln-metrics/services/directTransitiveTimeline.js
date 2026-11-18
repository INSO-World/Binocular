'use strict';

import { dependencyIdentity, eventComponent, vulnerabilityInstanceKey } from './dependencyIdentity.js';
import { normalizeSeverity } from './severityTimeline.js';

function dependencyTypeForEvent(event) {
  const value = String(event?.dependencyType || '').toUpperCase();
  if (value === 'DIRECT' || value === 'TRANSITIVE' || value === 'ABSENT') return value;
  return event?.direct === true ? 'DIRECT' : 'TRANSITIVE';
}

export function calculateDirectTransitiveTimeline({ events, triples, branch = 'main', attribution = new Map(), createdAt } = {}) {
  const generatedAt = createdAt || new Date().toISOString();
  const generatedTime = Date.parse(generatedAt);
  const commits = new Map();
  for (const [commitHash, metadata] of attribution) {
    commits.set(commitHash, { ...metadata, commitHash, events: [], fixes: new Set(), affects: new Map() });
  }
  for (const event of events || []) {
    if (!event?.commitHash || !event.library || (event.branchName && event.branchName !== branch)) continue;
    if (!commits.has(event.commitHash)) {
      const sequence = Number(event.sequence ?? Number.NaN);
      commits.set(event.commitHash, {
        commitHash: event.commitHash,
        timestamp: Number(event.timestamp),
        sequence: Number.isFinite(sequence) ? sequence : null,
        events: [],
        fixes: new Set(),
        affects: new Map(),
      });
    }
    commits.get(event.commitHash).events.push(event);
  }
  const ordered = [...commits.values()];
  const useSequence = ordered.every(({ sequence }) => Number.isFinite(sequence));
  ordered.sort(
    (a, b) => (useSequence ? a.sequence - b.sequence : 0) || a.timestamp - b.timestamp || a.commitHash.localeCompare(b.commitHash),
  );
  if (!ordered.length) return { snapshots: [], diagnostics: { commitsProcessed: 0 } };

  const disclosureDates = new Set();
  let triplesSeen = 0;
  for (const triple of triples || []) {
    triplesSeen++;
    const { event, vuln, conn } = triple;
    const commit = commits.get(event?.commitHash);
    const library = String(event?.library || '').trim();
    const vulnerabilityId = String(vuln?.vulnId || '').trim();
    if (!commit || !library || !vulnerabilityId || (event.branchName && event.branchName !== branch)) continue;
    const key = vulnerabilityInstanceKey(event, vulnerabilityId, library);
    const relation = String(conn?.relation || '').toUpperCase();
    if (relation === 'FIXES') commit.fixes.add(key);
    if (relation === 'AFFECTS') {
      const disclosure = Date.parse(vuln.publishedAt);
      if (Number.isFinite(disclosure)) disclosureDates.add(disclosure);
      commit.affects.set(key, {
        identity: dependencyIdentity(event, library),
        library,
        component: eventComponent(event),
        vulnerabilityId,
        severity: normalizeSeverity(vuln.severity),
        disclosure,
      });
    }
  }

  const libraries = new Map();
  const open = new Map();
  const pending = new Map();
  const pendingByTime = new Map();
  const disclosureTimes = [...disclosureDates].sort((a, b) => a - b);
  const snapshots = [];
  let parents = new Map();
  let disclosureIndex = 0;
  let previousCommit;
  let effectiveTime = -Infinity;

  function cancelPending(key) {
    pendingByTime.get(pending.get(key))?.delete(key);
    pending.delete(key);
  }

  function activateDisclosures(time) {
    const records = pendingByTime.get(time);
    for (const [key, record] of records || []) {
      pending.delete(key);
      open.set(key, record);
    }
    pendingByTime.delete(time);
    disclosureIndex++;
    return Boolean(records?.size);
  }

  function appendSnapshot(commit, time, disclosureSnapshot = false) {
    const instances = [];
    let directOpen = 0;
    for (const { identity, library, component, vulnerabilityId, severity } of open.values()) {
      const state = libraries.get(identity);
      const dependencyType = state?.type || 'TRANSITIVE';
      if (dependencyType === 'ABSENT') continue;
      if (dependencyType === 'DIRECT') directOpen++;
      const directParents =
        dependencyType === 'TRANSITIVE' ? parents.get(component)?.get(JSON.stringify([library, state?.version])) || [] : [];
      instances.push({ library, component, vulnerabilityId, severity, dependencyType, directParents });
    }
    snapshots.push({
      branch,
      commitHash: commit.commitHash,
      sequence: commit.sequence,
      date: new Date(time).toISOString(),
      directOpen,
      transitiveOpen: instances.length - directOpen,
      totalOpen: instances.length,
      instances,
      createdAt: generatedAt,
      ...(disclosureSnapshot ? { disclosureSnapshot: true } : {}),
    });
  }

  for (const commit of ordered) {
    const time = Number.isFinite(commit.timestamp) ? commit.timestamp * 1000 : generatedTime;
    effectiveTime = Math.max(effectiveTime, time);
    while (disclosureTimes[disclosureIndex] < effectiveTime) {
      const disclosure = disclosureTimes[disclosureIndex];
      if (activateDisclosures(disclosure)) appendSnapshot(previousCommit, disclosure, true);
    }

    if (commit.parents) parents = commit.parents;
    for (const event of commit.events) {
      libraries.set(dependencyIdentity(event), { type: dependencyTypeForEvent(event), version: event.newVersion });
    }

    for (const key of commit.fixes) {
      cancelPending(key);
      open.delete(key);
    }
    for (const [key, record] of commit.affects) {
      cancelPending(key);
      if (record.disclosure > effectiveTime) {
        if (!pendingByTime.has(record.disclosure)) pendingByTime.set(record.disclosure, new Map());
        pendingByTime.get(record.disclosure).set(key, record);
        pending.set(key, record.disclosure);
      } else {
        open.set(key, record);
      }
    }
    if (disclosureTimes[disclosureIndex] === effectiveTime) activateDisclosures(effectiveTime);
    appendSnapshot(commit, effectiveTime);
    previousCommit = commit;
  }

  while (disclosureTimes[disclosureIndex] <= generatedTime) {
    const disclosure = disclosureTimes[disclosureIndex];
    if (activateDisclosures(disclosure)) appendSnapshot(previousCommit, disclosure, true);
  }

  return {
    snapshots,
    diagnostics: { commitsProcessed: commits.size, triplesSeen, openNow: snapshots[snapshots.length - 1].totalOpen },
  };
}
