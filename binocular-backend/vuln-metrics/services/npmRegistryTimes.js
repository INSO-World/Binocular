'use strict';

import fetch from 'node-fetch';
import debug from 'debug';

const log = debug('vuln-metrics:npm');
const logDetail = debug('vuln-metrics:npm:detail');

const CONCURRENCY = 5;
const MAX_FAIL_LOG = 50;

const pkgTimesCache = new Map(); // pkg -> Map(version -> Date)

export async function getNpmPackageTimes(pkgName) {
  const name = String(pkgName || '').trim();
  if (!name) return new Map();

  if (pkgTimesCache.has(name)) return pkgTimesCache.get(name);

  const times = await fetchTimesFromRegistry(name);
  pkgTimesCache.set(name, times);
  return times;
}

export async function preloadNpmTimesForPackages(pkgs = []) {
  const list = [...new Set((pkgs || []).map((x) => String(x || '').trim()).filter(Boolean))];
  if (!list.length) return;

  let idx = 0;
  let failLogged = 0;

  async function worker(workerId) {
    while (true) {
      const i = idx++;
      if (i >= list.length) break;

      const pkg = list[i];
      if (pkgTimesCache.has(pkg)) continue;

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
      }
    }
  }

  const workers = Array.from({ length: Math.max(1, CONCURRENCY) }, (_, w) => worker(w));
  await Promise.all(workers);

  log(`[NPM][SUMMARY] preloadedPackages=${list.length}`);
}

async function fetchTimesFromRegistry(pkg) {
  // npm registry expects scoped packages URL-encoded
  const url = `https://registry.npmjs.org/${encodeURIComponent(pkg)}`;

  const res = await fetch(url, { method: 'GET', headers: { 'Accept': 'application/json' } });
  if (!res.ok) throw new Error(`npm registry ${res.status}`);

  const json = await res.json();
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
