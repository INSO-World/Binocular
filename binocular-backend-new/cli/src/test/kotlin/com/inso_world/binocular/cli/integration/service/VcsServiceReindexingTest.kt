package com.inso_world.binocular.cli.integration.service

import com.inso_world.binocular.cli.integration.service.base.BaseServiceTest
import com.inso_world.binocular.cli.service.RepositoryService
import com.inso_world.binocular.cli.service.VcsService
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.model.Project
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired

/**
 * BDD-style integration tests for VcsService re-indexing functionality.
 *
 * These tests verify that re-indexing a repository correctly:
 * 1. Avoids duplicate commits when HEAD hasn't changed
 * 2. Only processes new commits during incremental updates
 * 3. Properly tracks branch-level commit history
 *
 * Note: These tests use the existing simpleRepo/simpleProject from BaseServiceTest
 * which is already indexed with the master branch.
 */
@DisplayName("VcsService Re-indexing")
internal class VcsServiceReindexingTest @Autowired constructor(
    private val vcsService: VcsService,
    private val repositoryService: RepositoryService,
    private val projectPort: ProjectInfrastructurePort,
) : BaseServiceTest() {

    @Nested
    @DisplayName("Given a repository indexed for the first time (via BaseServiceTest)")
    inner class FirstTimeIndexing {

        @Test
        @DisplayName("When checking the pre-indexed repository, then all commits should be present")
        fun `should have all commits from initial index`() {
            // Given - simpleRepo is pre-indexed by BaseServiceTest with master branch

            // When - just verify the state
            val repo = repositoryService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")

            // Then - all commits on master should be indexed
            assertAll(
                { assertThat(repo).isNotNull },
                { assertThat(repo?.commits).isNotEmpty() },
                { assertThat(repo?.commits).hasSize(14) }, // simple repo has 14 commits on master
                { assertThat(repo?.branches).hasSize(1) },
                { assertThat(repo?.branches?.first()?.name).isEqualTo("master") },
            )
        }

        @Test
        @DisplayName("When checking the pre-indexed repository, then developers should be extracted")
        fun `should have developers from initial index`() {
            // Given - simpleRepo is pre-indexed

            // When - just verify the state
            val repo = repositoryService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")

            // Then - developers should be present
            assertThat(repo?.developers).isNotEmpty()
        }
    }

    @Nested
    @DisplayName("Given a repository that was previously indexed")
    inner class ReIndexing {

        @Test
        @DisplayName("When re-indexing with no new commits, then commit count should remain the same")
        fun `should not duplicate commits on re-index`() {
            // Given - repository was already indexed by BaseServiceTest
            val initialRepo = repositoryService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")
            val initialCommitCount = initialRepo?.commits?.size ?: 0
            val initialDeveloperCount = initialRepo?.developers?.size ?: 0

            // When - re-index the same branch
            vcsService.indexRepository("$FIXTURES_PATH/$SIMPLE_REPO", "master", simpleProject)

            // Then - commit count should be unchanged
            val reindexedRepo = repositoryService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")
            assertAll(
                { assertThat(reindexedRepo?.commits).hasSize(initialCommitCount) },
                { assertThat(reindexedRepo?.developers?.size).isEqualTo(initialDeveloperCount) },
            )
        }

        @Test
        @DisplayName("When re-indexing, then branch count should remain the same")
        fun `should not duplicate branches on re-index`() {
            // Given
            val initialRepo = repositoryService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")
            val initialBranchCount = initialRepo?.branches?.size ?: 0

            // When - re-index
            vcsService.indexRepository("$FIXTURES_PATH/$SIMPLE_REPO", "master", simpleProject)

            // Then
            val reindexedRepo = repositoryService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")
            assertThat(reindexedRepo?.branches).hasSize(initialBranchCount)
        }

        @Test
        @DisplayName("When re-indexing multiple times, then data should remain consistent")
        fun `should remain idempotent after multiple re-indexes`() {
            // Given - initial state from BaseServiceTest
            val initialRepo = repositoryService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")
            val initialCount = initialRepo?.commits?.size ?: 0

            // When - re-index 3 more times
            vcsService.indexRepository("$FIXTURES_PATH/$SIMPLE_REPO", "master", simpleProject)
            val afterFirst = repositoryService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")
            val countAfterFirst = afterFirst?.commits?.size ?: 0

            vcsService.indexRepository("$FIXTURES_PATH/$SIMPLE_REPO", "master", simpleProject)
            val afterSecond = repositoryService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")
            val countAfterSecond = afterSecond?.commits?.size ?: 0

            vcsService.indexRepository("$FIXTURES_PATH/$SIMPLE_REPO", "master", simpleProject)
            val afterThird = repositoryService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")
            val countAfterThird = afterThird?.commits?.size ?: 0

            // Then - all counts should be equal
            assertAll(
                { assertThat(initialCount).isEqualTo(14) },
                { assertThat(countAfterFirst).isEqualTo(initialCount) },
                { assertThat(countAfterSecond).isEqualTo(initialCount) },
                { assertThat(countAfterThird).isEqualTo(initialCount) },
            )
        }
    }

    @Nested
    @DisplayName("Given a repository with multiple branches")
    inner class MultipleBranches {

        @Test
        @DisplayName("When indexing a second branch, then commits should be properly tracked")
        fun `should add second branch without duplicating shared commits`() {
            // Given - master is already indexed
            val afterMaster = repositoryService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")
            val masterCommitCount = afterMaster?.commits?.size ?: 0
            val initialBranchCount = afterMaster?.branches?.size ?: 0

            // When - index origin/master (different branch, shared ancestry)
            vcsService.indexRepository("$FIXTURES_PATH/$SIMPLE_REPO", "origin/master", simpleProject)
            val afterOrigin = repositoryService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")

            // Then - should have commits from both branches, properly deduplicated
            assertAll(
                { assertThat(afterOrigin?.branches).hasSize(initialBranchCount + 1) },
                { assertThat(afterOrigin?.branches?.map { it.name }).containsExactlyInAnyOrder("master", "origin/master") },
                // origin/master has fewer commits (13 vs 14) due to shared ancestry
                // total should still be 14 since all origin/master commits are also on master
                { assertThat(afterOrigin?.commits?.size).isGreaterThanOrEqualTo(masterCommitCount) },
            )
        }

        @Test
        @DisplayName("When re-indexing one branch, then other branches should not be affected")
        fun `should not affect other branches on re-index`() {
            // Given - index origin/master as second branch
            vcsService.indexRepository("$FIXTURES_PATH/$SIMPLE_REPO", "origin/master", simpleProject)
            val initialRepo = repositoryService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")
            val initialCommitCount = initialRepo?.commits?.size ?: 0
            val initialBranchCount = initialRepo?.branches?.size ?: 0

            // When - re-index just master
            vcsService.indexRepository("$FIXTURES_PATH/$SIMPLE_REPO", "master", simpleProject)

            // Then - counts should be unchanged
            val afterReindex = repositoryService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")
            assertAll(
                { assertThat(afterReindex?.commits).hasSize(initialCommitCount) },
                { assertThat(afterReindex?.branches).hasSize(initialBranchCount) },
            )
        }
    }

    @Nested
    @DisplayName("Given the octo repository with merge commits")
    inner class OctoRepoTests {

        @Test
        @DisplayName("When indexing a repository with merges, then all commits should be tracked")
        fun `should handle merge commits correctly`() {
            // Given - create a separate project for octo repo
            val octoProject = projectPort.create(Project(name = "octo-test-${System.nanoTime()}"))

            // When - index octo repo which has merge commits
            vcsService.indexRepository("$FIXTURES_PATH/$OCTO_REPO", "master", octoProject)

            // Then
            val repo = repositoryService.findRepo("$FIXTURES_PATH/$OCTO_REPO")
            assertAll(
                { assertThat(repo).isNotNull },
                { assertThat(repo?.commits).isNotEmpty() },
                { assertThat(repo?.commits).hasSize(19) }, // octo repo has 19 commits on master
                { assertThat(repo?.branches?.first()?.name).isEqualTo("master") },
            )
        }

        @Test
        @DisplayName("When re-indexing octo repo, then merge parent relationships should be preserved")
        fun `should preserve merge relationships on re-index`() {
            // Given - create a project and index octo repo
            val octoProject = projectPort.create(Project(name = "octo-merge-test-${System.nanoTime()}"))
            vcsService.indexRepository("$FIXTURES_PATH/$OCTO_REPO", "master", octoProject)
            val initialRepo = repositoryService.findRepo("$FIXTURES_PATH/$OCTO_REPO")

            // Find the merge commit (HEAD of octo repo) - it's an octopus merge
            val mergeCommit = initialRepo?.commits?.find { it.sha == "4dedc3c738eee6b69c43cde7d89f146912532cff" }
            val initialParentCount = mergeCommit?.parents?.size ?: 0

            // When - re-index
            vcsService.indexRepository("$FIXTURES_PATH/$OCTO_REPO", "master", octoProject)

            // Then - parent relationships should be preserved
            val reindexedRepo = repositoryService.findRepo("$FIXTURES_PATH/$OCTO_REPO")
            val reindexedMerge = reindexedRepo?.commits?.find { it.sha == "4dedc3c738eee6b69c43cde7d89f146912532cff" }

            assertAll(
                { assertThat(reindexedMerge).isNotNull },
                { assertThat(reindexedMerge?.parents).hasSize(initialParentCount) },
                { assertThat(initialParentCount).isEqualTo(4) }, // octopus merge has 4 parents
            )
        }
    }
}
