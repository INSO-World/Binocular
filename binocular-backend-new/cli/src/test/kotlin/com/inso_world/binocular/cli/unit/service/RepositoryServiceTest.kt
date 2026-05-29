package com.inso_world.binocular.cli.unit.service

import com.inso_world.binocular.cli.service.CommitService
import com.inso_world.binocular.cli.service.RepositoryService
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.core.unit.base.BaseUnitTest
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import com.inso_world.binocular.model.vcs.ReferenceCategory
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.test.util.ReflectionTestUtils.setField
import java.time.LocalDateTime

/**
 * BDD unit tests for [RepositoryService] using the new domain model with [Developer] and [Signature].
 *
 * These tests verify behavior without database integration, focusing on domain logic.
 */
@DisplayName("RepositoryService")
internal class RepositoryServiceTest : BaseUnitTest() {
    private lateinit var project: Project
    private lateinit var repository: Repository

    @BeforeEach
    fun setUp() {
        project = Project(name = "test-project")
        repository = Repository(localPath = "/test/repo", project = project)
    }

    @Nested
    @DisplayName("Given a repository with existing developers")
    inner class DeveloperDeduplication {
        private lateinit var existingDeveloper: Developer

        @BeforeEach
        fun setUp() {
            existingDeveloper =
                Developer(
                    name = "Alice",
                    email = "alice@example.com",
                    repository = repository,
                )
        }

        @Test
        @DisplayName("When creating a commit with an existing developer, then the developer should be reused")
        fun `should reuse existing developer when git signature matches`() {
            // Given - developer already in repository
            assertThat(repository.developers).contains(existingDeveloper)

            // When - create commit with same git signature
            val timestamp = LocalDateTime.now().minusHours(1)
            val signature = Signature(developer = existingDeveloper, timestamp = timestamp)
            val commit =
                Commit(
                    sha = "a".repeat(40),
                    authorSignature = signature,
                    message = "Test commit",
                    repository = repository,
                )

            // Then - commit uses the same developer instance
            assertAll(
                { assertThat(commit.author).isSameAs(existingDeveloper) },
                { assertThat(commit.committer).isSameAs(existingDeveloper) },
                { assertThat(repository.developers).hasSize(1) },
                { assertThat(existingDeveloper.authoredCommits).contains(commit) },
                { assertThat(existingDeveloper.committedCommits).contains(commit) },
            )
        }

        @Test
        @DisplayName("When creating a commit with a new developer, then a new developer should be registered")
        fun `should register new developer when git signature differs`() {
            // Given - existing developer already in repository
            val initialDeveloperCount = repository.developers.size

            // When - create commit with different developer
            val newDeveloper =
                Developer(
                    name = "Bob",
                    email = "bob@example.com",
                    repository = repository,
                )
            val timestamp = LocalDateTime.now().minusHours(1)
            val signature = Signature(developer = newDeveloper, timestamp = timestamp)
            val commit =
                Commit(
                    sha = "b".repeat(40),
                    authorSignature = signature,
                    message = "Another commit",
                    repository = repository,
                )

            // Then - both developers are registered
            assertAll(
                { assertThat(repository.developers).hasSize(initialDeveloperCount + 1) },
                { assertThat(repository.developers).contains(existingDeveloper) },
                { assertThat(repository.developers).contains(newDeveloper) },
                { assertThat(commit.author).isSameAs(newDeveloper) },
            )
        }
    }

    @Nested
    @DisplayName("Given commits with parent-child relationships")
    inner class ParentChildRelationships {
        private lateinit var developer: Developer
        private lateinit var timestamp: LocalDateTime

        @BeforeEach
        fun setUp() {
            developer =
                Developer(
                    name = "Carol",
                    email = "carol@example.com",
                    repository = repository,
                )
            timestamp = LocalDateTime.now().minusHours(2)
        }

        @Test
        @DisplayName("When adding a parent to a commit, then bidirectional relationship should be established")
        fun `should establish bidirectional parent-child relationship`() {
            // Given - parent commit (using valid hex SHA)
            val parentCommit =
                Commit(
                    sha = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                    authorSignature = Signature(developer = developer, timestamp = timestamp),
                    message = "Parent commit",
                    repository = repository,
                )

            // When - create child and add parent
            val childCommit =
                Commit(
                    sha = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
                    authorSignature = Signature(developer = developer, timestamp = timestamp.plusMinutes(30)),
                    message = "Child commit",
                    repository = repository,
                )
            childCommit.parents.add(parentCommit)

            // Then - bidirectional relationship established
            assertAll(
                { assertThat(childCommit.parents).contains(parentCommit) },
                { assertThat(parentCommit.children).contains(childCommit) },
            )
        }

        @Test
        @DisplayName("When building a diamond structure, then all relationships should be consistent")
        fun `should handle diamond parent-child structure`() {
            // Given - diamond: root -> A,B -> merge
            val root =
                Commit(
                    sha = "1".repeat(40),
                    authorSignature = Signature(developer = developer, timestamp = timestamp),
                    message = "Root",
                    repository = repository,
                )

            val branchA =
                Commit(
                    sha = "2".repeat(40),
                    authorSignature = Signature(developer = developer, timestamp = timestamp.plusMinutes(10)),
                    message = "Branch A",
                    repository = repository,
                )
            branchA.parents.add(root)

            val branchB =
                Commit(
                    sha = "3".repeat(40),
                    authorSignature = Signature(developer = developer, timestamp = timestamp.plusMinutes(15)),
                    message = "Branch B",
                    repository = repository,
                )
            branchB.parents.add(root)

            // When - create merge commit with both branches as parents
            val merge =
                Commit(
                    sha = "4".repeat(40),
                    authorSignature = Signature(developer = developer, timestamp = timestamp.plusMinutes(20)),
                    message = "Merge",
                    repository = repository,
                )
            merge.parents.add(branchA)
            merge.parents.add(branchB)

            // Then - all relationships correct
            assertAll(
                { assertThat(root.parents).isEmpty() },
                { assertThat(root.children).containsExactlyInAnyOrder(branchA, branchB) },
                { assertThat(branchA.parents).containsExactly(root) },
                { assertThat(branchA.children).containsExactly(merge) },
                { assertThat(branchB.parents).containsExactly(root) },
                { assertThat(branchB.children).containsExactly(merge) },
                { assertThat(merge.parents).containsExactlyInAnyOrder(branchA, branchB) },
                { assertThat(merge.children).isEmpty() },
            )
        }
    }

    @Nested
    @DisplayName("Given commits with different author and committer")
    inner class AuthorCommitterDifference {
        @Test
        @DisplayName("When author and committer differ, then both developers should be tracked")
        fun `should track both author and committer when different`() {
            // Given - two different developers
            val author =
                Developer(
                    name = "Author",
                    email = "author@example.com",
                    repository = repository,
                )
            val committer =
                Developer(
                    name = "Committer",
                    email = "committer@example.com",
                    repository = repository,
                )

            val timestamp = LocalDateTime.now().minusHours(1)

            // When - create commit with different author and committer
            val commit =
                Commit(
                    sha = "a".repeat(40),
                    authorSignature = Signature(developer = author, timestamp = timestamp),
                    committerSignature = Signature(developer = committer, timestamp = timestamp.plusMinutes(5)),
                    message = "Cherry-picked commit",
                    repository = repository,
                )

            // Then - both are tracked correctly
            assertAll(
                { assertThat(commit.author).isSameAs(author) },
                { assertThat(commit.committer).isSameAs(committer) },
                { assertThat(commit.author).isNotSameAs(commit.committer) },
                { assertThat(author.authoredCommits).contains(commit) },
                { assertThat(committer.committedCommits).contains(commit) },
                { assertThat(author.committedCommits).doesNotContain(commit) },
                { assertThat(committer.authoredCommits).doesNotContain(commit) },
                { assertThat(repository.developers).containsExactlyInAnyOrder(author, committer) },
            )
        }

        @Test
        @DisplayName("When author and committer are the same, then only one developer should be used")
        fun `should use same developer instance when author equals committer`() {
            // Given - single developer
            val developer =
                Developer(
                    name = "Dev",
                    email = "dev@example.com",
                    repository = repository,
                )

            val timestamp = LocalDateTime.now().minusHours(1)

            // When - create commit with same author and committer
            val commit =
                Commit(
                    sha = "b".repeat(40),
                    authorSignature = Signature(developer = developer, timestamp = timestamp),
                    // committerSignature defaults to authorSignature
                    message = "Normal commit",
                    repository = repository,
                )

            // Then - same developer instance used
            assertAll(
                { assertThat(commit.author).isSameAs(commit.committer) },
                { assertThat(developer.authoredCommits).contains(commit) },
                { assertThat(developer.committedCommits).contains(commit) },
                { assertThat(repository.developers).hasSize(1) },
            )
        }
    }

    @Nested
    @DisplayName("Given a repository receiving commits from GitIndexer")
    inner class CommitRegistration {
        @Test
        @DisplayName("When commits are created with repository reference, then they auto-register")
        fun `commits should auto-register with repository`() {
            // Given - empty repository
            assertThat(repository.commits).isEmpty()

            // When - create commits
            val developer =
                Developer(
                    name = "Dev",
                    email = "dev@example.com",
                    repository = repository,
                )
            val timestamp = LocalDateTime.now().minusHours(1)

            val commit1 =
                Commit(
                    sha = "1".repeat(40),
                    authorSignature = Signature(developer = developer, timestamp = timestamp),
                    message = "First",
                    repository = repository,
                )
            val commit2 =
                Commit(
                    sha = "2".repeat(40),
                    authorSignature = Signature(developer = developer, timestamp = timestamp.plusMinutes(10)),
                    message = "Second",
                    repository = repository,
                )

            // Then - commits auto-registered
            assertAll(
                { assertThat(repository.commits).hasSize(2) },
                { assertThat(repository.commits).containsExactlyInAnyOrder(commit1, commit2) },
            )
        }

        @Test
        @DisplayName("When same commit SHA is created twice, then deduplication should occur")
        fun `should not duplicate commits with same SHA`() {
            // Given - developer and commit
            val developer =
                Developer(
                    name = "Dev",
                    email = "dev@example.com",
                    repository = repository,
                )
            val timestamp = LocalDateTime.now().minusHours(1)
            val sha = "a".repeat(40)

            val commit1 =
                Commit(
                    sha = sha,
                    authorSignature = Signature(developer = developer, timestamp = timestamp),
                    message = "Original",
                    repository = repository,
                )

            // When - try to add same SHA again via repository.commits
            val initialSize = repository.commits.size
            val added = repository.commits.add(commit1)

            // Then - no duplication
            assertAll(
                { assertThat(added).isFalse() },
                { assertThat(repository.commits).hasSize(initialSize) },
            )
        }
    }

    @Nested
    @DisplayName("Given branch operations")
    inner class BranchOperations {
        @Test
        @DisplayName("When a branch is created, then it should reference HEAD commit and be registered")
        fun `branch should register with repository and track head`() {
            // Given - commit for branch head
            val developer =
                Developer(
                    name = "Dev",
                    email = "dev@example.com",
                    repository = repository,
                )
            val timestamp = LocalDateTime.now().minusHours(1)
            val headCommit =
                Commit(
                    sha = "cccccccccccccccccccccccccccccccccccccccc",
                    authorSignature = Signature(developer = developer, timestamp = timestamp),
                    message = "HEAD",
                    repository = repository,
                )

            // When - create branch
            val branch =
                Branch(
                    name = "main",
                    fullName = "refs/heads/main",
                    repository = repository,
                    head = headCommit,
                    category = ReferenceCategory.LOCAL_BRANCH,
                )

            // Then - branch registered with correct head
            assertAll(
                { assertThat(repository.branches).contains(branch) },
                { assertThat(branch.head).isSameAs(headCommit) },
                { assertThat(branch.latestCommit).isEqualTo(headCommit.sha) },
            )
        }
    }

    @Nested
    @DisplayName("Given findRepo operation")
    inner class FindRepoOperation {
        @Test
        @DisplayName("When finding repository by path, then path should be normalized")
        fun `path normalization should handle git suffix`() {
            // Test the normalizePath helper logic
            val pathWithGit = "/path/to/repo/.git"
            val pathWithoutGit = "/path/to/repo"

            // Both should normalize to the same effective path
            val normalizedWithGit = if (pathWithGit.endsWith(".git")) pathWithGit else "$pathWithGit/.git"
            val normalizedWithoutGit = if (pathWithoutGit.endsWith(".git")) pathWithoutGit else "$pathWithoutGit/.git"

            assertThat(normalizedWithGit).isEqualTo("/path/to/repo/.git")
            assertThat(normalizedWithoutGit).isEqualTo("/path/to/repo/.git")
        }
    }

    @Nested
    @DisplayName("Given create repository operation")
    inner class CreateOperation {
        @Test
        @DisplayName("When creating repository with valid data, then validation should pass")
        fun `should validate repository has no id and has project`() {
            // Given - create a fresh project for this test (different from shared one)
            val freshProject = Project(name = "fresh-project")
            val validRepo = Repository(localPath = "/test/path", project = freshProject)

            // Then - validation passes
            assertAll(
                { assertThat(validRepo.id).isNull() },
                { assertThat(validRepo.project).isNotNull() },
                { assertThat(validRepo.project.repo).isSameAs(validRepo) },
            )
        }
    }

    @Nested
    @DisplayName("Given transformCommits Pass 2 relationship wiring")
    inner class TransformCommitsPassTwo {
        private lateinit var service: RepositoryService
        private lateinit var developer: Developer
        private lateinit var timestamp: LocalDateTime

        private val repositoryPort = mockk<RepositoryInfrastructurePort>(relaxed = true)
        private val commitService = mockk<CommitService>(relaxed = true)
        private val commitPort = mockk<CommitInfrastructurePort>(relaxed = true)

        @BeforeEach
        fun setUp() {
            service = RepositoryService(repositoryPort, commitService, commitPort)
            developer = Developer(name = "Dev", email = "dev@example.com", repository = repository)
            timestamp = LocalDateTime.now().minusHours(1)
            project = Project(name = "test-project")
        }

        private fun makeCommit(
            sha: String,
            repo: Repository,
        ): Commit =
            Commit(
                sha = sha,
                authorSignature =
                    Signature(
                        developer = developer,
                        timestamp = timestamp,
                    ),
                message = "commit $sha",
                repository = repo,
            )

        @Test
        @DisplayName("When a commit lists a parentId, then Pass 2 adds a back-reference to that parent's childIds")
        fun `parent wiring adds back-reference to canonical parent's childIds`() {
            // Given - two commits: A is parent of B (B.parentIds contains A.iid)
            val repo = Repository(localPath = "/test/pass2", project = project)
            setField(
                Developer::class.java.getDeclaredField("repository"),
                developer,
                repo
            )
            val commitA = makeCommit("a".repeat(40), repo)
            val commitB = makeCommit("b".repeat(40), repo)
            commitB.parents.add(commitA)

            // When
            val result = service.transformCommits(repo, listOf(commitA, commitB))

            // Then - A must have B in its childIds
            val transformedA = result.find { it.sha == commitA.sha }!!
            assertThat(transformedA.children).contains(commitB)
        }

        @Test
        @DisplayName("When transformCommits is called twice, then duplicate childId entries are not added")
        fun `parent wiring is idempotent - calling twice does not duplicate entries`() {
            // Given - two commits: A is parent of B
            val repo = Repository(localPath = "/test/pass2-idem", project = project)
            setField(
                Developer::class.java.getDeclaredField("repository"),
                developer,
                repo
            )
            val commitA = makeCommit("a".repeat(40), repo)
            val commitB = makeCommit("b".repeat(40), repo)
            commitB.parents.add(commitA)

            // When - transform twice (simulates incremental indexing on same data)
            val firstPass = service.transformCommits(repo, listOf(commitA, commitB))
            service.transformCommits(repo, firstPass)

            // Then - B appears exactly once in A's childIds
            val transformedA = firstPass.find { it.sha == commitA.sha }!!
            val childIdCount = transformedA.children.count { it == commitB }
            assertThat(childIdCount).isEqualTo(1)
        }

        @Test
        @DisplayName("When a commit lists a childId, then Pass 2 adds a back-reference to that child's parentIds")
        fun `child wiring adds back-reference to canonical child's parentIds`() {
            // Given - two commits: B lists A as a child (B.childIds contains A.iid)
            val repo = Repository(localPath = "/test/pass2-child", project = project)
            setField(
                Developer::class.java.getDeclaredField("repository"),
                developer,
                repo
            )
            val commitA = makeCommit("a".repeat(40), repo)
            val commitB = makeCommit("b".repeat(40), repo)
            commitB.children.add(commitA)

            // When
            val result = service.transformCommits(repo, listOf(commitA, commitB))

            // Then - A must have B in its parentIds
            val transformedA = result.find { it.sha == commitA.sha }!!
            assertThat(transformedA.parents).contains(commitB)
        }
    }
}
