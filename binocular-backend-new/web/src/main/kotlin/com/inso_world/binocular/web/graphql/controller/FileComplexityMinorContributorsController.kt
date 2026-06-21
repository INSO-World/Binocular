package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.web.usecases.GetBusFactorCIErrorRateMetric
import com.inso_world.binocular.model.metrics.BusFactorCIErrorRate
import com.inso_world.binocular.model.enums.Granularity
import com.inso_world.binocular.model.metrics.FileComplexityMinorContributors
import com.inso_world.binocular.web.usecases.FileComplexityMinorContributorsMetric
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class FileComplexityMinorContributorsController(
    @Autowired private val fileComplexityService: FileComplexityMinorContributorsMetric,
) {
    private var logger: Logger = LoggerFactory.getLogger(BuildController::class.java)

    @QueryMapping(name = "fileComplexityMinorContributors")
    fun findAll(
        @Argument repoPath: String,
    ): List<FileComplexityMinorContributors> {
        logger.info("Getting all FileComplexityMinorContributors...")

        val result = fileComplexityService.execute(repoPath)
        return result
    }
}
