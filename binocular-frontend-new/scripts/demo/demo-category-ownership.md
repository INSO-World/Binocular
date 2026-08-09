# Binocular Demo Category — Ownership (Code Ownership)

**Estimated runtime: ~90s**
**Self-contained** — does not assume the core script or any other category video played first.
`VisualizationPluginMetadataCategory.Ownership` currently has a single registered plugin, so this
is a one-segment category video.

Grounded in: `binocular-frontend-new/src/plugins/visualizationPlugins/ownership/codeOwnership/src/help/help.tsx`,
`.../codeOwnership/src/settings/settings.tsx` (Display Mode / Branch / Visualization Style / Show Sprints),
`.../codeOwnership/src/utils/*` (line-based hunk attribution), `.../codeOwnership/src/chart/chart.tsx`.

---

**[on screen: Binocular open with a full-viewport Code Ownership chart]**

> This is Binocular's Code Ownership view — it shows how much of your *current* codebase each
> contributor is the last author of, evolving over the project's whole history.

**[cue: point out the stacked area chart — bands per author, one gray "other" band]**

> Each band is one author's share of surviving lines at that point in time — not commit count,
> literal lines of code that are still there today and still attributed to them.

**[cue: switch "Display Mode" from absolute to relative]**

> Display Mode switches between absolute line counts and a relative 0-to-100% share, so you can
> compare proportional ownership even as the codebase grows.

**[cue: open the "Branch:" dropdown, pick a branch]**

> The Branch selector scopes the whole calculation to a specific branch's history.

**[cue: change "Visualization Style" dropdown, toggle "Show Sprints" on]**

> Show Sprints and Visualization Style behave exactly like they do everywhere else in Binocular —
> sprint overlays and curve interpolation, no change to the underlying numbers.

**[cue: hover a band that spikes on one commit]**

> The natural read: whoever's band is biggest "owns" the most of this project.

**[cue: hold on the chart]**

> That's true only in a narrow, literal sense. Ownership here is last-write-wins, per line — if
> someone reformats a file, renames a variable everywhere, or runs an auto-formatter across the
> repo, every line they touch becomes newly "theirs," even though they didn't write the actual
> logic. The person who originally designed that code can lose their ownership share to whoever
> most recently reformatted it. The plugin's own help text specifically recommends deselecting
> large auto-generated files in the File Tree — a `package-lock.json`, a generated bundle —
> because a single commit touching one of those can dominate the entire chart.

---

**[END CATEGORY — ≈90s]**
