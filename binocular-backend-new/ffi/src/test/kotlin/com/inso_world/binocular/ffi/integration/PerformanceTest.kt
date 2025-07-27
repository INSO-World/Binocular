package com.inso_world.binocular.ffi.integration

import com.inso_world.binocular.ffi.BinocularFfi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertTimeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Duration
import java.util.concurrent.TimeUnit

class PerformanceTest {
    private val ffi: BinocularFfi = BinocularFfi()
    private val repo = ffi.findRepo("../../.git")

    @ParameterizedTest
    @CsvSource(
        "origin/feature/5,219,13",
        "origin/feature/31,592,15",
        "origin/feature/32,658,15",
        "origin/main,1881,65",
        "origin/develop,2092,65",
        "origin/feature/backend-new-gha,2238,65"
    )
    fun `test branch`(branch: String, expectedCommits: Int, timeout: Long) {
        val results =
            assertTimeout(
                Duration.ofMillis(timeout)
            ) { this.ffi.traverseBranch(repo, branch) }

        assertThat(results).hasSize(expectedCommits)
    }
}
