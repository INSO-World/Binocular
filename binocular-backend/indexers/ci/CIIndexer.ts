import GitHub from '../../core/provider/github.ts';
import ProgressReporter from '../../utils/progress-reporter.ts';
import { GithubJob, GithubRun } from '../../types/GithubTypes.ts';
import Build from '../../models/models/Build.ts';
import ctx from '../../utils/context';
import debug from 'debug';
import { GitlabPipeline } from '../../types/GitlabTypes.ts';

const log = debug('idx:ci:indexer');

class CIIndexer {
  private controller: GitHub;
  private reporter: typeof ProgressReporter;
  private projectId: string;
  private buildMapper: (pipeline: GithubRun, jobs: GithubJob[]) => Promise<void>;
  private stopping: boolean;

  constructor(
    reporter: typeof ProgressReporter,
    controller: GitHub,
    projectId: string,
    createBuildArtifactHandler: (pipeline: GithubRun, jobs: GithubJob[]) => Promise<void>,
  ) {
    this.reporter = reporter;
    this.controller = controller;
    this.projectId = projectId;
    this.buildMapper = createBuildArtifactHandler;
    this.stopping = false;
  }

  index(): Promise<any> {
    log('indexing');
    let omitCount = 0;
    let persistCount = 0;
    return Promise.resolve(this.projectId).then((projectId: string) => {
      let pipelinesRequest: any;
      try {
        pipelinesRequest = this.controller.getPipelines(projectId);
      } catch (error: unknown) {
        // exception can be thrown if the server is unreachable
        console.error(`fetching ${projectId} failed because of following error: ${error}`);
      }
      // TODO potentially HttpStatus 500 on Github Site, happens sometimes
      return pipelinesRequest.then((pipelines: GithubRun[] | GitlabPipeline[]) => {
        // @ts-expect-error TODO function dynamically loaded, therefore not known by code highlighting
        this.reporter.setBuildCount(pipelines.length);
        return pipelines.map((pipeline) => {
          return Build.findOneBy('id', pipeline.id)
            .then((existingBuild) => {
              if (
                !this.stopping &&
                (!existingBuild ||
                  (existingBuild && existingBuild.data.jobs.length === 0) ||
                  // TODO write test for this case
                  (pipeline.updated_at && new Date(existingBuild?.data.updatedAt).getTime() < new Date(pipeline.updated_at).getTime()))
              ) {
                if (ctx.ciUrlProvider.provider === 'github') {
                  // read cli variable to decide if entity is saved with or without jobs
                  if (ctx.argv.jobs) {
                    log(`Processing build #${pipeline.id} [${persistCount + omitCount}]`);
                    return Promise.resolve(this.controller.getPipelineJobs(projectId, pipeline.id))
                      .then((jobs) => {
                        return this.buildMapper(pipeline, jobs);
                      })
                      .then(() => {
                        persistCount++;
                      });
                  } else {
                    log(`Processing build with no jobs #${pipeline.id} [${persistCount + omitCount}]`);
                    return this.buildMapper(pipeline, []).then(() => {
                      persistCount++;
                    });
                  }
                  // Gitlab does not need differentiation, because jobs are stored in the GraphQl for Workflows/Pipelines
                } else if (ctx.ciUrlProvider.provider === 'gitlab') {
                  log(`Processing build #${pipeline.id} [${persistCount + omitCount}]`);
                  return this.buildMapper(
                    pipeline,
                    pipeline.jobs.edges.map((edge) => edge.node),
                  );
                } else {
                  console.error('feature not implemented: CI not recognised');
                }
              } else {
                omitCount++;
                log(`Skipping build #${pipeline.id} [${persistCount + omitCount}]`);
              }
            })
            .then(() => {
              // @ts-expect-error TODO function dynamically loaded, therefore not known by code highlighting
              this.reporter.finishBuild();
            });
        });
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
