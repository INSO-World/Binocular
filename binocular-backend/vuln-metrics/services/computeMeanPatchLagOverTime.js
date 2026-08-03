'use strict';

import debug from 'debug';
import semver from 'semver';

import VersionChangeEvent from '../../models/VersionChangeEvent.js';
import VulnerabilityPatchLagSnapshot from '../../models/metrics/VulnerabilityPatchLagSnapshot.js';
import { walkVersionChangeVulnTriples } from '../walkers/walkVersionChangeVulnTriples.js';
import { preloadNpmTimesForPackages, getNpmPackageTimes } from './npmRegistryTimes.js';
import { vulnerabilityInstanceKey } from './dependencyIdentity.js';

const logStep = debug('vuln-metrics:patch-lag:detail');

const DAY_MS = 1000 * 60 * 60 * 24;
const SEVERITY_ORDER = ['CRITICAL', 'HIGH', 'MODERATE', 'LOW', 'MALICIOUS', 'UNKNOWN'];

function addWeeks(date, weeks) {
  const copy = new Date(date);
  copy.setDate(copy.getDate() + weeks * 7);
  return copy;
}

function median(values) {
  const arr = values.slice().sort((a, b) => a - b);
  if (!arr.length) return null;
  const mid = Math.floor(arr.length / 2);
  return arr.length % 2 === 0 ? (arr[mid - 1] + arr[mid]) / 2 : arr[mid];
}

function inferFixedVersionFromAffectedVersions(vuln) {
  const patched = Array.isArray(vuln?.patchedVersions) ? vuln.patchedVersions : [];
  const affected = Array.isArray(vuln?.affectedVersions) ? vuln.affectedVersions : [];
  const candidates = [];

  for (const s of patched) {
    const str = String(s || '').trim();
    const m = str.match(/^(?:>=?\s*)?([0-9A-Za-z.+-]+)$/);
    if (!m) continue;
    const v = m[1];
    const coerced = semver.coerce(v);
    if (coerced && semver.valid(coerced)) candidates.push(coerced.version);
  }

  // Compatibility with vulnerability documents created before patchedVersions
  // was populated. Only exclusive upper bounds represent an OSV "fixed" event.
  if (!candidates.length) {
    for (const s of affected) {
      const str = String(s || '').trim();
      const match = str.match(/(?:^|\s)<\s*([0-9A-Za-z.+-]+)(?:\s|$)/);
      if (!match) continue;
      const coerced = semver.coerce(match[1]);
      if (coerced && semver.valid(coerced)) candidates.push(coerced.version);
    }
  }

  if (!candidates.length) return null;
  candidates.sort(semver.compare);
  return candidates[0];
}

function normalizeSeverity(vuln) {
  const s = String(vuln?.severity || '').toUpperCase();
  if (SEVERITY_ORDER.includes(s)) return s;
  return 'UNKNOWN';
}

export async function computeMeanPatchLagOverTime(branch = 'main') {
  console.log(`[PATCHLAG][STEP5] started branch=${branch}`);

  const events = (await VersionChangeEvent.findAll())
    .filter((e) => e.branchName === branch)
    .sort((a, b) => Number(a.timestamp) - Number(b.timestamp));

  if (!events.length) {
    console.log(`[PATCHLAG][STEP5] no events for branch=${branch}`);
    return [];
  }

  const startDate = new Date(Number(events[0].timestamp) * 1000);
  const endDate = new Date(Number(events[events.length - 1].timestamp) * 1000);

  const openMap = new Map(); // key -> interval (open only)
  const intervals = []; // all intervals (open + closed)

  let strayFixes = 0;

  for await (const { eventDate, conn, vuln, event } of walkVersionChangeVulnTriples(branch, { relations: ['AFFECTS', 'FIXES'] })) {
    const vulnId = vuln?.vulnId;
    const library = event?.library || event?.libraryName || event?.package || null;
    if (!vulnId || !library) continue;

    const key = vulnerabilityInstanceKey(event, vulnId, library);
    const relation = String(conn?.relation || '').toUpperCase();
    const severity = normalizeSeverity(vuln);

    if (relation === 'AFFECTS') {
      if (!openMap.has(key)) {
        const fixedVersion = inferFixedVersionFromAffectedVersions(vuln);
        const rec = {
          library,
          vulnId,
          severity,
          openedAt: eventDate,
          closedAt: null,
          fixedVersion,
          fixedReleaseDate: null,
        };
        openMap.set(key, rec);
        intervals.push(rec);
      }
      continue;
    }

    if (relation === 'FIXES') {
      const cur = openMap.get(key);
      if (cur) {
        cur.closedAt = eventDate;
        openMap.delete(key);
      } else {
        strayFixes++;
      }
    }
  }

  const uniquePkgs = [...new Set(intervals.map((p) => p.library).filter(Boolean))];
  await preloadNpmTimesForPackages(uniquePkgs);

  let fixedResolved = 0;
  let fixedMissing = 0;

  for (const p of intervals) {
    if (!p.fixedVersion) {
      fixedMissing++;
      continue;
    }

    const times = await getNpmPackageTimes(p.library);
    const d = times.get(p.fixedVersion) || null;

    if (d && !isNaN(d.getTime())) {
      p.fixedReleaseDate = d;
      fixedResolved++;
    } else {
      fixedMissing++;
    }
  }

  const results = [];
  const createdAt = new Date().toISOString();

  for (let snapshotDate = startDate; snapshotDate <= endDate; snapshotDate = addWeeks(snapshotDate, 1)) {
    for (const sev of SEVERITY_ORDER) {
      const lags = [];
      let countUnavailableYet = 0;
      let countNoFixInfo = 0;

      for (const p of intervals) {
        if (p.severity !== sev) continue;

        if (p.openedAt > snapshotDate) continue;
        if (p.closedAt && p.closedAt <= snapshotDate) continue;

        if (!p.fixedVersion || !p.fixedReleaseDate) {
          countNoFixInfo++;
          continue;
        }

        if (p.fixedReleaseDate > snapshotDate) {
          countUnavailableYet++;
          continue;
        }

        const lagDays = (snapshotDate - p.fixedReleaseDate) / DAY_MS;
        if (lagDays >= 0) lags.push(lagDays);
      }

      const meanLagDays = lags.length ? lags.reduce((a, b) => a + b, 0) / lags.length : null;
      const medianLagDays = lags.length ? median(lags) : null;

      results.push({
        branch,
        date: snapshotDate.toISOString(),
        severity: sev,
        meanLagDays,
        medianLagDays,
        countIncluded: lags.length,
        countUnavailableYet,
        countNoFixInfo,
        createdAt,
      });
    }
  }

  await VulnerabilityPatchLagSnapshot.removeByBranch(branch);
  for (const doc of results) {
    await VulnerabilityPatchLagSnapshot.persist(doc);
  }

  console.log(
    `[PATCHLAG][STEP5][SUMMARY] branch=${branch} snapshots=${Math.ceil(results.length / SEVERITY_ORDER.length)} rows=${
      results.length
    } intervals=${intervals.length} pkgs=${
      uniquePkgs.length
    } fixedResolved=${fixedResolved} fixedMissing=${fixedMissing} strayFixes=${strayFixes}`,
  );

  logStep(`[PATCHLAG][DETAIL] dateRange=${startDate.toISOString()}..${endDate.toISOString()} severities=${SEVERITY_ORDER.join(',')}`);

  return results;
}

export default computeMeanPatchLagOverTime;
