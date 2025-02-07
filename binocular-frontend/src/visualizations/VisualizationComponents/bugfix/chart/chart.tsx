import * as React from 'react';
import * as d3 from 'd3';

import styles from '../styles.module.scss';

import moment from 'moment';
import { Author, Committer, Palette } from '../../../../types/authorTypes';
import { Commit } from '../../../../types/commitTypes';
import ZoomableVerticalBarchart from '../../../../components/ZoomableVerticalBarchart';
import CommitChangeDisplay from '../../../../components/CommitChangeDisplay';
import { useState } from 'react';
import ZoomableVerticalBarchartCommiters from '../../../../components/ZoomableVerticalBarchartCommiters';

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
  graphSwitch: boolean;
}

interface CommitChartData {
  date: number;

  [signature: string]: number;
}

// TODO: Some caching strategy??? and also fetch new things without the things in cache
// @ts-ignore
export default (props: Props) => {
  console.log('Props in bugfix chart', props);
  const [stateCommit, setStateCommit] = useState({});
  const changeCurrentCommit = (commitSha) => {
    console.log('Clicked on sha', commitSha);
    // Find the commit based on the commitSha
    for (const c of props.commits) {
      if (c['sha'] === commitSha) {
        console.log('Found the clicked commit', c);
        setStateCommit(c);
        break;
      }
    }
  };

  const testDataForChart = prepareTestData(props);

  const commitersTestData = prepareTestDataCommiters(props);

  const commitChart = (
    <div className={styles.chartLine}>
      <div className={styles.chart}>
        {testDataForChart !== undefined && testDataForChart.length > 0 ? (
          <ZoomableVerticalBarchart content={testDataForChart} changeCommit={changeCurrentCommit} />
        ) : (
          <div className={styles.errorMessage}>No data during this time period!</div>
        )}
      </div>
    </div>
  );

  const commitChartPerAuthor = (
    <div className={styles.chartLine}>
      <div className={styles.chart}>
        {testDataForChart !== undefined && testDataForChart.length > 0 ? (
          <ZoomableVerticalBarchartCommiters content={commitersTestData} changeCommit={changeCurrentCommit} />
        ) : (
          <div className={styles.errorMessage}>No data during this time period!</div>
        )}
      </div>
    </div>
  );

  const commitViewer =
    Object.keys(stateCommit).length !== 0 ? (
      <CommitChangeDisplay commit={stateCommit} />
    ) : (
      <div className={styles.errorMessage}>No code view available, click on commit hash in tooltip!</div>
    ); // TODO: Some warning when no content was chosen or maybe even no viewer
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
      {testDataForChart && (props.graphSwitch ? commitChart : commitChartPerAuthor)}
      {commitViewer}
    </div>
  );
};

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

  return out;
};

const prepareTestDataCommiters = (props: Props) => {
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
    if (`${commit['signature'].substring(0, commit['signature'].indexOf('<'))}` in temp) {
      temp[`${commit['signature'].substring(0, commit['signature'].indexOf('<'))}`]['count'] += 1;
      temp[`${commit['signature'].substring(0, commit['signature'].indexOf('<'))}`]['commits'].push(commit);
    } else {
      temp[`${commit['signature'].substring(0, commit['signature'].indexOf('<'))}`] = { count: 1, commits: [commit] };
    }
  }
  // Also use palette
  for (const key of Object.keys(props.palette)) {
    if (key !== 'others') {
      temp[`${key.substring(0, key.indexOf('<'))}`]['color'] = props.palette[key];
    }
  }

  console.log('temp', temp);

  const out: any[] = [];

  for (const k of Object.keys(temp)) {
    out.push({
      signature: k,
      bugfixes_count: temp[k]['count'],
      commits: temp[k]['commits'],
      color: temp[k]['color'],
    });
  }

  console.log('Preprocessed commits for authors', out);

  return out;
};
