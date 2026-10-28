# Binocular Demo Category — Builds (Builds)

**Estimated runtime: ~80s**
**Self-contained** — does not assume the core script or any other category video played first.
`VisualizationPluginMetadataCategory.Builds` currently has a single registered plugin, so this is
a one-segment category video.

Grounded in: `binocular-frontend-new/src/plugins/visualizationPlugins/builds/builds/src/settings/settings.tsx`,
`.../builds/builds/src/utilities/dataConverter.ts`, `.../builds/builds/src/help/help.tsx`,
`binocular-backend/core/provider/github.ts` (`getPipelines`), `binocular-backend/indexers/ci/CIIndexer.ts`,
`binocular-backend/models/models/Build.ts`.

---

**[on screen: Binocular open with a full-viewport Builds chart]**

> This is Binocular's Builds view — CI status over time, pulled straight from your GitHub Actions
> or GitLab CI history.

**[cue: point out the diverging chart — a positive band and a negative band around a center line]**

> Successful builds stack up above the axis, failed and cancelled builds stack down below it — so
> a healthy pipeline looks like a chart that stays mostly above the line.

**[cue: open settings → toggle "Split Builds per Author" on]**

> Split Builds per Author breaks that same success/fail split out per contributor, so you can see
> whose changes are triggering the most red.

**[cue: toggle "Show Sprints" on]**

> Show Sprints, same as elsewhere in Binocular, overlays your sprint boundaries on the timeline.

**[cue: change "Visualization Style" dropdown]**

> Visualization Style is the usual curved/stepped/linear cosmetic choice.

**[cue: point at a widening red band under the axis]**

> The obvious read here: a growing band below the line means your failure rate is climbing —
> something in the pipeline is getting less reliable.

**[cue: hold on the chart]**

> Two things worth knowing about what's actually feeding this chart. First, Binocular's CI indexer
> pulls workflow runs for the *entire repository*, with no branch filter — so builds from
> unrelated feature branches and pull requests are mixed into the same trend as your main branch.
> Second, builds are stored keyed by their run ID, and a newer run simply overwrites the old one
> in place — so if a failed build gets manually re-run to green, the original failure is gone from
> this chart entirely. What you're looking at is closer to "the latest known status of every
> workflow run across the whole repo" than "the true first-attempt pass rate of your main branch."

---

**[END CATEGORY — ≈80s]**
