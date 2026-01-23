'use strict';

import { retrieveVulnerabilityInfo } from './retrieveVulnerabilityInfo.js';
import Vulnerability from './../../models/Vulnerability.js';
import VersionChangeEventVulnerabilityConnection from './../../models/VersionChangeEventVulnerabilityConnection.js';
import VersionChangeEvent from './../../models/VersionChangeEvent.js';
import semver from 'semver';
import debug from 'debug';

const log = debug('vuln-metrics:enrich');
const logStep = debug('vuln-metrics:enrich:step2');

export async function enrichVersionChanges() {
  // Console: single start + end (keep clean)
  console.log('[VULN][STEP2] Enrichment started');

  const events = await VersionChangeEvent.findAll();
  if (!events.length) {
    console.log('[VULN][STEP2] No version change events found');
    return;
  }

  const libraries = [...new Set(events.map((e) => e.library).filter(Boolean))];
  console.log(`[VULN][STEP2][SUMMARY] events=${events.length} uniqueLibraries=${libraries.length}`);

  let vulnerabilities = [];
  try {
    vulnerabilities = await retrieveVulnerabilityInfo(libraries);
  } catch (error) {
    console.error('[VULN][STEP2][ERROR] Failed to retrieve vulnerabilities:', error.message);
    return;
  }

  if (!vulnerabilities.length) {
    console.log('[VULN][STEP2] No vulnerabilities found for provided libraries');
    return;
  }

  // Debug only: a bit more detail
  logStep(`Retrieved vulnerability sets for libraries=${vulnerabilities.length}`);

  // Build library -> events index (avoid O(n*m) filter per library)
  const eventsByLib = new Map();
  for (const e of events) {
    const lib = e?.library;
    if (!lib) continue;
    if (!eventsByLib.has(lib)) eventsByLib.set(lib, []);
    eventsByLib.get(lib).push(e);
  }

  let connectionsEnsured = 0;

  for (const { library, vulnerabilities: vulns } of vulnerabilities) {
    const relatedEvents = eventsByLib.get(library) || [];
    if (!relatedEvents.length || !Array.isArray(vulns) || !vulns.length) continue;

    logStep(`Enrich lib=${library} vulns=${vulns.length} events=${relatedEvents.length}`);

    for (const v of vulns) {
      if (!v?.vulnId) continue;

      const [storedVuln] = await Vulnerability.persist(v);

      for (const e of relatedEvents) {
        const wasVulnerable = isVersionAffected(e.oldVersion, v);
        const nowVulnerable = isVersionAffected(e.newVersion, v);

        if (wasVulnerable === nowVulnerable) continue;

        const relation = nowVulnerable ? 'AFFECTS' : 'FIXES';

        await VersionChangeEventVulnerabilityConnection.ensure(
          {
            relation,
            libraryVersionOld: e.oldVersion,
            libraryVersionNew: e.newVersion,
            createdAt: new Date().toISOString(),
          },
          { from: e, to: storedVuln },
        );

        connectionsEnsured++;
      }
    }
  }

  console.log('[VULN][STEP2][SUMMARY] Enrichment completed');
  log(`connectionsEnsured=${connectionsEnsured}`);
}

/**
 * Determine if a given version falls within the vulnerable versions of a vulnerability.
 */
function isVersionAffected(version, vuln) {
  if (!version || !vuln?.affectedVersions) return false;

  try {
    const affected = Array.isArray(vuln.affectedVersions) ? vuln.affectedVersions.filter((v) => typeof v === 'string') : [];
    if (!affected.length) return false;

    if (affected.includes(version)) return true;

    for (const range of affected) {
      if (typeof range === 'string' && /[<>]=?/.test(range)) {
        try {
          if (semver.satisfies(version, range, { includePrerelease: true })) return true;
        } catch {
          // ignore invalid semver range
        }
      }
    }

    return false;
  } catch (err) {
    // keep as debug to avoid console spam
    logStep(`Error comparing version=${version} vuln=${vuln?.vulnId}: ${err?.message || String(err)}`);
    return false;
  }
}
