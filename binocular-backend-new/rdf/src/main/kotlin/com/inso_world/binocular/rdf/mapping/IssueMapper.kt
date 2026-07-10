package com.inso_world.binocular.rdf.mapping

import org.apache.jena.rdf.model.Model
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Component

@Component
class IssueMapper {
    fun map(
        issues: List<Map<String, Any?>>,
        model: Model
    ) {
        val issueType = model.getResource("${RdfNamespaces.ITS_GH}Issue")
        val issueIdProp = model.getProperty(RdfNamespaces.BIO, "issueId")
        val titleProp = model.getProperty(RdfNamespaces.BIO, "title")
        val descriptionProp = model.getProperty(RdfNamespaces.BIO, "description")
        val statusProp = model.getProperty(RdfNamespaces.BIO, "issueStatus")
        val createdAtProp = model.getProperty(RdfNamespaces.BIO, "createdAt")
        val closedAtProp = model.getProperty(RdfNamespaces.BIO, "closedAt")

        issues.forEach { issue ->
            val key = issue["_key"] as String
            val uri = "${RdfNamespaces.INST}issue/$key"
            val resource = model.createResource(uri)
            resource.addProperty(RDF.type, issueType)

            (issue["iid"] as? Number)?.let { iid ->
                resource.addProperty(issueIdProp, iid.toString())
            }
            (issue["title"] as? String)?.let { title ->
                resource.addProperty(titleProp, title)
            }
            (issue["description"] as? String)?.let { desc ->
                resource.addProperty(descriptionProp, desc)
            }
            (issue["state"] as? String)?.let { state ->
                resource.addProperty(statusProp, state)
            }
            (issue["createdAt"] as? String)?.let { createdAt ->
                resource.addProperty(createdAtProp, createdAt)
            }
            (issue["closedAt"] as? String)?.let { closedAt ->
                resource.addProperty(closedAtProp, closedAt)
            }
        }
    }
}
