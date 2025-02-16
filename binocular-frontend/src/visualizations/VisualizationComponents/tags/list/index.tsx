import moment from 'moment/moment';
import { Commit } from '../../../../types/commitTypes.ts';
import { Author, Committer } from '../../../../types/authorTypes.ts';
import { GlobalState } from '../../../../types/globalTypes.ts';
import { connect } from 'react-redux';
import List from './list.tsx';

export interface Props {
  id: number;
  chartResolution: moment.unitOfTime.DurationConstructor;
  commits: Commit[];
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
  selectedAuthors: string[];
  size: string;
  universalSettings: boolean;
  totalCount: number;
  isFetching: boolean;
}

const mapStateToProps = (state: GlobalState): Props => {
  const tagsState = state.visualizations.tags.state;
  const data = tagsState.data;
  const universalSettings = state.universalSettings;
  return {
    otherCount: data.otherCount,
    commits: data.commits || [],
    committers: data.committers,
    firstCommitTimestamp: data.firstCommitTimestamp,
    lastCommitTimestamp: data.lastCommitTimestamp,
    firstSignificantTimestamp: data.firstSignificantTimestamp,
    lastSignificantTimestamp: data.lastSignificantTimestamp,
    displayMetric: tagsState.config.displayMetric,
    selectedAuthors: universalSettings.selectedAuthorsGlobal,
    otherAuthors: universalSettings.otherAuthors,
    mergedAuthors: universalSettings.mergedAuthors,
    chartResolution: universalSettings.chartResolution,
    excludeMergeCommits: universalSettings.excludeMergeCommits,
    excludedCommits: universalSettings.excludedCommits,
    excludeCommits: universalSettings.excludeCommits,
    totalCount: data.totalCount || 0,
    isFetching: data.isFetching || false,
  } as Props;
};

const mapDispatchToProps = () => {
  return {};
};

export default connect(mapStateToProps, mapDispatchToProps)(List);
