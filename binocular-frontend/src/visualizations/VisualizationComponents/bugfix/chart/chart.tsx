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
  commitersFromGlobalSettings: any;
  regexConfig: any;
}

interface CommitChartData {
  date: number;

  [signature: string]: number;
}

// TODO: Some caching strategy??? and also fetch new things without the things in cache
// @ts-ignore
export default (props: Props) => {
  console.log('Regex config in chart', props.regexConfig);
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

  let preparedCommits: Commit[] = [];
  if (!props.commits || props.commits.length === 0) {
    preparedCommits = [];
  } else {
    // Sort commits
    preparedCommits = props.commits.sort((a, b) => new Date(a.date) - new Date(b.date));
    // Filter based on rules in regexConfig
    const tempCommits: Commit[] = [];
    const regexCommitMessage = new RegExp('\\b' + props.regexConfig.commitMessage + '\\b', 'i');
    const regexIssueTitle = new RegExp('\\b' + props.regexConfig.issueTitle + '\\b', 'i');
    const regexIssueLabel = new RegExp('\\b' + props.regexConfig.issueLabelName + '\\b', 'i');
    for (const commit of preparedCommits) {
      if (regexCommitMessage.test(commit.message)) {
        tempCommits.push(commit);
        continue;
      }
      if (commit.issues) {
        for (const issue of commit.issues) {
          if (regexIssueTitle.test(issue.title)) {
            tempCommits.push(commit);
            break;
          }
          if (issue.labels) {
            for (const label of issue.labels) {
              if (regexIssueLabel.test(label.name)) {
                tempCommits.push(commit);
                break;
              }
            }
          }
        }
      }
    }
    console.log('Filtered commits', tempCommits);
    preparedCommits = tempCommits;
  }

  const testDataForChart = prepareTestData(preparedCommits);
  const commitersTestData = prepareTestDataCommiters(preparedCommits, props);

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

const prepareTestData = (commitsSorted: Commit[]) => {
  if (!commitsSorted || commitsSorted.length === 0) {
    return [];
  }

  // Step one: Prepare data used for bars
  // Structure [{ year: 2019, month: 11, day: 20, bugfixes_count: 5 }, ...] each year,month,day combo is unique and everything is sorted ...
  const temp: any = {};
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

const prepareTestDataCommiters = (commitsSorted: Commit[], props: Props) => {
  if (!commitsSorted || commitsSorted.length === 0) {
    return [];
  }

  // Step one: Prepare data used for bars
  // Structure [{ year: 2019, month: 11, day: 20, bugfixes_count: 5 }, ...] each year,month,day combo is unique and everything is sorted ...
  const temp: any = {};
  for (const commit of commitsSorted) {
    if (`${commit['signature'].substring(0, commit['signature'].indexOf('<'))}` in temp) {
      temp[`${commit['signature'].substring(0, commit['signature'].indexOf('<'))}`]['count'] += 1;
      temp[`${commit['signature'].substring(0, commit['signature'].indexOf('<'))}`]['commits'].push(commit);
    } else {
      temp[`${commit['signature'].substring(0, commit['signature'].indexOf('<'))}`] = { count: 1, commits: [commit] };
    }
  }
  // Also use palette
  const paletteNew = props.commitersFromGlobalSettings !== undefined ? props.commitersFromGlobalSettings : props.palette;
  for (const key of Object.keys(paletteNew)) {
    if (key !== 'other' && key !== 'others') {
      // Check if the author is even in the temp
      if (key.substring(0, key.indexOf('<')) in temp) {
        temp[`${key.substring(0, key.indexOf('<'))}`]['color'] = paletteNew[key];
      }
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
