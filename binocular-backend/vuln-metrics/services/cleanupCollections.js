'use strict';

import debug from 'debug';

import VersionChangeEvent from '../../models/VersionChangeEvent.js';
import Vulnerability from '../../models/Vulnerability.js';
import VersionChangeEventVulnerabilityConnection from '../../models/VersionChangeEventVulnerabilityConnection.js';
import VulnerabilityAgeBucket from '../../models/metrics/VulnerabilityAgeBucket.js';
import VulnerabilityRemediationTimeSnapshot from '../../models/metrics/VulnerabilityRemediationTimeSnapshot.js';
import VulnerabilityPatchLagSnapshot from '../../models/metrics/VulnerabilityPatchLagSnapshot.js';
import VulnerabilityDirectTransitiveSnapshot from '../../models/metrics/VulnerabilityDirectTransitiveSnapshot.js';

const log = debug('vuln-metrics:cleanup');

async function wipe(Model, label) {
  if (!Model) throw new Error(`wipe: Model missing for ${label}`);

  if (typeof Model.truncate === 'function') {
    log('[WIPE][%s] using Model.truncate()', label);
    return Model.truncate();
  }

  if (typeof Model.removeAll === 'function') {
    log('[WIPE][%s] using Model.removeAll()', label);
    return Model.removeAll();
  }

  if (typeof Model.deleteMany === 'function') {
    log('[WIPE][%s] using Model.deleteMany({})', label);
    return Model.deleteMany({});
  }

  const c1 = Model.collection;
  if (c1) {
    if (typeof c1.truncate === 'function') {
      log('[WIPE][%s] using Model.collection.truncate()', label);
      return c1.truncate();
    }
    if (typeof c1.deleteMany === 'function') {
      log('[WIPE][%s] using Model.collection.deleteMany({})', label);
      return c1.deleteMany({});
    }
  }

  if (typeof Model.collection === 'function') {
    const c2 = await Model.collection();
    if (c2) {
      if (typeof c2.truncate === 'function') {
        log('[WIPE][%s] using await Model.collection().truncate()', label);
        return c2.truncate();
      }
      if (typeof c2.deleteMany === 'function') {
        log('[WIPE][%s] using await Model.collection().deleteMany({})', label);
        return c2.deleteMany({});
      }
    }
  }

  const keys = Object.keys(Model);
  throw new Error(`No wipe/truncate API found for ${label}. Available static keys: ${keys.slice(0, 30).join(', ')}`);
}

export async function step0CleanupCollections() {
  const nodeEnv = String(process.env.NODE_ENV || '').toLowerCase();
  if (nodeEnv === 'production') {
    throw new Error('step0CleanupCollections: refusing to wipe collections in production (NODE_ENV=production).');
  }

  console.log('[VULN][STEP0] Cleanup collections (truncate)');

  const results = {};
  results.versionChangeEvents = await wipe(VersionChangeEvent, 'VersionChangeEvent');
  results.vulnerabilities = await wipe(Vulnerability, 'Vulnerability');
  results.vulnEventConnections = await wipe(VersionChangeEventVulnerabilityConnection, 'VersionChangeEventVulnerabilityConnection');
  results.vulnerabilityAgeBuckets = await wipe(VulnerabilityAgeBucket, 'VulnerabilityAgeBucket');
  results.vulnerabilityRemediationTimeSnapshots = await wipe(VulnerabilityRemediationTimeSnapshot, 'VulnerabilityRemediationTimeSnapshot');
  results.vulnerabilityPatchLagSnapshots = await wipe(VulnerabilityPatchLagSnapshot, 'VulnerabilityPatchLagSnapshot');
  results.vulnerabilityDirectTransitiveSnapshots = await wipe(
    VulnerabilityDirectTransitiveSnapshot,
    'VulnerabilityDirectTransitiveSnapshot',
  );

  log('Cleanup results: %O', results);

  console.log('[VULN][STEP0] Cleanup done');
  return results;
}

export default step0CleanupCollections;
