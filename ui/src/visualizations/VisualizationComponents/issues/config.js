'use strict';

import { connect } from 'react-redux';

import styles from './styles.scss';

import LegendCompact from '../../../components/LegendCompact';
import TabCombo from '../../../components/TabCombo';
import { setShowIssues } from './sagas';

const mapStateToProps = (state /*, ownProps*/) => {
  const dashboardState = state.visualizations.changes.state;

  return { showIssues: dashboardState.config.showIssues };
};

const mapDispatchToProps = (dispatch /*, ownProps*/) => {
  return { onClickIssues: (showIssues) => dispatch(setShowIssues(showIssues)) };
};

const IssuesConfigComponent = (props) => {
  return (
    <div className={styles.configContainer}>
      <div className={styles.field}>
        <div className="control">
          <TabCombo
            value={props.showIssues}
            options={[
              { label: 'All', icon: 'database', value: 'all' },
              { label: 'Open', icon: 'folder-open', value: 'open' },
              { label: 'Closed', icon: 'folder', value: 'closed' },
            ]}
            onChange={(value) => props.onClickIssues(value)}
          />
        </div>
      </div>
    </div>
  );
};

const IssuesConfig = connect(mapStateToProps, mapDispatchToProps)(IssuesConfigComponent);

export default IssuesConfig;
