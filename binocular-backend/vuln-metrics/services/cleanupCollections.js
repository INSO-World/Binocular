'use strict';

import debug from 'debug';
const log = debug('vuln-metrics:cleanup');

async function wipe(Model, label) {
  if (!Model) throw new Error(`wipe: Model missing for ${label}`);

  if (typeof Model.truncate === 'function') return Model.truncate();
  if (typeof Model.removeAll === 'function') return Model.removeAll();
  if (typeof Model.deleteMany === 'function') return Model.deleteMany({});

  const c1 = Model.collection;
  if (c1) {
    if (typeof c1.truncate === 'function') return c1.truncate();
    if (typeof c1.deleteMany === 'function') return c1.deleteMany({});
  }

  if (typeof Model.collection === 'function') {
    const c2 = await Model.collection();
    if (c2) {
      if (typeof c2.truncate === 'function') return c2.truncate();
      if (typeof c2.deleteMany === 'function') return c2.deleteMany({});
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

  const VersionChangeEvent = (await import('../../models/VersionChangeEvent.js')).default;
  const Vulnerability = (await import('../../models/Vulnerability.js')).default;
  const VersionChangeEventVulnerabilityConnection = (await import('../../models/VersionChangeEventVulnerabilityConnection.js')).default;
  const VulnerabilityAgeBucket = (await import('../../models/metrics/VulnerabilityAgeBucket.js')).default;
  const VulnerabilityRemediationTimeSnapshot = (await import('../../models/metrics/VulnerabilityRemediationTimeSnapshot.js')).default;

  // Console: single-line start/end only
  console.log('[VULN][STEP0] Cleanup collections (truncate)');

  const results = {};
  results.versionChangeEvents = await wipe(VersionChangeEvent, 'VersionChangeEvent');
  results.vulnerabilities = await wipe(Vulnerability, 'Vulnerability');
  results.vulnEventConnections = await wipe(VersionChangeEventVulnerabilityConnection, 'VersionChangeEventVulnerabilityConnection');
  results.vulnerabilityAgeBuckets = await wipe(VulnerabilityAgeBucket, 'VulnerabilityAgeBucket');
  results.vulnerabilityRemediationTimeSnapshots = await wipe(VulnerabilityRemediationTimeSnapshot, 'VulnerabilityRemediationTimeSnapshot');

  // Debug only: full object
  log('Cleanup results: %O', results);

  console.log('[VULN][STEP0] Cleanup done');
  return results;
}

export default step0CleanupCollections;
