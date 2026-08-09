# Binocular Demo Category — Expertise (Code Expertise, Knowledge Radar)

**Estimated runtime: ~155s (~2.6min)**
**Self-contained** — does not assume the core script or any other category video played first.
Code Hotspots is also registered under `VisualizationPluginMetadataCategory.Expertise`, but it's
`popoutOnly` (can only be viewed in its own popped-out window) and isn't wired into any existing
demo/screenshot automation, so it's excluded from this video for now.

Grounded in:
`binocular-frontend-new/src/plugins/visualizationPlugins/expertise/codeExpertise/src/{settings/settings.tsx,chart/chart.tsx,help/help.tsx}`,
`.../expertise/knowledgeRadar/src/{utilities/dataConverter.ts,help/help.tsx}`.

---

## Code Expertise (~85s)

**[on screen: Binocular open, full-viewport Code Expertise ring chart]**

> This is Binocular's Code Expertise view — one ring, one segment per developer, summarizing each
> person's footprint on the project at a glance.

**[cue: point out one segment's angular width]**

> The size of each segment is proportional to how many lines that developer has added across the
> project's entire history — bigger segment, more code they've written in total.

**[cue: point out the colored middle band vs. the hashed band within one segment]**

> Inside each segment, the solid colored band is lines they added that are still in the codebase
> today; the hashed band is lines they added that have since been replaced or deleted — written,
> but not surviving.

**[cue: point out the outer green/red arc, then the inner dotted band]**

> The outer arc splits green versus red — the ratio of their commits that passed CI versus failed
> it — and the inner dotted band shows their relative commit count against everyone else.

**[cue: open the "Branch:" dropdown — the plugin's only custom setting — and pick a branch]**

> The one control this plugin adds on top of the shared author and file filters is a Branch
> selector, scoping the whole ring to that branch's history.

**[cue: point at a large, mostly-solid segment]**

> The tempting read: this is your project's resident expert on this codebase.

**[cue: hold on the chart]**

> Be careful with "expert" here. This chart has no concept of time decay — it sums total lines
> added across the *entire* project history and checks which of those lines still exist today. A
> developer who wrote a huge amount of code years ago and hasn't opened the repository since
> scores exactly the same as someone actively working in it right now, as long as their old lines
> are still standing.

## Knowledge Radar (~70s)

**[on screen: full-viewport Knowledge Radar chart]**

> Knowledge Radar plots expertise differently: a radar chart with one axis per top-level package,
> distance from center showing how deeply the selected developer has worked in it.

**[cue: click a package name on the radar to navigate deeper into the hierarchy]**

> Clicking any package name on the radar drills one level deeper into that part of the codebase,
> and a back control lets you step back up.

**[cue: point at an axis where the plotted point sits right at the edge]**

> The natural read: a point sitting at the very edge means this developer is the expert in that
> package.

**[cue: hold on the chart]**

> The score behind that point is a plain ownership ratio — the developer's share of commits
> touching that package divided by everyone's commits there. A developer who wrote all 3 of the
> only 3 commits ever made to a tiny, rarely-touched package scores the maximum, exactly the same
> as someone who wrote 300 of 300 commits in the project's largest, most complex package. And the
> calculation only excludes a commit as a "merge commit" by checking whether its message literally
> contains the word "merge" — so a normal, hand-written commit that happens to mention merging
> something in its message (say, fixing merge conflicts in application logic) gets silently
> dropped from this developer's score, undercounting real work in whatever package it touched.

---

**[END CATEGORY — ≈155s]**
