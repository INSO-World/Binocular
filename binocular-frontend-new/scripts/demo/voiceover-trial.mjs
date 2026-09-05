#!/usr/bin/env node
// Quick proof-of-concept: synthesizes a demo-*.md script's narration via Windows' built-in OneCore TTS
// (through PowerShell's WinRT Windows.Media.SpeechSynthesis) and layers it onto an already-recorded
// demo video, purely to audition voice quality before investing in a real per-cue duration-synced
// recording pipeline.
// NOT time-synced: narration and video play independently, so pacing will drift. ffmpeg required on PATH.
// Usage: node scripts/demo/voiceover-trial.mjs <script.md> [video.mp4] [voiceDisplayName]

import { execFileSync } from 'node:child_process';
import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import path from 'node:path';
import { exit } from 'node:process';

const [, , scriptArg, videoArg, voiceArg] = process.argv;
const voiceName = voiceArg || 'Microsoft Mark';

function fail(message) {
  console.error(message);
  exit(1);
}

if (!scriptArg) {
  fail('Usage: node scripts/demo/voiceover-trial.mjs <script.md> [video.mp4]');
}

try {
  execFileSync('ffmpeg', ['-version'], { stdio: 'ignore' });
  execFileSync('ffprobe', ['-version'], { stdio: 'ignore' });
} catch {
  fail('ffmpeg/ffprobe not found on PATH.');
}

const scriptPath = path.resolve(scriptArg);
if (!existsSync(scriptPath)) fail(`Script not found: ${scriptPath}`);

// Narration lives in blockquote (`> `) paragraphs; stage directions (`**[cue: ...]**`) are skipped.
// Consecutive `>` lines are one cue; a blank line (or any non-`>` line) ends it.
function extractCues(mdText) {
  const cues = [];
  let current = [];
  const flush = () => {
    if (current.length) cues.push(current.join(' ').replace(/\s+/g, ' ').trim());
    current = [];
  };
  for (const line of mdText.split(/\r?\n/)) {
    const match = /^>\s?(.*)$/.exec(line);
    if (match) {
      current.push(match[1]);
    } else {
      flush();
    }
  }
  flush();
  return cues.filter(Boolean);
}

const cues = extractCues(readFileSync(scriptPath, 'utf-8'));
if (!cues.length) fail('No narration (blockquote) paragraphs found in script.');
console.log(`Found ${cues.length} narration cue(s) in ${path.basename(scriptPath)}.`);

const OUTPUT_DIR = path.resolve('demo-output/voiceover-trial');
const audioDir = path.join(OUTPUT_DIR, 'cues');
mkdirSync(audioDir, { recursive: true });

const toFfmpegPath = (p) => p.replace(/\\/g, '/');

const manifest = cues.map((text, i) => ({
  text,
  path: path.join(audioDir, `cue-${String(i + 1).padStart(2, '0')}.wav`),
}));
const manifestPath = path.join(OUTPUT_DIR, 'cues.json');
writeFileSync(manifestPath, JSON.stringify(manifest), 'utf-8');

// One PowerShell process synthesizes every cue (spawning per-cue would pay SpeechSynthesizer's
// startup cost each time). OneCore voices (Mark, David, George, ...) aren't visible to the legacy
// System.Speech/SAPI API, only to the WinRT Windows.Media.SpeechSynthesis API used below, which
// requires manually awaiting IAsyncOperation results (PowerShell 5.1 has no native WinRT await).
const synthScript = `
Add-Type -AssemblyName System.Runtime.WindowsRuntime
$asTaskGeneric = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object {
  $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation\`1'
})[0]
function Await-WinRTOperation($WinRtOperation, [Type]$ResultType) {
  $asTask = $asTaskGeneric.MakeGenericMethod($ResultType)
  $netTask = $asTask.Invoke($null, @($WinRtOperation))
  $netTask.Wait(-1) | Out-Null
  return $netTask.Result
}

[Windows.Media.SpeechSynthesis.SpeechSynthesizer,Windows.Media.SpeechSynthesis,ContentType=WindowsRuntime] | Out-Null
[Windows.Storage.Streams.DataReader,Windows.Storage.Streams,ContentType=WindowsRuntime] | Out-Null

$cues = Get-Content -Raw -LiteralPath '${manifestPath.replace(/'/g, "''")}' | ConvertFrom-Json
$synth = New-Object Windows.Media.SpeechSynthesis.SpeechSynthesizer
$voice = [Windows.Media.SpeechSynthesis.SpeechSynthesizer]::AllVoices | Where-Object { $_.DisplayName -eq '${voiceName.replace(/'/g, "''")}' } | Select-Object -First 1
if (-not $voice) { throw "Voice not found: ${voiceName.replace(/'/g, "''")}" }
$synth.Voice = $voice

foreach ($cue in $cues) {
  $stream = Await-WinRTOperation $synth.SynthesizeTextToStreamAsync($cue.text) ([Windows.Media.SpeechSynthesis.SpeechSynthesisStream])
  $size = [int]$stream.Size
  $inputStream = $stream.GetInputStreamAt(0)
  $reader = New-Object Windows.Storage.Streams.DataReader($inputStream)
  Await-WinRTOperation $reader.LoadAsync($size) ([uint32]) | Out-Null
  $bytes = New-Object byte[] $size
  $reader.ReadBytes($bytes)
  [System.IO.File]::WriteAllBytes($cue.path, $bytes)
  $reader.Dispose()
  $stream.Dispose()
}
`;
const synthScriptPath = path.join(OUTPUT_DIR, 'synth.ps1');
writeFileSync(synthScriptPath, synthScript, 'utf-8');

console.log(`Synthesizing narration via Windows OneCore voice (${voiceName})...`);
execFileSync('powershell.exe', ['-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', synthScriptPath], { stdio: 'inherit' });

// Silence clip between cues must match the synthesized wavs' sample rate/channels for concat -c copy to work.
function probeAudioFormat(file) {
  const out = execFileSync(
    'ffprobe',
    ['-v', 'error', '-select_streams', 'a:0', '-show_entries', 'stream=sample_rate,channels', '-of', 'json', file],
    { encoding: 'utf-8' },
  );
  return JSON.parse(out).streams[0];
}
const fmt = probeAudioFormat(manifest[0].path);

const silencePath = path.join(OUTPUT_DIR, 'silence.wav');
execFileSync(
  'ffmpeg',
  ['-y', '-f', 'lavfi', '-i', `anullsrc=r=${fmt.sample_rate}:cl=${fmt.channels === 2 ? 'stereo' : 'mono'}`, '-t', '0.6', silencePath],
  { stdio: 'ignore' },
);

const listPath = path.join(OUTPUT_DIR, 'concat-list.txt');
const listLines = [];
manifest.forEach((cue, i) => {
  listLines.push(`file '${toFfmpegPath(cue.path)}'`);
  if (i < manifest.length - 1) listLines.push(`file '${toFfmpegPath(silencePath)}'`);
});
writeFileSync(listPath, listLines.join('\n'), 'utf-8');

const voiceoverPath = path.join(OUTPUT_DIR, 'voiceover.wav');
console.log('Concatenating narration clips (with 0.6s gaps between cues)...');
execFileSync('ffmpeg', ['-y', '-f', 'concat', '-safe', '0', '-i', listPath, '-c', 'copy', voiceoverPath], { stdio: 'inherit' });

console.log(`\nVoiceover written to ${voiceoverPath}`);

if (videoArg) {
  const videoPath = path.resolve(videoArg);
  if (!existsSync(videoPath)) fail(`Video not found: ${videoPath}`);
  const combinedPath = path.join(OUTPUT_DIR, 'preview-with-voiceover.mp4');
  console.log('Muxing onto video for preview (NOT time-synced — narration and video run independently)...');
  execFileSync(
    'ffmpeg',
    ['-y', '-i', videoPath, '-i', voiceoverPath, '-map', '0:v', '-map', '1:a', '-c:v', 'copy', '-c:a', 'aac', '-b:a', '192k', combinedPath],
    { stdio: 'inherit' },
  );
  console.log(`Preview written to ${combinedPath}`);
}
