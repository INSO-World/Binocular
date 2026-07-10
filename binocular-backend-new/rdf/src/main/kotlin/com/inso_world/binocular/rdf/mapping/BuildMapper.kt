package com.inso_world.binocular.rdf.mapping

import org.apache.jena.rdf.model.Model
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Component

@Component
class BuildMapper {
    fun map(
        builds: List<Map<String, Any?>>,
        model: Model
    ) {
        val buildType = model.getResource("${RdfNamespaces.CI_GHA}WorkflowRun")
        val jobType = model.getResource("${RdfNamespaces.CI_GHA}Job")
        val statusProp = model.getProperty(RdfNamespaces.BIO, "status")
        val durationProp = model.getProperty(RdfNamespaces.BIO, "duration")
        val createdAtProp = model.getProperty(RdfNamespaces.BIO, "createdAt")
        val startedAtProp = model.getProperty(RdfNamespaces.BIO, "startedAt")
        val finishedAtProp = model.getProperty(RdfNamespaces.BIO, "finishedAt")
        val jobNameProp = model.getProperty(RdfNamespaces.BIO, "jobName")
        val hasJobProp = model.getProperty(RdfNamespaces.BIO, "hasJob")

        builds.forEach { build ->
            val key = build["_key"] as String
            val uri = "${RdfNamespaces.INST}build/$key"
            val buildResource = model.createResource(uri)
            buildResource.addProperty(RDF.type, buildType)

            (build["status"] as? String)?.let { status ->
                buildResource.addProperty(statusProp, status)
            }
            (build["duration"] as? Number)?.let { duration ->
                buildResource.addLiteral(durationProp, duration)
            }
            (build["createdAt"] as? String)?.let { createdAt ->
                buildResource.addProperty(createdAtProp, createdAt)
            }
            (build["startedAt"] as? String)?.let { startedAt ->
                buildResource.addProperty(startedAtProp, startedAt)
            }
            (build["finishedAt"] as? String)?.let { finishedAt ->
                buildResource.addProperty(finishedAtProp, finishedAt)
            }

            @Suppress("UNCHECKED_CAST")
            (build["jobs"] as? List<Map<String, Any?>>)?.forEach { job ->
                val jobId = (job["id"] as? String) ?: return@forEach
                val jobUri = "${RdfNamespaces.INST}job/$jobId"
                val jobResource = model.createResource(jobUri)
                jobResource.addProperty(RDF.type, jobType)

                (job["name"] as? String)?.let { name ->
                    jobResource.addProperty(jobNameProp, name)
                }
                (job["status"] as? String)?.let { jobStatus ->
                    jobResource.addProperty(statusProp, jobStatus)
                }
                (job["createdAt"] as? String)?.let { createdAt ->
                    jobResource.addProperty(createdAtProp, createdAt)
                }
                (job["finishedAt"] as? String)?.let { finishedAt ->
                    jobResource.addProperty(finishedAtProp, finishedAt)
                }

                buildResource.addProperty(hasJobProp, jobResource)
            }
        }
    }
}
