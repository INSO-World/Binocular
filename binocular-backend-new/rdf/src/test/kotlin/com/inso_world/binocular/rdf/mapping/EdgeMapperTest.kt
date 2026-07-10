package com.inso_world.binocular.rdf.mapping

import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EdgeMapperTest {
    @Test
    fun `maps commits-users edges to bio author relationships`() {
        val model = ModelFactory.createDefaultModel()
        model.setNsPrefix("bio", RdfNamespaces.BIO)

        // Pre-populate model with commit and user resources
        val commitUri = "${RdfNamespaces.INST}commit/796"
        val userUri = "${RdfNamespaces.INST}user/800"
        model.createResource(commitUri)
        model.createResource(userUri)

        val commitUserEdges =
            listOf(
                mapOf("_from" to "commits/796", "_to" to "users/800"),
                mapOf("_from" to "commits/893", "_to" to "users/800")
            )

        EdgeMapper().mapCommitsUsers(commitUserEdges, model)

        val authorProp = model.getProperty(RdfNamespaces.BIO, "author")
        val authorRels = model.listResourcesWithProperty(authorProp).toList()
        assertEquals(2, authorRels.size, "Should have 2 author relationships")
    }

    @Test
    fun `maps issues-commits with closes flag to appropriate relationships`() {
        val model = ModelFactory.createDefaultModel()
        model.setNsPrefix("bio", RdfNamespaces.BIO)

        // Pre-populate model with issue and commit resources
        val issueUri = "${RdfNamespaces.INST}issue/11011"
        val commit796Uri = "${RdfNamespaces.INST}commit/796"
        val commit893Uri = "${RdfNamespaces.INST}commit/893"
        model.createResource(issueUri)
        model.createResource(commit796Uri)
        model.createResource(commit893Uri)

        val issueCommitEdges =
            listOf(
                mapOf("_from" to "issues/11011", "_to" to "commits/796", "closes" to true),
                mapOf("_from" to "issues/11011", "_to" to "commits/893", "closes" to false)
            )

        EdgeMapper().mapIssuesCommits(issueCommitEdges, model)

        val containsCommitProp = model.getProperty(RdfNamespaces.BIO, "containsCommit")
        val stmts = model.listStatements(null, containsCommitProp, null as String?).toList()
        assertEquals(2, stmts.size, "Should have 2 containsCommit triples")

        val closedByProp = model.getProperty(RdfNamespaces.BIO, "closedByRevision")
        val closedStmts = model.listStatements(null, closedByProp, null as String?).toList()
        assertEquals(1, closedStmts.size, "Should have 1 closedByRevision triple")
    }

    @Test
    fun `maps commits-modules edges to bio changedModule relationships`() {
        val model = ModelFactory.createDefaultModel()
        model.setNsPrefix("bio", RdfNamespaces.BIO)

        val commitUri = "${RdfNamespaces.INST}commit/796"
        val moduleUri = "${RdfNamespaces.INST}module/869"
        model.createResource(commitUri)
        model.createResource(moduleUri)

        val commitModuleEdges =
            listOf(
                mapOf("_from" to "commits/796", "_to" to "modules/869")
            )

        EdgeMapper().mapCommitsModules(commitModuleEdges, model)

        val changedModuleProp = model.getProperty(RdfNamespaces.BIO, "changedModule")
        val changedRels = model.listResourcesWithProperty(changedModuleProp).toList()
        assertEquals(1, changedRels.size, "Should have 1 changedModule relationship")
    }
}
