# Binocular Demo — Core Script (v3)

**Estimated runtime: ~2:30**
**Covers:** first load, in-UI setup wizard, tabs tour. Nothing else.
**Does NOT cover:** `binocular setup`, `.binocularrc`, `binocular run` flags, or any CLI/backend
configuration — that's README territory and is intentionally cut from this script (see summary
notes for why). Also does not cover drag/resize, popout, theme toggle, or the export dialog —
those are dashboard-item mechanics, not tabs, and are out of scope for this core/tabs split.

This script is designed to precede **any subset** of the six visualization modules
(`demo-module-*.md`), including zero of them. Nothing after the bridge line assumes a specific
module plays next.

Grounded in: `binocular-frontend-new/src/components/setupDialog/**`,
`binocular-frontend-new/src/App.tsx` (tab declarations), `binocular-frontend-new/src/components/tabs/**`,
`binocular-frontend-new/src/components/tabs/help/helpGeneral/helpGeneral.tsx` (in-app tab documentation).

---

## 1. Hook / framing (~20–25s)

**[on screen: Binocular logo / landing, no cue yet]**

> Every git repository already has a story in it — who wrote what, which issues actually got
> closed, which builds keep breaking. That story is just scattered across git history, an issue
> tracker, and a CI dashboard that don't talk to each other.
>
> Binocular pulls all of it into one place and turns it into interactive visualizations you can
> actually explore. Let's start from a completely blank install.

---

## 2. First load & setup wizard (~55–60s)

**[cue: fresh browser load, `#setupDialog` auto-opens — this is the real first-run behavior:
the app checks its saved settings on boot and opens the wizard automatically when none exist]**

> The first time you open Binocular, it notices there's no configuration yet and walks you
> through a five-step setup wizard right in the browser.

**[cue: wizard step 1 — "Start"]**

> Step one is just a welcome screen — it explains that Binocular needs a data source before it
> can show you anything.

**[cue: click Next → wizard step 2 — "Database"]**

> Step two connects that data source. In a real deployment this is usually Binocular's own
> backend, indexing your repository into a database — but Binocular also ships data-source
> plugins that need no backend at all. For this walkthrough we'll connect the built-in Mock
> Data plugin so everything you see is running live, entirely in the browser.

**[cue: click "Add" on the Mock Data card]**

> One click, and Binocular has a dataset to work with.

**[cue: click Next → wizard step 3 — "Authors"]**

> Step three reviews the authors it found in the repository. This is where you'd merge
> duplicate identities — the same person committing under two email addresses — before they
> show up as two separate contributors everywhere else in the tool. It's optional, and you can
> always come back to it later from Settings.

**[cue: click Next → wizard step 4 — "Dashboard"]**

> Step four offers a couple of recommended starting dashboards, or you can skip straight to a
> blank one.

**[cue: click "Select" on a recommended dashboard]**

> Picking one pre-populates your workspace instead of starting from nothing.

**[cue: click Next → wizard step 5 — "Summary"]**

> And step five is just a summary of what you configured, with a chance to go back and change
> anything before committing to it.

**[cue: click Save — page reloads, `#tabBarTop` becomes visible]**

> Hit Save, Binocular reloads once, and you land on your actual workspace.

---

## 3. Tabs tour (~50–55s)

**[on screen: fast-cut, functional pass — click into each tab, let it visibly respond, move on.
This is deliberately shallow: settings/filters/buttons for each visualization get their own
dedicated module later. Tab set and order below is exactly what's declared in `App.tsx`: top bar
= Parameters, Visualizations, Sprints, Layouts; right bar = Authors, File Tree, Help. Note:
Parameters (top) and Authors (right) are each the *first-declared* tab on their side, so they're
already open by default the moment the dashboard appears — don't click their tab handles to
"open" them, that would toggle them closed instead. Clicking Visualizations/Sprints/Layouts (top)
or File Tree/Help (right) opens each in turn and auto-closes whichever tab was open on that same
side.]**

**[on screen: Parameters is already open — no tab click needed]**

> Around the dashboard, a ring of tabs handles everything that isn't a chart itself. Parameters
> holds the global date range and granularity — year, month, week, or day — plus a toggle to
> exclude merge commits. Every compatible visualization inherits these unless it's overridden
> per chart.

**[cue: click the "+M" quick button — date shifts forward a month]**

> A quick one-click nudge, and every chart using these parameters refetches.

**[cue: click `#tab_Visualizations`]**

> Visualizations is where you add new charts to the dashboard — click one, and it places
> itself automatically.

**[cue: click a recommended plugin button — new dashboard item appears]**

> No dragging required, though dragging works too.

**[cue: click `#tab_Sprints`]**

> Sprints lets you define time boxes that get overlaid onto supporting charts.

**[cue: click "Add Sprint", type a name in the "Name:" field, click "Add"]**

> Name it, and it's immediately available as an overlay.

**[cue: click `#tab_Layouts`]**

> Layouts holds recommended dashboard presets, and lets you save your current arrangement as
> your own reusable layout.

**[cue: click a different recommended layout card → confirmation dialog → "Yes"]**

> Swapping the whole dashboard is one click and one confirmation.

**[on screen: Authors (right bar) is already open by default — no tab click needed]**

> On the other side, Authors lists every contributor Binocular found. Unchecking someone here
> drops them from every chart that supports author filtering — and dragging one author onto
> another merges their identities.

**[cue: uncheck one author's checkbox — a chart visibly loses a series — then re-check it]**

> It's a live filter, not just a list.

**[cue: click `#tab_File Tree`]**

> File Tree mirrors the repository's folder structure, with the same idea — check or uncheck
> files and folders to control what feeds the charts.

**[cue: type into the Search field — tree filters in real time]**

> Search narrows it instantly.

**[cue: click `#tab_Help`]**

> And Help is built into the tool itself — general concepts up top, plus a dedicated help page
> for every single visualization, reachable right from here.

**[cue: click into one plugin's help entry, then the back button]**

> No separate documentation site required.

---

## 4. Bridge line (~10–12s)

*(Generic on purpose — this line should work whether zero, one, or all six visualization modules
follow it.)*

> That's the shell everything lives in. What actually goes on those dashboards — and how much
> each visualization can tell you, and where it can mislead you — is where things get
> interesting.

---

## 5. Wrap-up (~20–25s)

*(Generic on purpose — no reference to any specific visualization.)*

> Binocular is open source, runs against your own git history and issue tracker, and every
> chart you've seen came from data it indexed itself — nothing here is a mockup. If any of this
> looked useful for understanding your own project, the link to get started is below.

---

**[END CORE — total narration ≈ 2:25–2:30 depending on pacing pauses]**
