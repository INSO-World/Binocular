'use strict';

import VersionChangeEvent from '../../models/VersionChangeEvent.js';
import debug from 'debug';
import { v4 as uuidv4 } from 'uuid';
import { diffDependencies } from '../services/packageLockDependencies.js';
import { discoverDependencySources, readDependencySourceAtCommit } from '../services/dependencySources.js';

const log = debug('vuln-metrics:detect');
const logStep = debug('vuln-metrics:detect:step1');
const logChange = debug('vuln-metrics:detect:change');

const MAX_APPEAR_LOG = 5;
const MAX_DISAPPEAR_LOG = 5;

export async function persistVersionChanges(repo, branchName) {
  const commits = await repo.getFirstParentCommits(branchName);
  if (!Array.isArray(commits) || !commits.length) {
    log(`No commits to process on branch "${branchName}"`);
    return;
  }

  let persisted = 0;
  let lockAppearsBetween = 0;
  let lockDisappearsBetween = 0;
  let baselineEventsEmitted = 0;
  const fileCache = new Map();
  const sources = await discoverDependencySources(repo, branchName);

  function logBaseline(commit, source, dependencies) {
    const diag = countDepTypes(dependencies);
    logStep(
      `[VULN][STEP1][BASELINE] ${JSON.stringify({
        component: source.component,
        lockPath: source.lockPath,
        firstReadableLockSha: commit.oid,
        dependencies: Object.keys(dependencies).length,
        direct: diag.direct,
        transitive: diag.transitive,
      })}`,
    );
  }

  async function persistChanges(changes, commit, sequence, source) {
    for (const change of changes) {
      await VersionChangeEvent.persist({
        id: uuidv4(),
        commitHash: commit.oid,
        branchName,
        timestamp: commit?.commit?.committer?.timestamp,
        sequence,
        author: commit?.commit?.committer?.name,
        library: change.name,
        component: source.component,
        lockfilePath: source.lockPath,
        manifestPath: source.manifestPath,
        oldVersion: change.oldVersion,
        newVersion: change.newVersion,
        sourceType: 'commit',
        dependencyType: change.dependencyType,
        wasDependencyType: change.wasDependencyType,
      });

      persisted++;
      logChange(`Detected version change: ${change.name} ${change.oldVersion || '∅'} → ${change.newVersion || '∅'} at ${commit.oid}`);
    }
  }

  for (const source of sources) {
    let previous = null;
    let baselineRecorded = false;
    let appearLogged = 0;
    let disappearLogged = 0;

    for (let sequence = 0; sequence < commits.length; sequence++) {
      const commit = commits[sequence];
      const current = await readDependencySourceAtCommit(repo, source, commit.oid, fileCache);

      if (!current.exists && !previous?.exists) {
        previous = current;
        continue;
      }

      if (current.exists && !previous?.exists) {
        if (previous) lockAppearsBetween++;
        if (previous && appearLogged++ < MAX_APPEAR_LOG) {
          logStep(`[VULN][STEP1][LOCK] APPEARS component=${source.component} commit=${commit.oid} (${toIso(commit)})`);
        }
        if (!baselineRecorded) {
          baselineRecorded = true;
          logBaseline(commit, source, current.dependencies);
        }
        const changes = diffDependencies({}, current.dependencies);
        baselineEventsEmitted += changes.length;
        await persistChanges(changes, commit, sequence, source);
      } else if (!current.exists && previous?.exists) {
        lockDisappearsBetween++;
        if (disappearLogged++ < MAX_DISAPPEAR_LOG) {
          logStep(`[VULN][STEP1][LOCK] DISAPPEARS component=${source.component} commit=${commit.oid} (${toIso(commit)})`);
        }
        await persistChanges(diffDependencies(previous.dependencies, {}), commit, sequence, source);
      } else {
        await persistChanges(diffDependencies(previous.dependencies, current.dependencies), commit, sequence, source);
      }

      previous = current;
    }
  }

  const summary = {
    branch: branchName,
    sources: sources.map((source) => source.lockPath),
    commits: commits.length,
    persistedEvents: persisted,
    lockAppearsBetween,
    lockDisappearsBetween,
  };
  console.log(`[VULN][STEP1][SUMMARY] ${JSON.stringify(summary)}`);

  logStep(`[VULN][STEP1][DIAG] sources=${sources.length} baselineEventsEmitted=${baselineEventsEmitted}`);

  log(`Version change detection completed: ${JSON.stringify(summary)}`);
}

function toIso(commitObj) {
  const ts = Number(commitObj?.commit?.committer?.timestamp || 0);
  return ts ? new Date(ts * 1000).toISOString() : '∅';
}

function countDepTypes(deps) {
  return Object.values(deps || {}).reduce(
    (acc, d) => {
      if (d?.direct) acc.direct++;
      else acc.transitive++;
      return acc;
    },
    { direct: 0, transitive: 0 },
  );
}

export default persistVersionChanges;
