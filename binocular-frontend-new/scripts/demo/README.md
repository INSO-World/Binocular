# Demo Video Creation

How Binocular's narrated demo videos get made: `.md` scripts → recorded Playwright video → TTS
narration → muxed `.mp4`. This covers the whole pipeline, the invariants that must stay in sync,
and the mistakes that are easy to make.

## The pieces

| File | Role |
|---|---|
| `demo-core-v3.md`, `demo-category-*.md` | The script: narration text + stage directions. Source of truth for what's said and what's on screen. |
| `demo-core.test.ts`, `demo-category-*.test.ts` | Playwright test that drives the UI and records video. Paced by hand with `beat()` calls, cross-referenced to the `.md` via `// Cue N` comments. |
| `util/demoInteractions.ts` | Shared interaction primitives: `beat()` (named wait + timing log), `humanMove`/`humanClick` (real mouse movement so the cursor animates on camera), `dumpBeatLog()`. |
| `demoSetup.ts` | Seeds the `localStorage` state every recorded scene starts from (Mock Data plugin, tabs, sprints, date range). |
| `globalSetup.ts` | Playwright global setup — warms up every visualization plugin once before recording so the real run doesn't pay cold-start cost on camera. |
| `narrate.mjs` | Synthesizes narration audio per cue via Edge TTS, times it, and muxes it onto a recorded video. |
| `render-demo-videos.mjs` | Converts raw Playwright `.webm` recordings into `.mp4` (no narration yet — just format conversion). |

## `demo-output/` is local-only, never committed

`.gitignore` excludes `/demo-output/*` entirely. Everything under it — recorded `.webm`/`.mp4`
videos, the narration cache (`narration/<slug>__<voice>/`), real per-cue beat logs
(`cue-timing/<slug>.json`), and any manual `cue-timing/<slug>.anchors.json` — is generated locally
and regenerated from scratch on a fresh clone or CI run. Nobody else sees your recorded video or
your anchors file unless they run the pipeline themselves; don't assume one exists, and don't rely
on one surviving between machines or a `git clean`.

## Cue numbers: a human contract, not a tooling one

Every blockquote (`> `) paragraph in a `.md` script is a **cue**, numbered 1, 2, 3... by its
position in the file — nothing in the `.md` itself states the number. `narrate.mjs` doesn't read
`// Cue N` comments at all; those exist purely so whoever writes the `.test.ts` can see, by eye,
roughly where each cue's narration should land relative to the `beat(page, ...)` calls that pace
the recording.

**This means:** if you add, remove, or reorder a cue in the `.md`, every `// Cue N` comment in the
`.test.ts` for numbers *after* that point is now a stale human note and should be renumbered by
hand so the next person tuning that test isn't misled — nothing breaks silently if you don't, since
no code consumes these numbers, but the file becomes harder to hand-tune correctly. When editing a
script:

1. Count cue position by hand (or grep `^>` in the `.md`) before and after your edit.
2. Renumber every `// Cue N` (and `// Cues N+M`, `cue N` prose mentions) in the matching `.test.ts`
   for every cue after the edit point.

## Stage directions vs. narration

- Lines starting with `> ` are **spoken** — sent to TTS, shown in the `.srt`.
- `**[cue: ...]**` / `**[on screen: ...]**` lines are **not spoken** — stage directions read by
  whoever writes the `.test.ts`, describing what the UI should be doing during that narration.
- Consecutive `>` lines merge into a single cue; a blank line (or any non-`>` line) ends it.

## Workflow: writing or editing a script

1. **Write/edit the `.md`.** Keep narration in `> ` blockquotes, stage directions in `**[cue: ...]**`.
   Verify factual claims about data/behavior against the actual source or mock dataset before
   writing them down — narration claims go stale silently (see *Don't* list below).
2. **Write/edit the `.test.ts`.** Drive the UI with `humanClickLocator`/`humanFill`/etc., pace it
   with `beat(page, ms)`, and annotate roughly where each cue's narration should land with a
   `// Cue N: "..."` comment on or near the relevant `beat()` call.
3. **Record:** `npm run demo:record` (Playwright, `DEMO=1`, one worker). This produces raw `.webm`
   videos and — because each test calls `dumpBeatLog(slug)` — a real per-cue timing log at
   `demo-output/cue-timing/<slug>.json`.
4. **Render to mp4:** `npm run demo:render` (wraps `render-demo-videos.mjs`) — pure format
   conversion, no narration yet.
5. **Synthesize narration (audition):** `node scripts/demo/narrate.mjs synth <script.md> [voice]`.
   Caches per-cue audio + a manifest under `demo-output/narration/<slug>__<voice>/`, prints total
   duration. Cheap to re-run — a no-op if cue text and `MANIFEST_VERSION` haven't changed.
6. **Finalize (mux):** `node scripts/demo/narrate.mjs finalize <script.md> <video> [voice] [outPath]`.
   Re-synths (cache permitting), places cues **sequentially** (fixed `GAP_SEC` gaps — real
   video-timestamp matching from the beat log is deliberately disabled here, see comment in
   `finalize()`), writes the muxed `.mp4` plus a sidecar `.srt`.

Default voice is `en-GB-RyanNeural` (`DEFAULT_VOICE` in `narrate.mjs`); ffmpeg/ffprobe must be on
`PATH`.

**Every `synth` run (cache hit or not) prints a per-section runtime breakdown** — each `## Section
(~Ns)` heading's cues, summed with the fixed inter-cue gap, e.g.:

```
  per-section runtime (cue speech + inter-cue gaps, approximating the actual timeline):
    Changes: ~84.2s (6 cues)
    Sum Commits: ~69.7s (6 cues)
    ...
```

Use this after editing a section's cues to see immediately whether its real runtime drifted from
the `(~Ns)` estimate in its `## ` heading — update the heading by hand if it's meaningfully off, or
use it as a sanity check before deciding a section needs trimming/padding. It's computed from the
manifest (`extractCues`'s per-cue `section` tag + real measured `durationSec`), not from the
recorded video — see *Timing model* below for why those two can diverge.

## Fixing mispronunciations

TTS mispronunciation is common for camelCase identifiers, abbreviations, and slash-separated lists.
Two independent mechanisms, in order of preference:

1. **Reword the `.md` itself**, if the visible/subtitle text can just say it differently (e.g. we
   replaced narrated "SHAs" with "commit hashes" rather than fighting the pronunciation — simpler
   and more honest than a phonetic hack).
2. **`SPEECH_OVERRIDES` in `narrate.mjs`**, when the visible script text should stay as-is but the
   *spoken* version needs to differ. A list of `[pattern, replacement]` pairs applied to the whole
   cue before synthesis — doesn't touch the raw cue text used for the `.srt`. Use for: bare
   abbreviations (`KPI` → `K P I`), unwrapped camelCase (`MaxBurst` → `Max Burst`), slash-lists
   (`curved/stepped/linear` → `curved, stepped, or linear`), and brand names the TTS voice
   mis-reads (`GitLab` → `Gitlab`).
3. Real code identifiers wrapped in backticks (`` `changeRatio` ``) already get automatic cleanup
   via `cleanForSpeech`: `===`/`!==` → "equals", `.`/`_`/`-` → space, camelCase → spaced words. This
   is the mechanism to reach for first for genuine code spans — wrap it in backticks rather than
   adding a one-off override.

**Whenever you change `cleanForSpeech` or `SPEECH_OVERRIDES`, bump `MANIFEST_VERSION`.** The cache
is keyed by cue text + this version; if the *text* didn't change but the *TTS-generation logic*
did, a stale cache would otherwise keep serving the old (wrong) audio. Bumping it invalidates cached
narration for every script, not just the one you're working on — expect the next `synth`/`finalize`
on any script to resynthesize from scratch.

## Timing model

- `beat(page, ms)` holds the current frame for `ms` and logs the real wall-clock offset since the
  test started. The `ms` argument is **hand-tuned per call** to roughly match how long the
  corresponding narration cue takes to say — nothing computes it for you.
- `dumpBeatLog(slug)` (call once, at the end of each test) persists that log to
  `demo-output/cue-timing/<slug>.json`, but `narrate.mjs` no longer reads it — `finalize()` places
  cues sequentially with a fixed gap instead. Matching real video timestamps only ever placed
  narration where the video *happened* to already be, and any mismatch between recorded pacing and
  narration length became dead air (confirmed: up to ~79s silences). The beat-log-to-cue mapping and
  manual anchor-correction machinery this used to rely on (`// Cue N` comments,
  `demo-output/cue-timing/<slug>.anchors.json`) has been removed from `narrate.mjs`; the beat log
  itself is still written but is now unused.
- **Practical upshot:** if a cue's spoken text changes enough to meaningfully change its duration
  (a `SPEECH_OVERRIDES` entry, a reworded sentence, an added/removed cue), the *narration and `.srt`
  timing* re-derive automatically from the freshly-measured audio next `synth`/`finalize` — but the
  *recorded video's* pacing does not. The `beat()` `ms` values in the `.test.ts` were tuned to the
  old speech length and may now run short or long relative to the new narration. Re-check (or
  re-record) the affected segment before finalizing.

## Do

- Wrap real code identifiers/values in backticks in the `.md` so `cleanForSpeech`'s cleanup applies.
- Bump `MANIFEST_VERSION` whenever `cleanForSpeech`/`SPEECH_OVERRIDES` logic changes.
- Renumber every downstream `// Cue N` comment when a cue is added/removed/reordered.
- Re-record (`npm run demo:record`) a category after a `beat()`-affecting text change, before
  `finalize`-ing it for real — otherwise the muxed narration and the recorded video drift apart.

## Don't

- Don't add an SSML `<break>` element — this Edge TTS endpoint rejects the whole request
  ("SSML is invalid") outright if one's present. Pauses come from splitting a cue on em dash (`—`)
  into separate TTS requests instead, spliced with a short silence.
- Don't leave a slash-separated list (`success/fail`) in narration prose — `/` gets vocalized as
  "slash". Reword with "or"/commas, or add a `SPEECH_OVERRIDES` entry.
- Don't assume a bare abbreviation reads fine — some TTS voices spell out `CI`/`ID` correctly but
  garble others, or (as with `GitLab`/`git`) inconsistently mispronounce near-identical brand
  names side-by-side. Audition anything non-obvious.
- Don't run `finalize` (or trust its narration timing) as a substitute for re-recording after a
  `beat()`-relevant wording change — it does not resync video pacing, only audio/subtitle timing.
