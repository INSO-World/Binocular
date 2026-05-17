package com.inso_world.binocular.rdf.mapping

import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class IssueMapperTest {

    @Test
    fun `maps GitHub issues to its-gh Issue with properties`() {
        val model = ModelFactory.createDefaultModel()
        model.setNsPrefix("its-gh", RdfNamespaces.ITS_GH)
        model.setNsPrefix("bio", RdfNamespaces.BIO)

        val issues = listOf(
            mapOf(
                "_key" to "11011",
                "iid" to 2,
                "title" to "Fix critical vulnerabilities reported by npm",
                "description" to "",
                "state" to "CLOSED",
                "createdAt" to "2019-04-09T05:28:25Z",
                "closedAt" to "2019-04-10T00:58:29Z"
            ),
            mapOf(
                "_key" to "11035",
                "iid" to 5,
                "title" to "Code Clone Visualization",
                "description" to "",
                "state" to "OPEN",
                "createdAt" to "2019-07-19T12:12:29Z",
                "closedAt" to null
            )
        )

        IssueMapper().map(issues, model)

        val issueType = model.getResource("${RdfNamespaces.ITS_GH}Issue")
        val issueResources = model.listResourcesWithProperty(RDF.type, issueType).toList()
        assertEquals(2, issueResources.size, "Should have 2 issue resources")

        val issue1 = model.getResource("${RdfNamespaces.INST}issue/11011")
        assertNotNull(issue1)
        assertEquals("2", issue1.getProperty(model.getProperty(RdfNamespaces.BIO, "issueId"))?.string)
        assertEquals("Fix critical vulnerabilities reported by npm",
            issue1.getProperty(model.getProperty(RdfNamespaces.BIO, "title"))?.string)
        assertEquals("CLOSED", issue1.getProperty(model.getProperty(RdfNamespaces.BIO, "issueStatus"))?.string)
    }
}
