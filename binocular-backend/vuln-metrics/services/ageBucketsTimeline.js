'use strict';

import { vulnerabilityInstanceKey } from './dependencyIdentity.js';

const DAY_MS = 1000 * 60 * 60 * 24;

function asDate(value) {
  const date = value instanceof Date ? new Date(value) : new Date(value);
  return Number.isFinite(date.getTime()) ? date : null;
}

function bucketForAge(ageDays) {
  if (ageDays <= 30) return '0-30';
  if (ageDays <= 90) return '31-90';
  if (ageDays <= 180) return '91-180';
  return '181+';
}

export function buildVulnerabilityAgeIntervals(transitions) {
  const active = new Map();
  const intervals = [];

  for (const transition of transitions || []) {
    const eventDate = asDate(transition?.eventDate);
    const vulnerabilityId = String(transition?.vulnerabilityId || '').trim();
    const library = transition?.event?.library || transition?.event?.libraryName || transition?.event?.package || null;
    const relation = String(transition?.relation || '').toUpperCase();
    if (!eventDate || !vulnerabilityId || !library || (relation !== 'AFFECTS' && relation !== 'FIXES')) continue;

    const key = vulnerabilityInstanceKey(transition.event, vulnerabilityId, library);
    if (relation === 'AFFECTS') {
      if (active.has(key)) continue;

      const interval = {
        key,
        introducedAt: eventDate,
        disclosureAt: asDate(transition.disclosureAt) || eventDate,
        fixedAt: null,
      };
      active.set(key, interval);
      intervals.push(interval);
      continue;
    }

    const interval = active.get(key);
    if (!interval) continue;
    interval.fixedAt = eventDate;
    active.delete(key);
  }

  return intervals;
}

export function calculateVulnerabilityAgeBucketTimeline({
  events,
  transitions,
  branch = 'main',
  createdAt = new Date().toISOString(),
  intervalDays = 7,
}) {
  const eventDates = (events || [])
    .map((event) => asDate(Number(event?.timestamp) * 1000))
    .filter(Boolean)
    .sort((left, right) => left.getTime() - right.getTime());
  if (!eventDates.length) return { snapshots: [], intervals: [] };

  const intervals = buildVulnerabilityAgeIntervals(transitions);
  const snapshots = [];
  const startTime = eventDates[0].getTime();
  const endTime = eventDates[eventDates.length - 1].getTime();
  const step = Math.max(1, Number(intervalDays) || 7) * DAY_MS;

  for (let snapshotTime = startTime; snapshotTime <= endTime; snapshotTime += step) {
    const buckets = { '0-30': 0, '31-90': 0, '91-180': 0, '181+': 0 };

    for (const interval of intervals) {
      if (interval.introducedAt.getTime() > snapshotTime) continue;
      if (interval.fixedAt && interval.fixedAt.getTime() <= snapshotTime) continue;

      const ageStart = Math.max(interval.introducedAt.getTime(), interval.disclosureAt.getTime());
      const ageDays = (snapshotTime - ageStart) / DAY_MS;
      if (ageDays < 0) continue;
      buckets[bucketForAge(ageDays)]++;
    }

    snapshots.push({
      branch,
      date: new Date(snapshotTime).toISOString(),
      buckets,
      createdAt,
    });
  }

  return { snapshots, intervals };
}
