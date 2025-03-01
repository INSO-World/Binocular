'use strict';

import * as React from 'react';
import * as baseStyles from './commitChangeDisplay.module.scss';
import CodeMirror from '@uiw/react-codemirror';
import { Commit } from '../../types/commitTypes.ts';

interface Props {
  commit: Commit | any;
}

interface State {
  commit: Commit;
  componentMounted: boolean;
  allChangedFiles: any; // [{code: string, filename: string, patch: string}]
  currentIndex: number;
}

export default class CommitChangeDisplay extends React.Component<Props, State> {
  protected styles: any;
  constructor(props: Props | Readonly<Props>, styles: any) {
    super(props);
    console.log('Props from CommitChangeDisplay', props);

    this.styles = Object.freeze(Object.assign({}, baseStyles, styles));
    this.state = {
      allChangedFiles: [],
      currentIndex: 0,
      commit: props.commit,
      componentMounted: false,
    };
  }

  componentDidUpdate(prevProps: Readonly<Props>) {
    if (prevProps.commit['sha'] !== this.props.commit['sha']) {
      this.setState({ currentIndex: 0, commit: this.props.commit });
      this.fetchAllSourceCode(); // Update the source code once the sha of commit changes
    }
  }

  componentDidMount() {
    //Needed to restrict d3 to only access DOM when the component is already mounted
    this.setState({ componentMounted: true });
    this.fetchAllSourceCode();
  }

  componentWillUnmount() {
    this.setState({ componentMounted: false });
  }
  handleClickNextFile = () => {
    this.setState((prevState) => ({
      currentIndex: prevState.allChangedFiles.length - 1 > prevState.currentIndex ? prevState.currentIndex + 1 : prevState.currentIndex,
    }));
  };
  handleClickPrevFile = () => {
    this.setState((prevState) => ({
      currentIndex: prevState.currentIndex > 0 ? prevState.currentIndex - 1 : prevState.currentIndex,
    }));
  };

  // TODO: Show just one file now ... (Or prepare at least some DEMO)
  render() {
    // TODO: Display some warning if no content ...
    // TODO: Change classes ....
    return (
      <div className={this.styles.mainContainer}>
        <div className={this.styles.commitInfo}>
          <table className={this.styles.tableCommit}>
            <tbody>
              <tr>
                <td>
                  <strong>Commit hash: </strong>
                  <a href={this.state.commit.webUrl} target={'_blank'}>
                    {this.state.commit.sha}
                  </a>
                </td>
                <td>
                  <strong>Time: </strong>
                  {new Date(this.state.commit.date).toLocaleString()}
                </td>
              </tr>
              <tr>
                <td>
                  <strong>Author's signature: </strong>
                  {this.state.commit.signature}
                </td>
                <td>
                  <strong className={this.styles.green}>Additions: </strong>
                  {this.state.commit.stats.additions}, <strong className={this.styles.red}>Deletions: </strong>{' '}
                  {this.state.commit.stats.deletions}
                </td>
              </tr>
              <tr>
                <td>
                  <strong>Commit summary: </strong>
                  {this.state.commit.messageHeader}
                </td>
                <td>
                  <strong>Bug possibly introduced in: </strong>
                  {/* TODO: Different kind of links */}
                  {this.state.commit.parents.map((name, index) => (
                    <a key={index} href={`https://github.com/INSO-World/Binocular/commit/${name}`} target={'_blank'}>
                      {name.substring(0, 6)}
                      {index < this.state.commit.parents.length - 1 && ', '}
                    </a>
                  ))}
                </td>
              </tr>
            </tbody>
          </table>
          <div className={this.styles.commitChange}>
            <strong>Current file: </strong>
            {this.state.currentIndex + 1}
            {'/'}
            {this.state.allChangedFiles.length} <button onClick={this.handleClickPrevFile}>&lt;</button>
            <button onClick={this.handleClickNextFile}>&gt;</button>
            <em>
              {' '}
              {this.state.allChangedFiles.length === 0
                ? 'There is no code loaded yet'
                : this.state.allChangedFiles[this.state.currentIndex]['filename']}
            </em>
          </div>
        </div>
        <div className={this.styles.codeView} id={'codeViewContainer'}>
          <CodeMirror
            id={'codeView'}
            ref={'codeView'}
            value={
              this.state.allChangedFiles.length === 0 ? 'No code loaded yet' : this.state.allChangedFiles[this.state.currentIndex]['patch']
            }
            basicSetup={{
              highlightActiveLineGutter: false,
              foldGutter: false,
            }}
            readOnly={true}
            options={{
              mode: 'diff',
            }}
          />
        </div>
      </div>
    );
  }

  // Based on commit hash and webUrl of the source code, fetches all the files of the source code that were changed
  // In that commit
  async fetchAllSourceCode() {
    // TODO: AUTH for no private repos ??? Maybe do this in backend
    const commitUrl = this.state.commit.webUrl;
    const commitSha = this.state.commit.sha;
    if (commitUrl.includes('https://github.com')) {
      // Fetch commit data from api.github.com/repos/... This includes diff patch
      const responseCommitData = await fetch(commitUrl.replace('github.com/', 'api.github.com/repos/').replace('/commit/', '/commits/'));
      if (responseCommitData.ok) {
        const data = await responseCommitData.json();
        const preparedFiles: any[] = [];
        for (const fileData of data['files']) {
          // eslint-disable-next-line max-len
          const requestedCode = await fetch(
            decodeURIComponent(fileData['raw_url'].replace('github.com/', 'raw.githubusercontent.com/').replace('/raw/', '/')),
          );
          // TODO: Error handling in case of Unauthenificated Requests (403 forbidden)
          const fileTemp = {
            filename: fileData.filename,
            patch: fileData.patch,
            code: await requestedCode.text(),
          };
          preparedFiles.push(fileTemp);
        }
        console.log(preparedFiles);
        this.setState({ allChangedFiles: preparedFiles });
      }
      // Fetch all the source code from the files in the commit
    }
    // TODO: Other VCS
  }
}
