# Binocular Demo Category — Author Behaviour (Time Spent, Collaboration, Repository Activity)

**Estimated runtime: ~220s (~3.7min)**
**Self-contained** — does not assume the core script or any other category video played first.
Covers every plugin registered under `VisualizationPluginMetadataCategory.AuthorBehaviour`.

Grounded in:
`binocular-frontend-new/src/plugins/visualizationPlugins/authorBehaviour/timeSpent/src/{settings/settings.tsx,utilities/dataConverter.ts,help/help.tsx}`,
`.../authorBehaviour/collaboration/src/{settings/settings.tsx,utilities/dataConverter.ts,chart/networkChart.tsx}`,
`.../authorBehaviour/repositoryActivity/src/{settings/settings.tsx,utilities/activityTimelineUtils.ts,help/help.tsx}`.

---

## Time Spent (~80s)

**[on screen: Binocular open, full-viewport Time Spent chart]**

> (GitLab only) Time Spent tracks the hours users logged against issues, pulled from GitLab's
> time-tracking notes.

**[cue: open settings → toggle "Breakdown (Total Time)" on]**

> Breakdown switches from per-interval time logged to a running cumulative total — how much time
> has been logged so far, at any point.

**[cue: toggle "Split Time per Issue" on]**

> Split Time per Issue breaks that same total out per issue or merge request instead of per
> author, so you can see where the hours are actually going.

**[cue: change "Visualization Style" dropdown]**

> Visualization Style is the same curved/stepped/linear cosmetic choice as elsewhere in Binocular.

**[cue: hold on the chart with Breakdown enabled]**

> Here's a real read this supports: the cumulative Breakdown line for time spent should only ever
> climb — Binocular explicitly clamps it to never decrease, even when someone logs negative time
> (a correction).

**[cue: hold on the chart, no further action]**

> That clamp is worth knowing about precisely because of what it hides. If someone logs 10 hours
> against an issue and later corrects that down by logging -8 hours, the Spent line still shows
> the original, pre-correction total climbing — it never drops. The correction shows up instead as
> a separate "time removed" line, clamped the opposite way. A single logged-then-fully-reverted
> time entry ends up looking like two separate movements in the chart rather than netting out to
> zero, which can visually suggest more total time was tracked than actually stuck.

## Collaboration (~90s)

**[on screen: full-viewport Collaboration force-directed graph]**

> Collaboration is a network graph of the people working on your project, built from your issue
> tracker rather than your commit history.

**[cue: point out nodes (avatars) and edges connecting them]**

> Every node is a contributor. An edge between two people means they've both been attached to the
> same issue or merge request — as author, as assignee, or both.

**[cue: point out edge thickness differences]**

> Thicker edges mean more shared issues and merge requests between that pair — it's literally the
> square root of how many they have in common, so the visual weight doesn't scale linearly.

**[cue: drag a node — graph re-settles via force simulation]**

> The whole thing is a live force layout — drag any node and the rest resettles around it.

**[cue: open "Collaboration Strength Range" → raise Min from 1 to a higher value]**

> Collaboration Strength Range lets you filter the graph down to only the stronger connections —
> raise the minimum and the loosely-connected edges disappear, leaving just your core
> collaborating groups.

**[cue: toggle "Include commit message references" on]**

> Include Commit Message References adds one more source of edges: if someone's commit message
> mentions an issue or merge request by number, they're linked to everyone else already attached
> to that item — even if they never touched it through the tracker UI at all.

**[cue: point at two densely-connected nodes]**

> The natural read: these two people work closely together.

**[cue: hold on the graph]**

> What the edge actually guarantees is much thinner than that. Building these links has no concept
> of time at all — two people get connected just for both appearing in the same issue's
> participant list, whether that happened in the same week or three years apart. This graph shows
> shared *bureaucratic association* with the same tracker items — not verified pairing, not
> real-time communication.

## Repository Activity (~65s)

**[on screen: full-viewport Repository Activity heatmap]**

> Repository Activity is a GitHub-style contribution heatmap: every commit — and other tracked
> activity — bucketed by day, then colored by volume.

**[cue: hover a few cells to show the tooltip's date and activity breakdown]**

> Hovering any cell shows exactly what made up that day: commits, builds, issues, merge requests,
> notes, and branch events, all counted toward the same total.

**[cue: open settings → toggle "Show Activity Timeline" off]**

> Toggling Show Activity Timeline off switches from this calendar overview to a detailed 24-hour
> by 7-day heatmap for a single selected week, showing what time of day activity tends to happen.

**[cue: hold on the calendar view]**

> One thing worth knowing about how "activity" is counted: every type — a single-line commit, a
> sprawling 500-file commit, a CI rerun, an issue comment — counts as exactly one unit toward that
> day's total. A day with twenty automated CI reruns of the same commit inflates the heatmap
> exactly as much as twenty distinct, meaningfully different human contributions would — the color
> intensity can't tell the two apart.

---

**[END CATEGORY — ≈220s]**
