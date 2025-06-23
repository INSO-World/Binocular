package com.inso_world.binocular.ffi.integration

import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest
import com.inso_world.binocular.ffi.BinocularFfi
import com.inso_world.binocular.internal.BinocularException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Paths
import java.util.stream.Stream

internal class BinocularFfiTest : BaseFixturesIntegrationTest() {
    private val ffi: BinocularFfi = BinocularFfi()

    @ParameterizedTest
    @ValueSource(strings = [SIMPLE_REPO, OCTO_REPO, ADVANCED_REPO])
    fun findRepo_shouldBeFound(paths: String) {
        val repo = ffi.findRepo("${FIXTURES_PATH}/$paths")

        assertAll(
            "Check $paths Repository",
            { assertThat(repo.commonDir).isNull() },
            // use Paths.get() here to avoid failing tests on Windows
            {
                assertThat(Paths.get(repo.gitDir).toString()).isEqualTo(
                    Paths.get("${FIXTURES_PATH}/$paths/.git").toString(),
                )
            },
            { assertThat(repo.workTree).isNotNull() },
            { assertThat(repo.workTree).isEqualTo("${FIXTURES_PATH}/$paths") },
        )
    }

    @ParameterizedTest
    @CsvSource(
        "$SIMPLE_REPO,b51199ab8b83e31f64b631e42b2ee0b1c7e3259a",
        "$OCTO_REPO,4dedc3c738eee6b69c43cde7d89f146912532cff",
        "$ADVANCED_REPO,379dc91fb055ba385b5e5446428ffbe38804fa99",
    )
    fun checkHeadCommits(
        path: String,
        headSha: String,
    ) {
        val repo = ffi.findRepo("${FIXTURES_PATH}/$path")
        val cmt = ffi.findCommit(repo, "HEAD")

        assertAll(
            "Check HEAD for Repo $path",
            { assertThat(cmt).isNotNull() },
            { assertThat(cmt).isEqualTo(headSha) },
        )
    }

    @ParameterizedTest
    // git rev-list --topo-order --all | wc -l
    @CsvSource(
        "$SIMPLE_REPO,HEAD,14",
        "$SIMPLE_REPO,b51199ab8b83e31f64b631e42b2ee0b1c7e3259a,14",
        "$SIMPLE_REPO,3d28b65c324cc8ee0bb7229fb6ac5d7f64129e90,13",
        "$SIMPLE_REPO,2403472fd3b2c4487f66961929f1e5895c5013e1,9", // git rev-list --topo-order 2403472fd3b2c4487f66961929f1e5895c5013e1 | wc -l
        "$SIMPLE_REPO,48a384a6a9188f376835005cd10fd97542e69bf7,1", // git rev-list --topo-order 2403472fd3b2c4487f66961929f1e5895c5013e1 | wc -l
        // OCTO
        "$OCTO_REPO,HEAD,19",
        "$OCTO_REPO,4dedc3c738eee6b69c43cde7d89f146912532cff,19", // HEAD
        "$OCTO_REPO,f556329d268afeb5e5298e37fd8bfb5ef2058a9d,15", // merge commit
        "$OCTO_REPO,bf51258d6da9aaca9b75e2580251539026b6246a,16", // octo3
        "$OCTO_REPO,d5d38cc858bd78498efbe0005052f5cb1fd38cb9,16", // octo2
        "$OCTO_REPO,42fbbe93509ed894cbbd61e4dbc07a440720c491,16", // octo1
        "$OCTO_REPO,d16fb2d78e3d867377c078a03aadc5aa34bdb408,17", // head of feature branch
        "$OCTO_REPO,3e15df55908eefdb720a7bc78065bcadb6b9e9cc,17", // head of bugfix branch
        // ADVANCED
        "$ADVANCED_REPO,HEAD,35",
        "$ADVANCED_REPO,379dc91fb055ba385b5e5446428ffbe38804fa99,35",
        "$ADVANCED_REPO,3c47b3a6ba6811bcefd21203809d79b2aa1b4b4b,34",
        "$ADVANCED_REPO,82df82770ef416d66c52b383281d21e03376fde0,29",
        "$ADVANCED_REPO,09aa9cb6a6322b4ba4506f168b944f0045b11cbe,4", // head of imported branch, right before merge commit
        "$ADVANCED_REPO,ed167f854e871a1566317302c158704f71f8d16c,1", // imported branch
        "$ADVANCED_REPO,5c81ebfb36467b8d1f70295adf2f9ae5a93a2c33,1", // initial
    )
    fun checkCorrectNumberOfCommits(path: String, startSha: String, noOfCommits: Int) {
        val repo = ffi.findRepo("${FIXTURES_PATH}/$path")
        val cmt = ffi.findCommit(repo, startSha)
        val hashes = ffi.traverse(repo, cmt, null)

        assertAll(
            "Check no. of Commits for Repo $path",
            { assertThat(hashes).isNotEmpty() },
            { assertThat(hashes).hasSize(noOfCommits) },
        )
    }

    @ParameterizedTest
//  @Timeout(value = 32, unit = TimeUnit.MILLISECONDS, threadMode = Timeout.ThreadMode.SAME_THREAD)
    // git rev-list --topo-order --all | wc -l
    @MethodSource("find_all_branches_data")
    fun find_all_branches_all_repos(
        path: String,
        localBranches: Collection<String>,
        remoteBranches: Collection<String>,
        noOfBranches: Int,
    ) {
        val repo = ffi.findRepo("${FIXTURES_PATH}/$path")
        val branches = ffi.findAllBranches(repo)

        assertAll(
            "Check no. of Commits for Repo $path",
            { assertThat(branches).isNotEmpty() },
            { assertThat(branches).hasSize(noOfBranches) },
            { assertThat(branches.map { it.name }).containsAll(localBranches) },
            { assertThat(branches.map { it.name }).containsAll(remoteBranches) },
        )
    }

    @ParameterizedTest
    @CsvSource(
        "$SIMPLE_REPO,master,14",
        "$SIMPLE_REPO,origin/master,13",
        // OCTO
        "$OCTO_REPO,master,19",
        "$OCTO_REPO,octo1,16",
        "$OCTO_REPO,octo2,16",
        "$OCTO_REPO,octo3,16",
        "$OCTO_REPO,bugfix,17",
        "$OCTO_REPO,feature,17",
        "$OCTO_REPO,imported,1",
        // ADVANCED
        "$ADVANCED_REPO,master,35",
        "$ADVANCED_REPO,imported,4",
    )
    fun find_all_commits_on_branch(
        path: String,
        branchName: String,
        noOfCommits: Int,
    ) {
        val repo = ffi.findRepo("${FIXTURES_PATH}/$path")
        val commits = ffi.traverseBranch(repo, branchName)

        assertAll(
            { assertThat(commits).hasSize(noOfCommits) },
        )
    }

    @Test
    fun find_all_commits_non_existing_branch() {
        val repo = ffi.findRepo("${FIXTURES_PATH}/${SIMPLE_REPO}")
        assertThrows<BinocularException.OperationFailed> {
            ffi.traverseBranch(repo, "branchName")
        }
    }

    companion object {
        @JvmStatic
        protected fun find_all_branches_data(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    SIMPLE_REPO,
                    listOf("refs/heads/master"),
                    listOf("refs/remotes/origin/master"),
                    2,
                ),
                Arguments.of(
                    OCTO_REPO,
                    listOf(
                        "refs/heads/bugfix",
                        "refs/heads/feature",
                        "refs/heads/imported",
                        "refs/heads/master",
                        "refs/heads/octo1",
                        "refs/heads/octo2",
                        "refs/heads/octo3",
                    ),
                    emptyList<String>(),
                    7,
                ),
                Arguments.of(
                    ADVANCED_REPO,
                    listOf(
                        "refs/heads/bugfix",
                        "refs/heads/extra",
                        "refs/heads/feature",
                        "refs/heads/imported",
                        "refs/heads/master",
                        "refs/heads/octo1",
                        "refs/heads/octo2",
                        "refs/heads/octo3",
                    ),
                    emptyList<String>(),
                    8,
                ),
            )
    }
}
