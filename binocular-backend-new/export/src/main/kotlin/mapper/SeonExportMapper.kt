package mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.inso_world.binocular.core.service.SeonExportPort
import com.inso_world.binocular.model.BranchExportData
import org.springframework.stereotype.Service

@Service
class SeonExportMapper : SeonExportPort {

    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    private val jsonLdBuilder = JsonLdExportDocumentBuilder(objectMapper)

    override fun map(exportData: BranchExportData): String {
        val jsonLdDoc = jsonLdBuilder.buildBranchExportDocument(exportData)

        return objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(jsonLdDoc)
    }
}
