package mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.inso_world.binocular.model.BranchExportData
import org.springframework.stereotype.Service
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.inso_world.binocular.core.service.SeonExportPort


@Service
class SeonExportMapper: SeonExportPort {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    override fun map(exportData: BranchExportData): String {
        val dataMap = objectMapper.convertValue(exportData, Map::class.java) as Map<String, Any>

        val jsonLdMap = mutableMapOf<String, Any>()

        //TODO: FIX
        jsonLdMap["@context"] = "https://schemas.inso-world.com/binocular/v1/contextTestForGit.jsonld"
        jsonLdMap["@id"] = "binocular:branch:${exportData.branchName}/export/${exportData.commitSha}"
        jsonLdMap["@type"] = "BranchExport"

        jsonLdMap.putAll(dataMap)

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonLdMap)
    }
}
