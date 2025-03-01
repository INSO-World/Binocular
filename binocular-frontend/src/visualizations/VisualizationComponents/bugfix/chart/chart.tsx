import * as React from 'react';

import styles from '../styles.module.scss';

import { Author, Committer, Palette } from '../../../../types/authorTypes';
import { Commit } from '../../../../types/commitTypes';
import ZoomableVerticalBarchart from '../../../../components/ZoomableVerticalBarchart';
import CommitChangeDisplay from '../../../../components/CommitChangeDisplay';
import { useState } from 'react';
import ZoomableVerticalBarchartCommiters from '../../../../components/ZoomableVerticalBarchartCommiters';

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

interface BugfixesPerDate {
  signature: string;
  bugfixes_count: number;
  commits: Commit[];
}

interface BugfixesPerAuthor {
  date: Date;
  bugfixes_count: number;
  commits: Commit[];
  color: string;
}

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

  // TODO: Merge authors ????
  // TODO: Update without switch to other graph
  // TODO: Files
  // TODO: Delete commits

  let preparedCommits: Commit[] = [];
  if (!props.filteredCommits || props.filteredCommits.length === 0) {
    preparedCommits = [];
  } else {
    // Sort commits
    // @ts-ignore
    preparedCommits = props.filteredCommits.sort((a, b) => new Date(a.date) - new Date(b.date));
    let tempCommits: Commit[] = [];
    // Filter out commits based on unselected authors
    // TODO: Assert commiters and selected authors are arrays
    const excludedAuthors = props.committers.filter((s) => !props.selectedAuthors.includes(s));
    console.log('Excluded authors', excludedAuthors);
    for (const commit of preparedCommits) {
      if (!excludedAuthors.includes(commit.signature)) {
        tempCommits.push(commit);
      }
    }
    preparedCommits = tempCommits;
    tempCommits = [];

    // Filter out commits not in the selected branch
    if (props.selectedBranch !== undefined) {
      for (const commit of preparedCommits) {
        if (commit.branch === props.selectedBranch) {
          tempCommits.push(commit);
        }
      }
      preparedCommits = tempCommits;
      tempCommits = [];
    }

    // Filter based on rules in regexConfig
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

  // Prepare the commits for visualisation
  const dataPerDay: BugfixesPerDate[] = prepareByDateCommits(preparedCommits);
  const dataPerCommiter: BugfixesPerAuthor[] = prepareByCommiterCommits(preparedCommits, props);
  const usedData: BugfixesPerAuthor[] | BugfixesPerDate[] = props.graphSwitch ? dataPerDay : dataPerCommiter;

  const commitChart = (
    <div className={styles.chartLine}>
      <div className={styles.chart}>
        {dataPerDay && dataPerDay.length > 0 ? (
          <ZoomableVerticalBarchart content={dataPerDay} changeCommit={changeCurrentCommit} />
        ) : (
          <div className={styles.errorMessage}>No data during this time period!</div>
        )}
      </div>
    </div>
  );

  const commitChartPerAuthor = (
    <div className={styles.chartLine}>
      <div className={styles.chart}>
        {dataPerCommiter && dataPerCommiter.length > 0 ? (
          <ZoomableVerticalBarchartCommiters content={dataPerCommiter} changeCommit={changeCurrentCommit} />
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
      {usedData === null && loadingHint}
      {props.graphSwitch ? commitChart : commitChartPerAuthor}
      {commitViewer}
    </div>
  );
};

const prepareByDateCommits = (commitsSorted: Commit[]): BugfixesPerDate[] => {
  if (!commitsSorted || commitsSorted.length === 0) {
    return [];
  }

  // Step one: Count number of commits per day and save those commits for step two
  const temp: any = {};
  for (const commit of commitsSorted) {
    // Count all commits in that day and add the commit data to the right date
    const date = new Date(commit.date); // Converting the string into Date object
    if (`${date.getFullYear()}-${date.getMonth() + 1}-${date.getUTCDate()}` in temp) {
      temp[`${date.getFullYear()}-${date.getMonth() + 1}-${date.getUTCDate()}`]['count'] += 1;
      temp[`${date.getFullYear()}-${date.getMonth() + 1}-${date.getUTCDate()}`]['commits'].push(commit);
    } else {
      temp[`${date.getFullYear()}-${date.getMonth() + 1}-${date.getUTCDate()}`] = { count: 1, commits: [commit] };
    }
  }

  console.log('temp in prepareByDateCommits', temp);

  const out: any[] = [];

  // Step two: prepare the BugfixesPerDate[] data structure
  for (const k of Object.keys(temp)) {
    const date = new Date(k);
    out.push({
      date: new Date(`${date.getFullYear()}-${date.getMonth() + 1}-${date.getUTCDate()}`),
      bugfixes_count: temp[k]['count'],
      commits: temp[k]['commits'],
    });
  }

  console.log('Preprocessed commits', out);

  return out;
};

const prepareByCommiterCommits = (commitsSorted: Commit[], props: Props): BugfixesPerAuthor[] => {
  if (!commitsSorted || commitsSorted.length === 0) {
    return [];
  }

  // Step one: Count number of commits per author and save those commits for step three
  const temp: any = {};
  for (const commit of commitsSorted) {
    if (`${commit['signature'].substring(0, commit['signature'].indexOf('<'))}` in temp) {
      temp[`${commit['signature'].substring(0, commit['signature'].indexOf('<'))}`]['count'] += 1;
      temp[`${commit['signature'].substring(0, commit['signature'].indexOf('<'))}`]['commits'].push(commit);
    } else {
      temp[`${commit['signature'].substring(0, commit['signature'].indexOf('<'))}`] = { count: 1, commits: [commit] };
    }
  }

  // Step two: Save the right color for the author
  const paletteNew = props.commitersFromGlobalSettings !== undefined ? props.commitersFromGlobalSettings : props.palette;
  for (const key of Object.keys(paletteNew)) {
    if (key !== 'other' && key !== 'others') {
      // Check if the author is even in the temp
      if (key.substring(0, key.indexOf('<')) in temp) {
        temp[`${key.substring(0, key.indexOf('<'))}`]['color'] = paletteNew[key];
      }
    }
  }

  console.log('temp in prepareByCommiterCommits', temp);

  const out: any[] = [];

  // Step three: Prepare the BugfixesPerAuthor[] data structure
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
