'use strict';

import { connect } from 'react-redux';
import Chart from './chart';
import { GlobalState } from '../../../../types/globalTypes';
import moment from 'moment/moment';
import { Commit } from '../../../../types/commitTypes';
import { Author, Committer, Palette } from '../../../../types/authorTypes';

interface Props {
  chartResolution: moment.unitOfTime.DurationConstructor;
  commits: Commit[];
  filteredCommits: Commit[];
  committers: string[];
  displayMetric: string;
  excludeMergeCommits: boolean;
  excludedCommits: string[];
  excludeCommits: boolean;
  firstCommitTimestamp: number;
  lastCommitTimestamp: number;
  firstSignificantTimestamp: number;
  lastSignificantTimestamp: number;
  mergedAuthors: Author[];
  otherAuthors: Committer[];
  otherCount: number;
  palette: Palette;
  selectedAuthors: string[];
  graphSwitch: boolean;
}
const mapStateToProps = (state: GlobalState): Props => {
  const bugfixState = state.visualizations.bugfix.state;
  const universalSettings = state.universalSettings;
  return {
    palette: bugfixState.data.data.palette,
    otherCount: bugfixState.data.data.otherCount,
    filteredCommits: bugfixState.data.data.filteredCommits,
    commits: bugfixState.data.data.commits,
    committers: bugfixState.data.data.committers,
    firstCommitTimestamp: bugfixState.data.data.firstCommitTimestamp,
    lastCommitTimestamp: bugfixState.data.data.lastCommitTimestamp,
    firstSignificantTimestamp: bugfixState.data.data.firstSignificantTimestamp,
    lastSignificantTimestamp: bugfixState.data.data.lastSignificantTimestamp,
    displayMetric: bugfixState.config.displayMetric,
    selectedAuthors: universalSettings.selectedAuthorsGlobal,
    otherAuthors: universalSettings.otherAuthors,
    mergedAuthors: universalSettings.mergedAuthors,
    chartResolution: universalSettings.chartResolution,
    excludeMergeCommits: universalSettings.excludeMergeCommits,
    excludedCommits: universalSettings.excludedCommits,
    excludeCommits: universalSettings.excludeCommits,
    graphSwitch: bugfixState.config.graphSwitch,
  };
};

const mapDispatchToProps = () => {
  return {};
};

export default connect(mapStateToProps, mapDispatchToProps)(Chart);
