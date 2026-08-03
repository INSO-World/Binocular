'use strict';

import VersionChangeEvent from '../../models/VersionChangeEvent.js';
import debug from 'debug';
import { v4 as uuidv4 } from 'uuid';
import { diffDependencies, extractDependencies } from '../services/packageLockDependencies.js';

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
  let skippedPairsNoPkgLock = 0;
  let prevMissing = 0;
  let currMissing = 0;
  let lockAppearsBetween = 0;
  let lockDisappearsBetween = 0;
  let appearLogged = 0;
  let disappearLogged = 0;
  let firstReadableLockSha = null;
  let firstReadableLockDepsCount = 0;
  let baselineEventsEmitted = 0;
  const fileCache = new Map();

  async function readDependencyFiles(commit) {
    const sha = commit.oid;
    if (fileCache.has(sha)) return fileCache.get(sha);

    let lock = null;
    let manifest = null;
    try {
      lock = await repo.readFileAtCommit('package-lock.json', sha);
    } catch {
      // A missing lockfile is a valid historical state.
    }

    const hasLock = typeof lock === 'string' && lock.length > 0;
    if (hasLock) {
      try {
        manifest = await repo.readFileAtCommit('package.json', sha);
      } catch {
        // package-lock v2/v3 embeds the root manifest; v1 uses best-effort fallback
      }
    }

    const result = { lock, manifest, hasLock };
    fileCache.set(sha, result);
    return result;
  }

  function recordFirstReadableLock(commit, files, dependencies) {
    if (firstReadableLockSha || !files.hasLock) return;

    firstReadableLockSha = commit.oid;
    firstReadableLockDepsCount = Object.keys(dependencies).length;
    const diag = countDepTypes(dependencies);
    logStep(
      `[VULN][STEP1][BASELINE] ${JSON.stringify({
        firstReadableLockSha,
        dependencies: firstReadableLockDepsCount,
        direct: diag.direct,
        transitive: diag.transitive,
      })}`,
    );
  }

  async function persistChanges(changes, commit, sequence) {
    for (const change of changes) {
      await VersionChangeEvent.persist({
        id: uuidv4(),
        commitHash: commit.oid,
        branchName,
        timestamp: commit?.commit?.committer?.timestamp,
        sequence,
        author: commit?.commit?.committer?.name,
        library: change.name,
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

  const firstCommit = commits[0];
  const firstFiles = await readDependencyFiles(firstCommit);
  if (firstFiles.hasLock) {
    const dependencies = extractDependencies(firstFiles.lock, firstFiles.manifest);
    recordFirstReadableLock(firstCommit, firstFiles, dependencies);
    const baselineChanges = diffDependencies({}, dependencies);
    baselineEventsEmitted += baselineChanges.length;
    await persistChanges(baselineChanges, firstCommit, 0);
  }

  for (let i = 1; i < commits.length; i++) {
    const prev = commits[i - 1];
    const curr = commits[i];
    const prevSha = prev.oid;
    const currSha = curr.oid;
    const prevFiles = await readDependencyFiles(prev);
    const currFiles = await readDependencyFiles(curr);
    if (!prevFiles.hasLock) prevMissing++;
    if (!currFiles.hasLock) currMissing++;

    if (!prevFiles.hasLock && !currFiles.hasLock) {
      skippedPairsNoPkgLock++;
      continue;
    }

    let prevDeps = {};
    let currDeps = {};

    if (!prevFiles.hasLock && currFiles.hasLock) {
      lockAppearsBetween++;
      if (appearLogged < MAX_APPEAR_LOG) {
        appearLogged++;
        logStep(`[VULN][STEP1][LOCK] APPEARS prev=${prevSha} (${toIso(prev)}) curr=${currSha} (${toIso(curr)}) (baseline moment)`);
      }
      currDeps = extractDependencies(currFiles.lock, currFiles.manifest);
      recordFirstReadableLock(curr, currFiles, currDeps);
      baselineEventsEmitted += Object.keys(currDeps).length;
    } else if (prevFiles.hasLock && !currFiles.hasLock) {
      lockDisappearsBetween++;
      if (disappearLogged < MAX_DISAPPEAR_LOG) {
        disappearLogged++;
        logStep(`[VULN][STEP1][LOCK] DISAPPEARS prev=${prevSha} (${toIso(prev)}) curr=${currSha} (${toIso(curr)})`);
      }
      prevDeps = extractDependencies(prevFiles.lock, prevFiles.manifest);
    } else {
      prevDeps = extractDependencies(prevFiles.lock, prevFiles.manifest);
      currDeps = extractDependencies(currFiles.lock, currFiles.manifest);
      recordFirstReadableLock(curr, currFiles, currDeps);
    }

    const changes = diffDependencies(prevDeps, currDeps);
    await persistChanges(changes, curr, i);
  }

  const summary = {
    branch: branchName,
    commits: commits.length,
    persistedEvents: persisted,
    skippedPairsNoPkgLock,
    lockAppearsBetween,
    lockDisappearsBetween,
  };
  console.log(`[VULN][STEP1][SUMMARY] ${JSON.stringify(summary)}`);

  logStep(
    `[VULN][STEP1][DIAG] prevMissing=${prevMissing} currMissing=${currMissing} firstReadableLockSha=${
      firstReadableLockSha || '∅'
    } deps=${firstReadableLockDepsCount} baselineEventsEmitted=${baselineEventsEmitted}`,
  );

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
