package com.inso_world.binocular.rdf.mapping

import org.apache.jena.rdf.model.Model
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Component

@Component
class ContributorMapper {

    fun map(users: List<Map<String, Any?>>, model: Model) {
        val contributorType = model.getResource("${RdfNamespaces.BIO}Contributor")
        val nameProp = model.getProperty(RdfNamespaces.BIO, "name")
        val emailProp = model.getProperty(RdfNamespaces.BIO, "email")

        val seenEmails = mutableSetOf<String>()

        users.forEach { user ->
            val gitSignature = user["gitSignature"] as? String ?: return@forEach
            val (name, email) = parseGitSignature(gitSignature) ?: return@forEach

            // Deduplicate by email
            if (seenEmails.contains(email)) return@forEach
            seenEmails.add(email)

            val key = user["_key"] as String
            val uri = "${RdfNamespaces.INST}user/$key"
            val resource = model.createResource(uri)
            resource.addProperty(RDF.type, contributorType)
            resource.addProperty(nameProp, name)
            resource.addProperty(emailProp, email)
        }
    }

    private fun parseGitSignature(signature: String): Pair<String, String>? {
        val regex = """^(.*?)\s*<(.+?)>$""".toRegex()
        val match = regex.find(signature) ?: return null
        val (name, email) = match.destructured
        return Pair(name.trim(), email.trim())
    }
}
