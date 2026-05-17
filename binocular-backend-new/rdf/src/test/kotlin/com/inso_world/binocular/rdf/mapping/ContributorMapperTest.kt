package com.inso_world.binocular.rdf.mapping

import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ContributorMapperTest {

    @Test
    fun `maps users to bio Contributor with parsed email and name`() {
        val model = ModelFactory.createDefaultModel()
        model.setNsPrefix("bio", RdfNamespaces.BIO)

        val users = listOf(
            mapOf("_key" to "800", "gitSignature" to "Roman Decker <roman.decker@gmail.com>"),
            mapOf("_key" to "30245", "gitSignature" to "Johann Grabner <johann.grabner@inso.tuwien.ac.at>")
        )

        ContributorMapper().map(users, model)

        val contributorType = model.getResource("${RdfNamespaces.BIO}Contributor")
        val contributors = model.listResourcesWithProperty(RDF.type, contributorType).toList()
        assertEquals(2, contributors.size, "Should have 2 contributor resources")

        // Check first contributor
        val contrib1 = model.getResource("${RdfNamespaces.INST}user/800")
        assertNotNull(contrib1)
        assertEquals("Roman Decker",
            contrib1.getProperty(model.getProperty(RdfNamespaces.BIO, "name"))?.string)
        assertEquals("roman.decker@gmail.com",
            contrib1.getProperty(model.getProperty(RdfNamespaces.BIO, "email"))?.string)
    }

    @Test
    fun `deduplicates identical git signatures`() {
        val model = ModelFactory.createDefaultModel()
        model.setNsPrefix("bio", RdfNamespaces.BIO)

        val users = listOf(
            mapOf("_key" to "800", "gitSignature" to "Roman Decker <roman.decker@gmail.com>"),
            mapOf("_key" to "801", "gitSignature" to "Roman Decker <roman.decker@gmail.com>")
        )

        ContributorMapper().map(users, model)

        val contributorType = model.getResource("${RdfNamespaces.BIO}Contributor")
        val contributors = model.listResourcesWithProperty(RDF.type, contributorType).toList()
        assertEquals(1, contributors.size, "Should deduplicate same email")
    }
}
