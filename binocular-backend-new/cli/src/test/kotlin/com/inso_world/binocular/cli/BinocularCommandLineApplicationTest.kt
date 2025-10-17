package com.inso_world.binocular.cli

import com.inso_world.binocular.cli.base.AbstractCliIntegrationTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.test.context.junit.jupiter.EnabledIf

internal class BinocularCommandLineApplicationTest : AbstractCliIntegrationTest() {
    @Test
    fun contextLoads() {
    }

    @Test
    @EnabledIf(
        expression = "#{environment.acceptsProfiles('postgres')}",
        reason = "🏋🏻‍ Because spring.profiles.active = postgres",
        loadContext = true
    )
    fun checkPgContainerIsRunning() {
        assertTrue(com.inso_world.binocular.infrastructure.sql.SqlTestConfig.pg.isRunning)
    }
}
