package com.inso_world.binocular.cli.commands

import mapper.ExportMapper
import com.inso_world.binocular.cli.service.BranchService
import validation.SHACLValidator
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
    private val expMapper: ExportMapper,
    private val shaclValidator: SHACLValidator
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
    ) {
        val exportData = this.branchService.getBranchExportData(branchId)
        val jsonLdString = expMapper.map(exportData)

        logger.info("\n--- JSON-LD EXPORT OUTPUT (branch_id: $branchId) ---")
        println("$jsonLdString\n---------------------------------------------------")

        val conforms = shaclValidator.validate(jsonLdString)

        if (!conforms) {
            // The validator service logs the error details itself.
            logger.warn("Export data for branch $branchId did NOT pass SHACL validation.")
            // You could stop execution here by throwing an exception if invalid data is not allowed.
        } else {
            logger.info("Export data passed SHACL validation and is ready for use.")
        }
    }
}
// The branch to use: branches/15385, has multiple children commits
// export project -b branches/15385
