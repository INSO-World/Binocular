'use strict';

import debug from 'debug';
import semver from 'semver';

import VersionChangeEvent from '../../models/VersionChangeEvent.js';
import VulnerabilityPatchLagSnapshot from '../../models/metrics/VulnerabilityPatchLagSnapshot.js';
import { walkVersionChangeVulnTriples } from '../walkers/walkVersionChangeVulnTriples.js';
import { preloadNpmTimesForPackages, getNpmPackageTimes } from './npmRegistryTimes.js';

const log = debug('vuln-metrics:patch-lag');
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

/**
 * Infer a "fixed version" from vuln.affectedVersions which (in your normalization)
 * may include strings like "<1.2.3" from OSV SEMVER ranges.
 */
function inferFixedVersionFromAffectedVersions(vuln) {
  const aff = Array.isArray(vuln?.affectedVersions) ? vuln.affectedVersions : [];
  const candidates = [];

  for (const s of aff) {
    const str = String(s || '').trim();
    // We only treat "<x.y.z" and "<=x.y.z" as fixed-boundary indicators
    const m = str.match(/^<=?\s*([0-9A-Za-z.+-]+)$/);
    if (!m) continue;
    const v = m[1];
    if (semver.valid(semver.coerce(v))) candidates.push(semver.coerce(v).version);
  }

  if (!candidates.length) return null;

  // Lowest fixed boundary is the earliest non-vulnerable candidate.
  candidates.sort(semver.compare);
  return candidates[0];
}

function normalizeSeverity(vuln) {
  const s = String(vuln?.severity || '').toUpperCase();
  if (SEVERITY_ORDER.includes(s)) return s;
  return 'UNKNOWN';
}

/**
 * Computes weekly patch-lag snapshots for open vuln instances on a given branch.
 * Stores both mean and median lag days grouped by severity.
 */
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

  // Track open intervals by (library|vulnId)
  // key -> { library, vulnId, severity, openedAt: Date, closedAt: Date|null, fixedVersion, fixedReleaseDate: Date|null }
  const openMap = new Map();
  const allPairs = new Map(); // store final interval data even after close

  for await (const { eventDate, conn, vuln, event } of walkVersionChangeVulnTriples(branch, { relations: ['AFFECTS', 'FIXES'] })) {
    const vulnId = vuln?.vulnId;
    const library = event?.library || event?.libraryName || event?.package || null;
    if (!vulnId || !library) continue;

    const key = `${library}||${vulnId}`;
    const severity = normalizeSeverity(vuln);

    if (conn?.relation === 'AFFECTS') {
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
        allPairs.set(key, rec);
      }
    }

    if (conn?.relation === 'FIXES') {
      const cur = openMap.get(key);
      if (cur) {
        cur.closedAt = eventDate;
        openMap.delete(key);
      } else {
        // In case we missed an AFFECTS earlier (data oddities), still store close info.
        const fixedVersion = inferFixedVersionFromAffectedVersions(vuln);
        const rec = {
          library,
          vulnId,
          severity,
          openedAt: eventDate,
          closedAt: eventDate,
          fixedVersion,
          fixedReleaseDate: null,
        };
        allPairs.set(key, rec);
      }
    }
  }

  const pairs = [...allPairs.values()];
  const uniquePkgs = [...new Set(pairs.map((p) => p.library).filter(Boolean))];

  // Preload npm "time" maps once per package to avoid per-snapshot fetches
  await preloadNpmTimesForPackages(uniquePkgs);

  // Resolve fixedReleaseDate per pair where fixedVersion is known
  let fixedResolved = 0;
  let fixedMissing = 0;

  for (const p of pairs) {
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

      for (const p of pairs) {
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
    `[PATCHLAG][STEP5][SUMMARY] branch=${branch} snapshots=${Math.ceil(results.length / SEVERITY_ORDER.length)} rows=${results.length} pairs=${pairs.length} pkgs=${uniquePkgs.length} fixedResolved=${fixedResolved} fixedMissing=${fixedMissing}`,
  );

  logStep(
    `[PATCHLAG][DETAIL] dateRange=${startDate.toISOString()}..${endDate.toISOString()} severities=${SEVERITY_ORDER.join(',')}`,
  );

  return results;
}

export default computeMeanPatchLagOverTime;
