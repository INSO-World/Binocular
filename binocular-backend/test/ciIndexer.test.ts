'use strict';

import { expect } from 'chai';
import ReporterMock from './helper/reporter/reporterMock.ts';
import Db from '../core/db/db';
import conf from '../utils/config.js';
import ctx from '../utils/context.ts';
import GitLabCIIndexer from './helper/gitlab/gitLabCIIndexerRewire.js';
import GitHubCIIndexer from './helper/github/gitHubCIIndexerRewire.js';
import Build, { BuildDataType } from '../models/models/Build';
import repositoryFake from './helper/git/repositoryFake.js';
import GitLabMock from './helper/gitlab/gitLabMock.js';
import { getAllEntriesInCollection, remapGitHubApiCall, remapUnpaginatedGitlabApiCall } from './helper/utils.ts';
import './base.test.ts';

describe('ci', function () {
  const config = conf.get();
  const db = new Db(config.arango);
  const reporter = new ReporterMock(undefined, ['build']);

  config.token = '1234567890';
  config.testSetup = {};

  const setupDb = async () => {
    await db.ensureDatabase('test', ctx);
    await db.truncate();
    await Build.ensureCollection();
  };

  const remapRemoteFunctions = (repo) => {
    repo.listAllCommitsRemote = repo.listAllCommits;
    repo.getAllBranchesRemote = repo.getAllBranches;
    repo.getLatestCommitForBranchRemote = repo.getLatestCommitForBranch;
    repo.getFilePathsForBranchRemote = repo.getFilePathsForBranch;
  };

  const getAllInCollection = async (collection: string) => getAllEntriesInCollection(db, collection);

  describe('#indexGitLab', function () {
    const gitLabSetup = async () => {
      const repo = await repositoryFake.repository();
      ctx.targetPath = repo.path;
      ctx.ciUrlProvider = { provider: 'gitlab' };

      //Remap Remote functions to local ones because remote repository doesn't exist anymore.
      remapRemoteFunctions(repo);

      repo.getOriginUrl = async function () {
        return 'git@gitlab.com:Test/Test-Project.git';
      };
      await setupDb();
      const gitLabCIIndexer = new GitLabCIIndexer(repo, reporter);
      gitLabCIIndexer.gitlab = new GitLabMock();
      await gitLabCIIndexer.configure(config);
      return gitLabCIIndexer;
    };

    it('should index all GitLab pipelines and create all necessary db collections and connections', async function () {
      const gitLabCIIndexer = await gitLabSetup();
      await gitLabCIIndexer.index();
      const dbBuildsCollectionData = (await getAllInCollection('builds')) as unknown as BuildDataType[];

      expect(dbBuildsCollectionData.length).to.equal(3);
      expect(dbBuildsCollectionData[0].jobs.length).to.equal(3);
      for (const i in dbBuildsCollectionData[0].jobs) {
        expect(dbBuildsCollectionData[0].jobs[i].webUrl).to.equal('https://gitlab.com/Test/Test-Project/jobs/' + i);
      }
      expect(dbBuildsCollectionData[0].webUrl).to.equal(dbBuildsCollectionData[0].webUrl);
    });

    it('should not persist GitLab workflows twice if indexer is called twice', async function () {
      const gitLabCIIndexer = await gitLabSetup();

      await gitLabCIIndexer.index();
      const builds = await getAllInCollection('builds');

      // index again. Should not add documents to the collection.
      await gitLabCIIndexer.index();
      const buildsUpdated = await getAllInCollection('builds');

      expect(builds.length).to.equal(buildsUpdated.length);
    });

    it('should be able to handle empty response', async function () {
      const gitLabCIIndexer = await gitLabSetup();
      remapUnpaginatedGitlabApiCall(gitLabCIIndexer, 'getPipelines', []);

      await gitLabCIIndexer.index();
      const builds = await getAllInCollection('builds');

      expect(builds.length).to.equal(0);
    });
  });

  describe('#indexGitHub', function () {
    const gitHubSetup = async (pipelineVersion?: number) => {
      const repo = await repositoryFake.repository();
      ctx.targetPath = repo.path;
      ctx.ciUrlProvider = { provider: 'github' };
      //Remap Remote functions to local ones because remote repository doesn't exist anymore.
      remapRemoteFunctions(repo);
      repo.getOriginUrl = async function () {
        return 'git@github.com/Test/Test-Project.git';
      };
      await setupDb();
      const gitHubCIIndexer = new GitHubCIIndexer(repo, reporter);
      config.testSetup.pipelineVersion = pipelineVersion || 0;
      await gitHubCIIndexer.configure(config);
      return gitHubCIIndexer;
    };

    it('should index all GitHub workflows and create all necessary db collections and connections', async function () {
      const gitHubCIIndexer = await gitHubSetup();
      await gitHubCIIndexer.index();
      const dbBuildsCollectionData = (await getAllInCollection('builds')) as unknown as BuildDataType[];

      expect(dbBuildsCollectionData.length).to.equal(3);

      const build = dbBuildsCollectionData.find((item) => item.id === 0) as BuildDataType;
      expect(build.jobs.length).to.equal(3);
      expect(build.jobs[0].id).to.equal('0');
      expect(build.jobs[0].status).to.equal('success');
      expect(build.jobs[1].id).to.equal('1');
      expect(build.jobs[1].status).to.equal('success');
      expect(build.jobs[2].id).to.equal('2');
      expect(build.jobs[2].status).to.equal('failure');

      expect(dbBuildsCollectionData[0].status).to.equal('success');
    });

    it('should not persist GitHub workflows twice if indexer is called twice', async function () {
      const gitHubCIIndexer = await gitHubSetup();

      await gitHubCIIndexer.index();
      const builds = await getAllInCollection('builds');

      // index again. Should not add documents to the collection.
      await gitHubCIIndexer.index();
      const buildsUpdated = await getAllInCollection('builds');

      expect(builds.length).to.equal(buildsUpdated.length);
    });

    it('should be able to handle empty response', async function () {
      const gitHubCIIndexer = await gitHubSetup();
      remapGitHubApiCall(gitHubCIIndexer, 'getPipelines', []);

      await gitHubCIIndexer.index();
      const builds = await getAllInCollection('builds');

      expect(builds.length).to.equal(0);
    });

    it('should be able to update existing pipelines in db with outdated jobs', async function () {
      let gitHubCIIndexer = await gitHubSetup();
      await gitHubCIIndexer.index();
      const builds = (await getAllInCollection('builds')) as unknown as BuildDataType[];
      const build = builds.find((item) => item.id === 0);
      expect(build!.jobs.length).to.equal(3);

      gitHubCIIndexer = await gitHubSetup(1);
      await gitHubCIIndexer.index();
      const buildsUpdated = (await getAllInCollection('builds')) as unknown as BuildDataType[];
      const updatedPipeline = buildsUpdated.find((item) => item.id === 0);
      expect(updatedPipeline!.jobs.length).to.equal(4);
    });

    it('should be able to update existing pipelines in db with no jobs', async function () {
      let gitHubCIIndexer = await gitHubSetup();
      await gitHubCIIndexer.index();
      const builds = (await getAllInCollection('builds')) as unknown as BuildDataType[];
      const build = builds.find((item) => item.id === 1);
      expect(build!.jobs.length).to.equal(0);

      gitHubCIIndexer = await gitHubSetup(1);
      await gitHubCIIndexer.index();
      const buildsUpdated = (await getAllInCollection('builds')) as unknown as BuildDataType[];
      const updatedPipeline = buildsUpdated.find((item) => item.id === 1);
      expect(updatedPipeline!.jobs.length).to.equal(4);
    });

    it('should be able to index 201 pipelines using batches', async function () {
      this.timeout(4000); // set test timeout to 4 seconds, because the timeout for batch updating is 1s per batch
      const gitHubCIIndexer = await gitHubSetup(2);
      await gitHubCIIndexer.index();
      const builds = await getAllInCollection('builds');

      expect(builds.length).to.equal(201);
    });
  });

  describe('#configureGitHubIncorrectly', function () {
    it('should configure GitHubCIIndexer incorrectly and throw error', async function () {
      const gitHubCIIndexer = new GitHubCIIndexer(repositoryFake, new ReporterMock(undefined, []));
      try {
        await gitHubCIIndexer.configure(undefined);
      } catch (e: any) {
        expect(e.name).to.equal('ConfigurationError');
        expect(e.message).to.equal('GitHub/Octokit cannot be configured!');
      }
    });
  });
});