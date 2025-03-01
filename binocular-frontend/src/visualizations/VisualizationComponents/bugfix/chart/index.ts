'use strict';

import { connect } from 'react-redux';
import Chart from './chart';
import { GlobalState } from '../../../../types/globalTypes';
import { Commit } from '../../../../types/commitTypes';
import { Author, Committer, Palette } from '../../../../types/authorTypes';

interface Props {
  commits: Commit[];
  filteredCommits: Commit[]; // Commits in interval [firstSignificantTimestamp, lastSignificantTimestamp]
  committers: string[];
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
  selectedBranch: string | undefined;
  palette: Palette;
  selectedAuthors: string[];
  graphSwitch: boolean;
  commitersFromGlobalSettings: any;
  regexConfig: any;
}

const mapStateToProps = (state: GlobalState): Props => {
  const bugfixState = state.visualizations.bugfix.state;
  const universalSettings = state.universalSettings;
  return {
    palette: bugfixState.data.data.palette,
    otherCount: bugfixState.data.data.otherCount,
    selectedBranch: bugfixState.config.currentBranch?.branch,
    filteredCommits: bugfixState.data.data.filteredCommits,
    commits: bugfixState.data.data.commits,
    committers: bugfixState.data.data.committers,
    commitersFromGlobalSettings: universalSettings.allAuthors,
    firstCommitTimestamp: bugfixState.data.data.firstCommitTimestamp,
    lastCommitTimestamp: bugfixState.data.data.lastCommitTimestamp,
    firstSignificantTimestamp: bugfixState.data.data.firstSignificantTimestamp,
    lastSignificantTimestamp: bugfixState.data.data.lastSignificantTimestamp,
    selectedAuthors: universalSettings.selectedAuthorsGlobal,
    otherAuthors: universalSettings.otherAuthors,
    mergedAuthors: universalSettings.mergedAuthors,
    excludeMergeCommits: universalSettings.excludeMergeCommits,
    excludedCommits: universalSettings.excludedCommits,
    excludeCommits: universalSettings.excludeCommits,
    graphSwitch: bugfixState.config.graphSwitch,
    regexConfig: bugfixState.config.regexConfig,
  };
};

const mapDispatchToProps = () => {
  return {};
};

export default connect(mapStateToProps, mapDispatchToProps)(Chart);
