package com.inso_world.binocular.cli.integration.service

import com.inso_world.binocular.cli.integration.service.base.BaseServiceTest
import com.inso_world.binocular.cli.service.FfiService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import java.util.stream.Stream

@Deprecated("legacy")
@Disabled
internal class FfiServiceTest(
    @Autowired private val ffiService: FfiService,
) : BaseServiceTest() {
    @ParameterizedTest
    @MethodSource("find_all_branches_data")
    fun `find all branches all repos`(
        path: String,
        localBranches: Collection<String>,
        remoteBranches: Collection<String>,
        noOfBranches: Int,
    ) {
        val repo = ffiService.findRepo("${FIXTURES_PATH}/$path")
        val branches = this.ffiService.findAllBranches(repo)

        assertAll(
            "Check no. of Commits for Repo $path",
            { assertThat(branches).isNotEmpty() },
            { assertThat(branches).hasSize(noOfBranches) },
            { assertThat(branches.map { it.name }).containsAll(localBranches) },
            { assertThat(branches.map { it.name }).containsAll(remoteBranches) },
        )
    }

    companion object {
        @JvmStatic
        protected fun find_all_branches_data(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    SIMPLE_REPO,
                    listOf("master"),
                    listOf("origin/master"),
                    2,
                ),
                Arguments.of(
                    OCTO_REPO,
                    listOf(
                        "bugfix",
                        "feature",
                        "imported",
                        "master",
                        "octo1",
                        "octo2",
                        "octo3",
                    ),
                    emptyList<String>(),
                    7,
                ),
                Arguments.of(
                    ADVANCED_REPO,
                    listOf(
                        "bugfix",
                        "extra",
                        "feature",
                        "imported",
                        "master",
                        "octo1",
                        "octo2",
                        "octo3",
                    ),
                    emptyList<String>(),
                    8,
                ),
            )
    }
}
