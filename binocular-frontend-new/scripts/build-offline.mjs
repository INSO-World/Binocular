// Runs `vite build` for the offline (PouchDB) target with a default heap size.
// A NODE_OPTIONS already set by the caller (e.g. mine-and-host.yml raising or lowering
// the heap for the target repo/runner) is left untouched -- cross-env can't express
// "use this unless already set", so this wrapper does it in plain, cross-platform JS.
//
// Run: node binocular-frontend-new/scripts/build-offline.mjs

import { spawnSync } from 'node:child_process';

const env = { ...process.env, PRE_CONFIGURE_DB: 'pouchdb' };
if (!env.NODE_OPTIONS) {
  env.NODE_OPTIONS = '--max-old-space-size=12288';
}

const result = spawnSync('vite', ['build'], {
  stdio: 'inherit',
  shell: true,
  env,
});
process.exit(result.status ?? 1);
