# Binocular Demo Category — Statistics (Repository Stats)

**Estimated runtime: ~65s**
**Self-contained** — does not assume the core script or any other category video played first.
`VisualizationPluginMetadataCategory.Statistics` currently has a single registered plugin, so this
is a one-segment category video.

Grounded in: `binocular-frontend-new/src/plugins/visualizationPlugins/stats/repositoryStats/src/chart/chart.tsx`,
`.../stats/repositoryStats/src/settings/settings.tsx`, `.../stats/repositoryStats/src/help/help.tsx`.

---

**[on screen: Binocular open with a full-viewport Repository Stats panel]**

> Repository Stats is the plainest visualization in Binocular: five toggleable KPI tiles —
> Contributors, Commits, Issues, Builds, and Merge Requests — counted within your selected date
> range.

**[cue: open settings → toggle "Show merge requests" off]**

> Every one of these tiles can be switched on or off independently — turn off the ones you don't
> want cluttering the dashboard.

**[cue: toggle "Show builds" off]**

> Same for Builds — it's purely a display toggle, the underlying counts aren't affected either
> way.

**[cue: point at the "Contributors" tile]**

> The natural read: this is simply how many contributors, commits, or issues this project has,
> full stop.

**[cue: hold on the panel]**

> Not quite. Every one of these numbers comes from a Redux selector that responds to the shared
> Parameters date range — the chart dispatches `setDateRange` whenever that range changes, and all
> five tiles recompute against it. There's nothing in the tile labels themselves — no "12
> contributors" caveat, no visible date window — to tell you these are scoped to a time range at
> all. It always reads as one ambient, all-time truth even though it's quietly filtered underneath
> by whatever date range happens to be selected elsewhere in the tool.

---

**[END CATEGORY — ≈65s]**
