package com.inso_world.binocular.rdf.mapping

import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ModuleMapperTest {
    @Test
    fun `maps modules to bio Module with path as name`() {
        val model = ModelFactory.createDefaultModel()
        model.setNsPrefix("bio", RdfNamespaces.BIO)

        val modules =
            listOf(
                mapOf("_key" to "869", "path" to "."),
                mapOf("_key" to "968", "path" to "./lib")
            )

        ModuleMapper().map(modules, model)

        val moduleType = model.getResource("${RdfNamespaces.BIO}Module")
        val moduleResources = model.listResourcesWithProperty(RDF.type, moduleType).toList()
        assertEquals(2, moduleResources.size, "Should have 2 module resources")

        val mod1 = model.getResource("${RdfNamespaces.INST}module/869")
        assertNotNull(mod1)
        assertEquals(
            ".",
            mod1.getProperty(model.getProperty(RdfNamespaces.BIO, "name"))?.string
        )
    }
}
