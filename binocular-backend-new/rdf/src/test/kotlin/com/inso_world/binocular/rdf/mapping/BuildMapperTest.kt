package com.inso_world.binocular.rdf.mapping

import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BuildMapperTest {
    @Test
    fun `maps builds to ci-gha WorkflowRun with Job nodes`() {
        val model = ModelFactory.createDefaultModel()
        model.setNsPrefix("ci-gha", RdfNamespaces.CI_GHA)
        model.setNsPrefix("bio", RdfNamespaces.BIO)

        val builds =
            listOf(
                mapOf(
                    "_key" to "43335",
                    "id" to "19760007940",
                    "status" to "success",
                    "duration" to 65,
                    "createdAt" to "2025-11-28T09:43:34.000Z",
                    "startedAt" to "2025-11-28T09:43:35.000Z",
                    "finishedAt" to "2025-11-28T09:44:40.000Z",
                    "jobs" to
                        listOf(
                            mapOf(
                                "id" to "56619486993",
                                "name" to "MaximilianZenz",
                                "status" to "success",
                                "createdAt" to "2025-11-28T09:43:35.000Z",
                                "finishedAt" to "2025-11-28T09:44:40.000Z"
                            )
                        )
                )
            )

        BuildMapper().map(builds, model)

        val buildType = model.getResource("${RdfNamespaces.CI_GHA}WorkflowRun")
        val buildResources = model.listResourcesWithProperty(RDF.type, buildType).toList()
        assertEquals(1, buildResources.size, "Should have 1 build resource")

        val build1 = model.getResource("${RdfNamespaces.INST}build/43335")
        assertNotNull(build1)
        assertEquals(
            "success",
            build1.getProperty(model.getProperty(RdfNamespaces.BIO, "status"))?.string
        )

        // Check job exists
        val jobType = model.getResource("${RdfNamespaces.CI_GHA}Job")
        val jobResources = model.listResourcesWithProperty(RDF.type, jobType).toList()
        assertEquals(1, jobResources.size, "Should have 1 job resource")

        val hasJobProp = model.getProperty(RdfNamespaces.BIO, "hasJob")
        val jobLinks = model.listResourcesWithProperty(hasJobProp).toList()
        assertEquals(1, jobLinks.size, "Should have 1 hasJob relationship")
    }
}
