package com.inso_world.binocular.cli.commands

import com.inso_world.binocular.cli.service.BranchService
import com.inso_world.binocular.core.service.SeonExportPort
import com.inso_world.binocular.core.service.ShaclValidationPort
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.command.annotation.Command
import org.springframework.shell.command.annotation.Option

@Command(
    command = ["export"],
    group = "Export Commands",
    description = "Commands for exporting repository and related data sources",
)
open class Export (
    @Autowired private val branchService: BranchService,
    private val expMapper: SeonExportPort,
    private val shaclValidator: ShaclValidationPort
) {
    companion object {
        private var logger: Logger = LoggerFactory.getLogger(Index::class.java)
    }

    @Command(command = ["project"])
    open fun commits(
        @Option(
            longNames = ["branch_id"],
            shortNames = ['b'],
            required = true,
            description = "ID of the branch.",
        ) branchId: String,
        @Option(
            longNames = ["repo_path"],
            shortNames = ['p'],
            required = true,
            description = "Path to the repository.",
        ) repoPath: String,
        @Option(
            longNames = ["verbose"],
            shortNames = ['v'],
            required = false,
            defaultValue = "false",
            description = "Output the full RDF SHACL validation report if validation fails."
        ) verbose: Boolean,
        @Option(
            longNames = ["export-all"],
            shortNames = ['e'],
            required = false,
            defaultValue = "false",
            description = "Disables export policy restrictions and exports the complete repository snapshot."
        ) exportAll: Boolean,
        @Option(
            longNames = ["include-content"],
            shortNames = ['i'],
            required = false,
            defaultValue = "false",
            description = "Disables export policy restrictions and exports complete contents of the files."
        ) includeContent: Boolean,
    ) {
        val exportData = this.branchService.getBranchExportData(
            branchId,
            repoPath,
            exportAll,
            includeContent)
        val jsonLdString = expMapper.map(exportData)

        logger.info("\n--- JSON-LD EXPORT OUTPUT (branch_id: $branchId) ---")
        println("$jsonLdString\n---------------------------------------------------")

        val report = shaclValidator.validate(jsonLdString)

        if (report.warnings.isNotEmpty()) {
            println("\n STYLE WARNINGS:")
            report.warnings.forEach { println("   $it") }
        }

        if (!report.conforms) {
            println("\n CRITICAL ERRORS:")
            report.criticalErrors.forEach { println("   $it") }

            if (verbose) {
                println("\n--- RAW RDF REPORT ---\n${report.rawRdf}")
            }
        } else {
            println("\n Data is valid!")
        }
    }
}
// The branch to use: branches/15385, has multiple children commits
// export project -b branches/15385 -p "D:/Binocular" --export-all
