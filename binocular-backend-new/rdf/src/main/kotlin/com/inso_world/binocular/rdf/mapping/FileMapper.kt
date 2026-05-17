package com.inso_world.binocular.rdf.mapping

import org.apache.jena.rdf.model.Model
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Component

@Component
class FileMapper {

    fun map(files: List<Map<String, Any?>>, model: Model) {
        val fileType = model.getResource("${RdfNamespaces.BIO}File")
        val nameProp = model.getProperty(RdfNamespaces.BIO, "name")

        files.forEach { file ->
            val key = file["_key"] as String
            val uri = "${RdfNamespaces.INST}file/$key"
            val resource = model.createResource(uri)
            resource.addProperty(RDF.type, fileType)

            (file["path"] as? String)?.let { path ->
                resource.addProperty(nameProp, path)
            }
        }
    }
}
