'use strict';

import Build from '../../models/models/Build';
import debug from 'debug';

const log = debug('idx:ci:indexer');

class CIIndexer {
  constructor(progressReporter, controller, projectId, createBuildArtifactHandler) {
    this.reporter = progressReporter;
    this.controller = controller;
    this.projectId = projectId;
    this.stopping = false;

    if (typeof createBuildArtifactHandler !== 'function') {
      throw new Error('createBuildArtifactHandler must hold a build mapping function!');
    }
    this.buildMapper = createBuildArtifactHandler;
  }

  index() {
    let omitCount = 0;
    let persistCount = 0;

    return Promise.resolve(this.projectId)
      .then((projectId) => {
        let pipelinesRequest;
        try {
          pipelinesRequest = this.controller.getPipelines(projectId);
        } catch (error) {
          // exception can be thrown if the server is unreachable
          console.error(`fetching ${projectId} failed because of following error: ${error.toString()}`);
        }

        if (!pipelinesRequest) {
          return Promise.resolve();
        }
        return pipelinesRequest.then((pipelines) => {
          this.reporter.setBuildCount(pipelines.length);
          return pipelines.map((pipeline) => {
            pipeline.id = pipeline.id.toString();
            // TODO i think this won't work. `findOneById` checks if there is a build in the db with `_id === pipeline.id`,
            //  but we want to find a build with `id === pipeline.id`
            return Build.findOneById(pipeline.id)
              .then((existingBuild) => {
                if (
                  !this.stopping &&
                  (!existingBuild || new Date(existingBuild.updatedAt).getTime() < new Date(pipeline.updatedAt).getTime())
                ) {
                  log(`Processing build #${pipeline.id} [${persistCount + omitCount}]`);

                  /*Only necessary fot GitHub.
                  GitLab already requests all jobs as part of the pipelines gwl query
                  All of this can be removed once GitHub supports GraphQl for Workflows.*/
                  return Promise.resolve(this.controller.getPipelineJobs(projectId, pipeline.id))
                    .then((jobs) => {
                      if (jobs === 'gitlab') {
                        const artifactPromises = pipeline.jobs.edges.flatMap((jobEdge) => {
                          return jobEdge.node.artifacts.nodes.map((artifact) => {
                            if (artifact.name === 'jacoco.zip') {
                              const projectIdNumber = pipeline.project.id.slice(pipeline.project.id.lastIndexOf('/') + 1);
                              const jobID = jobEdge.node.id.slice(jobEdge.node.id.lastIndexOf('/') + 1);

                              return this.controller.downloadJacocoArtifact(projectIdNumber, jobID).then((xmlContent) => {
                                return {
                                  ...artifact,
                                  created_at: jobEdge.node.finishedAt,
                                  xmlContent: xmlContent,
                                }; // Return a promise with xml content for jacoco artifacts
                              });
                            } else {
                              return Promise.resolve(artifact); // Return a promise for other artifacts
                            }
                          });
                        });

                        return Promise.all(artifactPromises).then((pipelineArtifacts) => {
                          return this.buildMapper(
                            pipeline,
                            pipeline.jobs.edges.map((edge) => edge.node),
                            pipelineArtifacts,
                          );
                        });
                      } else {
                        return this.controller
                          .getPipelineArtifacts(projectId, pipeline.id)
                          .then((artifacts) => this.buildMapper(pipeline, jobs, artifacts));
                      }
                    })
                    .then(() => {
                      persistCount++;
                    });
                } else {
                  log(`Skipping build #${pipeline.id} [${persistCount + omitCount}]`);
                  omitCount++;
                }
              })
              .then(() => {
                this.reporter.finishBuild();
              });
          });
        });
      })
      .then(function (resp) {
        log('Persisted %d new builds (%d already present)', persistCount, omitCount);
        if (resp === false) {
          return Promise.resolve(resp);
        } else {
          return Promise.all(resp);
        }
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
