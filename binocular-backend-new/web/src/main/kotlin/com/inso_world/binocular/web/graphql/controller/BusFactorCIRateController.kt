package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.core.service.usecase.GetBusFactorCIErrorRateMetric
import com.inso_world.binocular.model.metrics.BusFactorCIErrorRate
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.enums.Granularity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Controller
class BusFactorCIRateController(
    @Autowired private val busfactorService: GetBusFactorCIErrorRateMetric,
) {
    private var logger: Logger = LoggerFactory.getLogger(BuildController::class.java)

    @QueryMapping(name = "busFactor")
    fun findAll(
        @Argument repoPath: String,
        @Argument since: Long,
        @Argument until: Long,
        @Argument granularity: Granularity
    ): List<BusFactorCIErrorRate> {
        logger.info("Getting all BusFactorPoints...")

        val result = busfactorService.execute(repoPath, since, until, granularity)
        return result
    }
}
