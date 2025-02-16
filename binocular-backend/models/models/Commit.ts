'use strict';

import debug from 'debug';
import Model, { Entry } from '../Model';

import File, { FileDataType } from './File';
import User, { UserDataType } from './User';
import Stats from '../../types/supportingTypes/Stats';
import CommitCommitConnection from '../connections/CommitCommitConnection';
import CommitFileConnection from '../connections/CommitFileConnection';
import CommitUserConnection from '../connections/CommitUserConnection';

import IllegalArgumentError from '../../errors/IllegalArgumentError';
import { exec } from 'child_process';
import * as utils from '../../utils/utils';
import config from '../../utils/config.js';
import Repository from '../../core/provider/git';
import GitHubUrlProvider from '../../url-providers/GitHubUrlProvider';
import GitLabUrlProvider from '../../url-providers/GitLabUrlProvider';
import GatewayService from '../../utils/gateway-service';
import Context from '../../utils/context';
import _ from 'lodash';
import CommitDto from '../../types/dtos/CommitDto';
import { promisify } from 'node:util';

const log = debug('git:commit');

const execAsync = promisify(exec);

export interface CommitDataType {
  sha: string;
  date: string;
  message: string;
  webUrl: string;
  branch: string;
  stats: Stats;
}

interface DiffLine {
  content: string;
  origin: string;
}

interface DiffHunk {
  oldStart: number;
  oldLines: number;
  newStart: number;
  newLines: number;
  lines: DiffLine[];
}

interface DiffFile {
  oldFilePath: string;
  newFilePath: string;
  status: string;
  hunks: DiffHunk[];
}

interface DiffData {
  files: DiffFile[];
}

class Commit extends Model<CommitDataType> {
  constructor() {
    super({
      name: 'Commit',
      keyAttribute: 'sha',
    });
  }

  /**
   * get or create an new commit and connect it to its parents
   *
   * @param repo contains the repository object
   * @param _commitData contains the current commit that is created by the given repo object and holds the required data
   * @param urlProvider contains the given remote vcs webapp provider to link them
   * @returns Commit returns an already existing or newly created commit
   */
  async persist(repo: Repository, _commitData: CommitDto, urlProvider: GitHubUrlProvider | GitLabUrlProvider) {
    const commitData = _.clone(_commitData);
    if (!repo || !commitData) {
      throw IllegalArgumentError('repository and git-commit has to be set!');
    }

    const sha = commitData.oid;

    const instance = await this.findOneBy('sha', sha);
    if (instance) {
      log('Skipped', sha);

      return instance;
    }
    log('Processing', sha);
    let parents = '';

    commitData.commit.parent.forEach((p, i) => {
      parents += p;
      if (i < commitData.commit.parent.length - 1) {
        parents += ',';
      }
    });

    const authorSignature = utils.fixUTF8(commitData.commit.author.name + ' <' + commitData.commit.author.email + '>');

    if (!commitData.commit.author.timestamp) {
      throw Error('Timestamp undefined!');
    }

    const commit = await this.create(
      {
        sha,
        date: new Date(commitData.commit.author.timestamp * 1000).toISOString(),
        message: commitData.commit.message,
        webUrl: urlProvider ? urlProvider.getCommitUrl(sha) : '',
        branch: commitData.commit.branch,
        stats: {
          additions: 0,
          deletions: 0,
        },
      },
      { isNew: true },
    );
    // Process each parent commit
    await Promise.all(
      parents.split(',').map(async (parentSha) => {
        if (parentSha === '') {
          return;
        }
        const parentCommit = await this.findOneBy('sha', parentSha);
        if (parentCommit === null) {
          return;
        }

        const diff = await this.computeDiff(repo, parentSha, sha);

        await CommitCommitConnection.connect({ diff }, { from: parentCommit, to: commit });
      }),
    );
    const results = await User.ensureBy('gitSignature', authorSignature, {} as UserDataType);
    const user = results[0];
    await CommitUserConnection.connect({}, { from: commit, to: user });
    return commit;
  }

  async computeDiff(repo: Repository, parentSha: string, currentSha: string): Promise<DiffData> {
    try {
      const { stdout: diffStdout, stderr: diffStderr } = await execAsync(`git diff --unified=0 ${parentSha} ${currentSha} --`, {
        cwd: repo.path,
        maxBuffer: 1024 * 1024 * 10,
      });

      if (diffStderr) {
        console.warn(`Stderr while computing detailed diff between ${parentSha} and ${currentSha}:`, diffStderr);
      }

      const lines = diffStdout.split('\n');
      const diffData: DiffData = {
        files: [],
      };

      let currentFile: DiffFile | null = null;

      for (const line of lines) {
        if (line.startsWith('diff --git')) {
          const parts = line.split(' ');
          const oldFile = parts[2].replace('a/', '');
          const newFile = parts[3].replace('b/', '');
          currentFile = {
            oldFilePath: oldFile,
            newFilePath: newFile,
            status: 'modified',
            hunks: [],
          };
          diffData.files.push(currentFile);
        } else if (line.startsWith('@@')) {
          const hunkHeader = line.match(/@@ -(\d+)(?:,(\d+))? \+(\d+)(?:,(\d+))? @@/);
          if (hunkHeader && currentFile) {
            const oldStart = parseInt(hunkHeader[1], 10);
            const oldLines = hunkHeader[2] ? parseInt(hunkHeader[2], 10) : 1;
            const newStart = parseInt(hunkHeader[3], 10);
            const newLines = hunkHeader[4] ? parseInt(hunkHeader[4], 10) : 1;

            const currentHunk: DiffHunk = {
              oldStart,
              oldLines,
              newStart,
              newLines,
              lines: [],
            };
            currentFile.hunks.push(currentHunk);
          }
        } else if (['+', '-', ' '].includes(line[0])) {
          if (currentFile && currentFile.hunks.length > 0) {
            const currentHunk = currentFile.hunks[currentFile.hunks.length - 1];
            currentHunk.lines.push({
              content: line.slice(1),
              origin: line[0],
            });
          }
        } else if (line.startsWith('rename from')) {
          if (currentFile) {
            currentFile.status = 'renamed';
          }
        } else if (line.startsWith('new file mode')) {
          if (currentFile) {
            currentFile.status = 'added';
          }
        } else if (line.startsWith('deleted file mode')) {
          if (currentFile) {
            currentFile.status = 'deleted';
          }
        }
      }

      for (const file of diffData.files) {
        if (!file.status) {
          file.status = 'modified';
        }
      }

      return diffData;
    } catch (error) {
      console.error(`Error computing diff between ${parentSha} and ${currentSha}:`, error);
      return { files: [] };
    }
  }

  /**
   * process and store a commit and its associated data objects
   *
   * @param commitDAO
   * @param repo contains the repository object
   * @param commitData contains the current commit that is created by the given repo object and holds the required data
   * @param currentBranch current checked out branch of the repository
   * @param urlProvider contains the given remote vcs webapp provider to link them
   * @param gateway contains the given gateway object to process commits based on various registered services
   * @param context
   * @returns {*}
   */
  async processTree(
    commitDAO: Entry<CommitDataType>,
    repo: Repository,
    commitData: CommitDto,
    currentBranch: string,
    urlProvider: GitHubUrlProvider | GitLabUrlProvider,
    gateway: GatewayService,
    context: typeof Context,
  ): Promise<any> {
    const ignoreFiles = config.get().ignoreFiles || [];
    const ignoreFilesRegex = ignoreFiles.map((i) => new RegExp(i.replace('*', '.*')));
    return Promise.resolve(
      repo.getCommitChanges.bind(this)(
        commitDAO,
        repo,
        commitData.oid,
        commitData.commit.parent[0],
        async (filepath: string, parentCommitEntry: any, currentCommitEntry: any, commitFiles: string[], parentCommitFiles: string[]) => {
          try {
            // ignore directories
            const currentOid = commitData.oid;
            if (!(commitFiles.includes(filepath) || parentCommitFiles.includes(filepath))) {
              return;
            }

            //ignore files that are listed in "ignoreFiles" in the .binocularrc file
            for (const ignoredFile of ignoreFilesRegex) {
              const matches = filepath.match(ignoredFile);
              if (matches && matches.length !== 0) {
                return;
              }
            }

            const changes: any[] = [];

            let parentContent = '';
            let currentContent = '';
            let lineCount = 0;
            let action = 'modified';

            try {
              if (!(commitFiles.includes(filepath) || parentCommitFiles.includes(filepath))) {
                //File not in commit or parent commit
                return;
              } else if (commitFiles.includes(filepath) && !parentCommitFiles.includes(filepath)) {
                //file added
                action = 'added';
                currentContent = Buffer.from(await currentCommitEntry.content()).toString('utf8');
                lineCount = currentContent.split(/\r\n|\r|\n/).length;
                changes.push({ lhs: { at: 0, del: 0 }, rhs: { at: 1, add: lineCount } });
              } else if (!commitFiles.includes(filepath) && parentCommitFiles.includes(filepath)) {
                //file deleted
                action = 'deleted';
                //return;
                //index file deletions as changes
                parentContent = Buffer.from(await parentCommitEntry.content()).toString('utf8');
                const parentLineCount = parentContent.split('\n').length;
                changes.push({ lhs: { at: 0, del: parentLineCount }, rhs: { at: 0, add: 0 } });
              } else {
                //file modified
                parentContent = Buffer.from(await parentCommitEntry.content()).toString('utf8');
                currentContent = Buffer.from(await currentCommitEntry.content()).toString('utf8');
                if (parentContent === currentContent) {
                  return;
                }

                const diffOutput: string = await new Promise((resolve) => {
                  //go to the target directory, execute git diff for a specific file to get the changes between the parent/current commit
                  exec(
                    `cd ${context.targetPath} && git diff --unified=0 ${commitData.commit.parent[0]} ${commitData.oid} -- ${filepath}`,
                    { maxBuffer: 1024 * 10000 },
                    (error, stdout, stderr) => {
                      if (error) {
                        console.log(`error: ${error.message}`);
                        resolve('');
                      }
                      if (stderr) {
                        console.log(`stderr: ${stderr}`);
                        resolve('');
                      }
                      resolve(stdout);
                    },
                  );
                });

                //diff output also includes the changed lines which we dont care about.
                //we only need the chunk headers: "@@ -oldFileStartLine,oldFileLineNumbers +newFileStartLine,newFileLineNumbers @@"
                const hunkHeaders: string[] = diffOutput.split('\n').filter((s) => s.startsWith('@@'));
                hunkHeaders.forEach((header) => {
                  //remove the '+' and '-'
                  const headerArray = header.replace('+', '').replace('-', '').split(' ');
                  //part of the header containing the numbers for the old/new file
                  const oldHeader = headerArray[1].split(',');
                  const newHeader = headerArray[2].split(',');

                  const oldStart = parseInt(oldHeader[0]);
                  //if oldFileLineNumbers is 1, it is omitted from the header
                  const oldLines = oldHeader.length === 2 ? parseInt(oldHeader[1]) : 1;

                  const newStart = parseInt(newHeader[0]);
                  //if newFileLineNumbers is 1, it is omitted from the header
                  const newLines = newHeader.length === 2 ? parseInt(newHeader[1]) : 1;

                  changes.push({
                    lhs: { at: oldStart, del: oldLines },
                    rhs: { at: newStart, add: newLines },
                  });
                });
              }
            } catch (e: any) {
              console.log(e.message);
            }

            const webUrl = urlProvider.getFileUrl(currentBranch, filepath);
            const file = await File.ensureBy('path', filepath, {
              webUrl: webUrl,
            } as FileDataType).then((f) => f[0]);

            let additionsForFile = 0;
            let deletionsForFile = 0;

            const hunks = changes.map((change) => {
              const oldStart = change.lhs.at;
              const oldLines = change.lhs.del;
              const newStart = change.rhs.at;
              const newLines = change.rhs.add;
              //add additions and deletions of this change to this commit obj
              commitDAO.data.stats.additions += newLines;
              commitDAO.data.stats.deletions += oldLines;
              //add additions and deletions of this change to counter for the commits-files connection
              additionsForFile += newLines;
              deletionsForFile += oldLines;
              return {
                webUrl: urlProvider ? urlProvider.getHunkUrl(currentOid, filepath, newLines, oldLines) : null,
                newLines: newLines,
                newStart: newStart,
                oldLines: oldLines,
                oldStart: oldStart,
              };
            });

            await this.save(commitDAO);

            return Promise.all([
              file,
              lineCount,
              {
                additions: additionsForFile,
                deletions: deletionsForFile,
              },
              hunks,
            ]).then((results) => {
              const file = results[0];
              const lineCount = results[1];
              const stats = results[2];
              const hunks = results[3];
              return {
                file,
                lineCount,
                stats,
                hunks,
                action,
              };
            });
          } catch (e) {
            console.log(e);
          }
        },
      ),
    ).then((patches) =>
      patches.map(async (patch: any) => {
        const connection = await CommitFileConnection.ensure(
          {
            lineCount: patch.lineCount,
            hunks: patch.hunks,
            stats: patch.stats,
            action: patch.action,
          },
          { from: commitDAO, to: patch.file },
        );

        return Object.assign(patch, {
          hunkConnection: !commitDAO.justCreated ? null : connection,
        });
      }),
    );
  }
}

export default new Commit();
