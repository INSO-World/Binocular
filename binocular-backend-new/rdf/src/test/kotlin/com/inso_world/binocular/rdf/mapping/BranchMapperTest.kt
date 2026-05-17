package com.inso_world.binocular.rdf.mapping

import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BranchMapperTest {

    @Test
    fun `maps branches to bio MutableLabel with pointsTo commit`() {
        val model = ModelFactory.createDefaultModel()
        model.setNsPrefix("bio", RdfNamespaces.BIO)

        val branches = listOf(
            mapOf(
                "_key" to "506",
                "branch" to "159",
                "latestCommit" to "ab4ae24df323c40d58515e26ee14045999b745bf"
            ),
            mapOf(
                "_key" to "510",
                "branch" to "2888",
                "latestCommit" to "0f19849be9b57d56285a57ea31f3622f8ddecdf6"
            )
        )

        val commitShaToUri = mapOf(
            "ab4ae24df323c40d58515e26ee14045999b745bf" to "${RdfNamespaces.INST}commit/some_key1",
            "0f19849be9b57d56285a57ea31f3622f8ddecdf6" to "${RdfNamespaces.INST}commit/some_key2"
        )

        BranchMapper(commitShaToUri).map(branches, model)

        val branchType = model.getResource("${RdfNamespaces.BIO}MutableLabel")
        val branchResources = model.listResourcesWithProperty(RDF.type, branchType).toList()
        assertEquals(2, branchResources.size, "Should have 2 branch resources")

        val branch1 = model.getResource("${RdfNamespaces.INST}branch/506")
        assertNotNull(branch1)
        assertEquals("159",
            branch1.getProperty(model.getProperty(RdfNamespaces.BIO, "name"))?.string)

        val pointsToProp = model.getProperty(RdfNamespaces.BIO, "pointsTo")
        val pointsToRel = branch1.getProperty(pointsToProp)
        assertNotNull(pointsToRel, "Branch should have pointsTo relationship")
    }
}
