package com.inso_world.binocular.core.unit.base

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * Base class for all unit tests.
 * Provides common functionality and configuration for unit tests.
 *
 * Features:
 * - Mockito support with strict stubbing
 * - Common test utilities
 * - Clock for time-based testing
 * - Common test data
 */
@Tag("unit")
abstract class BaseUnitTest {
    /**
     * Fixed clock for time-based testing.
     * Default time is set to 2024-01-01T12:00:00Z
     */
    protected val fixedClock: Clock =
        Clock.fixed(
            Instant.parse("2024-01-01T12:00:00Z"),
            ZoneId.of("UTC"),
        )

    /**
     * Common test data and utilities can be added here
     */
    companion object {
        const val TEST_PROJECT_ID = 1L
        const val TEST_USER_ID = 1L
        const val TEST_GROUP_ID = 1L
        const val TEST_MERGE_REQUEST_ID = 1L
        const val TEST_ISSUE_ID = 1L
        const val TEST_NOTE_ID = 1L
        const val TEST_MILESTONE_ID = 1L
        const val TEST_LABEL_ID = 1L
        const val TEST_COMMIT_ID = "abc123"
        const val TEST_BRANCH_NAME = "main"
        const val TEST_TAG_NAME = "v1.0.0"
    }

    /**
     * Setup method that runs before each test.
     * Override this method in test classes to add custom setup.
     */
    @BeforeEach
    open fun setUp() {
        // Common setup code can be added here
    }

    /**
     * Utility method to create a test exception with a message.
     */
    protected fun createTestException(message: String): Exception = Exception(message)

    /**
     * Utility method to create a test runtime exception with a message.
     */
    protected fun createTestRuntimeException(message: String): RuntimeException = RuntimeException(message)

    /**
     * Utility method to create a test illegal argument exception with a message.
     */
    protected fun createTestIllegalArgumentException(message: String): IllegalArgumentException = IllegalArgumentException(message)

    /**
     * Utility method to create a test illegal state exception with a message.
     */
    protected fun createTestIllegalStateException(message: String): IllegalStateException = IllegalStateException(message)
}
