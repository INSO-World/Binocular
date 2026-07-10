package com.inso_world.binocular.rdf.mapping

import org.apache.jena.rdf.model.Model
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Component

@Component
class BranchMapper(
    val commitShaToUri: Map<String, String> = emptyMap()
) {
    fun map(
        branches: List<Map<String, Any?>>,
        model: Model
    ) {
        val labelType = model.getResource("${RdfNamespaces.BIO}MutableLabel")
        val nameProp = model.getProperty(RdfNamespaces.BIO, "name")
        val pointsToProp = model.getProperty(RdfNamespaces.BIO, "pointsTo")

        branches.forEach { branch ->
            val key = branch["_key"] as String
            val uri = "${RdfNamespaces.INST}branch/$key"
            val resource = model.createResource(uri)
            resource.addProperty(RDF.type, labelType)

            (branch["branch"] as? String)?.let { branchName ->
                resource.addProperty(nameProp, branchName)
            }

            (branch["latestCommit"] as? String)?.let { sha ->
                commitShaToUri[sha]?.let { commitUri ->
                    val commitResource = model.getResource(commitUri)
                    resource.addProperty(pointsToProp, commitResource)
                }
            }
        }
    }
}
