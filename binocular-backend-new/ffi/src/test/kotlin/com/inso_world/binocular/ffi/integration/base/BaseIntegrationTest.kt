package com.inso_world.binocular.ffi.integration.base

import org.junit.jupiter.api.Tag

/**
 * Base class for all integration tests.
 * Provides common functionality and configuration for integration tests.
 * Uses Spring's test context framework for integration testing.
 */
//@ExtendWith(SpringExtension::class)
@Tag("integration")
abstract class BaseIntegrationTest {
  // Add common setup and utilities for integration tests here
}
