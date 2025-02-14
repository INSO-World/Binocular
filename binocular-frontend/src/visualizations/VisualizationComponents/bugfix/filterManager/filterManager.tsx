'use strict';

import * as React from 'react';
import styles from './filterManager.module.scss';

interface Props {
  close: () => void;
  saveConfig: (config: any) => void;
  filters: any;
}

export default (props: Props) => {
  const [localConfig, setLocalConfig] = React.useState({
    commitMessage: props.filters.commitMessage,
    issueTitle: props.filters.issueTitle,
    issueLabelName: props.filters.issueLabelName,
  }); // OR are next array element, AND are recursive objects
  return (
    <div className={styles.filterManager}>
      <div className={styles.backgroundBlur} onClick={props.close}>
        <div
          className={styles.filterManagerContainer}
          onClick={(event) => {
            event.stopPropagation();
          }}>
          <div className={styles.filterManagerScroll}>
            <h1>Filter Manager (regex)</h1>

            <h2>
              Following rules are used as filters for finding bugfix commit. Rules are in regex format and are OR matched. You can delete
              commits selectively by hovering over the commit in graph. Matching based on regex is case insensitive.
            </h2>
            <div className={styles.mb05 + ' ' + styles.p05} id={'commitMessageRegexInput'}>
              <span>Commit Message Regex:</span>
              <input
                className={'input'}
                placeholder={'Commit Message Regex'}
                value={localConfig.commitMessage}
                type={'text'}
                onChange={(e) => {
                  setLocalConfig({
                    commitMessage: e.target.value,
                    issueTitle: localConfig.issueTitle,
                    issueLabelName: localConfig.issueLabelName,
                  });
                }}></input>
            </div>
            <div className={styles.mb05 + ' ' + styles.p05} id={'issueTitleRegexInput'}>
              <span>Issue Title Regex:</span>
              <input
                className={'input'}
                placeholder={'Issue Title Regex'}
                value={localConfig.issueTitle}
                type={'text'}
                onChange={(e) => {
                  setLocalConfig({
                    commitMessage: localConfig.commitMessage,
                    issueTitle: e.target.value,
                    issueLabelName: localConfig.issueLabelName,
                  });
                }}></input>
            </div>
            <div className={styles.mb05 + ' ' + styles.p05} id={'issueLabelRegexInput'}>
              <span>Issue Label Regex:</span>
              <input
                className={'input'}
                placeholder={'Issue Label Regex'}
                value={localConfig.issueLabelName}
                type={'text'}
                onChange={(e) => {
                  setLocalConfig({
                    commitMessage: localConfig.commitMessage,
                    issueTitle: localConfig.issueTitle,
                    issueLabelName: e.target.value,
                  });
                }}></input>
            </div>

            <button
              className={'button'}
              onClick={() => {
                props.saveConfig(localConfig);
                props.close();
              }}>
              Save
            </button>
            <button
              className={'button'}
              onClick={() => {
                props.close();
              }}>
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
