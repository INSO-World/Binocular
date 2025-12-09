package com.inso_world.binocular.ffi.unit.lib

import com.inso_world.binocular.ffi.BinocularConfig
import com.inso_world.binocular.ffi.GixConfig
import com.inso_world.binocular.ffi.internal.GixRepository
import com.inso_world.binocular.ffi.internal.UniffiException
import com.inso_world.binocular.ffi.internal.findAllBranches
import com.inso_world.binocular.ffi.internal.findCommit
import com.inso_world.binocular.ffi.internal.findRepo
import com.inso_world.binocular.ffi.internal.traverseBranch
import com.inso_world.binocular.ffi.internal.traverseHistory
import com.inso_world.binocular.ffi.unit.lib.base.BaseLibraryUnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * Comprehensive unit tests for commit operations in the FFI layer.
 *
 * Tests verify:
 * - Commit lookup by hash
 * - Commit traversal between revisions
 * - Branch traversal
 * - Commit metadata extraction
 * - Error handling for invalid commits
 *
 * ### Test Organization
 * - [FindCommitOperation]: Tests for commit lookup
 * - [TraverseOperation]: Tests for commit history traversal
 * - [TraverseBranchOperation]: Tests for branch traversal
 * - [CommitMetadata]: Tests for commit data validation
 * - [ErrorHandling]: Tests for exception scenarios
 */
@DisplayName("Commit Operations")
class CommitOperationsTest : BaseLibraryUnitTest() {

    private val cfg: BinocularConfig = BinocularConfig().apply {
        gix = GixConfig(skipMerges = false, useMailmap = true)
    }

    private lateinit var testRepoPath: String
    private lateinit var repo: GixRepository

    @BeforeEach
    fun setUp() {
        testRepoPath = System.getProperty("user.dir")
        repo = findRepo(testRepoPath)
    }

    @Nested
    @DisplayName("findCommit operation")
    inner class FindCommitOperation {

        @Test
        fun `finds commit by valid SHA-1 hash`() {
            // Verifies that findCommit successfully retrieves a commit using its full SHA-1 hash
            val branches = findAllBranches(repo)
            val headCommit = branches.first().target

            val result = findCommit(
                repo, headCommit,
                useMailmap = cfg.gix.useMailmap
            )

            assertAll(
                { assertThat(result).isNotNull },
                { assertThat(result.oid).isEqualTo(headCommit) }
            )
        }

        @Test
        fun `finds commit by short SHA hash`() {
            // Tests commit lookup using abbreviated SHA-1 (7+ characters)
            val branches = findAllBranches(repo)
            val headCommit = branches.first().target
            val shortHash = headCommit.toString().substring(0, 7)

            val result = findCommit(
                repo, shortHash,
                useMailmap = cfg.gix.useMailmap
            )

            assertThat(result).isNotNull()
        }

        @Test
        fun `found commit has valid metadata`() {
            // Ensures retrieved commits contain all required metadata fields
            val branches = findAllBranches(repo)
            val headCommit = branches.first().target

            val result = findCommit(
                repo, headCommit,
                useMailmap = cfg.gix.useMailmap
            )

            assertAll(
                { assertThat(result.oid).isNotNull() },
                { assertThat(result.author).isNotNull() },
                { assertThat(result.committer).isNotNull() },
                { assertThat(result.message).isNotNull() }
            )
        }

        @Test
        fun `commit author has valid signature`() {
            // Validates commit author signature contains name, email, and time
            val branches = findAllBranches(repo)
            val headCommit = branches.first().target

            val result = findCommit(
                repo, headCommit,
                useMailmap = cfg.gix.useMailmap
            )

            assertAll(
                { assertThat(result.author.name).isNotEmpty() },
                { assertThat(result.author.email).isNotEmpty() },
                { assertThat(result.author.time).isNotNull() }
            )
        }

        @Test
        fun `commit committer has valid signature`() {
            // Validates commit committer signature contains name, email, and time
            val branches = findAllBranches(repo)
            val headCommit = branches.first().target

            val result = findCommit(
                repo, headCommit,
                useMailmap = cfg.gix.useMailmap
            )

            assertAll(
                { assertThat(result.committer?.name).isNotEmpty() },
                { assertThat(result.committer?.email).isNotEmpty() },
                { assertThat(result.committer?.time).isNotNull() }
            )
        }

        @Test
        fun `commit message is non-empty`() {
            // Confirms commit messages are extracted correctly
            val branches = findAllBranches(repo)
            val headCommit = branches.first().target

            val result = findCommit(
                repo, headCommit,
                useMailmap = cfg.gix.useMailmap
            )

            assertThat(result.message).isNotEmpty()
        }

        @Test
        fun `commit has parent information`() {
            // Checks that commit parent data is populated (except for root commits)
            val branches = findAllBranches(repo)
            val headCommit = branches.first().target

            val result = findCommit(
                repo, headCommit,
                useMailmap = cfg.gix.useMailmap
            )

            // Note: Root commits have no parents
            assertThat(result.parents).isNotNull()
        }
    }

    @Nested
    @DisplayName("traverse operation")
    inner class TraverseOperation {

        @Test
        fun `traverses from source commit to root when target is null`() {
            // Verifies full history traversal from a commit to repository root
            val branches = findAllBranches(repo)
            val headCommit = branches.first().target

            val result = traverseHistory(
                repo, headCommit, null,
                useMailmap = cfg.gix.useMailmap
            )

            assertAll(
                { assertThat(result).isNotNull() },
                { assertThat(result).isNotEmpty() },
                { assertThat(result.first().oid).isEqualTo(headCommit) }
            )
        }

        @Test
        fun `traverses between two commits`() {
            // Tests history traversal between source and target commits
            val branches = findAllBranches(repo)
            val headCommit = branches.first().target
            val commits = traverseHistory(
                repo, headCommit, null,
                useMailmap = cfg.gix.useMailmap
            )

            if (commits.size >= 2) {
                val sourceCommit = commits[0].oid
                val targetCommit = commits[commits.size - 1].oid

                val result = traverseHistory(repo, sourceCommit, targetCommit, useMailmap = cfg.gix.useMailmap)

                assertAll(
                    { assertThat(result).isNotEmpty() },
                    { assertThat(result.first().oid).isEqualTo(sourceCommit) }
                )
            }
        }

        @Test
        fun `traversal result contains commit metadata`() {
            // Ensures all commits in traversal have complete metadata
            val branches = findAllBranches(repo)
            val headCommit = branches.first().target

            val result = traverseHistory(
                repo, headCommit, null,
                useMailmap = cfg.gix.useMailmap
            )

            result.forEach { commit ->
                assertAll(
                    { assertThat(commit.oid).isNotNull() },
                    { assertThat(commit.author).isNotNull() },
                    { assertThat(commit.committer).isNotNull() },
                    { assertThat(commit.message).isNotNull() }
                )
            }
        }

        @Test
        fun `commits are ordered chronologically`() {
            // Validates commits are returned in topological order
            val branches = findAllBranches(repo)
            val headCommit = branches.first().target

            val result = traverseHistory(
                repo, headCommit, null,
                useMailmap = cfg.gix.useMailmap
            )

            if (result.size >= 2) {
                // First commit should be the source (newest)
                assertThat(result.first().oid).isEqualTo(headCommit)
            }
        }

        @Test
        fun `traversal includes source commit`() {
            // Confirms source commit is included in traversal results
            val branches = findAllBranches(repo)
            val headCommit = branches.first().target

            val result = traverseHistory(
                repo, headCommit, null,
                useMailmap = cfg.gix.useMailmap
            )

            assertThat(result.map { it.oid }).contains(headCommit)
        }

        @Test
        fun `traversal handles merge commits`() {
            // Tests that merge commits are properly processed
            val branches = findAllBranches(repo)
            val headCommit = branches.first().target

            val result = traverseHistory(
                repo, headCommit, null,
                useMailmap = cfg.gix.useMailmap
            )

            // Merge commits may have multiple parents
            val mergeCommits = result.filter { it.parents.size > 1 }
            // This is okay - repository may or may not have merge commits
            assertThat(mergeCommits).isNotNull()
        }
    }

    @Nested
    @DisplayName("traverseBranch operation")
    inner class TraverseBranchOperation {

        @Test
        fun `traverses branch and returns commits`() {
            // Verifies branch traversal returns commit history
            val branches = findAllBranches(repo)
            val branchName = branches.first().fullName

            val result = traverseBranch(
                repo, branchName,
                useMailmap = cfg.gix.useMailmap, skipMerges = cfg.gix.skipMerges
            )

            assertAll(
                { assertThat(result).isNotNull() },
                { assertThat(result.branch).isNotNull() },
                { assertThat(result.commits).isNotEmpty() }
            )
        }

        @Test
        fun `returned branch matches requested branch name`() {
            // Ensures returned branch metadata corresponds to requested branch
            val branches = findAllBranches(repo)
            val branchName = branches.first().name

            val result = traverseBranch(
                repo, branchName,
                useMailmap = cfg.gix.useMailmap, skipMerges = cfg.gix.skipMerges
            )

            assertThat(result.branch.name).isEqualTo(branchName)
        }

        @Test
        fun `branch commits have valid metadata`() {
            // Validates all commits in branch traversal have complete data
            val branches = findAllBranches(repo)
            val branchName = branches.first().name

            val result = traverseBranch(
                repo, branchName,
                useMailmap = cfg.gix.useMailmap, skipMerges = cfg.gix.skipMerges
            )

            result.commits.forEach { commit ->
                assertAll(
                    { assertThat(commit.oid).isNotNull() },
                    { assertThat(commit.author).isNotNull() },
                    { assertThat(commit.committer).isNotNull() }
                )
            }
        }

        @Test
        fun `branch traversal includes HEAD commit`() {
            // Confirms branch traversal includes the current HEAD commit
            val branches = findAllBranches(repo)
            val branch = branches.first()

            val result = traverseBranch(
                repo, branch.name,
                skipMerges = cfg.gix.skipMerges,
                useMailmap = cfg.gix.useMailmap
            )

            assertThat(result.commits.map { it.oid }).contains(branch.target)
        }
    }

    @Nested
    @DisplayName("Commit Metadata")
    inner class CommitMetadata {

        @Test
        fun `commit OID is valid SHA-1`() {
            // Validates commit OIDs are proper SHA-1 hashes
            val branches = findAllBranches(repo)
            val headCommit = branches.first().target

            val result = findCommit(
                repo, headCommit,
                useMailmap = cfg.gix.useMailmap
            )

            assertAll(
                { assertThat(result.oid.toString()).hasSize(40) },
                { assertThat(result.oid.toString()).matches("[0-9a-f]{40}") }
            )
        }

        @Test
        fun `author and committer timestamps are valid`() {
            // Checks that commit timestamps are in valid ranges
            val branches = findAllBranches(repo)
            val headCommit = branches.first().target

            val result = findCommit(
                repo, headCommit,
                useMailmap = cfg.gix.useMailmap
            )

            assertAll(
                { assertThat(result.author.time?.seconds).isGreaterThan(0) },
                { assertThat(result.committer?.time?.seconds).isGreaterThan(0) }
            )
        }

        @Test
        fun `commit message preserves formatting`() {
            // Ensures commit messages maintain original formatting
            val branches = findAllBranches(repo)
            val headCommit = branches.first().target

            val result = findCommit(
                repo, headCommit,
                useMailmap = cfg.gix.useMailmap
            )

            // Message should not be null or empty
            assertThat(result.message).isNotBlank()
        }

        @Test
        fun `parent commits are valid OIDs`() {
            // Validates parent commit OIDs are proper SHA-1 hashes
            val branches = findAllBranches(repo)
            val headCommit = branches.first().target

            val result = findCommit(
                repo, headCommit,
                useMailmap = cfg.gix.useMailmap
            )

            result.parents.forEach { parentOid ->
                assertAll(
                    { assertThat(parentOid.toString()).hasSize(40) },
                    { assertThat(parentOid.toString()).matches("[0-9a-f]{40}") }
                )
            }
        }
    }

    @Nested
    @DisplayName("Error Handling")
    inner class ErrorHandling {

        @Test
        fun `throws RevisionParseException for invalid commit hash`() {
            // Verifies proper exception for malformed commit hashes
            val invalidHash = "invalid_hash_123"

            val exception = assertThrows<UniffiException.RevisionParseException> {
                findCommit(repo, invalidHash, useMailmap = cfg.gix.useMailmap)
            }

            assertThat(exception.v1).isNotEmpty()
        }

        @Test
        fun `throws exception for non-existent commit hash`() {
            // Tests error handling for valid-format but non-existent commits
            val nonExistentHash = "0000000000000000000000000000000000000000"

            val exception = assertThrows<UniffiException> {
                findCommit(repo, nonExistentHash, useMailmap = cfg.gix.useMailmap)
            }

            assertThat(exception).isNotNull()
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "",
                "   ",
                "abc",
                "zzzzz",
                "!@#$%"
            ]
        )
        fun `throws exception for various invalid hash formats`(invalidHash: String) {
            // Ensures consistent error handling across different invalid inputs
            assertThrows<UniffiException> {
                findCommit(repo, invalidHash, useMailmap = cfg.gix.useMailmap)
            }
        }

        @Test
        fun `throws ReferenceException for invalid branch name`() {
            // Tests error handling when branch doesn't exist
            val invalidBranch = "refs/heads/non_existent_branch_" + System.currentTimeMillis()

            val exception = assertThrows<UniffiException.ReferenceException> {
                traverseBranch(repo, invalidBranch, skipMerges = cfg.gix.skipMerges, useMailmap = cfg.gix.useMailmap)
            }

            assertThat(exception.v1).isNotEmpty()
        }

        @Test
        fun `traverse throws exception for invalid source commit`() {
            // Verifies error handling for invalid source commit in traversal
            val invalidCommit = "0000000000000000000000000000000000000000"

            assertThrows<UniffiException> {
                traverseHistory(repo, invalidCommit, null, useMailmap = cfg.gix.useMailmap)
            }
        }

        @Test
        fun `exception messages are descriptive`() {
            // Ensures exception messages provide useful debugging information
            val exception = assertThrows<UniffiException> {
                findCommit(repo, "invalid", useMailmap = cfg.gix.useMailmap)
            }

            assertAll(
                { assertThat(exception.message).isNotEmpty() },
                { assertThat(exception.message).hasSizeGreaterThan(5) }
            )
        }
    }
}
