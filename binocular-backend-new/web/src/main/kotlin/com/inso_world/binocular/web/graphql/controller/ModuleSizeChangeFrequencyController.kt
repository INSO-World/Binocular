package com.inso_world.binocular.web.graphql.controller

import com.inso_world.binocular.model.metrics.ModuleSizeChangeFrequency
import com.inso_world.binocular.web.usecases.GetModuleSizeChangeFrequencyMetric
import org.springframework.beans.factory.annotation.Autowired
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class ModuleSizeChangeFrequencyController(
    @Autowired private val service: GetModuleSizeChangeFrequencyMetric,
) {
    private val logger: Logger = LoggerFactory.getLogger(ModuleSizeChangeFrequencyController::class.java)

    @QueryMapping(name = "moduleSizeChangeFrequency")
    fun findAll(
        @Argument repoPath: String,
        @Argument since: Long,
        @Argument until: Long,
        @Argument neededModules: List<String>,
    ): List<ModuleSizeChangeFrequency> {
        logger.info("Getting module size / change frequency metric...")
        return service.execute(repoPath, since, until, neededModules)
    }
}
