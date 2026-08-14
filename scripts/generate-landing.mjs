#!/usr/bin/env node
// Maintains the Binocular run-history site on the storage branch.
//
// Given the site directory and details of the run that was just published into
// <site>/<slug>/, this script:
//   1. upserts the run into <site>/runs.json,
//   2. prunes entries (and their folders) older than --max-age-days,
//   3. regenerates <site>/index.html — a landing page that links to every kept run.
//
// Usage:
//   node generate-landing.mjs \
//     --site site \
//     --slug 2026-06-25-a1b2c3d \
//     --sha <full-sha> \
//     --run 42 \
//     --subject "commit subject" \
//     --repo owner/repo \
//     --commit-url https://github.com/owner/repo/commit/<full-sha> \
//     --max-age-days 7
//
// Dependency-free (Node ESM, built-ins only) so it runs on any GitHub or GitLab runner.

import fs from 'node:fs';
import path from 'node:path';

function parseArgs(argv) {
  const args = {};
  for (let i = 0; i < argv.length; i += 2) {
    const key = argv[i];
    if (!key.startsWith('--')) continue;
    args[key.slice(2)] = argv[i + 1];
  }
  return args;
}

const args = parseArgs(process.argv.slice(2));
const siteDir = args.site;
if (!siteDir) {
  console.error('generate-landing: --site is required');
  process.exit(1);
}

const manifestPath = path.join(siteDir, 'runs.json');
const maxAgeDays = Number(args['max-age-days'] ?? 7);
const repo = args.repo || '';

// --- load existing manifest -------------------------------------------------
let runs = [];
if (fs.existsSync(manifestPath)) {
  try {
    const parsed = JSON.parse(fs.readFileSync(manifestPath, 'utf8'));
    if (Array.isArray(parsed)) runs = parsed;
  } catch {
    console.warn('generate-landing: runs.json was unreadable, starting fresh');
  }
}

// --- upsert the new run (if one was provided) -------------------------------
const nowIso = new Date().toISOString();
if (args.slug) {
  runs = runs.filter((r) => r.slug !== args.slug);
  runs.push({
    slug: args.slug,
    builtAt: nowIso,
    sha: args.sha || '',
    runNumber: args.run ? Number(args.run) : null,
    subject: args.subject || '',
    commitUrl: args['commit-url'] || '',
  });
}

// --- prune entries older than the retention window --------------------------
const cutoff = Date.now() - maxAgeDays * 24 * 60 * 60 * 1000;
const kept = [];
for (const run of runs) {
  const builtMs = Date.parse(run.builtAt);
  // Keep when within the window (or when the timestamp is unparseable, to be safe).
  if (Number.isNaN(builtMs) || builtMs >= cutoff) {
    kept.push(run);
    continue;
  }
  const dir = path.join(siteDir, run.slug);
  fs.rmSync(dir, { recursive: true, force: true });
  console.log(`generate-landing: pruned ${run.slug} (built ${run.builtAt})`);
}

// newest first
kept.sort((a, b) => Date.parse(b.builtAt) - Date.parse(a.builtAt));

// --- write manifest + landing page ------------------------------------------
fs.writeFileSync(manifestPath, JSON.stringify(kept, null, 2) + '\n');

const esc = (s) =>
  String(s ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);

function fmtDate(iso) {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return esc(iso);
  // e.g. 2026-06-25 14:30 UTC
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getUTCFullYear()}-${pad(d.getUTCMonth() + 1)}-${pad(d.getUTCDate())} ${pad(d.getUTCHours())}:${pad(
    d.getUTCMinutes(),
  )} UTC`;
}

const items = kept
  .map((run) => {
    const shortSha = (run.sha || '').slice(0, 7);
    const runNo = run.runNumber != null ? `#${run.runNumber}` : '';
    const shaLink = run.commitUrl || '';
    const meta = [fmtDate(run.builtAt), shortSha ? (shaLink ? `<a href="${esc(shaLink)}">${esc(shortSha)}</a>` : esc(shortSha)) : '', runNo]
      .filter(Boolean)
      .join(' &middot; ');
    const subject = run.subject ? `<div class="subject">${esc(run.subject)}</div>` : '';
    return `      <li>
        <a class="run" href="./${esc(run.slug)}/index.html">
          <span class="meta">${meta}</span>
          ${subject}
        </a>
      </li>`;
  })
  .join('\n');

const title = repo ? `Binocular runs — ${esc(repo)}` : 'Binocular runs';
const empty = '<li class="empty">No runs available.</li>';

const html = `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>${title}</title>
  <style>
    :root { color-scheme: light dark; }
    body { font-family: system-ui, -apple-system, Segoe UI, Roboto, sans-serif; margin: 0; padding: 2.5rem 1.5rem; background: #0d1117; color: #e6edf3; }
    .wrap { max-width: 760px; margin: 0 auto; }
    h1 { font-size: 1.4rem; margin: 0 0 0.25rem; }
    p.sub { margin: 0 0 1.75rem; color: #8b949e; font-size: 0.9rem; }
    ul { list-style: none; padding: 0; margin: 0; }
    li { margin: 0 0 0.6rem; }
    a.run { display: block; padding: 0.85rem 1.1rem; border: 1px solid #30363d; border-radius: 8px; text-decoration: none; color: inherit; transition: border-color .15s, background .15s; }
    a.run:hover { border-color: #58a6ff; background: #161b22; }
    .meta { font-size: 0.95rem; }
    .meta a { color: #58a6ff; text-decoration: none; }
    .subject { color: #8b949e; font-size: 0.85rem; margin-top: 0.25rem; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .empty { color: #8b949e; }
    footer { margin-top: 2rem; color: #8b949e; font-size: 0.8rem; }
  </style>
</head>
<body>
  <div class="wrap">
    <h1>${title}</h1>
    <p class="sub">Previous mining runs — newest first. Keeping runs from the last ${esc(maxAgeDays)} day(s).</p>
    <ul>
${items || empty}
    </ul>
    <footer>Generated by Binocular at ${esc(nowIso)}</footer>
  </div>
</body>
</html>
`;

fs.writeFileSync(path.join(siteDir, 'index.html'), html);
console.log(`generate-landing: wrote index.html with ${kept.length} run(s)`);
