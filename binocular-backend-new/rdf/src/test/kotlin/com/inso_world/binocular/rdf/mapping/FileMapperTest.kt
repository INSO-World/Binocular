package com.inso_world.binocular.rdf.mapping

import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FileMapperTest {

    @Test
    fun `maps files to bio File with path as name`() {
        val model = ModelFactory.createDefaultModel()
        model.setNsPrefix("bio", RdfNamespaces.BIO)

        val files = listOf(
            mapOf("_key" to "102319", "path" to ".pupilrc_ci"),
            mapOf("_key" to "10597", "path" to "package-lock.json")
        )

        FileMapper().map(files, model)

        val fileType = model.getResource("${RdfNamespaces.BIO}File")
        val fileResources = model.listResourcesWithProperty(RDF.type, fileType).toList()
        assertEquals(2, fileResources.size, "Should have 2 file resources")

        val file1 = model.getResource("${RdfNamespaces.INST}file/102319")
        assertNotNull(file1)
        assertEquals(".pupilrc_ci",
            file1.getProperty(model.getProperty(RdfNamespaces.BIO, "name"))?.string)
    }
}
