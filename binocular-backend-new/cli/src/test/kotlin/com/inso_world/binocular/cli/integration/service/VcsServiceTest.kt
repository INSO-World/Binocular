package com.inso_world.binocular.cli.integration.service

import com.inso_world.binocular.cli.integration.service.base.BaseServiceTest
import com.inso_world.binocular.cli.service.VcsService
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.FileDiff
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.stream.Stream

internal class VcsServiceTest() : BaseServiceTest() {
    @Autowired
    private lateinit var vcsService: VcsService

    companion object {
        private fun createDummyCommit(sha: String): Commit {
            val dummyProject = Project(name = "dummy-project-$sha")
            val dummyRepo = Repository(localPath = "dummy-repo-$sha", project = dummyProject)
            val dummyDeveloper = Developer(
                name = "Dummy User",
                email = "dummy@test.com",
                repository = dummyRepo
            )
            val signature = Signature(
                developer = dummyDeveloper,
                timestamp = LocalDateTime.now().minusHours(1)
            )
            return Commit(
                sha = sha,
                message = null,
                authorSignature = signature,
                repository = dummyRepo,
            )
        }

        @JvmStatic
        protected fun simple_repo_diff_pairs(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    emptySet<Pair<Commit, Commit>>(),
                    0,
                ),
                Arguments.of(
                    run {
                        val cmtA = createDummyCommit("b51199ab8b83e31f64b631e42b2ee0b1c7e3259a")
                        val cmtb = createDummyCommit("3d28b65c324cc8ee0bb7229fb6ac5d7f64129e90")
                        return@run setOf(Pair(cmtA, cmtb))
                    },
                    1,
                ),
                Arguments.of(
                    run {
                        val cmtA = createDummyCommit("8f34ebee8f593193048f8bcbf848501bf2465865")
                        val cmtb = createDummyCommit("48a384a6a9188f376835005cd10fd97542e69bf7")
                        return@run setOf(Pair(cmtA, cmtb))
                    },
                    1,
                ),
                Arguments.of(
                    run {
                        val cmtb = createDummyCommit("48a384a6a9188f376835005cd10fd97542e69bf7")
                        return@run setOf(Pair(cmtb, null))
                    },
                    1,
                ),
            )
    }

    @ParameterizedTest
    @MethodSource("simple_repo_diff_pairs")
    fun `index diffs, simple repo`(
        pairs: Set<Pair<Commit, Commit>>,
        amount: Int
    ) {
        val result = vcsService.indexDiffs(simpleRepo, pairs)
        assertThat(result).hasSize(amount)
    }

    @Test
    fun `diff root with nothing, simple repo`() {
        val pairs = run {
            val cmtb = createDummyCommit("48a384a6a9188f376835005cd10fd97542e69bf7")
            return@run setOf(Pair(cmtb, null))
        }

        val result = vcsService.indexDiffs(simpleRepo, pairs)
        assertThat(result).hasSize(1)
        val diff = result.toList()[0]
        assertAll(
            { assertThat(diff.repository).isNotNull() },
            { assertThat(diff.repository).isEqualTo(simpleRepo) },
            { assertThat(diff.source.sha).isEqualTo("48a384a6a9188f376835005cd10fd97542e69bf7") },
            { assertThat(diff.target).isNull() },
            { assertThat(diff.files).hasSize(1) },
        )
        val file = diff.files.toList()[0]
        assertAll(
            { assertThat(file.stats.deletions).isEqualTo(0) },
            { assertThat(file.stats.additions).isEqualTo(1) },
            { assertThat(file.pathBefore).isNull() },
            { assertThat(file.pathAfter).isEqualTo("file1.txt") },
            { assertThat(file.change).isEqualTo(FileDiff.ChangeType.ADDITION) },
        )
    }

    @Test
    fun `diff Rename file1 txt to file1 renamed txt, simple repo`() {
        val pairs = run {
            val cmta = createDummyCommit("9976fc27929ff24a2e8618476e62ba5bf3619c91")
            val cmtb = createDummyCommit("f1719ed00c8e42b478dab3c01898e4af57ff0004")
            return@run setOf(Pair(cmta, cmtb))
        }

        val result = vcsService.indexDiffs(simpleRepo, pairs)
        assertThat(result).hasSize(1)
        val diff = result.toList()[0]
        assertThat(diff.files).hasSize(1)
        val file = diff.files.toList()[0]
        assertAll(
            { assertThat(file.stats.deletions).isEqualTo(0) },
            { assertThat(file.stats.additions).isEqualTo(0) },
            { assertThat(file.pathBefore).isEqualTo("file1.txt") },
            { assertThat(file.pathAfter).isEqualTo("file1-renamed.txt") },
            { assertThat(file.change).isEqualTo(FileDiff.ChangeType.REWRITE) },
        )
    }

    @Test
    fun `diff Delete file2 txt, simple repo`() {
        val pairs = run {
            val cmta = createDummyCommit("6fb5a9bc0fb9ea3ab5e9d85e3b30d25d22145f89")
            val cmtb = createDummyCommit("9976fc27929ff24a2e8618476e62ba5bf3619c91")
            return@run setOf(Pair(cmta, cmtb))
        }

        val result = vcsService.indexDiffs(simpleRepo, pairs)
        assertThat(result).hasSize(1)
        val diff = result.toList()[0]
        assertThat(diff.files).hasSize(1)
        val file = diff.files.toList()[0]
        assertAll(
            { assertThat(file.stats.deletions).isEqualTo(2) },
            { assertThat(file.stats.additions).isEqualTo(0) },
            { assertThat(file.pathBefore).isEqualTo("file2.txt") },
            { assertThat(file.pathAfter).isNull() },
            { assertThat(file.change).isEqualTo(FileDiff.ChangeType.DELETION) },
        )
    }
}
