package mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.inso_world.binocular.model.BranchExportData
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class JsonLdExportDocumentBuilder(
    private val objectMapper: ObjectMapper
) {

    companion object {
        const val CONTEXT_URL = "https://schemas.inso-world.com/binocular/v1/contextTestForGit.jsonld"
        const val ID_BASE = "https://data.inso-world.com/id/binocular"
    }

    fun buildBranchExportDocument(exportData: BranchExportData): Map<String, Any> {
        val rootId = mintBranchExportId(exportData.branchName, exportData.commitSha)

        val rootNode = toMutableMap(exportData)
        val childNodes = buildChildCommitNodes(exportData)

        // Replace embedded child objects with IRI references
        rootNode["childrenCommits"] = childNodes.map { it["@id"] as String }

        // Root node identity + type
        rootNode["@id"] = rootId
        rootNode["@type"] = "BranchExport"

        // One JSON-LD artifact, normalized internally
        return linkedMapOf(
            "@context" to CONTEXT_URL,
            "@graph" to (listOf(rootNode) + childNodes)
        )
    }

    private fun buildChildCommitNodes(exportData: BranchExportData): List<MutableMap<String, Any?>> {
        return exportData.childrenCommits.map { child ->
            val childNode = toMutableMap(child)

            val childId = mintChildCommitId(childNode)
            childNode["@id"] = childId
            childNode["@type"] = "ChildCommitDetail"

            childNode
        }
    }

    private fun toMutableMap(value: Any): MutableMap<String, Any?> {
        return objectMapper.convertValue(value, MutableMap::class.java) as MutableMap<String, Any?>
    }

    private fun mintBranchExportId(branchName: String, commitSha: String): String {
        return "$ID_BASE/branch/${urlEncode(branchName)}/export/$commitSha"
    }

    /**
     * Uses child commit SHA if available (recommended, stable Git identity).
     * Falls back to internal commitId if SHA is not yet in ChildCommitDetail.
     */
    private fun mintChildCommitId(childNode: Map<String, Any?>): String {
        val commitSha = childNode["commitSha"]?.toString()?.trim()
        if (!commitSha.isNullOrBlank()) {
            return "$ID_BASE/commit/$commitSha"
        }

        val commitId = childNode["commitId"]?.toString()?.trim()
            ?: throw IllegalArgumentException(
                "ChildCommitDetail missing both commitSha and commitId; cannot mint @id"
            )

        return "$ID_BASE/commit-internal/${urlEncode(commitId)}"
    }

    private fun urlEncode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20")
}
