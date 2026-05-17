package com.inso_world.binocular.rdf.mapping

import org.apache.jena.rdf.model.Model
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Component

@Component
class MergeRequestMapper {

    fun map(mergeRequests: List<Map<String, Any?>>, model: Model) {
        val prType = model.getResource("${RdfNamespaces.ITS_GH}PullRequest")
        val prIdProp = model.getProperty(RdfNamespaces.BIO, "prId")
        val titleProp = model.getProperty(RdfNamespaces.BIO, "title")
        val statusProp = model.getProperty(RdfNamespaces.BIO, "prStatus")
        val createdAtProp = model.getProperty(RdfNamespaces.BIO, "createdAt")
        val closedAtProp = model.getProperty(RdfNamespaces.BIO, "closedAt")

        mergeRequests.forEach { mr ->
            val key = mr["_key"] as String
            val uri = "${RdfNamespaces.INST}mr/$key"
            val resource = model.createResource(uri)
            resource.addProperty(RDF.type, prType)

            (mr["iid"] as? Number)?.let { iid ->
                resource.addProperty(prIdProp, iid.toString())
            }
            (mr["title"] as? String)?.let { title ->
                resource.addProperty(titleProp, title)
            }
            (mr["state"] as? String)?.let { state ->
                resource.addProperty(statusProp, state)
            }
            (mr["createdAt"] as? String)?.let { createdAt ->
                resource.addProperty(createdAtProp, createdAt)
            }
            (mr["closedAt"] as? String)?.let { closedAt ->
                resource.addProperty(closedAtProp, closedAt)
            }
        }
    }
}
