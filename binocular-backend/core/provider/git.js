'use strict';

import isomorphicGit from 'isomorphic-git';
import path from 'path';
import fs from 'fs';
import debug from 'debug';
import util from 'util';
import * as customUtils from '../../utils/utils.ts';
import { exec as child_process, execFile as childProcessExecFile } from 'child_process';
import _ from 'lodash';
const exec = util.promisify(child_process);
const execFile = util.promisify(childProcessExecFile);

const log = debug('git.js');

class Repository {
  constructor(/*repo,*/ path, currPath) {
    this.path = path;
    this.currPath = currPath;
  }

  getLatestCommitForBranchRemote(branchName) {
    return isomorphicGit.log({
      fs,
      dir: this.currPath || '.',
      ref: 'origin/' + branchName,
      depth: 1,
    });
  }

  getLatestCommitForBranch(branchName) {
    return isomorphicGit.log({
      fs,
      dir: this.currPath || '.',
      ref: branchName,
      depth: 1,
    });
  }

  // throws NotFoundError when branch does not exist (anymore)
  async getFilePathsForBranchRemote(branchName) {
    if (branchName) {
      if (!branchName.startsWith('origin')) {
        branchName = 'origin/' + branchName;
      }
      // retrieve all files of the project at the specified commit
      return await isomorphicGit.listFiles({ fs, dir: this.currPath || '.', ref: branchName });
    }
    return [];
  }

  // throws NotFoundError when branch does not exist (anymore)
  async getFilePathsForBranch(branchName) {
    if (branchName) {
      // retrieve all files of the project at the specified commit
      return await isomorphicGit.listFiles({ fs, dir: this.currPath || '.', ref: branchName });
    }
    return [];
  }

  async getPreviousFilenames(branchName, fileName, context) {
    if (branchName === undefined || branchName === null || fileName === undefined || fileName === null) {
      return [];
    }

    const cmd = `cd ${context.targetPath} && git log --format="%ad" --name-only --follow --diff-filter=AR ${branchName} -- ${fileName}`;
    try {
      const { stdout } = await exec(cmd, { maxBuffer: 1024 * 1000 });
      if (stdout.length === 0) return [];
      let groups = _.chunk(stdout.split('\n'), 3);
      //since exec returns a newline at the end, the last group is always empty. remove it
      groups = groups.filter((g) => g.length > 1);

      //the first element of the group is always the current filename.
      //we are only interested in the previous filenames, so we only need groups with length > 1
      if (groups.length <= 1) return [];
      //sample entry in group:
      //["Tue Nov 8 10:59:25 2022 +0100", "", "path/to/file"]
      return groups.map((g) => {
        return { fileName: g[2], timestamp: new Date(g[0]) };
      });
    } catch (e) {
      log('error in get-blame.js: ', e);
      return [];
    }
  }

  async getPreviousFilenamesRemote(branchName, fileName, context) {
    if (branchName === undefined || branchName === null || fileName === undefined || fileName === null) {
      return [];
    }

    if (!branchName.startsWith('origin')) {
      branchName = 'origin/' + branchName;
    }

    return this.getPreviousFilenames(branchName, fileName, context);
  }

  async getOwnershipForFile(file, commit, context) {
    try {
      const blameOutput = (await exec(`cd ${context.targetPath} && git blame -p ${commit} -- "${file}"`, { maxBuffer: 1024 * 100000 }))
        .stdout;
      return customUtils.parseBlameOutput(blameOutput);
    } catch (e) {
      console.log('error ', e.message);
      return {};
    }
  }

  getAllBranchesRemote() {
    return isomorphicGit.listBranches({ fs, dir: this.currPath || '.', remote: 'origin' });
  }

  getAllBranches() {
    return isomorphicGit.listBranches({ fs, dir: this.currPath || '.' });
  }

  getRoot() {
    return path.resolve(this.path, '..');
  }

  getPath() {
    return path.resolve(this.path);
  }

  getName() {
    return path.basename(path.dirname(this.path));
  }

  pathFromRoot() {
    return path.resolve(this.getRoot(), ...arguments);
  }

  getHeadPath() {
    return path.resolve(this.path, 'FETCH_HEAD');
  }

  getCurrentBranch() {
    return isomorphicGit.currentBranch({ fs, dir: this.currPath || '.' });
  }

  async getOriginUrl() {
    const remotes = await isomorphicGit.listRemotes({ fs, dir: this.currPath || '.' });
    return remotes[0].url;
  }

  async createCommit(files, author, message) {
    for (const filepath of files) {
      await isomorphicGit.add({ fs, dir: this.currPath || '.', filepath });
    }

    return isomorphicGit.commit({
      fs,
      dir: this.currPath || '.',
      message,
      author,
    });
  }

  async removeFromStagingArea(files) {
    for (const filepath of files) {
      await isomorphicGit.remove({ fs, dir: this.currPath || '.', filepath });
    }
  }

  createBranch(ref) {
    return isomorphicGit.branch({
      fs,
      dir: this.currPath || '.',
      ref: ref,
    });
  }

  checkout(ref) {
    return isomorphicGit.checkout({
      fs,
      dir: this.currPath || '.',
      ref: ref,
    });
  }

  async listAllCommits() {
    let branches = await this.getAllBranches();
    const commits = [];
    branches = branches.filter((b) => b !== 'HEAD').reverse();
    for (const b of branches) {
      let branchCommits = await isomorphicGit.log({
        fs,
        dir: this.currPath || '.',
        ref: b,
      });
      branchCommits = branchCommits.reverse();
      branchCommits = branchCommits.map((c) => {
        c.commit.branch = b;
        return c;
      });
      branchCommits.forEach((commit) => {
        if (commits.find((c) => c.oid === commit.oid) === undefined) {
          commits.push(commit);
        }
      });
    }
    return commits;
  }

  async listAllCommitsRemote() {
    let branches = await this.getAllBranchesRemote();
    const commits = [];
    branches = branches.filter((b) => b !== 'HEAD').reverse();
    for (const b of branches) {
      let branchCommits = await isomorphicGit.log({
        fs,
        dir: this.currPath || '.',
        ref: 'origin/' + b,
      });
      branchCommits = branchCommits.reverse();
      branchCommits = branchCommits.map((c) => {
        c.commit.branch = b;
        return c;
      });
      branchCommits.forEach((commit) => {
        if (commits.find((c) => c.oid === commit.oid) === undefined) {
          commits.push(commit);
        }
      });
    }
    return commits;
  }

  async getCommitChanges(repo, sha, parentSha, mapFunction) {
    //reset addition/deletion counters
    this.stats.additions = 0;
    this.stats.deletions = 0;

    let files;
    let parentFiles;
    try {
      files = sha === undefined ? [] : await isomorphicGit.listFiles({ fs, dir: repo.currPath || '.', ref: sha });
      parentFiles = parentSha === undefined ? [] : await isomorphicGit.listFiles({ fs, dir: repo.currPath || '.', ref: parentSha });
    } catch {
      files = [];
      parentFiles = [];
    }

    return isomorphicGit.walk({
      fs,
      dir: repo.currPath || '.',
      trees: [isomorphicGit.TREE({ ref: parentSha }), isomorphicGit.TREE({ ref: sha })],
      map: async function (filepath, [parentEntry, currentEntry]) {
        return mapFunction(filepath, parentEntry, currentEntry, files, parentFiles);
      },
    });
  }

  /**
   * Returns all commits of a branch by following only the first parent chain.
   * This replicates `git rev-list --first-parent <branch>`, and is useful
   * to track linear history (ignores side branches).
   *
   * @param {string} branchName - The name of the branch (e.g., 'main')
   * @returns {Promise<Array>} - Array of commit objects from oldest to newest
   */
  async getFirstParentCommits(branchName) {
    if (!branchName || typeof branchName !== 'string') {
      throw new Error('branchName must be a non-empty string');
    }

    const commits = [];
    const cwd = this.currPath || '.';
    const visited = new Set();

    let ref;

    try {
      ref = await isomorphicGit.resolveRef({ fs, dir: cwd, ref: branchName });
    } catch (err) {
      try {
        ref = await isomorphicGit.resolveRef({ fs, dir: cwd, ref: `origin/${branchName}` });
      } catch {
        throw new Error(`Branch '${branchName}' not found in repo at ${cwd}`);
      }
    }

    while (ref && !visited.has(ref)) {
      visited.add(ref);

      const commit = await isomorphicGit.readCommit({ fs, dir: cwd, oid: ref });
      commits.unshift(commit); // oldest-to-newest

      const parents = commit.commit.parent;
      if (!parents || parents.length === 0) break;

      ref = parents[0]; // first parent only
    }

    return commits;
  }

  /**
   * Uses native git to return only first-parent commits that changed a file.
   * This avoids inflating every commit and tree when a metric only depends on
   * package-lock.json history.
   */
  async getFirstParentFileCommits(branchName, filepath) {
    if (!branchName || typeof branchName !== 'string') {
      throw new Error('branchName must be a non-empty string');
    }
    if (!filepath || typeof filepath !== 'string') {
      throw new Error('filepath must be a non-empty string');
    }

    const cwd = this.currPath || '.';
    let ref;
    try {
      ref = await isomorphicGit.resolveRef({ fs, dir: cwd, ref: branchName });
    } catch {
      try {
        ref = await isomorphicGit.resolveRef({ fs, dir: cwd, ref: `origin/${branchName}` });
      } catch {
        throw new Error(`Branch '${branchName}' not found in repo at ${cwd}`);
      }
    }

    const gitArgs = ['-C', cwd, 'log', '--first-parent', '--reverse', '--format=%H%x09%ct', ref, '--', filepath];
    const { stdout } = await execFile('git', gitArgs, { maxBuffer: 1024 * 10000 });

    return stdout
      .split('\n')
      .map((line) => line.trim())
      .filter(Boolean)
      .map((line) => {
        const [oid, timestamp] = line.split('\t');
        return {
          oid,
          commit: { committer: { timestamp: Number(timestamp) } },
        };
      })
      .filter((commit) => commit.oid && Number.isFinite(commit.commit.committer.timestamp));
  }

  async readFileAtCommit(filepath, commitSha) {
    const { blob } = await isomorphicGit.readBlob({
      fs,
      dir: this.currPath || '.',
      oid: commitSha,
      filepath,
    });
    return new TextDecoder().decode(blob);
  }

  static fromPath(currPath) {
    return Promise.resolve(isomorphicGit.init({ fs, dir: currPath || '.' })).then(() => {
      return Promise.resolve(new Repository(path.resolve(currPath + '/.git'), currPath || '.'));
    });
  }

  static fromRepo(repo) {
    return Promise.resolve(new Repository(repo, repo.path()));
  }
}

export default Repository;
