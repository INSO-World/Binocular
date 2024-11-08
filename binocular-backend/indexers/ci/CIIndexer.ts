import GitHub from '../../core/provider/github.ts';
import ProgressReporter from '../../utils/progress-reporter.ts';
import { GithubJob, GithubRun } from '../../types/GithubTypes.ts';
import Build from '../../models/models/Build.ts';
import ctx from '../../utils/context.ts';
import debug from 'debug';
import { GitlabNode, GitlabPipeline } from '../../types/GitlabTypes.ts';
import GitLab from '../../core/provider/gitlab';
import { setTimeout } from 'node:timers/promises';

const log = debug('idx:ci:indexer');

class CIIndexer {
  private controller: GitHub | GitLab;
  private reporter: typeof ProgressReporter;
  private projectId: string;
  private buildMapper: (build: GithubRun | GitlabPipeline, jobs: GithubJob[] | GitlabNode[]) => Promise<void>;
  private stopping: boolean;
  private omitCount: number;
  private persistCount: number;

  constructor(
    reporter: typeof ProgressReporter,
    controller: GitHub | GitLab,
    projectId: string,
    createBuildArtifactHandler: (build: GithubRun | GitlabPipeline, jobs: GithubJob[] | GitlabNode[]) => Promise<void>,
  ) {
    this.reporter = reporter;
    this.controller = controller;
    this.projectId = projectId;
    this.buildMapper = createBuildArtifactHandler;
    this.stopping = false;
    this.omitCount = 0;
    this.persistCount = 0;
  }

  async index() {
    log('indexing');
    this.omitCount = 0;
    this.persistCount = 0;
    const mainClass = this;
    return Promise.resolve(this.projectId)
      .then((projectId: string) => {
        let buildRequest: any;
        try {
          buildRequest = this.controller.getPipelines(projectId);
        } catch (error: unknown) {
          // exception can be thrown if the server is unreachable
          console.error(`fetching ${projectId} failed because of following error: ${error}`);
        }
        // TODO find out why, potentially HttpStatus 500 on Github Site, happens sometimes, no reason found(November 2024)
        return buildRequest.then(async (builds: GithubRun[] | GitlabPipeline[]) => {
          // @ts-expect-error function dynamically loaded, therefore not known by code highlighting
          this.reporter.setBuildCount(builds.length);
          if (ctx.ciUrlProvider.provider === 'gitlab') {
            return this.processBuilds(projectId, builds);
          } else if (ctx.ciUrlProvider.provider === 'github') {
            // determine if sending the requests as batches is needed
            // secondary rate limit is 100 requests per second
            const batchSize = 100;
            if (builds.length > batchSize) {
              builds = builds as GithubRun[];
              const allPromises: Promise<void>[] = [];
              let previousTime = Date.now();
              for (let i = 0; i < builds.length; i += batchSize) {
                const batchPromises = await this.processBuilds(projectId, builds.slice(i, i + batchSize));
                allPromises.push(...batchPromises);
                // waiting maximum of 1.001 seconds to avoid secondary rate limit
                if (i * batchSize < batchSize * (builds.length - 1)) {
                  const waitingTime = 1001 + previousTime - Date.now();
                  log('Waiting %o ms', waitingTime);
                  await setTimeout(waitingTime);
                  previousTime = Date.now();
                }
              }
              return Promise.all(allPromises);
            }
            return this.processBuilds(projectId, builds);
          } else {
            console.error('Feature not implemented: CI not recognised');
          }
        });
      })
      .then(function (resp) {
        log('Persisted %d new builds (%d already present)', mainClass.persistCount, mainClass.omitCount);
        if (resp === false) {
          return Promise.resolve(resp);
        } else {
          return Promise.all(resp);
        }
      });
  }

  async processBuilds(projectId: string, builds: GithubRun[] | GitlabPipeline[]) {
    log('Process builds with a length of %o', builds.length);

    return builds.map((build: GitlabPipeline | GithubRun) => {
      return Build.findOneBy('id', build.id)
        .then(async (existingBuild) => {
          // program should be running AND (existing Build OR existing Build with empty jobs OR saved Build is not the latest Build)
          // determine if the build should be saved/updated
          if (
            !this.stopping &&
            (!existingBuild ||
              (existingBuild && existingBuild.data.jobs.length === 0) ||
              (build['updated_at'] && new Date(existingBuild?.data.updatedAt).getTime() < new Date(build['updated_at']).getTime()))
          ) {
            log('Repository type %o', ctx.ciUrlProvider.provider);
            if (ctx.ciUrlProvider.provider === 'github') {
              // read cli variable to decide if entity is saved with or without jobs
              if (ctx.argv.jobs) {
                log(`Processing build #${build.id} [${this.persistCount + this.omitCount}]`);
                return Promise.resolve(await this.controller.getPipelineJobs(projectId, build.id))
                  .then((jobs: GithubJob[]) => {
                    return this.buildMapper(build, jobs);
                  })
                  .then(() => {
                    this.persistCount++;
                  })
                  .catch(() => {
                    log('getPipelineJobs with id %o failed', build.id);
                  });
              } else {
                log(`Processing build with no jobs #${build.id} [${this.persistCount + this.omitCount}]`);
                return this.buildMapper(build, []).then(() => {
                  this.persistCount++;
                });
              }
              // Gitlab does not need differentiation, because jobs are stored in the GraphQl for builds
            } else if (ctx.ciUrlProvider.provider === 'gitlab') {
              if (!build['jobs']) {
                console.error('Gitlab build should have jobs');
              } else {
                log(`Processing build #${build.id} [${this.persistCount + this.omitCount}]`);
                build = build as GitlabPipeline;
                return this.buildMapper(
                  build,
                  build.jobs.edges.map((edge) => edge.node),
                );
              }
            }
          } else {
            this.omitCount++;
            log(`Skipping build #${build.id} [${this.persistCount + this.omitCount}]`);
          }
        })
        .then(() => {
          // @ts-expect-error function dynamically loaded, therefore not known by code highlighting
          this.reporter.finishBuild();
        });
    });
  }

  isStopping() {
    return this.stopping;
  }

  stop() {
    this.stopping = true;
  }
}

export default CIIndexer;
