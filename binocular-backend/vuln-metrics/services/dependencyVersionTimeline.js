'use strict';

import { normalizeSeverity } from './severityTimeline.js';
import { dependencyIdentity, eventComponent } from './dependencyIdentity.js';

const SEVERITY_RANK = Object.freeze({
  NONE: 0,
  UNKNOWN: 1,
  LOW: 2,
  MODERATE: 3,
  HIGH: 4,
  CRITICAL: 5,
  MALICIOUS: 6,
});

function eventKey(event) {
  return `${String(event?.commitHash || '')}||${dependencyIdentity(event)}`;
}

function orderedEvents(events) {
  return [...(Array.isArray(events) ? events : [])].sort((left, right) => {
    const sequenceDifference = (Number(left?.sequence) || 0) - (Number(right?.sequence) || 0);
    if (sequenceDifference) return sequenceDifference;
    const timestampDifference = (Number(left?.timestamp) || 0) - (Number(right?.timestamp) || 0);
    if (timestampDifference) return timestampDifference;
    const commitDifference = String(left?.commitHash || '').localeCompare(String(right?.commitHash || ''));
    if (commitDifference) return commitDifference;
    return String(left?.library || '').localeCompare(String(right?.library || ''));
  });
}

function vulnerabilityDetails(vulnerability) {
  return {
    id: String(vulnerability?.vulnId || ''),
    severity: normalizeSeverity(vulnerability?.severity),
    title: vulnerability?.title ? String(vulnerability.title) : null,
    advisoryUrl: vulnerability?.advisoryUrl ? String(vulnerability.advisoryUrl) : null,
    cve: vulnerability?.cve ? String(vulnerability.cve) : null,
  };
}

function highestSeverity(vulnerabilities) {
  return vulnerabilities.reduce(
    (highest, vulnerability) => (SEVERITY_RANK[vulnerability.severity] > SEVERITY_RANK[highest] ? vulnerability.severity : highest),
    'NONE',
  );
}

export function calculateDependencyVersionTimeline({ events, triples, branch = 'main', createdAt } = {}) {
  const sortedEvents = orderedEvents(events).filter((event) => event?.commitHash && event?.library);
  if (!sortedEvents.length) {
    return { snapshots: [], diagnostics: { eventsProcessed: 0, triplesSeen: 0, vulnerableSnapshots: 0 } };
  }

  const transitionsByEvent = new Map();
  let triplesSeen = 0;
  for (const triple of Array.isArray(triples) ? triples : []) {
    triplesSeen++;
    const key = eventKey(triple?.event);
    const relation = String(triple?.conn?.relation || '').toUpperCase();
    const vulnerability = vulnerabilityDetails(triple?.vuln);
    if (!vulnerability.id || (relation !== 'AFFECTS' && relation !== 'FIXES')) continue;
    if (!transitionsByEvent.has(key)) transitionsByEvent.set(key, { affects: new Map(), fixes: new Set() });
    const transitions = transitionsByEvent.get(key);
    if (relation === 'AFFECTS') transitions.affects.set(vulnerability.id, vulnerability);
    else transitions.fixes.add(vulnerability.id);
  }

  const generatedAt = createdAt || new Date().toISOString();
  const openByLibrary = new Map();
  const snapshots = [];
  let vulnerableSnapshots = 0;

  for (const event of sortedEvents) {
    const library = String(event.library);
    const component = eventComponent(event);
    const identity = dependencyIdentity(event, library);
    if (!openByLibrary.has(identity)) openByLibrary.set(identity, new Map());
    const open = openByLibrary.get(identity);
    const transitions = transitionsByEvent.get(eventKey(event));

    for (const vulnerabilityId of transitions?.fixes || []) open.delete(vulnerabilityId);
    for (const [vulnerabilityId, vulnerability] of transitions?.affects || []) open.set(vulnerabilityId, vulnerability);

    const vulnerabilities = [...open.values()].sort((left, right) => left.id.localeCompare(right.id));
    const severity = highestSeverity(vulnerabilities);
    if (vulnerabilities.length) vulnerableSnapshots++;

    snapshots.push({
      branch: String(event.branchName || branch),
      library,
      component,
      commitHash: String(event.commitHash),
      sequence: Number.isFinite(Number(event.sequence)) ? Number(event.sequence) : null,
      date: new Date((Number(event.timestamp) || 0) * 1000).toISOString(),
      author: event.author ? String(event.author) : null,
      oldVersion: event.oldVersion === null || event.oldVersion === undefined ? null : String(event.oldVersion),
      newVersion: event.newVersion === null || event.newVersion === undefined ? null : String(event.newVersion),
      dependencyType: String(event.dependencyType || 'ABSENT').toUpperCase(),
      wasDependencyType: String(event.wasDependencyType || 'ABSENT').toUpperCase(),
      sourceType: String(event.sourceType || 'commit'),
      highestSeverity: severity,
      vulnerabilityCount: vulnerabilities.length,
      vulnerabilities,
      createdAt: generatedAt,
    });
  }

  return {
    snapshots,
    diagnostics: { eventsProcessed: snapshots.length, triplesSeen, vulnerableSnapshots },
  };
}

export default calculateDependencyVersionTimeline;
