import * as React from 'react';
import * as d3 from 'd3';

import styles from '../styles.module.scss';

import moment from 'moment';
import { Author, Committer, Palette } from '../../../../types/authorTypes';
import { Commit } from '../../../../types/commitTypes';
import ZoomableVerticalBarchart from '../../../../components/ZoomableVerticalBarchart';

interface Props {
  id: number;
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
  size: string;
  universalSettings: boolean;
}

interface CommitChartData {
  date: number;

  [signature: string]: number;
}

// TODO: Some caching strategy??? and also fetch new things without the things in cache 

export default (props: Props) => {
  // TODO: You must wait until commits are loaded in
  const testDataForChart = prepareTestData(props);

  const commitChart = (
    <div className={styles.chartLine}>
      <div className={styles.chart}>
        {testDataForChart !== undefined && testDataForChart.length > 0 ? (
          <ZoomableVerticalBarchart content={testDataForChart} />
        ) : (
          <div className={styles.errorMessage}>No data during this time period!</div>
        )}
      </div>
    </div>
  );
  const loadingHint = (
    <div className={styles.loadingHintContainer}>
      <h1 className={styles.loadingHint}>
        Loading... <i className="fas fa-spinner fa-pulse" />
      </h1>
    </div>
  );
  return (
    <div className={styles.chartContainer}>
      {testDataForChart === null && loadingHint}
      {testDataForChart && commitChart}
    </div>
  );
};

// TODO: Fetch some testdata (e.g. all commit data)

// TODO: Function that prepares the data for zoomable vertical barchart (dont forget for tooltip data (best sorted by day) ... then I can
//  show in year bar view all bugfixes sorted by day (or at least some of them) and in day view all (really all, not just few))

// TODO: Function that prepares the data for the sortable barchart with users as x axis

const prepareTestData = (props: Props) => {
  if (!props.commits || props.commits.length === 0) {
    return [];
  }
  console.log('commits', props.commits);
  // Prepares the data that is similiar in structure as the real data with only bugfixes

  // Step one: Prepare data used for bars
  // Structure [{ year: 2019, month: 11, day: 20, bugfixes_count: 5 }, ...] each year,month,day combo is unique and everything is sorted ...
  const temp: any = {};
  const commitsSorted = props.commits.sort((a, b) => new Date(a.date) - new Date(b.date));
  for (const commit of commitsSorted) {
    // Count all commits in that day and add the commit data to the right date
    const date = new Date(commit.date); // converting the string into Date object
    if (`${date.getFullYear()}-${date.getMonth() + 1}-${date.getUTCDate()}` in temp) {
      temp[`${date.getFullYear()}-${date.getMonth() + 1}-${date.getUTCDate()}`]['count'] += 1;
      temp[`${date.getFullYear()}-${date.getMonth() + 1}-${date.getUTCDate()}`]['commits'].push(commit);
    } else {
      temp[`${date.getFullYear()}-${date.getMonth() + 1}-${date.getUTCDate()}`] = { count: 1, commits: [commit] };
    }
  }

  console.log('temp', temp);

  const out: any[] = [];

  for (const k of Object.keys(temp)) {
    const date = new Date(k);
    out.push({
      date: new Date(`${date.getFullYear()}-${date.getMonth() + 1}-${date.getUTCDate()}`),
      year: date.getFullYear(),
      month: date.getMonth(),
      day: date.getUTCDate(),
      bugfixes_count: temp[k]['count'],
      commits: temp[k]['commits'],
    });
  }

  console.log('Preprocessed commits', out);

  // TODO: Prepare tooltip test data
  return out;
};
