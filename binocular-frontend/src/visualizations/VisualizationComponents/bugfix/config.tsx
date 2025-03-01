'use strict';

import { connect } from 'react-redux';
import {
  setActiveFiles,
  setAllBranches,
  setBranchOptions,
  setCurrentBranch,
  setFiles,
  setGraphStyle,
  setRegexConfig,
  setShowFilterMenu,
} from './sagas';
import styles from './styles.module.scss';
import { GlobalState } from '../../../types/globalTypes';
import * as React from 'react';
import { useEffect } from 'react';
import { getBranches, getFilenamesForBranch } from './sagas/helper';
import Filepicker from '../../../components/Filepicker';
import FilterManager from './filterManager/filterManager.tsx';

const mapStateToProps = (state: GlobalState) => {
  // Global state from redux store
  const bugfixState: GlobalState = state.visualizations.bugfix.state;
  const currentBranch = bugfixState.config.currentBranch;
  const currentBranchName = (currentBranch && currentBranch.branch) || undefined;
  const currentActiveFiles = bugfixState.config.activeFiles;

  // State from sagas config
  const allBranches = bugfixState.config.allBranches;
  const branchOptions = bugfixState.config.branchOptions;
  const files = bugfixState.config.files;

  return {
    currentBranchName: currentBranchName,
    branchOptions: branchOptions,
    currentBranch: currentBranch,
    allBranches: allBranches,
    currentActiveFiles: currentActiveFiles,
    files: files,
    showFilterMenu: bugfixState.config.showFilterMenu,
    regexConfig: bugfixState.config.regexConfig,
  };
};

// Dispatches sagas and other functions to props
const mapDispatchToProps = (dispatch: any) => {
  return {
    onSetBranch: (branchName: string, allBranches) => {
      const branchObject = allBranches.filter((b) => b.branch === branchName)[0];
      dispatch(setCurrentBranch(branchObject));
    },
    setAllBranches: (allBranches) => {
      dispatch(setAllBranches(allBranches));
    },
    setBranchOptions: (branchesElements) => {
      dispatch(setBranchOptions(branchesElements));
    },
    resetActiveFiles: () => {
      dispatch(setActiveFiles([]));
    },
    setFiles: (files) => {
      dispatch(setFiles(files));
    },
    setActiveFiles: (files) => {
      dispatch(setActiveFiles(files));
    },
    setGraphStyle: (b) => {
      dispatch(setGraphStyle(b));
    },
    setShowFilterMenu: (b) => {
      dispatch(setShowFilterMenu(b));
    },
    setRegexConfig: (b) => {
      dispatch(setRegexConfig(b));
    },
  };
};

// TODO: Other types
interface Props {
  currentBranchName: string | undefined;
  currentActiveFiles: any;
  branchOptions: any; // TODO: What types????
  currentBranch: any;
  files: File[];
  allBranches: any;
  setAllBranches: any;
  setBranchOptions: any;
  resetActiveFiles: any;
  setFiles: any;
  setActiveFiles: any;
  onSetBranch: (branchName: string, allBranches: any) => void;
  setGraphStyle: (graphStyleBool: any) => void;
  setShowFilterMenu: (showFilterMenuBool: any) => void;
  showFilterMenu: boolean;
  setRegexConfig: (regexConfig: any) => void;
  regexConfig: any;
}

const BugfixConfigComponent = (props: Props) => {
  useEffect(() => {
    // Get all branches for branch-select
    getBranches()
      .then((branches) => branches.sort((a, b) => a.branch.localeCompare(b.branch)))
      .then((branches) => {
        props.setAllBranches(branches);
        // Select the currently active branch
        if (!props.currentBranch) {
          let activeBranch = branches.filter((b) => b.active === 'true')[0];
          if (!activeBranch) {
            activeBranch = branches[0];
          }
          props.onSetBranch(activeBranch, branches);
        }
        // Return just the names of the branches
        return branches.map((b) => b.branch);
      })
      .then((branches) => [...new Set(branches)])
      .then((branches) => {
        // Build the selection box
        const temp: JSX.Element[] = [];
        //Placeholder option
        temp.push(
          <option key={-1} value={''}>
            Select a Branch
          </option>,
        );
        for (const i in branches) {
          temp.push(
            <option key={i} value={String(branches[i])}>
              {String(branches[i])}
            </option>,
          );
        }
        props.setBranchOptions(temp);
      });
  }, []);

  // Update files every time the branch changes
  // Also reset selected files
  useEffect(() => {
    if (props.currentBranch) {
      props.resetActiveFiles(); // Resets current active files back to []
      getFilenamesForBranch(props.currentBranch.branch).then((files) => {
        props.setFiles(files);
        //preselect all files
        props.setActiveFiles(files);
      });
    }
  }, [props.currentBranch]);

  return (
    <div className={styles.configContainer}>
      {props.showFilterMenu ? (
        <FilterManager
          close={() => {
            props.setShowFilterMenu(false);
          }}
          saveConfig={props.setRegexConfig}
          filters={props.regexConfig}
        />
      ) : (
        ''
      )}
      <div className="field">
        <input
          id="switchGraphBugfix"
          type="checkbox"
          name="switchGraphBugfix"
          className={'switch is-rounded is-outlined is-info'}
          defaultChecked={false}
          onChange={(e) => props.setGraphStyle(e.target.checked)}
        />
        <label htmlFor="switchGraphBugfix" className={styles.switch}>
          Switch Graph Style
        </label>
      </div>
      <>
        <h2>Filters</h2>
        <button
          className={'button'}
          onClick={() => {
            props.setShowFilterMenu(true);
          }}>
          Manage Filters for Bugfixes
        </button>
      </>
      <form>
        {/* select branch, reused code from code-ownership */}
        <div className="field">
          <div className="control">
            <label className="label">Branch (by default all bugfixes for all branches/files are shown):</label>
            <div className="select">
              <select value={props.currentBranchName} onChange={(e) => props.onSetBranch(e.target.value, props.allBranches)}>
                {props.branchOptions}
              </select>
            </div>
          </div>
        </div>

        {/* Display a warning if the current branch cannot track file renames */}
        {props.currentBranch && props.currentBranch.tracksFileRenames !== 'true' && props.currentBranch.tracksFileRenames !== true && (
          <>
            <p>
              <b>Attention:</b> This branch does <b>not</b> track file renames!
            </p>
            <p>If you want to track file renames for this branch, add it to the 'fileRenameBranches' array in
              '.binocularrc'</p>
          </>
        )}

        {/* Copied mostly from code ownership, only the Show Ownership toggle is disabled */}
        {/* TODO: Show number of bugfixes near each file/module and make space between the warning and file selector */}
        {props.currentBranch && (
          <div className="field">
            <div className="control">
              <label className="label">Choose Files and Modules to visualize:</label>
              <Filepicker
                fileList={props.files}
                globalActiveFiles={props.currentActiveFiles}
                setActiveFiles={(files) => props.setActiveFiles(files)}
              />
            </div>
          </div>
        )}
      </form>
    </div>
  );
};

const DashboardConfig = connect(mapStateToProps, mapDispatchToProps)(BugfixConfigComponent);

export default DashboardConfig;
