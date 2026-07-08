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

/**
 * GraphQL controller for the per-module "Bus Factor vs. CI Error Rate" metric.
 *
 * It exposes a single query ("busFactorCIErrorRateModules") and simply forwards the
 * incoming arguments to [GetBusFactorCIErrorRateMetric.execute2].
 */
@Controller
class BusFactorCIRateModulesController(
    @Autowired private val busFactorService: GetBusFactorCIErrorRateMetric,
) {
    // Logger used to trace when the query is called.
    private var logger: Logger = LoggerFactory.getLogger(BuildController::class.java)

    /**
     * Handles the GraphQL query "busFactorCIErrorRateModules".
     *
     * @param repoPath        name of the repository to analyse
     * @param since           start of the CI time window (epoch millis, UTC)
     * @param until           end of the CI time window (epoch millis, UTC)
     * @param excludedAuthors git signatures of authors to treat as "gone" for the bus factor
     * @param neededModules   module paths to return; an empty list means "all modules"
     * @return one [BusFactorCIErrorRate] entry per module
     */
    @QueryMapping(name = "busFactorCIErrorRateModules")
    fun findAll(
        @Argument repoPath: String,
        @Argument since: Long,
        @Argument until: Long,
        @Argument excludedAuthors: List<String>,
        @Argument neededModules: List<String>,
    ): List<BusFactorCIErrorRate> {
        logger.info("Getting all BusFactorCIRateModulesController...")

        // Delegate everything to the use case (per-module variant) and return its result as-is.
        val result = busFactorService.execute2(repoPath, since, until, excludedAuthors, neededModules)
        return result
    }
}
