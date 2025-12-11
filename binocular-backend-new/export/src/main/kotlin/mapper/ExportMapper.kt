package mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.inso_world.binocular.model.BranchExportData
import org.springframework.stereotype.Service
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper


@Service
class ExportMapper {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    fun map(exportData: BranchExportData): String {
        val dataMap = objectMapper.convertValue(exportData, Map::class.java) as Map<String, Any>

        val jsonLdMap = mutableMapOf<String, Any>()

        jsonLdMap["@context"] = "https://schemas.inso-world.com/binocular/v1/contextTestForGit.jsonld"
        jsonLdMap["@id"] = "binocular:branch:${exportData.branchName}/export/${exportData.commitSha}"
        jsonLdMap["@type"] = "BranchExport"

        jsonLdMap.putAll(dataMap)

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonLdMap)
    }
}
