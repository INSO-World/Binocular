#!/usr/bin/env node
// Converts `npm run demo:record`'s raw .webm recordings into YouTube-ready H.264 .mp4 files (core.mp4 + category-*.mp4); requires ffmpeg on PATH. Usage: npm run demo:render.

import { execFileSync } from 'node:child_process';
import { existsSync, mkdirSync, readFileSync } from 'node:fs';
import path from 'node:path';
import { exit } from 'node:process';

const RESULTS_FILE = path.resolve('test-results/demo-results.json');
const OUTPUT_DIR = path.resolve('demo-output');

function fail(message) {
  console.error(message);
  exit(1);
}

if (!existsSync(RESULTS_FILE)) {
  fail(`No ${RESULTS_FILE} found — run \`npm run demo:record\` first.`);
}

try {
  execFileSync('ffmpeg', ['-version'], { stdio: 'ignore' });
} catch {
  fail('ffmpeg not found on PATH. Install it (e.g. `winget install Gyan.FFmpeg`) and try again.');
}

function slugify(title) {
  return title
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '');
}

// Recursively walk Playwright's JSON reporter suite tree collecting { title, videoPath } pairs.
function collectVideos(suites, out = []) {
  for (const suite of suites ?? []) {
    for (const spec of suite.specs ?? []) {
      for (const specTest of spec.tests ?? []) {
        for (const result of specTest.results ?? []) {
          const videoAttachment = (result.attachments ?? []).find((a) => a.name === 'video');
          if (videoAttachment?.path) {
            out.push({ title: spec.title, videoPath: videoAttachment.path });
          }
        }
      }
    }
    collectVideos(suite.suites, out);
  }
  return out;
}

const report = JSON.parse(readFileSync(RESULTS_FILE, 'utf-8'));
const videos = collectVideos(report.suites);

if (videos.length === 0) {
  fail('No recorded videos found in demo-results.json — did demo:record actually run any tests?');
}

mkdirSync(OUTPUT_DIR, { recursive: true });

for (const { title, videoPath } of videos) {
  // Two sources feed this results file: demo-core.test.ts ("Binocular core") and demo-category-*.test.ts ("Category: <...>").
  const isCore = /^binocular core$/i.test(title);
  const categoryMatch = /^category:\s*(.+)$/i.exec(title);
  const outputName = isCore ? 'core.mp4' : categoryMatch ? `category-${slugify(categoryMatch[1])}.mp4` : `${slugify(title)}.mp4`;
  const outputPath = path.join(OUTPUT_DIR, outputName);

  console.log(`Rendering "${title}" -> ${outputName}`);
  execFileSync('ffmpeg', ['-y', '-i', videoPath, '-c:v', 'libx264', '-crf', '18', '-preset', 'slow', '-pix_fmt', 'yuv420p', outputPath], {
    stdio: 'inherit',
  });
}

console.log(`\nDone — ${videos.length} clip(s) written to ${OUTPUT_DIR}`);
