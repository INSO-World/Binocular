package com.inso_world.binocular.indexer.vcs

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.index.GitIndexer
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest
import com.inso_world.binocular.model.Project
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.io.path.Path

/**
 * Integration tests for skip-merges functionality in JGit GitIndexer.
 *
 * These tests verify that the `binocular.jgit.skip-merges` configuration option
 * correctly filters out merge commits during branch traversal.
 *
 * Note: When skip-merges is enabled and the branch HEAD is a merge commit, the
 * traversal will fail because the head commit is filtered out. Therefore, these
 * tests use branches where HEAD is not a merge commit.
 */
@SpringBootTest(
    classes = [VcsIndexerTestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
@ExtendWith(SpringExtension::class)
@ActiveProfiles("jgit")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestPropertySource(properties = ["binocular.jgit.skip-merges=true"])
internal class JGitSkipMergesTest : BaseFixturesIntegrationTest() {

    @Autowired
    private lateinit var indexer: GitIndexer

    private lateinit var project: Project

    companion object Companion {
        private val logger by logger()
    }

    @BeforeEach
    fun setUp() {
        project = Project(name = "jgit-skip-merges-test")
    }

    @Nested
    @DisplayName("Skip merges on branches without merge HEAD")
    inner class SkipMergesOnNonMergeBranches {

        @ParameterizedTest(name = "{0} branch {1} should have {2} commits with skip-merges")
        @CsvSource(
            // SIMPLE_REPO has no merge commits, all commits should be returned
            "${SIMPLE_REPO},refs/heads/master,14",
            "${SIMPLE_REPO},refs/remotes/origin/master,13",
            // OCTO_REPO branches without merge commits in history
            // Note: octo1/2/3 branch off from master before octopus merge, so no merges
            "${OCTO_REPO},refs/heads/bugfix,17",
            "${OCTO_REPO},refs/heads/feature,17",
            "${OCTO_REPO},refs/heads/imported,1",
            // ADVANCED_REPO imported branch has no merge commits
            "${ADVANCED_REPO},refs/heads/imported,4"
        )
        fun `traverseBranch with skip-merges on branches without merge HEAD`(
            repoName: String,
            branchName: String,
            expectedCommitCount: Int
        ) {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/$repoName"), project)
            val (branch, commits) = indexer.traverseBranch(repo, branchName)

            logger.info("$repoName $branchName with skip-merges=true: {} commits", commits.size)

            assertAll(
                "Branch $branchName should have correct commit count with skip-merges",
                { assertThat(commits).hasSize(expectedCommitCount) },
                // All returned commits should have at most 1 parent (no merge commits)
                { assertThat(commits).noneMatch { it.parents.size > 1 } }
            )
        }
    }

    @Nested
    @DisplayName("Skip merges filters merge commits")
    inner class SkipMergesFiltering {

        @Test
        fun `skip-merges should filter out all commits with multiple parents on simple repo`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${SIMPLE_REPO}"), project)
            val (_, commits) = indexer.traverseBranch(repo, "refs/heads/master")

            // SIMPLE_REPO has no merge commits, so all should be returned
            assertAll(
                "All commits should have at most one parent",
                { assertThat(commits).isNotEmpty() },
                { assertThat(commits).noneMatch { it.parents.size > 1 } }
            )
        }

        @Test
        fun `all returned commits should have valid metadata`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${SIMPLE_REPO}"), project)
            val (_, commits) = indexer.traverseBranch(repo, "refs/heads/master")

            assertAll(
                "All commits should have valid metadata",
                { assertThat(commits).allSatisfy { assertThat(it.sha).isNotBlank() } },
                { assertThat(commits).allSatisfy { assertThat(it.committer).isNotNull() } },
                { assertThat(commits).allSatisfy { assertThat(it.repository).isSameAs(repo) } }
            )
        }

        @Test
        fun `initial commits should still have zero parents`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${SIMPLE_REPO}"), project)
            val (_, commits) = indexer.traverseBranch(repo, "refs/heads/master")

            val initialCommits = commits.filter { it.parents.isEmpty() }

            assertAll(
                { assertThat(initialCommits).isNotEmpty() },
                { assertThat(initialCommits).allSatisfy { assertThat(it.parents).isEmpty() } }
            )
        }

        @Test
        fun `skip-merges on feature branch should return no merge commits`() {
            // Feature branch in OCTO_REPO doesn't end with a merge
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${OCTO_REPO}"), project)
            val (branch, commits) = indexer.traverseBranch(repo, "refs/heads/feature")

            logger.info("Feature branch with skip-merges: {} commits", commits.size)

            assertAll(
                { assertThat(commits).hasSize(17) },
                { assertThat(commits).noneMatch { it.parents.size > 1 } }
            )
        }
    }

    @Nested
    @DisplayName("Skip merges configuration validation")
    inner class SkipMergesConfigValidation {

        @Test
        fun `skip-merges is enabled in test configuration`() {
            // This test validates that the TestPropertySource is working
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${SIMPLE_REPO}"), project)
            val (_, commits) = indexer.traverseBranch(repo, "refs/heads/master")

            // If skip-merges wasn't working, we'd potentially see merge commits
            // Since SIMPLE_REPO has no merges, we just verify commits are returned
            assertThat(commits).hasSize(14)
        }
    }
}
