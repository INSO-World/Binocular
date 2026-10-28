// Registry of every visualization plugin plus the readiness signal and preset settings needed to capture it.
// Shared by the screenshots suite and the demo-video suite; `filename` is only consumed by the former.

// waitFor/waitForText wait for a selector/text to appear, waitForHidden waits for text to disappear (e.g. a loading indicator); omit all three if synchronous.
export const VISUALIZATIONS: Array<{
  pluginName: string;
  filename: string;
  waitFor?: string;
  waitForText?: string;
  waitForHidden?: string;
  navigateTo?: string[];
  settings?: object;
}> = [
  {
    pluginName: 'Changes',
    filename: 'Changes.png',
    waitFor: 'svg g.areas path',
    settings: { visualizationStyle: 'stepped', splitAdditionsDeletions: false, showSprints: false },
  },
  { pluginName: 'Sum Commits', filename: 'SumCommits.png', waitFor: 'svg g rect' },
  {
    pluginName: 'File Changes',
    filename: 'File Changes.png',
    waitFor: 'svg g path',
    settings: {
      file: 'frontend/src/app/app-routing.module.ts',
      splitAdditionsDeletions: true,
      visualizationStyle: 'curved',
      showSprints: false,
      showExtraMetrics: false,
    },
  },
  { pluginName: 'Commit By File', filename: 'CommitByFile.png', waitForHidden: 'No Data' },
  { pluginName: 'Builds', filename: 'Builds.png', waitFor: 'svg g path' },
  {
    pluginName: 'Issues',
    filename: 'Issues.png',
    waitFor: 'svg g path',
    settings: { splitIssuesPerAuthor: true, breakdown: true, visualizationStyle: 'curved', showSprints: false },
  },
  {
    pluginName: 'Merge Requests',
    filename: 'MergeRequests.png',
    waitFor: 'svg g path',
    settings: { splitMergeRequestsPerAuthor: true, breakdown: true, visualizationStyle: 'curved', showSprints: false },
  },
  {
    pluginName: 'Issues Timeline',
    filename: 'IssuesTimeline.png',
    waitFor: 'svg rect',
    settings: { coloringMode: 'assignee', showSprints: false },
  },
  { pluginName: 'Burndown', filename: 'Burndown.png', waitFor: 'svg g path' },
  {
    pluginName: 'Time Spent',
    filename: 'TimeSpent.png',
    waitFor: 'svg g path',
    settings: { breakdown: true, visualizationStyle: 'linear', splitTimePerIssue: false, splitSpentRemoved: false, showSprints: false },
  },
  { pluginName: 'Collaboration', filename: 'Collaboration.png', waitForHidden: 'Simulating graph layout...' },
  { pluginName: 'Repository Activity', filename: 'RepositoryActivity.png', waitFor: 'svg rect' },
  { pluginName: 'Repository Stats', filename: 'RepositoryStats.png', waitForText: 'Contributors' },
  { pluginName: 'Code Ownership', filename: 'CodeOwnership.png', waitFor: 'svg g path' },
  { pluginName: 'Code Expertise', filename: 'CodeExpertise.png', waitFor: 'svg text' },
  { pluginName: 'Knowledge Radar', filename: 'KnowledgeRadar.png', waitFor: 'svg text' },
  {
    pluginName: 'Change Frequency',
    filename: 'ChangeFrequency.png',
    waitFor: 'svg circle',
    navigateTo: ['frontend', 'src', 'app', 'components'],
  },
];
