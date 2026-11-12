import GitHub from '../../core/provider/github.ts';
import ProgressReporter from '../../utils/progress-reporter.ts';
import { GithubJob, GithubRun } from '../../types/GithubTypes.ts';
import Build, { BuildDataType } from '../../models/models/Build.ts';
import { Entry } from '../../models/Model.ts';
import ctx from '../../utils/context.ts';
import debug from 'debug';
import { GitlabNode, GitlabPipeline } from '../../types/GitlabTypes.ts';
import GitLab from '../../core/provider/gitlab';

const log = debug('idx:ci:indexer');

// GitHub asks clients to stay below ~10 concurrent requests, above that its secondary rate limit triggers
const MAX_CONCURRENT_GITHUB_REQUESTS = 10;
// process the runs in bounded batches, so a rate limited run still persists its finished chunks
const GITHUB_BATCH_SIZE = 100;

// the buildMapper persists a build and resolves with the persist result ([entry, written]) or nothing
type BuildMapper = (build: GithubRun | GitlabPipeline, jobs: GithubJob[] | GitlabNode[]) => Promise<[Entry<BuildDataType>, boolean] | void>;

type FetchJobs = (projectId: string, buildId: number) => Promise<GithubJob[]>;

// a 403/429 raised after the built-in retry and throttle handling gave up
const isRateLimitError = (error: any) => error?.status === 403 || error?.status === 429 || /rate limit/i.test(error?.message || '');

// persist results resolve with a [entry, written] tuple; only the second element tells whether the database changed
const wasWritten = (result: [Entry<BuildDataType>, boolean] | void) => Array.isArray(result) && result[1] === true;

class CIIndexer {
  private controller: GitHub | GitLab;
  private reporter: typeof ProgressReporter;
  private projectId: string;
  private buildMapper: BuildMapper;
  private stopping: boolean;
  private omitCount: number;

  constructor(reporter: typeof ProgressReporter, controller: GitHub | GitLab, projectId: string, createBuildArtifactHandler: BuildMapper) {
    this.reporter = reporter;
    this.controller = controller;
    this.projectId = projectId;
    this.buildMapper = createBuildArtifactHandler;
    this.stopping = false;
    this.omitCount = 0;
  }

  async index() {
    log('indexing');
    this.omitCount = 0;
    const projectId = this.projectId;
    let builds: GithubRun[] | GitlabPipeline[];
    try {
      // getPipelines returns a promise for the whole paginated list, so a failing page surfaces here as well
      builds = await this.controller.getPipelines(projectId);
    } catch (error: unknown) {
      // exception can be thrown if the server is unreachable or a request failed after all retries
      console.error(`fetching ${projectId} failed because of following error: ${error}`);
      return false;
    }
    // TODO find out why, potentially HttpStatus 500 on Github Site, happens sometimes, no reason found(November 2024)
    if (!builds) {
      return false;
    }
    // @ts-expect-error function dynamically loaded, therefore not known by code highlighting
    this.reporter.setBuildCount(builds.length);
    let results: boolean[];
    if (ctx.ciUrlProvider.provider === 'gitlab') {
      // Gitlab does not need the extra requests, because jobs are stored in the GraphQl response for builds
      results = await this.processBuilds(projectId, builds, () => Promise.resolve([] as GithubJob[]));
    } else if (ctx.ciUrlProvider.provider === 'github') {
      // the job requests are limited by the request pool in processGitHubBuilds (limitGitHubRequests)
      results = await this.processBuilds(
        projectId,
        builds as GithubRun[],
        (project, buildId) => Promise.resolve(this.controller.getPipelineJobs(project, buildId)) as Promise<GithubJob[]>,
      );
    } else {
      console.error('Feature not implemented: CI not recognised');
      return false;
    }
    // builds counted in omitCount were already up to date; failed fetches resolved to false and stay untouched
    const updated = results.filter((wasUpdated) => wasUpdated).length;
    log('Updated %d builds (%d already up to date)', updated, this.omitCount);
    return results;
  }

  /**
   * Runs the given job fetches with a bounded number of concurrent GitHub requests,
   * to stay below GitHub's secondary rate limit (it flags more than ~100 concurrent requests
   * and sustained high request rates on content generating endpoints).
   */
  async limitGitHubRequests<T>(tasks: (() => Promise<T>)[]): Promise<T[]> {
    const results: T[] = new Array(tasks.length);
    let next = 0;
    const worker = async () => {
      while (next < tasks.length) {
        const index = next++;
        results[index] = await tasks[index]();
      }
    };
    await Promise.all(Array.from({ length: Math.min(MAX_CONCURRENT_GITHUB_REQUESTS, tasks.length) }, () => worker()));
    return results;
  }

  async processBuilds(projectId: string, builds: GithubRun[] | GitlabPipeline[], fetchJobs: FetchJobs): Promise<boolean[]> {
    log('Process builds with a length of %o', builds.length);

    if (ctx.ciUrlProvider.provider === 'github') {
      return this.processGitHubBuilds(projectId, builds as GithubRun[], fetchJobs);
    }
    const pending = (builds as GitlabPipeline[]).map((build: GitlabPipeline) => {
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
            if (!build['jobs']) {
              console.error('Gitlab build should have jobs');
            } else {
              log(`Processing build #${build.id}`);
              return this.buildMapper(
                build,
                build.jobs.edges.map((edge) => edge.node),
              ).then((persistResult) => wasWritten(persistResult));
            }
          } else {
            this.omitCount++;
            log(`Skipping build #${build.id} [${this.omitCount}]`);
          }
          return false;
        })
        .then((written) => {
          // @ts-expect-error function dynamically loaded, therefore not known by code highlighting
          this.reporter.finishBuild();
          return written;
        });
    });
    return Promise.all(pending);
  }

  async processGitHubBuilds(projectId: string, builds: GithubRun[], fetchJobs: FetchJobs): Promise<boolean[]> {
    let processed = 0;
    const runBuild = async (build: GithubRun): Promise<boolean> => {
      // a build that never started is not handled, so it must not raise the finished count
      if (this.stopping) {
        return false;
      }
      // every path below reaches the finally, so a handled build is counted exactly once:
      // the stop decision after the database read, the jobs-less persist and a failing job fetch alike
      try {
        const existingBuild = await Build.findOneBy('id', build.id);
        const isOutdated =
          build.updated_at !== null &&
          (existingBuild === null || new Date(existingBuild.data.updatedAt).getTime() < new Date(build.updated_at).getTime());
        // refetch only if the saved run data is outdated or jobs were requested but never stored;
        // without ctx.argv.jobs an empty jobs array is intentional, refetching it could never be persisted and would burn requests;
        // jobsFetched marks a build whose (possibly empty) job list was already answered by the API
        const needsJobs =
          ctx.argv.jobs &&
          existingBuild !== null &&
          (existingBuild.data.jobs?.length ?? 0) === 0 &&
          existingBuild.data.jobsFetched !== true;
        if (this.stopping || (!isOutdated && !needsJobs)) {
          this.omitCount++;
          log(
            `Skipping build #${build.id} [${this.omitCount}] (outdated=%o, needsJobs=%o, storedJobs=%o, jobsFetched=%o)`,
            isOutdated,
            needsJobs,
            existingBuild?.data.jobs?.length,
            existingBuild?.data.jobsFetched,
          );
          return false;
        }
        log(`Processing build #${build.id} [${++processed + this.omitCount}]${needsJobs ? ' (missing jobs)' : ''}`);
        if (!ctx.argv.jobs) {
          // awaited, so the build is counted once the persist settled and not once its promise was merely created
          return await this.buildMapper(build, [])
            .then((persistResult) => wasWritten(persistResult))
            .catch(() => false);
        }
        try {
          const jobs = await fetchJobs(projectId, build.id);
          return wasWritten(await this.buildMapper(build, jobs));
        } catch (error) {
          // surface API failures (e.g. a rate limit after all retries) instead of swallowing them
          if (isRateLimitError(error)) {
            console.error(`Fetching jobs for build #${build.id} aborted by GitHub rate limit — retry later`);
          } else {
            log('getPipelineJobs with id %o failed: %o', build.id, error);
          }
          return false;
        }
      } finally {
        // @ts-expect-error function dynamically loaded, therefore not known by code highlighting
        this.reporter.finishBuild();
      }
    };

    // split into batches, so a rate limited run makes progress in bounded chunks; requests inside a batch are pooled
    const results: boolean[] = [];
    for (let i = 0; i < builds.length; i += GITHUB_BATCH_SIZE) {
      const batch = builds.slice(i, i + GITHUB_BATCH_SIZE);
      results.push(...(await this.limitGitHubRequests(batch.map((build) => () => runBuild(build)))));
    }
    return results;
  }

  isStopping() {
    return this.stopping;
  }

  stop() {
    this.stopping = true;
  }
}

export default CIIndexer;
