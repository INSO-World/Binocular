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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.Base64
import kotlin.io.path.Path

/**
 * Baseline tests to verify merge commits are present when skip-merges is disabled.
 *
 * These tests verify that merge commits exist in the test fixtures and are
 * correctly returned when skip-merges=false.
 *
 * Note: Due to limitations in the fixture scripts, only octopus merges are created
 * properly (the `git_commit && git merge` pattern fails for regular merges).
 * - OCTO_REPO: 1 octopus merge commit (4 parents)
 * - ADVANCED_REPO: 3 merge commits (1 octopus + 2 regular merges via different path)
 *
 */
@SpringBootTest(
    classes = [VcsIndexerTestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
@ExtendWith(SpringExtension::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestPropertySource(properties = ["binocular.vcs.skip-merges=false"])
internal class MergeCommitsBaselineTest : BaseFixturesIntegrationTest() {
    @Autowired
    private lateinit var indexer: GitIndexer

    private lateinit var project: Project

    companion object Companion {
        private val logger by logger()
    }

    @BeforeEach
    fun setUp() {
        project = Project(name = "merge-baseline-test")
    }

    @Nested
    @DisplayName("Merge commits present in fixtures")
    inner class MergeCommitsPresent {
        @Test
        fun `OCTO_REPO master should contain octopus merge commit when skip-merges is false`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${OCTO_REPO}"), project)
            val (branch, commits) = indexer.traverseBranch(repo, "refs/heads/master")

            val mergeCommits = commits.filter { it.parents.size > 1 }
            val octopusMerges = commits.filter { it.parents.size > 2 }

            logger.info(
                "OCTO_REPO master with skip-merges=false: {} total commits, {} merge commits, {} octopus merges",
                commits.size,
                mergeCommits.size,
                octopusMerges.size,
            )

            assertAll(
                "OCTO_REPO master should have the octopus merge commit",
                { assertThat(commits).hasSize(19) },
                // The fixture creates only the octopus merge (4 parents)
                { assertThat(octopusMerges).hasSize(1) },
                { assertThat(branch.commits).hasSize(19) },
            )
        }

        @Test
        fun `ADVANCED_REPO master should contain merge commits when skip-merges is false`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${ADVANCED_REPO}"), project)
            val (branch, commits) = indexer.traverseBranch(repo, "refs/heads/master")

            val mergeCommits = commits.filter { it.parents.size > 1 }
            val octopusMerges = commits.filter { it.parents.size > 2 }

            logger.info(
                "ADVANCED_REPO master with skip-merges=false: {} total commits, {} merge commits, {} octopus merges",
                commits.size,
                mergeCommits.size,
                octopusMerges.size,
            )

            assertAll(
                "ADVANCED_REPO master should have merge commits",
                { assertThat(commits).hasSize(35) },
                // ADVANCED_REPO has more merge commits than OCTO_REPO
                { assertThat(mergeCommits).hasSizeGreaterThanOrEqualTo(1) },
                { assertThat(branch.commits).hasSize(35) },
            )
        }

        @Test
        fun `OCTO_REPO should have octopus merge with more than 2 parents`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${OCTO_REPO}"), project)
            val (_, commits) = indexer.traverseBranch(repo, "refs/heads/master")

            val octopusMerges = commits.filter { it.parents.size > 2 }

            logger.info("OCTO_REPO octopus merges: {}", octopusMerges.map { "${it.sha.take(7)}: ${it.parents.size} parents" })

            assertAll(
                "OCTO_REPO should have at least one octopus merge",
                { assertThat(octopusMerges).isNotEmpty() },
                {
                    assertThat(octopusMerges).anySatisfy { commit ->
                        // Octopus merge of octo1, octo2, octo3 should have 4 parents
                        assertThat(commit.parents.size).isEqualTo(4)
                    }
                },
            )
        }
    }

    @Nested
    @DisplayName("Merge commit structure verification")
    inner class MergeCommitStructure {
        @Test
        fun `merge commits should have valid parent references`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${OCTO_REPO}"), project)
            val (_, commits) = indexer.traverseBranch(repo, "refs/heads/master")

            val mergeCommits = commits.filter { it.parents.size > 1 }

            assertAll(
                "All merge commits should have valid parent references",
                {
                    mergeCommits.forEach { merge ->
                        assertThat(merge.parents)
                            .describedAs("Merge ${merge.sha.take(7)} should have valid parents")
                            .allSatisfy { parent ->
                                assertThat(parent.repository).isSameAs(repo)
                                assertThat(parent.sha).isNotBlank()
                            }
                    }
                },
            )
        }

        @Test
        fun `octopus merge commit should have correct structure`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${OCTO_REPO}"), project)
            val (_, commits) = indexer.traverseBranch(repo, "refs/heads/master")

            val octopusMerges = commits.filter { it.parents.size > 2 }

            assertThat(octopusMerges).hasSize(1)

            val octopusMerge = octopusMerges.first()

            assertAll(
                "Octopus merge should have correct structure",
                { assertThat(octopusMerge.parents).hasSize(4) },
                { assertThat(octopusMerge.author.name).isEqualTo("Alice") },
                { assertThat(octopusMerge.committer.name).isEqualTo("Alice") },
                {
                    // Message might be base64 encoded by FFI
                    val message =
                        try {
                            String(Base64.getDecoder().decode(octopusMerge.message ?: ""))
                        } catch (e: Exception) {
                            octopusMerge.message ?: ""
                        }
                    assertThat(message.lowercase()).contains("octopus")
                },
            )
        }
    }

    @Nested
    @DisplayName("Non-merge branches verification")
    inner class NonMergeBranches {
        @Test
        fun `SIMPLE_REPO should have no merge commits`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${SIMPLE_REPO}"), project)
            val (_, commits) = indexer.traverseBranch(repo, "refs/heads/master")

            val mergeCommits = commits.filter { it.parents.size > 1 }

            logger.info(
                "SIMPLE_REPO master: {} total commits, {} merge commits",
                commits.size,
                mergeCommits.size,
            )

            assertAll(
                "SIMPLE_REPO should have no merge commits",
                { assertThat(commits).hasSize(14) },
                { assertThat(mergeCommits).isEmpty() },
            )
        }

        @Test
        fun `feature branch should have no merge commits`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${OCTO_REPO}"), project)
            val (_, commits) = indexer.traverseBranch(repo, "refs/heads/feature")

            val mergeCommits = commits.filter { it.parents.size > 1 }

            assertAll(
                "Feature branch should have no merge commits",
                { assertThat(commits).hasSize(17) },
                { assertThat(mergeCommits).isEmpty() },
            )
        }
    }
}
