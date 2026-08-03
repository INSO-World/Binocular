'use strict';

import fetch from 'node-fetch';
import debug from 'debug';

const log = debug('vuln-metrics:npm');
const logDetail = debug('vuln-metrics:npm:detail');

const CONCURRENCY = 5;
const MAX_FAIL_LOG = 50;
const REGISTRY_TIMEOUT_MS = 30000;

const pkgTimesCache = new Map(); // pkg -> Map(version -> Date)

export async function getNpmPackageTimes(pkgName) {
  const name = String(pkgName || '').trim();
  if (!name) return new Map();

  if (pkgTimesCache.has(name)) return pkgTimesCache.get(name);

  const times = await fetchTimesFromRegistry(name);
  pkgTimesCache.set(name, times);
  return times;
}

export async function preloadNpmTimesForPackages(pkgs = [], options = {}) {
  const list = [...new Set((pkgs || []).map((x) => String(x || '').trim()).filter(Boolean))];
  if (!list.length) return;

  let idx = 0;
  let failLogged = 0;
  let completed = 0;
  const concurrency = Math.max(1, Number(options.concurrency) || CONCURRENCY);
  const onProgress = typeof options.onProgress === 'function' ? options.onProgress : null;

  function reportProgress(pkg) {
    completed++;
    if (onProgress) onProgress({ completed, total: list.length, pkg });
  }

  async function worker(workerId) {
    while (idx < list.length) {
      const i = idx++;
      if (i >= list.length) break;

      const pkg = list[i];
      if (pkgTimesCache.has(pkg)) {
        reportProgress(pkg);
        continue;
      }

      try {
        const times = await fetchTimesFromRegistry(pkg);
        pkgTimesCache.set(pkg, times);
      } catch (err) {
        if (failLogged < MAX_FAIL_LOG) {
          failLogged++;
          log(`[NPM][FAIL] pkg=${pkg} worker=${workerId} err=${err?.message || String(err)}`);
        } else {
          logDetail(`[NPM][FAIL] pkg=${pkg} worker=${workerId} err=${err?.message || String(err)}`);
        }
        pkgTimesCache.set(pkg, new Map());
      } finally {
        reportProgress(pkg);
      }
    }
  }

  const workers = Array.from({ length: concurrency }, (_, w) => worker(w));
  await Promise.all(workers);

  log(`[NPM][SUMMARY] preloadedPackages=${list.length}`);
}

async function fetchTimesFromRegistry(pkg) {
  // npm registry expects scoped packages URL-encoded
  const url = `https://registry.npmjs.org/${encodeURIComponent(pkg)}`;

  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), REGISTRY_TIMEOUT_MS);
  let json;
  try {
    const res = await fetch(url, {
      method: 'GET',
      headers: { Accept: 'application/json' },
      signal: controller.signal,
    });
    if (!res.ok) throw new Error(`npm registry ${res.status}`);
    json = await res.json();
  } finally {
    clearTimeout(timeout);
  }
  const timeObj = json?.time || {};

  const map = new Map();
  for (const [version, iso] of Object.entries(timeObj)) {
    // time has keys like "created", "modified" too; filter by semver-ish versions
    if (!version || version === 'created' || version === 'modified') continue;
    const d = new Date(iso);
    if (!isNaN(d.getTime())) map.set(version, d);
  }
  return map;
}
