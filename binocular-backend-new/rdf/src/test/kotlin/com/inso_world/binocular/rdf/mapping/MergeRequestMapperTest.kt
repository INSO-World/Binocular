package com.inso_world.binocular.rdf.mapping

import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MergeRequestMapperTest {

    @Test
    fun `maps merge requests to its-gh PullRequest with properties`() {
        val model = ModelFactory.createDefaultModel()
        model.setNsPrefix("its-gh", RdfNamespaces.ITS_GH)
        model.setNsPrefix("bio", RdfNamespaces.BIO)

        val mrs = listOf(
            mapOf(
                "_key" to "23431",
                "iid" to 1,
                "title" to "Fix critical vulnerabilities reported by npm",
                "description" to "closes #2",
                "state" to "MERGED",
                "createdAt" to "2019-04-09T03:17:02Z",
                "closedAt" to "2019-04-09T13:25:59Z"
            ),
            mapOf(
                "_key" to "23572",
                "iid" to 3,
                "title" to "Add .travis.yml",
                "description" to "",
                "state" to "MERGED",
                "createdAt" to "2019-04-10T09:59:59Z",
                "closedAt" to "2019-04-12T09:08:28Z"
            )
        )

        MergeRequestMapper().map(mrs, model)

        val prType = model.getResource("${RdfNamespaces.ITS_GH}PullRequest")
        val prResources = model.listResourcesWithProperty(RDF.type, prType).toList()
        assertEquals(2, prResources.size, "Should have 2 PR resources")

        val pr1 = model.getResource("${RdfNamespaces.INST}mr/23431")
        assertNotNull(pr1)
        assertEquals("1", pr1.getProperty(model.getProperty(RdfNamespaces.BIO, "prId"))?.string)
        assertEquals("Fix critical vulnerabilities reported by npm",
            pr1.getProperty(model.getProperty(RdfNamespaces.BIO, "title"))?.string)
        assertEquals("MERGED", pr1.getProperty(model.getProperty(RdfNamespaces.BIO, "prStatus"))?.string)
    }
}
