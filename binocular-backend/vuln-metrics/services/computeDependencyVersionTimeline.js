'use strict';

import VersionChangeEvent from '../../models/VersionChangeEvent.js';
import DependencyVersionSnapshot from '../../models/metrics/DependencyVersionSnapshot.js';
import { walkVersionChangeVulnTriples } from '../walkers/walkVersionChangeVulnTriples.js';
import { calculateDependencyVersionTimeline } from './dependencyVersionTimeline.js';

export async function computeDependencyVersionTimeline(branch = 'main') {
  console.log(`[VERSIONTIMELINE][START] branch=${branch}`);
  const events = (await VersionChangeEvent.findAll()).filter((event) => event.branchName === branch);
  if (!events.length) {
    console.log(`[VERSIONTIMELINE][DONE] no events for branch=${branch}`);
    return [];
  }

  const triples = [];
  for await (const triple of walkVersionChangeVulnTriples(branch, { relations: ['AFFECTS', 'FIXES'] })) triples.push(triple);

  const { snapshots, diagnostics } = calculateDependencyVersionTimeline({ events, triples, branch });
  await DependencyVersionSnapshot.removeByBranch(branch);
  for (const snapshot of snapshots) await DependencyVersionSnapshot.persist(snapshot);

  console.log(`[VERSIONTIMELINE][SUMMARY] ${JSON.stringify({ branch, rows: snapshots.length, ...diagnostics })}`);
  return snapshots;
}

export default computeDependencyVersionTimeline;
