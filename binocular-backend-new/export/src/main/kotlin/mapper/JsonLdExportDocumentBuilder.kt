package mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.inso_world.binocular.model.BranchExportData
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Builds a normalized JSON-LD export document from BranchExportData.
 *
 * The builder turns the input DTO into one JSON-LD artifact with a single @context
 * and a @graph containing separate nodes for the root export, child commits,
 * programmers, files, and file content versions.
 *
 * Its main responsibility is structural transformation and stable @id generation.
 * It does not fetch data or validate the result.
 */
class JsonLdExportDocumentBuilder(
    private val objectMapper: ObjectMapper
) {

    companion object {
        const val CONTEXT_URL = "https://schemas.inso-world.com/binocular/v1/contextTestForGit.jsonld"
        const val ID_BASE = "https://data.inso-world.com/id/binocular"
    }

    /**
     * Builds the full JSON-LD export artifact for one branch snapshot.
     *
     * The result is emitted as a normalized graph:
     * - one root BranchExport node
     * - separate ChildCommitDetail nodes
     * - separate Programmer nodes
     * - separate FileContent nodes
     * - separate Content/Version nodes
     *
     * References between nodes are represented via minted IRIs instead of nested objects.
     */
    fun buildBranchExportDocument(exportData: BranchExportData): Map<String, Any> {

        // Start from the DTO shape, then replace embedded objects and raw identifiers
        // with JSON-LD node references and minted IRIs.
        val rootId = mintBranchExportId(exportData.branchId, exportData.commitSha)

        val rootNode = toMutableMap(exportData)
        val childNodes = buildChildCommitNodes(exportData)
        val rootCommitterIri = mintProgrammerId(exportData.committerId)
        val programmerNodes = buildProgrammerNodes(exportData)
        val (fileNodes, versionNodes, packagedVersionIris) = buildFileAndVersionNodes(exportData)

        // Root node fields
        rootNode["_section"] = "root"
        rootNode["@id"] = rootId
        rootNode["@type"] = "BranchExport"
        rootNode["committerId"] = rootCommitterIri
        rootNode["childrenCommits"] = childNodes.map { it["@id"] as String }
        rootNode["fileContents"] = packagedVersionIris


        // One JSON-LD artifact, normalized internally
        return linkedMapOf(
            "@context" to CONTEXT_URL,
            "@graph" to (
                    listOf(rootNode) +
                    childNodes +
                    programmerNodes +
                    fileNodes +
                    versionNodes
            )
        )
    }

    /**
     * Builds file-related graph nodes for the export.
     *
     * Returns:
     * - fileNodes: nodes describing exported files
     * - versionNodes: nodes describing concrete blob/content versions
     * - packagedVersionIris: a flattened, de-duplicated list of version IRIs referenced by the root node
     *
     * Embedded DTO content entries are normalized into separate Version-like nodes so the
     * JSON-LD graph uses explicit identity-based references instead of nested structures.
     */
    private fun buildFileAndVersionNodes(
        exportData: BranchExportData
    ): Triple<List<MutableMap<String, Any?>>, List<MutableMap<String, Any?>>, List<String>> {

        val versionNodes = mutableListOf<MutableMap<String, Any?>>()
        val fileNodes = mutableListOf<MutableMap<String, Any?>>()
        val packagedVersionIris = linkedSetOf<String>()

        for (file in exportData.fileContents) {
            // Build File node (Artifact_CI-ish)
            val fileNode = linkedMapOf<String, Any?>(
                "@id" to mintFileId(file.filePath),
                "@type" to "FileContent",
                "_section" to "files",
                "filePath" to file.filePath
            )

            // Replace embedded content objects with Version IRIs
            val versionIrisForFile = mutableListOf<String>()

            for (c in file.content) {
                val blobId = c.id?.trim()
                if (blobId.isNullOrBlank()) continue

                val versionIri = mintVersionId(blobId)
                versionIrisForFile.add(versionIri)
                packagedVersionIris.add(versionIri)

                // Emit a version node for each export entry.
                // If strong de-duplication by blobId is required, add a seen-set here.
                val versionNode = linkedMapOf<String, Any?>(
                    "@id" to versionIri,
                    "@type" to "Content",
                    "_section" to "versions",
                    "id" to blobId,
                    "contentText" to (c.contentText ?: "")
                )
                versionNodes.add(versionNode)
            }

            fileNode["content"] = versionIrisForFile
            fileNodes.add(fileNode)
        }

        return Triple(fileNodes, versionNodes, packagedVersionIris.toList())
    }

    /**
     * Converts child commit DTO entries into separate JSON-LD graph nodes.
     *
     * Each child receives:
     * - a stable commit IRI
     * - a typed JSON-LD node representation
     * - a committer reference rewritten from raw id to Programmer IRI
     */
    private fun buildChildCommitNodes(exportData: BranchExportData): List<MutableMap<String, Any?>> {
        return exportData.childrenCommits.map { child ->
            val childNode = toMutableMap(child)

            val childId = mintChildCommitId(childNode)
            val committerIri = mintProgrammerId(child.committerId)

            childNode["committerId"] = committerIri
            childNode["@id"] = childId
            childNode["@type"] = "ChildCommitDetail"

            childNode
        }
    }

    private fun toMutableMap(value: Any): MutableMap<String, Any?> {
        return objectMapper.convertValue(value, MutableMap::class.java) as MutableMap<String, Any?>
    }

    private fun mintBranchExportId(branchId: String, commitSha: String): String {
        return "$ID_BASE/branch/${urlEncode(branchId)}/export/$commitSha"
    }

    private fun mintProgrammerId(committerId: String): String {
        return "$ID_BASE/programmer/${urlEncode(committerId)}"
    }

    private fun mintVersionId(blobId: String): String {
        return "$ID_BASE/version/${urlEncode(blobId)}"
    }

    private fun mintFileId(filePath: String): String {
        return "$ID_BASE/artifact/${urlEncode(filePath)}"
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

    /**
     * Builds one Programmer node per distinct committer referenced in the export.
     *
     * Both the root committer and child committers are collected so all person references
     * used in the graph resolve to explicit nodes.
     */
    private fun buildProgrammerNodes(exportData: BranchExportData): List<Map<String, Any>> {
        val allCommitters = buildSet {
            add(exportData.committerId)
            exportData.childrenCommits.forEach { add(it.committerId) }
        }

        return allCommitters.map { committerId ->
            linkedMapOf(
                "@id" to mintProgrammerId(committerId),
                "@type" to "Programmer",
                "programmerId" to committerId
            )
        }
    }

    private fun urlEncode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20")
}
