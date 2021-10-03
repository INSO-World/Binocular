'use strict';

import { connect } from 'react-redux';
import styles from './styles.scss';

import {
  setSelectedAuthors,
  setCommitBoxHeight,
  setCommitBoxWidth,
  setCommitBoxColor,
  setSelectedBranches,
  setShowCommitDate,
  setShowCommitMessage,
  setShowCommitAuthor
} from './sagas';

import React from 'react';
import CheckboxLegend from '../../components/CheckboxLegend';
import TabCombo from '../../components/TabCombo';

const mapStateToProps = (state /*, ownProps*/) => {
  //console.log(state);
  const FileEvolutionState = state.visualizations.fileEvolution.state;
  console.log(FileEvolutionState);
  return {
    branches: FileEvolutionState.data.data.branches,
    committers: FileEvolutionState.data.data.committers,
    commits: FileEvolutionState.data.data.commits,
    authorsColorPalette: FileEvolutionState.data.data.authorsColorPalette,
    branchesColorPalette: FileEvolutionState.data.data.branchesColorPalette,
    selectedAuthors: FileEvolutionState.config.selectedAuthors,
    selectedBranches: FileEvolutionState.config.selectedBranches,
    commitBoxHeight: FileEvolutionState.config.commitBoxHeight,
    commitBoxWidth: FileEvolutionState.config.commitBoxWidth,
    commitBoxColor: FileEvolutionState.config.commitBoxColor,
    showCommitDate: FileEvolutionState.config.showCommitDate,
    showCommitAuthor: FileEvolutionState.config.showCommitAuthor,
    showCommitMessage: FileEvolutionState.config.showCommitMessage
  };
};

const mapDispatchToProps = (dispatch /*, ownProps*/) => {
  return {
    onSetSelectedAuthors: selectedAuthors => dispatch(setSelectedAuthors(selectedAuthors)),
    onSetSelectedBranches: selectedBranches => dispatch(setSelectedBranches(selectedBranches)),
    onSetCommitBoxHeight: commitBoxHeight => dispatch(setCommitBoxHeight(commitBoxHeight)),
    onSetCommitBoxWidth: commitBoxWidth => dispatch(setCommitBoxWidth(commitBoxWidth)),
    onClickShowCommitBoxDate: showCommitDate => dispatch(setShowCommitDate(showCommitDate)),
    onClickShowCommitBoxAuthor: showCommitAuthor => dispatch(setShowCommitAuthor(showCommitAuthor)),
    onSetShowCommitMessage: showCommitMessage => dispatch(setShowCommitMessage(showCommitMessage)),
    onClickCommitBoxColor: commitBoxColor => dispatch(setCommitBoxColor(commitBoxColor))
  };
};

//TODO ICONS in TabCombo
const FileEvolutionConfigComponent = props => {
  return (
    <div className={styles.configContainer}>
      <form className={styles.form}>
        <div className={styles.field}>
          <div className="control">
            <label className="label">Chart Box Settings</label>
            <div className={styles.input}>
              <label className={styles.label}>Box Height: &nbsp;</label>
              <input
                type="number"
                id="CommitBoxHeight"
                name="CommitBoxHeight"
                min="30"
                max="200"
                value={props.commitBoxHeight}
                onChange={e => {
                  props.onSetCommitBoxHeight(parseInt(e.target.value, 10));
                }}
              />
            </div>
            <div className={styles.input}>
              <label className={styles.label}>Box Width: &nbsp;</label>
              <input
                type="number"
                id="CommitBoxWidth"
                name="CommitBoxWidth"
                min="30"
                max="200"
                value={props.commitBoxWidth}
                onChange={e => {
                  props.onSetCommitBoxWidth(parseInt(e.target.value, 10));
                }}
              />
            </div>
            <label className={styles.label}>Box Color</label>

            <TabCombo
              value={props.commitBoxColor}
              options={[
                { label: 'Author', icon: 'file-alt', value: 'author' },
                { label: 'Branch', icon: 'cloud-upload-alt', value: 'branch' }
              ]}
              onChange={value => props.onClickCommitBoxColor(value)}
            />
          </div>
          <div className="control">
            <label className="label">Show Commit Infos</label>
            <label className={styles.checkboxLabel}>
              <input
                name="showAuthor"
                type="checkbox"
                onChange={() => props.onClickShowCommitBoxAuthor(!props.showCommitAuthor)}
                checked={props.showCommitAuthor}
              />{' '}
              Show Author{' '}
            </label>
            <label className={styles.checkboxLabel}>
              <input
                name="showAuthor"
                type="checkbox"
                onChange={() => props.onClickShowCommitBoxDate(!props.showCommitDate)}
                checked={props.showCommitDate}
              />{' '}
              Show Date{' '}
            </label>

            <label className={styles.label}>Show Message</label>
            <TabCombo
              value={props.showCommitMessage}
              options={[
                { label: 'None', icon: 'times', value: 'none' },
                { label: 'Header', icon: 'file-alt', value: 'header' },
                { label: 'All', icon: 'database', value: 'all' }
              ]}
              onChange={value => props.onSetShowCommitMessage(value)}
            />
          </div>
          <div className="control">
            <label className="label">Show Commits</label>
            <div>
              <CheckboxLegend
                palette={props.branchesColorPalette}
                onClick={props.onSetSelectedBranches.bind(this)}
                title={'Branches:'}
                explanation={'Color for Box'}
                split={false}
                otherCommitters={[]}
              />
            </div>
            <div>
              <CheckboxLegend
                palette={props.authorsColorPalette}
                onClick={props.onSetSelectedAuthors.bind(this)}
                title={'Authors:'}
                explanation={'Color for Box'}
                split={false}
                otherCommitters={[]}
              />
            </div>
          </div>
        </div>
      </form>
    </div>
  );
};

const FileEvolutionConfig = connect(mapStateToProps, mapDispatchToProps)(FileEvolutionConfigComponent);

export default FileEvolutionConfig;
