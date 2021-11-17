'use strict';

import { connect } from 'react-redux';
import {
  setResolution,
  setShowIssues,
  setDisplayMetric,
  setSelectedAuthors,
  setShowCIChart,
  setShowIssueChart,
  setShowChangesChart
} from './sagas';
import TabCombo from '../../components/TabCombo.js';
import styles from './styles.scss';

import LegendCompact from '../../components/LegendCompact';
import CheckboxLegend from '../../components/CheckboxLegend';

const mapStateToProps = (state /*, ownProps*/) => {
  const projectIssueState = state.visualizations.projectIssue.state;

  return {
    committers: projectIssueState.data.data.committers,
    resolution: projectIssueState.config.chartResolution,
    showIssues: projectIssueState.config.showIssues,
    palette: projectIssueState.data.data.issues,
    metric: projectIssueState.config.displayMetric,
    selectedAuthors: projectIssueState.config.selectedAuthors,
    showCIChart: projectIssueState.config.showCIChart,
    showIssueChart: projectIssueState.config.showIssueChart,
    showChangesChart: projectIssueState.config.showChangesChart
  };
};

const mapDispatchToProps = (dispatch /*, ownProps*/) => {
  return {
    onClickResolution: resolution => dispatch(setResolution(resolution)),
    onClickIssues: showIssues => dispatch(setShowIssues(showIssues)),
    onClickMetric: metric => dispatch(setDisplayMetric(metric)),
    onClickCheckboxLegend: selected => dispatch(setSelectedAuthors(selected)),
    onClickShowCIChart: showCIChart => dispatch(setShowCIChart(showCIChart)),
    onClickShowIssueChart: showIssueChart => dispatch(setShowIssueChart(showIssueChart)),
    onClickShowChangesChart: showChangesChart => dispatch(setShowChangesChart(showChangesChart))
  };
};

const ProjectIssueConfigComponent = props => {
  let otherIssues;
  if (props.palette && 'others' in props.palette) {
    otherIssues = props.committers.length - (Object.keys(props.palette).length - 1);
  }

  return (
    <div className={styles.configContainer}>
      <form className={styles.form}>
        <div className={styles.field}>
          <div className="control">
            <label className="label">General Chart Settings</label>
            <TabCombo
              value={props.resolution}
              options={[
                { label: 'Years', icon: 'calendar-plus', value: 'years' },
                { label: 'Months', icon: 'calendar', value: 'months' },
                { label: 'Weeks', icon: 'calendar-week', value: 'weeks' }
              ]}
              onChange={value => props.onClickResolution(value)}
            />
            <div>
              <label className={styles.checkboxLabel}>
                <input
                  name="showCI"
                  type="checkbox"
                  onChange={() => props.onClickShowCIChart(!props.showCIChart)}
                  checked={props.showCIChart}
                />{' '}
                Show standard Chart{' '}
              </label>
            </div>
            <div>
              <label className={styles.checkboxLabel}>
                <input
                  name="showIssues"
                  type="checkbox"
                  onChange={() => props.onClickShowIssueChart(!props.showIssueChart)}
                  checked={props.showIssueChart}
                />{' '}
                Show normalized Chart{' '}
              </label>
            </div>
            <div>
              <label className={styles.checkboxLabel}>
                <input
                  name="showChanges"
                  type="checkbox"
                  onChange={() => props.onClickShowChangesChart(!props.showChangesChart)}
                  checked={props.showChangesChart}
                />{' '}
                Show milestone Chart{' '}
              </label>
            </div>
          </div>
        </div>
        <div className={styles.field}>
          <div className="control">
            <label className="label">Issues</label>
            <LegendCompact text="Opened | Closed" color="#3461eb" color2="#8099e8" />
            <TabCombo
              value={props.showIssues}
              options={[
                { label: 'All', icon: 'database', value: 'all' },
                { label: 'Open', icon: 'folder-open', value: 'open' },
                { label: 'Closed', icon: 'folder', value: 'closed' }
              ]}
              onChange={value => props.onClickIssues(value)}
            />
          </div>
        </div>
        <div className={styles.field}>
          <CheckboxLegend
            palette={props.palette}
            onClick={props.onClickCheckboxLegend.bind(this)}
            title="Issues:"
            split={props.metric === 'linesChanged'}
            otherCommiters={otherIssues}
          />
        </div>
      </form>
    </div>
  );
};

const ProjectIssueConfig = connect(mapStateToProps, mapDispatchToProps)(ProjectIssueConfigComponent);

export default ProjectIssueConfig;
