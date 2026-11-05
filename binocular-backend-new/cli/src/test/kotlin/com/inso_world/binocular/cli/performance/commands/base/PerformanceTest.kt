package com.inso_world.binocular.cli.performance.commands.base

import com.inso_world.binocular.cli.base.AbstractCliIntegrationTest
import com.inso_world.binocular.core.integration.base.InfrastructureDataSetup
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired

internal class PerformanceTest : AbstractCliIntegrationTest() {
    @Autowired
    private lateinit var testDataSetupService: InfrastructureDataSetup

    @BeforeEach
    fun setup() {
        testDataSetupService.teardown()
    }

    @AfterEach
    fun cleanup() {
        testDataSetupService.teardown()
    }
}
