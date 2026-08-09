# Binocular Demo Category — Issues (Issues, Merge Requests, Issues Timeline, Burndown)

**Estimated runtime: ~295s (~4.9min)**
**Self-contained** — does not assume the core script or any other category video played first.
Covers every plugin registered under `VisualizationPluginMetadataCategory.Issues`.

Grounded in:
`binocular-frontend-new/src/plugins/visualizationPlugins/issues/issues/src/{settings/settings.tsx,utilities/dataConverter.ts,help/help.tsx}`,
`.../issues/mergeRequests/src/{settings/settings.tsx,utilities/dataConverter.ts,help/help.tsx}`,
`.../issues/issuesTimeline/src/{settings/settings.tsx,chart/helper/groupIntoTracks.ts,help/help.tsx}`,
`.../issues/burndown/src/{settings/settings.tsx,chart/helper/groupIssuesByGranularity.ts,help/help.tsx}`,
`binocular-backend/indexers/its/GitHubITSIndexer.ts`, `binocular-backend/core/provider/github.ts`.

---

## Issues (~85s)

**[on screen: Binocular open, full-viewport Issues chart]**

> This is Binocular's Issues view — it tracks the issues in your GitHub or GitLab tracker over
> time, right alongside your git history.

**[cue: point out the chart — two series, "Opened" rising, "Closed" rising alongside it]**

> By default you get two series: how many issues were opened in each time bucket, and how many
> were closed — the flow in and out of your backlog.

**[cue: open settings → toggle "Breakdown (Total Open Issues)" on]**

> Turn on Breakdown, and it collapses those into a single line: the actual number of issues
> sitting open at any given moment. Same underlying data, two different questions — one shows
> activity rate, the other shows backlog size.

**[cue: toggle "Split Issues per Assignee" on]**

> Split Issues per Assignee breaks that same data out per person, so you can see whose queue is
> actually growing.

**[cue: change "Visualization Style" dropdown]**

> Visualization Style is the same curved/stepped/linear interpolation choice you'll see across
> most of Binocular's time-series charts — cosmetic only.

**[cue: hover the Closed series]**

> Here's a natural read: if the Closed line is climbing steadily, the team is keeping up with
> incoming work.

**[cue: hold on the chart]**

> One thing this view can't show you: reopened issues. GitHub only reports an issue's *current*
> closed timestamp — the moment it's reopened, that timestamp is cleared, and Binocular's indexer
> doesn't separately capture reopen events. An issue that was closed, reopened, and is still open
> today has no historical "closed" moment anywhere in this data — it silently reads as having been
> open the whole time.

## Merge Requests (~75s)

**[on screen: full-viewport Merge Requests chart]**

> Merge Requests is the same shape as Issues, but tracks pull/merge request lifecycle instead —
> opened, merged, or closed over time.

**[cue: open settings → toggle "Breakdown (Total Open Merge Requests)" on]**

> Breakdown here works exactly like it does for Issues — one line for the current open count
> instead of separate opened/merged/closed series.

**[cue: toggle "Split Merge Requests per Assignee" on]**

> Split Merge Requests per Assignee breaks the chart down per person.

**[cue: hold on the chart, point at a MR bucketed as "Account not assigned"]**

> Splitting by assignee surfaces two fallback buckets you won't see anywhere else: "Unassigned"
> for merge requests with no assignee at all, and "Account not assigned" for ones that do have a
> platform assignee, but the indexer couldn't match that account to a local git user. Both mean
> the same practical thing — this MR won't show up under any real contributor's line, no matter
> who's actually responsible for it.

**[cue: hold on the chart, no further action]**

> That account-linking gap runs deeper than just the fallback buckets. Whenever GitHub or GitLab
> account matching fails, that person's merge requests get folded into "Account not assigned"
> instead of their own line — so a per-assignee split always risks undercounting real
> contributors purely because of a linking failure, not because they aren't actually doing the
> work.

## Issues Timeline (~80s)

**[on screen: full-viewport Issues Timeline — horizontal bars on a calendar axis]**

> Issues Timeline lays every issue and merge request out as a horizontal bar on a real calendar
> timeline, one track per overlapping item.

**[cue: open settings → change "Coloring Mode" dropdown to author, then note the other options]**

> Coloring Mode changes what each bar's color represents — author, assignee, most time spent on
> GitLab, or the labels attached to the issue.

**[cue: point out two bars sitting on separate tracks despite similar dates]**

> The natural read: an issue's vertical position in the timeline reflects something about the
> issue itself — maybe its priority, or its type.

**[cue: hold on the chart]**

> It doesn't. Tracks are assigned purely by an overlap-packing algorithm: an issue lands in the
> first track where none of the existing bars overlap its own open date range. Which track any
> given issue ends up in is an artifact of processing order, not a property of the issue — the
> same issue can land on a different track from one render to the next if the underlying list
> order changes. And if you switch Coloring Mode to "labels," similar label text (like "bug" and
> "bug fix") can get grouped into the same color purely by string edit-distance, even if they
> represent unrelated categories of work.

## Burndown (~55s)

**[on screen: full-viewport Burndown chart — an ideal line over the selected time span]**

> Burndown is the simplest one in this group: it plots the open-ticket trajectory for whatever
> time span and granularity you've selected, no per-author or per-status split at all.

**[cue: open settings → toggle "Show Sprints" on]**

> Show Sprints is the only setting here — it overlays your defined sprint boundaries on the
> timeline, same as everywhere else in Binocular.

**[cue: hold on the chart, no further action]**

> One granularity quirk worth knowing: at every setting except "years," a date counts as "open"
> using an inclusive range check against an issue's created and closed timestamps — so an issue
> closed on exactly the day being checked still counts as open for that day. And if you switch
> Granularity to "years," the underlying test changes entirely: it stops checking whether an issue
> was open during that year at all, and instead checks only whether the issue was *created* in
> that year. Switching to yearly granularity silently turns this from an open-ticket burndown into
> a creation-year histogram, with nothing in the chart signaling that the meaning changed.

---

**[END CATEGORY — ≈295s]**
