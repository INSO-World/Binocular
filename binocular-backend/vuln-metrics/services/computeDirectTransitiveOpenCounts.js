'use strict';

import debug from 'debug';

import VersionChangeEvent from '../../models/VersionChangeEvent.js';
import VulnerabilityDirectTransitiveSnapshot from '../../models/metrics/VulnerabilityDirectTransitiveSnapshot.js';
import { walkVersionChangeVulnTriples } from '../walkers/walkVersionChangeVulnTriples.js';
import { calculateDirectTransitiveTimeline } from './directTransitiveTimeline.js';

const log = debug('vuln-metrics:direct-transitive');

export async function computeDirectTransitiveOpenCounts(branch = 'main') {
  console.log(`[DIRTRANS][START] branch=${branch}`);

  const events = (await VersionChangeEvent.findAll()).filter((event) => event.branchName === branch);
  if (!events.length) {
    console.log(`[DIRTRANS][DONE] no events for branch=${branch}`);
    return [];
  }

  const triples = [];
  for await (const triple of walkVersionChangeVulnTriples(branch, { relations: ['AFFECTS', 'FIXES'] })) {
    triples.push(triple);
  }

  const { snapshots, diagnostics } = calculateDirectTransitiveTimeline({ events, triples, branch });

  await VulnerabilityDirectTransitiveSnapshot.removeByBranch(branch);
  for (const snapshot of snapshots) await VulnerabilityDirectTransitiveSnapshot.persist(snapshot);

  console.log(`[DIRTRANS][SUMMARY] ${JSON.stringify({ branch, rows: snapshots.length, ...diagnostics })}`);
  log(`completed branch=${branch} rows=${snapshots.length}`);
  return snapshots;
}

export default computeDirectTransitiveOpenCounts;
