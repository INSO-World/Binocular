'use strict';

import VersionChangeEvent from '../../models/VersionChangeEvent.js';
import debug from 'debug';
import { v4 as uuidv4 } from 'uuid';

const log = debug('vuln-metrics:detect');
const logStep = debug('vuln-metrics:detect:step1');
const logChange = debug('vuln-metrics:detect:change'); // per-change (off by default)

// Keep debug noise bounded
const MAX_APPEAR_LOG = 5;
const MAX_DISAPPEAR_LOG = 5;

export async function persistVersionChanges(repo, branchName) {
  const commits = await repo.getFirstParentCommits(branchName);
  if (!Array.isArray(commits) || commits.length < 2) {
    log(`Not enough commits to process on branch "${branchName}"`);
    return;
  }

  // NOTE: We do NOT infer ordering from timestamps. We trust the order returned by
  // repo.getFirstParentCommits(branchName) (first-parent topology traversal).

  let persisted = 0;

  // Pair-level package-lock readability stats
  let skippedPairsNoPkgLock = 0;
  let prevMissing = 0;
  let currMissing = 0;

  // detect “lock appears/disappears” between commits
  let lockAppearsBetween = 0; // prev missing, curr present
  let lockDisappearsBetween = 0; // prev present, curr missing
  let appearLogged = 0;
  let disappearLogged = 0;

  // baseline visibility
  let firstReadableLockSha = null;
  let firstReadableLockDepsCount = 0;
  let baselineEventsEmitted = 0;

  for (let i = 1; i < commits.length; i++) {
    const prev = commits[i - 1];
    const curr = commits[i];

    const prevSha = prev.oid;
    const currSha = curr.oid;

    let prevPkgStr = null;
    let currPkgStr = null;

    try {
      prevPkgStr = await repo.readFileAtCommit('package-lock.json', prevSha);
    } catch {
      prevMissing++;
    }

    try {
      currPkgStr = await repo.readFileAtCommit('package-lock.json', currSha);
    } catch {
      currMissing++;
    }

    const prevHas = typeof prevPkgStr === 'string' && prevPkgStr.length > 0;
    const currHas = typeof currPkgStr === 'string' && currPkgStr.length > 0;

    if (!prevHas && !currHas) {
      skippedPairsNoPkgLock++;
      continue;
    }

    // First readable lock (debug only)
    if (!firstReadableLockSha && currHas) {
      const deps = extractDependencies(currPkgStr);
      firstReadableLockSha = currSha;
      firstReadableLockDepsCount = Object.keys(deps).length;
      logStep(`[VULN][STEP1][BASELINE] firstReadableLockSha=${firstReadableLockSha} deps=${firstReadableLockDepsCount}`);
    }

    let prevDeps = {};
    let currDeps = {};

    if (!prevHas && currHas) {
      // lock appears => baseline moment (null -> version)
      lockAppearsBetween++;

      if (appearLogged < MAX_APPEAR_LOG) {
        appearLogged++;
        logStep(`[VULN][STEP1][LOCK] APPEARS prev=${prevSha} (${toIso(prev)}) curr=${currSha} (${toIso(curr)}) (baseline moment)`);
      }

      prevDeps = {};
      currDeps = extractDependencies(currPkgStr);

      baselineEventsEmitted += Object.keys(currDeps).length;
    } else if (prevHas && !currHas) {
      // lock disappears => treat as all removed (version -> null)
      lockDisappearsBetween++;

      if (disappearLogged < MAX_DISAPPEAR_LOG) {
        disappearLogged++;
        logStep(`[VULN][STEP1][LOCK] DISAPPEARS prev=${prevSha} (${toIso(prev)}) curr=${currSha} (${toIso(curr)})`);
      }

      prevDeps = extractDependencies(prevPkgStr);
      currDeps = {};
    } else {
      // normal diff
      prevDeps = extractDependencies(prevPkgStr);
      currDeps = extractDependencies(currPkgStr);
    }

    const changes = diffDependencies(prevDeps, currDeps);
    if (!changes.length) continue;

    for (const change of changes) {
      await VersionChangeEvent.persist({
        id: uuidv4(),
        commitHash: currSha,
        branchName,
        timestamp: curr?.commit?.committer?.timestamp,
        author: curr?.commit?.committer?.name,
        library: change.name,
        oldVersion: change.oldVersion,
        newVersion: change.newVersion,
        sourceType: 'commit',
        direct: change.isDirect,
        wasDirect: change.wasDirect,
      });

      persisted++;

      logChange(`Detected version change: ${change.name} ${change.oldVersion || '∅'} → ${change.newVersion || '∅'} at ${currSha}`);
    }
  }

  // Console: only the essential, always-visible summary
  console.log(
    // eslint-disable-next-line max-len
    `[VULN][STEP1][SUMMARY] branch=${branchName} commits=${commits.length} persistedEvents=${persisted} skippedPairsNoPkgLock=${skippedPairsNoPkgLock} lockAppearsBetween=${lockAppearsBetween} lockDisappearsBetween=${lockDisappearsBetween}`,
  );

  // Debug-only richer diagnostics
  logStep(
    // eslint-disable-next-line max-len
    `[VULN][STEP1][DIAG] prevMissing=${prevMissing} currMissing=${currMissing} firstReadableLockSha=${
      firstReadableLockSha || '∅'
    } deps=${firstReadableLockDepsCount} baselineEventsEmitted=${baselineEventsEmitted}`,
  );

  log(
    // eslint-disable-next-line max-len
    `Version change detection completed for branch="${branchName}" commits=${commits.length} persistedEvents=${persisted} skippedPairsNoPkgLock=${skippedPairsNoPkgLock}`,
  );
}

function toIso(commitObj) {
  const ts = Number(commitObj?.commit?.committer?.timestamp || 0);
  return ts ? new Date(ts * 1000).toISOString() : '∅';
}

function extractDependencies(pkgLockStr) {
  try {
    const lock = JSON.parse(pkgLockStr);
    const deps = {};

    const walk = (node, isRoot = false) => {
      for (const [name, info] of Object.entries(node.dependencies || {})) {
        deps[name] = { version: info.version || null, direct: isRoot };
        walk(info, false);
      }
    };

    walk(lock, true);
    return deps;
  } catch {
    return {};
  }
}

function diffDependencies(prevDeps, currDeps) {
  const changes = [];

  for (const [name, currInfo] of Object.entries(currDeps)) {
    const prevInfo = prevDeps[name];

    if (!prevInfo) {
      changes.push({
        name,
        oldVersion: null,
        newVersion: currInfo.version,
        wasDirect: false,
        isDirect: currInfo.direct,
      });
      continue;
    }

    if (prevInfo.version !== currInfo.version || prevInfo.direct !== currInfo.direct) {
      changes.push({
        name,
        oldVersion: prevInfo.version,
        newVersion: currInfo.version,
        wasDirect: prevInfo.direct,
        isDirect: currInfo.direct,
      });
    }
  }

  for (const [name, prevInfo] of Object.entries(prevDeps)) {
    if (!(name in currDeps)) {
      changes.push({
        name,
        oldVersion: prevInfo.version,
        newVersion: null,
        wasDirect: prevInfo.direct,
        isDirect: false,
      });
    }
  }

  return changes;
}

export default persistVersionChanges;
