package com.inso_world.binocular.ffi.integration

import com.inso_world.binocular.ffi.GixIndexer
import com.inso_world.binocular.model.Project
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.assertTimeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Duration
import kotlin.io.path.Path

@Disabled
class PerformanceTest {
    private val ffi: GixIndexer = GixIndexer()
    private val repo = ffi.findRepo(Path("../../.git"), project = Project(name = "binocular"))

    @ParameterizedTest
    @CsvSource(
        "origin/feature/5,219,13",
        "origin/feature/31,592,15",
        "origin/feature/32,658,15",
        "origin/main,1881,65",
        "origin/develop,2282,65",
        "feature/363,2350,65"
    )
    fun `test branch`(branch: String, expectedCommits: Int, timeout: Long) {
        val results =
            assertTimeout(
                Duration.ofMillis(timeout)
            ) { this.ffi.traverseBranch(repo,branch) }

        assertThat(results.second).hasSize(expectedCommits)
    }
}
