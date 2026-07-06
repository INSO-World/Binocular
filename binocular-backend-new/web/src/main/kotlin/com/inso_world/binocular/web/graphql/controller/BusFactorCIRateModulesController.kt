package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.web.usecases.GetBusFactorCIErrorRateMetric
import com.inso_world.binocular.model.metrics.BusFactorCIErrorRate
import com.inso_world.binocular.model.enums.Granularity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class BusFactorCIRateModulesController(
    @Autowired private val busFactorService: GetBusFactorCIErrorRateMetric,
) {
    private var logger: Logger = LoggerFactory.getLogger(BuildController::class.java)

    @QueryMapping(name = "busFactorCIErrorRateModules")
    fun findAll(
        @Argument repoPath: String,
        @Argument since: Long,
        @Argument until: Long,
        @Argument excludedAuthors: List<String>,
        @Argument neededModules: List<String>,
    ): List<BusFactorCIErrorRate> {
        logger.info("Getting all BusFactorCIRateModulesController...")

        val result = busFactorService.execute2(repoPath, since, until, excludedAuthors,neededModules)
        return result
    }
}
