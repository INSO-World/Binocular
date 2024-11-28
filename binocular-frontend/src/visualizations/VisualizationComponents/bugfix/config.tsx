'use strict';

import { connect } from 'react-redux';
import { setActiveFiles, setAllBranches, setBranchOptions, setCurrentBranch, setFiles } from './sagas';
import styles from './styles.module.scss';
import { GlobalState } from '../../../types/globalTypes'; // TODO: check if the file route is correct
import * as React from 'react';
import { useEffect, useState } from 'react';
import { getBranches, getFilenamesForBranch } from './sagas/helper';
import Filepicker from '../../../components/Filepicker';

const mapStateToProps = (state: GlobalState) => {
  // Declarations mostly copied from code-ownership because of similarity in dashboard layout and functionality

  //global state from redux store
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
  };
};

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
}

const BugfixConfigComponent = (props: Props) => {
  useEffect(() => {
    //get all branches for branch-select
    getBranches()
      .then((branches) => branches.sort((a, b) => a.branch.localeCompare(b.branch)))
      .then((branches) => {
        props.setAllBranches(branches);
        //select the currently active branch
        if (!props.currentBranch) {
          let activeBranch = branches.filter((b) => b.active === 'true')[0];
          if (!activeBranch) {
            activeBranch = branches[0];
          }
          props.onSetBranch(activeBranch, branches);
        }
        //return just the names of the branches
        return branches.map((b) => b.branch);
      })
      .then((branches) => [...new Set(branches)])
      .then((branches) => {
        //build the selection box
        const temp: JSX.Element[] = [];
        //placeholder option
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

  // update files every time the branch changes
  // also reset selected files
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
      <form>
        {/* select branch, reused code from code-ownership */}
        <div className="field">
          <div className="control">
            <label className="label">Branch:</label>
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
            <p>If you want to track file renames for this branch, add it to the 'fileRenameBranches' array in '.binocularrc'</p>
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
