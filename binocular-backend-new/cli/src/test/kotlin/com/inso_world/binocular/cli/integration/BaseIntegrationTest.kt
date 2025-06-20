package com.inso_world.binocular.cli.integration

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * Base class for all integration tests.
 * Provides common functionality and configuration for integration tests.
 * Uses Spring's test context framework for integration testing.
 */
@ExtendWith(SpringExtension::class)
@Tag("integration")
abstract class BaseIntegrationTest {
    // Add common setup and utilities for integration tests here
}
