package com.inso_world.binocular.rdf.mapping

import org.apache.jena.rdf.model.Model
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Component

@Component
class EdgeMapper {
    fun mapCommitsUsers(
        edges: List<Map<String, Any?>>,
        model: Model
    ) {
        val authorProp = model.getProperty(RdfNamespaces.BIO, "author")

        edges.forEach { edge ->
            val fromKey = extractKey(edge["_from"] as? String) ?: return@forEach
            val toKey = extractKey(edge["_to"] as? String) ?: return@forEach

            val commitUri = "${RdfNamespaces.INST}commit/$fromKey"
            val userUri = "${RdfNamespaces.INST}user/$toKey"

            val commit = model.getResource(commitUri)
            val user = model.getResource(userUri)

            commit.addProperty(authorProp, user)
        }
    }

    fun mapCommitsFiles(
        edges: List<Map<String, Any?>>,
        model: Model
    ) {
        val modifiedFileProp = model.getProperty(RdfNamespaces.BIO, "modifiedFile")
        val changedFileProp = model.getProperty(RdfNamespaces.BIO, "changedFile")
        val fileChangeType = model.getResource("${RdfNamespaces.BIO}FileChange")

        edges.forEach { edge ->
            val fromKey = extractKey(edge["_from"] as? String) ?: return@forEach
            val toKey = extractKey(edge["_to"] as? String) ?: return@forEach

            val commitUri = "${RdfNamespaces.INST}commit/$fromKey"
            val fileUri = "${RdfNamespaces.INST}file/$toKey"

            val commit = model.getResource(commitUri)
            val fileChange = model.createResource()
            fileChange.addProperty(RDF.type, fileChangeType)
            fileChange.addProperty(changedFileProp, model.getResource(fileUri))

            commit.addProperty(modifiedFileProp, fileChange)
        }
    }

    fun mapIssuesCommits(
        edges: List<Map<String, Any?>>,
        model: Model
    ) {
        val closedByRevisionProp = model.getProperty(RdfNamespaces.BIO, "closedByRevision")
        val containsCommitProp = model.getProperty(RdfNamespaces.BIO, "containsCommit")

        edges.forEach { edge ->
            val fromKey = extractKey(edge["_from"] as? String) ?: return@forEach
            val toKey = extractKey(edge["_to"] as? String) ?: return@forEach
            val closes = edge["closes"] as? Boolean ?: false

            val issueUri = "${RdfNamespaces.INST}issue/$fromKey"
            val commitUri = "${RdfNamespaces.INST}commit/$toKey"

            val issue = model.getResource(issueUri)
            val commit = model.getResource(commitUri)

            issue.addProperty(containsCommitProp, commit)

            if (closes) {
                issue.addProperty(closedByRevisionProp, commit)
            }
        }
    }

    fun mapCommitsModules(
        edges: List<Map<String, Any?>>,
        model: Model
    ) {
        val changedModuleProp = model.getProperty(RdfNamespaces.BIO, "changedModule")

        edges.forEach { edge ->
            val fromKey = extractKey(edge["_from"] as? String) ?: return@forEach
            val toKey = extractKey(edge["_to"] as? String) ?: return@forEach

            val commitUri = "${RdfNamespaces.INST}commit/$fromKey"
            val moduleUri = "${RdfNamespaces.INST}module/$toKey"

            val commit = model.getResource(commitUri)
            val module = model.getResource(moduleUri)

            commit.addProperty(changedModuleProp, module)
        }
    }

    fun mapCommitsBuilds(
        edges: List<Map<String, Any?>>,
        model: Model
    ) {
        val triggeredByProp = model.getProperty(RdfNamespaces.BIO, "triggeredBy")

        edges.forEach { edge ->
            val fromKey = extractKey(edge["_from"] as? String) ?: return@forEach
            val toKey = extractKey(edge["_to"] as? String) ?: return@forEach

            val commitUri = "${RdfNamespaces.INST}commit/$fromKey"
            val buildUri = "${RdfNamespaces.INST}build/$toKey"

            val build = model.getResource(buildUri)
            val commit = model.getResource(commitUri)

            build.addProperty(triggeredByProp, commit)
        }
    }

    fun mapModulesFiles(
        edges: List<Map<String, Any?>>,
        model: Model
    ) {
        // Note: This represents the extension property, not a named predicate
        // Skipping for now as per ontology design
    }

    fun mapModulesModules(
        edges: List<Map<String, Any?>>,
        model: Model
    ) {
        val containsModuleProp = model.getProperty(RdfNamespaces.BIO, "containsModule")

        edges.forEach { edge ->
            val fromKey = extractKey(edge["_from"] as? String) ?: return@forEach
            val toKey = extractKey(edge["_to"] as? String) ?: return@forEach

            val parentUri = "${RdfNamespaces.INST}module/$fromKey"
            val childUri = "${RdfNamespaces.INST}module/$toKey"

            val parent = model.getResource(parentUri)
            val child = model.getResource(childUri)

            parent.addProperty(containsModuleProp, child)
        }
    }

    private fun extractKey(id: String?): String? = id?.substringAfterLast("/")
}
