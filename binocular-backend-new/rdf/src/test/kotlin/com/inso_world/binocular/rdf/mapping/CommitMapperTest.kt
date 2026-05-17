package com.inso_world.binocular.rdf.mapping

import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.ResourceFactory
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CommitMapperTest {

    @Test
    fun `maps commits to vcs-git Commit RDF triples`() {
        val model = ModelFactory.createDefaultModel()
        model.setNsPrefix("bio", RdfNamespaces.BIO)
        model.setNsPrefix("vcs-git", RdfNamespaces.VCS_GIT)

        val commits = listOf(
            mapOf(
                "_key" to "796",
                "sha" to "945935697466022f2ddf7b1ea4f8cf9587b12a18",
                "date" to "2016-11-16T13:22:07.000Z",
                "message" to "Initial commit\n\nBasic setup"
            ),
            mapOf(
                "_key" to "893",
                "sha" to "2c58aacb573d02bf41c5e4930210264db31b8b7b",
                "date" to "2016-11-16T13:30:59.000Z",
                "message" to "Added support for yarn"
            )
        )

        CommitMapper().map(commits, model)

        val commitType = model.getResource("${RdfNamespaces.VCS_GIT}Commit")
        val commitResources = model.listResourcesWithProperty(RDF.type, commitType).toList()
        assertEquals(2, commitResources.size, "Should have 2 commit resources")

        // Check first commit properties
        val commit1 = model.getResource("${RdfNamespaces.INST}commit/796")
        assertNotNull(commit1)
        assertEquals("945935697466022f2ddf7b1ea4f8cf9587b12a18",
            commit1.getProperty(model.getProperty(RdfNamespaces.BIO, "identifier")).string)
        assertEquals("Initial commit\n\nBasic setup",
            commit1.getProperty(model.getProperty(RdfNamespaces.BIO, "message")).string)
    }
}
