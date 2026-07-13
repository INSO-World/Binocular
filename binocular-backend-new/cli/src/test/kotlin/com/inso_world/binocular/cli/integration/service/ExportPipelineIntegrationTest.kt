package com.inso_world.binocular.cli.integration.service

import com.inso_world.binocular.cli.service.BranchService
import com.inso_world.binocular.core.service.SeonExportPort
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@DisplayName("Export pipeline evidence")
internal class ExportPipelineEvidenceTest @Autowired constructor(
    private val branchService: BranchService,
    private val exportPort: SeonExportPort,
) {

    @Test
    @DisplayName("Default export should encode omitted content explicitly")
    fun `should encode omitted content explicitly when include content is false`() {
        val dto = branchService.getBranchExportData(
            "branches/15385",
            "D:/Binocular",
            false,
            false
        )

        val omittedEntry = dto.fileContents
            .flatMap { file -> file.content.map { content -> file.filePath to content } }
            .first { (_, content) -> content.contentText == "Content omitted for export size" }

        val jsonLd = exportPort.map(dto)

        println("----- DTO OMITTED ENTRY -----")
        println("filePath=${omittedEntry.first}")
        println("blobId=${omittedEntry.second.id}")
        println("contentText=${omittedEntry.second.contentText}")
        println("-----------------------------")

        println("----- JSON-LD OMITTED MARKER PRESENT -----")
        println(jsonLd.contains("\"contentText\" : \"Content omitted for export size\""))
        println("------------------------------------------")

        assertAll(
            { assertThat(omittedEntry.first).isNotBlank() },
            { assertThat(omittedEntry.second.id).isNotBlank() },
            { assertThat(omittedEntry.second.contentText).isEqualTo("Content omitted for export size") },
            { assertThat(jsonLd).contains("\"contentText\" : \"Content omitted for export size\"") }
        )
    }
}
