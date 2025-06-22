package com.inso_world.binocular.cli.unit

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
@Tag("unit")
abstract class BaseUnitTest {
    /**
     * Setup method that runs before each test.
     * Override this method in test classes to add custom setup.
     */
    @BeforeEach
    open fun setUp() {
        // Common setup code can be added here
    }
}
