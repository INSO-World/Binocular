package com.inso_world.binocular.rdf.mapping

import org.apache.jena.rdf.model.Model
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Component

@Component
class CommitMapper {

    fun map(commits: List<Map<String, Any?>>, model: Model) {
        val commitType = model.getResource("${RdfNamespaces.VCS_GIT}Commit")
        val identifierProp = model.getProperty(RdfNamespaces.BIO, "identifier")
        val messageProp = model.getProperty(RdfNamespaces.BIO, "message")
        val createdAtProp = model.getProperty(RdfNamespaces.BIO, "createdAt")

        commits.forEach { commit ->
            val key = commit["_key"] as String
            val uri = "${RdfNamespaces.INST}commit/$key"
            val resource = model.createResource(uri)
            resource.addProperty(RDF.type, commitType)

            (commit["sha"] as? String)?.let { sha ->
                resource.addProperty(identifierProp, sha)
            }
            (commit["message"] as? String)?.let { msg ->
                resource.addProperty(messageProp, msg)
            }
            (commit["date"] as? String)?.let { date ->
                resource.addProperty(createdAtProp, date)
            }
        }
    }
}
