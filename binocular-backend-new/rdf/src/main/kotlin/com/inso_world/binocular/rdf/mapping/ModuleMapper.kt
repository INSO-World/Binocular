package com.inso_world.binocular.rdf.mapping

import org.apache.jena.rdf.model.Model
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Component

@Component
class ModuleMapper {

    fun map(modules: List<Map<String, Any?>>, model: Model) {
        val moduleType = model.getResource("${RdfNamespaces.BIO}Module")
        val nameProp = model.getProperty(RdfNamespaces.BIO, "name")

        modules.forEach { module ->
            val key = module["_key"] as String
            val uri = "${RdfNamespaces.INST}module/$key"
            val resource = model.createResource(uri)
            resource.addProperty(RDF.type, moduleType)

            (module["path"] as? String)?.let { path ->
                resource.addProperty(nameProp, path)
            }
        }
    }
}
