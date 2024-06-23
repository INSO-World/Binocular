'use strict';

import { connect } from 'react-redux';
import { GlobalState } from '../../../types/globalTypes.ts';
import { setThreshold, setSelectedCommitType, setSelectedBranch } from './sagas';
import { Palette } from '../../../types/authorTypes.ts';
import styles from './styles.module.scss';
import * as React from 'react';

const mapStateToProps = (state: GlobalState) => {
  const dashboardState = state.visualizations.commitTimeTracking.state;

  return {
    committers: dashboardState.data.data.committers,
    resolution: dashboardState.config.chartResolution,
    palette: dashboardState.data.data.palette,
    selectedBranch: dashboardState.config.selectedBranch,
    branches: dashboardState.data.data.branches,
    commitType: dashboardState.config.commitType,
    threshold: dashboardState.config.threshold,
    selectedAuthors: dashboardState.config.selectedAuthors,
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    onChangeThreshold: (threshold: {value: number, threshold: string}) => dispatch(setThreshold(threshold)),
    onChangeBranch: (branch: string) => dispatch(setSelectedBranch(branch)),
    onChangeCommitType: (commitType: string) => dispatch(setSelectedCommitType(commitType)),
  };
};

interface Props {
  committers: string[];
  selectedBranch: string;
  branches: string[];
  commitType: string;
  threshold: {
    hours: { lower: number; upper: number };
    change: { lower: number; upper: number };
    ratio: { lower: number; upper: number };
  };
  palette: Palette;
  resolution: string;
  selectedAuthors: string[];
  onChangeThreshold: (threshold: {value: number, threshold: string}) => void;
  onChangeBranch: (branchName: string) => void;
  onChangeCommitType: (commitType: string) => void;
}

const CommitTimeTrackingConfigComponent = (props: Props) => {
  return (
    <div className={styles.configContainer}>
      <div className={styles.field}>
        <label className="label">Branch</label>
        <div style={{ marginBottom: '0.5em' }}>
          <div className="select">
            <select value={props.selectedBranch} onChange={(e) => props.onChangeBranch(e.target.value)}>
              {props.branches
                ? props.branches.map((b) => (
                    <option value={b} key={b}>
                      {b}
                    </option>
                  ))
                : 'Nothing'}
            </select>
          </div>
        </div>
        <label className="label">Threshold</label>
        <div style={{ marginBottom: '0.5em' }}>
          <label className="label">Time spent</label>
          <label className="label">Lower bound</label>
          <input
            type="number"
            value={props.threshold.hours.lower}
            onChange={(e) => props.onChangeThreshold({ value: +e.target.value, threshold: 'hours-lower'})}
          />
          <label className="label">Upper bound</label>
          <input
            type="number"
            value={props.threshold.hours.upper}
            onChange={(e) => props.onChangeThreshold({ value: +e.target.value, threshold: 'hours-upper'})}
          />
          <label className="label">Lines changed</label>
          <label className="label">Lower bound</label>
          <input
            type="number"
            value={props.threshold.change.lower}
            onChange={(e) => props.onChangeThreshold({ value: + e.target.value, threshold: 'change-lower'})}
          />
          <label className="label">Upper bound</label>
          <input
            type="number"
            value={props.threshold.change.upper}
            onChange={(e) => props.onChangeThreshold({ value: + e.target.value, threshold: 'change-upper'})}
          />
          <label className="label">Ratio</label>
          <label className="label">Lower bound</label>
          <input
            type="number"
            value={props.threshold.ratio.lower}
            onChange={(e) => props.onChangeThreshold({ value: + e.target.value, threshold: 'ratio-lower'})}
          />
          <label className="label">Upper bound</label>
          <input
            type="number"
            value={props.threshold.ratio.upper}
            onChange={(e) => props.onChangeThreshold({ value: + e.target.value, threshold: 'ratio-upper'})}
          />
        </div>

        <label className="label">Commit type</label>
        <div style={{ marginBottom: '0.5em' }}>
          <div className="select">
            <select value={props.commitType} onChange={(e) => props.onChangeCommitType(e.target.value)}>
              <option value="all">All</option>
              <option value="corrective">Corrective</option>
              <option value="features">Features</option>
              <option value="perfective">Perfective</option>
              <option value="nonfunctional">Non-Functional</option>
              <option value="unknown">Unknown</option>
            </select>
          </div>
        </div>
      </div>
    </div>
  );
};

const DashboardConfig = connect(mapStateToProps, mapDispatchToProps)(CommitTimeTrackingConfigComponent);

export default DashboardConfig;
