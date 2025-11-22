package com.inso_world.binocular.ffi.unit.extensions

import com.inso_world.binocular.core.unit.base.BaseUnitTest
import com.inso_world.binocular.ffi.extensions.toDomain
import com.inso_world.binocular.ffi.extensions.toLocalDateTime
import com.inso_world.binocular.ffi.internal.GixCommit
import com.inso_world.binocular.ffi.internal.GixSignature
import com.inso_world.binocular.ffi.internal.GixTime
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDateTime

/**
 * Unit tests for [toDomain] extension functions.
 *
 * Tests both single-item and batch mapping with comprehensive C4 coverage.
 *
 * ## Implementation Features:
 *
 * 1. **Early SHA validation**: Validates SHA format at boundary before construction
 * 2. **Indexed lookup**: Batch mapper uses O(1) indexed lookup for efficiency
 * 3. **Strict parent requirement**: All parent commits must exist in batch or repository
 * 4. **Requires committer timestamp**: `committer.time` must be present (no fallback to author)
 */
class GixCommitTest : BaseUnitTest() {

    private lateinit var project: Project
    private lateinit var repository: Repository

    private val validSha = "a".repeat(40)
    private val anotherValidSha = "b".repeat(40)
    private val thirdValidSha = "c".repeat(40)

    private val testTime = GixTime(
        seconds = 1704067200L, // 2024-01-01T00:00:00Z
        offset = 0
    )

    @BeforeEach
    fun setUp() {
        project = Project(name = "test-project")
        repository = Repository(
            localPath = "/path/to/repo",
            project = project
        )
    }

    // ========== Single Item Mapper Tests ==========

    @Nested
    inner class SingleItemMapper {

        @Test
        fun `toDomain creates new commit with all fields`() {
            val authorSig = GixSignature(
                name = "Author Name",
                email = "author@example.com",
                time = testTime
            )
            val committerSig = GixSignature(
                name = "Committer Name",
                email = "committer@example.com",
                time = testTime
            )

            val vec = GixCommit(
                oid = validSha,
                message = "Test commit message",
                author = authorSig,
                committer = committerSig,
                branch = null,
                parents = emptyList(),
                fileTree = emptyList()
            )

            val result = vec.toDomain(repository)

            assertThat(result.sha).isEqualTo(validSha)
            assertThat(result.message).isEqualTo("Test commit message")
            assertThat(result.author).isNotNull
            assertThat(result.author?.name).isEqualTo("Author Name")
            assertThat(result.committer).isNotNull
            assertThat(result.committer?.name).isEqualTo("Committer Name")
            assertThat(result.commitDateTime).isEqualTo(testTime.toLocalDateTime())
            assertThat(result.authorDateTime).isEqualTo(testTime.toLocalDateTime())
        }

        @Test
        fun `toDomain registers commit in repository`() {
            val vec = createBasicCommitVec(validSha)

            val result = vec.toDomain(repository)

            assertThat(repository.commits).contains(result)
        }

        @Test
        fun `toDomain returns existing commit if SHA already exists`() {
            val testCommitter = User(name = "Test Committer", repository = repository)
            val existingCommit = Commit(
                sha = validSha,
                commitDateTime = LocalDateTime.of(2023, 1, 1, 0, 0),
                repository = repository,
                committer = testCommitter
            )

            val vec = createBasicCommitVec(validSha)

            val result = vec.toDomain(repository)

            assertThat(result).isSameAs(existingCommit)
        }

        @Test
        fun `toDomain uses committer time for commitDateTime when both present`() {
            val authorTime = GixTime(seconds = 1704067200L, offset = 0) // 2024-01-01
            val committerTime = GixTime(seconds = 1704153600L, offset = 0) // 2024-01-02

            val vec = GixCommit(
                oid = validSha,
                message = "test",
                author = GixSignature("Author", "a@test.com", authorTime),
                committer = GixSignature("Committer", "c@test.com", committerTime),
                branch = null,
                parents = emptyList(),
                fileTree = emptyList()
            )

            val result = vec.toDomain(repository)

            assertThat(result.commitDateTime).isEqualTo(committerTime.toLocalDateTime())
            assertThat(result.authorDateTime).isEqualTo(authorTime.toLocalDateTime())
        }

        @Test
        fun `toDomain throws exception when committer is null`() {
            val authorTime = GixTime(seconds = 1704067200L, offset = 0)

            val vec = GixCommit(
                oid = validSha,
                message = "test",
                author = GixSignature("Author", "a@test.com", authorTime),
                committer = null,
                branch = null,
                parents = emptyList(),
                fileTree = emptyList()
            )

            val exception = assertThrows<IllegalArgumentException> {
                vec.toDomain(repository)
            }

            assertThat(exception.message).contains("requires committer timestamp")
        }

        @Test
        fun `toDomain enriches existing author when committer present`() {
            val existingAuthor = User(name = "Existing Author", repository = repository)
            val testCommitter = User(name = "Test Committer", repository = repository)
            val existingCommit = Commit(
                sha = validSha,
                commitDateTime = LocalDateTime.of(2023, 1, 1, 0, 0),
                repository = repository,
                committer = testCommitter
            )
            existingCommit.author = existingAuthor

            val vec = GixCommit(
                oid = validSha,
                message = "test",
                author = GixSignature("Different Author", "diff@test.com", testTime),
                committer = GixSignature("Committer", "c@test.com", testTime),
                branch = null,
                parents = emptyList(),
                fileTree = emptyList()
            )

            val result = vec.toDomain(repository)

            assertThat(result).isSameAs(existingCommit)
            assertThat(result.author).isSameAs(existingAuthor) // Keeps existing
            assertThat(result.author?.name).isEqualTo("Existing Author")
        }

        @Test
        fun `toDomain does not overwrite existing committer`() {
            val existingCommitter = User(name = "Existing Committer", repository = repository)
            val existingCommit = Commit(
                sha = validSha,
                commitDateTime = LocalDateTime.of(2023, 1, 1, 0, 0),
                repository = repository,
                committer = existingCommitter
            )

            val vec = GixCommit(
                oid = validSha,
                message = "test",
                author = null,
                committer = GixSignature("Different Committer", "diff@test.com", testTime),
                branch = null,
                parents = emptyList(),
                fileTree = emptyList()
            )

            val result = vec.toDomain(repository)

            assertThat(result.committer).isSameAs(existingCommitter)
            assertThat(result.committer.name).isEqualTo("Existing Committer")
        }

        @Test
        fun `toDomain preserves committer on existing commit`() {
            val existingCommitter = User(name = "Existing Committer", repository = repository)
            val existingCommit = Commit(
                sha = validSha,
                commitDateTime = LocalDateTime.of(2023, 1, 1, 0, 0),
                repository = repository,
                committer = existingCommitter
            )

            val vec = GixCommit(
                oid = validSha,
                message = "test",
                author = null,
                committer = GixSignature("New Committer", "new@test.com", testTime),
                branch = null,
                parents = emptyList(),
                fileTree = emptyList()
            )

            val result = vec.toDomain(repository)

            assertThat(result).isSameAs(existingCommit)
            assertThat(result.committer.name).isEqualTo("Existing Committer")
        }

        @Test
        fun `toDomain sets author on existing commit when author is null`() {
            val testCommitter = User(name = "Test Committer", repository = repository)
            val existingCommit = Commit(
                sha = validSha,
                commitDateTime = LocalDateTime.of(2023, 1, 1, 0, 0),
                repository = repository,
                committer = testCommitter
            )
            assertThat(existingCommit.author).isNull()

            val vec = GixCommit(
                oid = validSha,
                message = "test",
                author = GixSignature("New Author", "new-a@test.com", testTime),
                committer = GixSignature("New Committer", "new-c@test.com", testTime),
                branch = null,
                parents = emptyList(),
                fileTree = emptyList()
            )

            val result = vec.toDomain(repository)

            assertThat(result).isSameAs(existingCommit)
            assertThat(result.author?.name).isEqualTo("New Author")
        }

        @Test
        fun `toDomain handles null message`() {
            val vec = GixCommit(
                oid = validSha,
                message = "",
                author = null,
                committer = GixSignature("C", "c@test.com", testTime),
                branch = null,
                parents = emptyList(),
                fileTree = emptyList()
            )

            val result = vec.toDomain(repository)

            assertThat(result.message).isEmpty()
        }

        @Test
        fun `toDomain throws exception for invalid SHA - too short`() {
            val vec = GixCommit(
                oid = "abc123", // Only 6 chars, not 40
                message = "test",
                author = null,
                committer = GixSignature("C", "c@test.com", testTime),
                branch = null,
                parents = emptyList(),
                fileTree = emptyList()
            )

            val exception = assertThrows<IllegalArgumentException> {
                vec.toDomain(repository)
            }

            assertThat(exception.message).contains("must be exactly 40 characters")
        }

        @Test
        fun `toDomain throws exception for invalid SHA - non-hex characters`() {
            val invalidSha = "g" + "a".repeat(39) // 'g' is not hex

            val vec = GixCommit(
                oid = invalidSha,
                message = "test",
                author = null,
                committer = GixSignature("C", "c@test.com", testTime),
                branch = null,
                parents = emptyList(),
                fileTree = emptyList()
            )

            assertThrows<IllegalArgumentException> {
                vec.toDomain(repository)
            }
        }
    }

    // ========== Batch Mapper Tests ==========

    @Nested
    inner class BatchMapper {

        @Test
        fun `batch toDomain maps empty collection`() {
            val result = emptyList<GixCommit>().toDomain(repository)

            assertThat(result).isEmpty()
        }

        @Test
        fun `batch toDomain maps single commit`() {
            val vec = createBasicCommitVec(validSha)

            val result = listOf(vec).toDomain(repository)

            assertThat(result).hasSize(1)
            assertThat(result[0].sha).isEqualTo(validSha)
        }

        @Test
        fun `batch toDomain preserves input order`() {
            val vec1 = createBasicCommitVec(validSha, message = "First")
            val vec2 = createBasicCommitVec(anotherValidSha, message = "Second")
            val vec3 = createBasicCommitVec(thirdValidSha, message = "Third")

            val result = listOf(vec1, vec2, vec3).toDomain(repository)

            assertThat(result).hasSize(3)
            assertThat(result[0].message).isEqualTo("First")
            assertThat(result[1].message).isEqualTo("Second")
            assertThat(result[2].message).isEqualTo("Third")
        }

        @Test
        fun `batch toDomain wires parent-child relationships`() {
            val parentVec = createBasicCommitVec(validSha, message = "Parent")
            val childVec = createBasicCommitVec(
                sha = anotherValidSha,
                message = "Child",
                parents = listOf(validSha)
            )

            val result = listOf(parentVec, childVec).toDomain(repository)

            val parent = result.find { it.sha == validSha }!!
            val child = result.find { it.sha == anotherValidSha }!!

            assertThat(child.parents).contains(parent)
            assertThat(parent.children).contains(child)
        }

        @Test
        fun `batch toDomain throws NoSuchElementException for missing parent`() {
            val missingParentSha = "d".repeat(40)
            val childVec = createBasicCommitVec(
                sha = validSha,
                message = "Child",
                parents = listOf(missingParentSha)
            )

            val exception = assertThrows<NoSuchElementException> {
                listOf(childVec).toDomain(repository)
            }

            // NoSuchElementException thrown by getValue() when parent not found
            assertThat(exception.message).contains(missingParentSha)
        }

        @Test
        fun `batch toDomain succeeds when parent exists in repository`() {
            // Create parent in repository first
            val testCommitter = User(name = "Test Committer", repository = repository)
            val parentCommit = Commit(
                sha = validSha,
                commitDateTime = LocalDateTime.of(2023, 1, 1, 0, 0),
                repository = repository,
                committer = testCommitter
            )

            val childVec = createBasicCommitVec(
                sha = anotherValidSha,
                message = "Child",
                parents = listOf(validSha)
            )

            val result = listOf(childVec).toDomain(repository)

            val child = result[0]
            assertThat(child.parents).contains(parentCommit)
            assertThat(parentCommit.children).contains(child)
        }

        @Test
        fun `batch toDomain throws exception for invalid parent SHA`() {
            val invalidParentSha = "invalid" // Not 40 hex chars
            val childVec = createBasicCommitVec(
                sha = validSha,
                message = "Child with invalid parent",
                parents = listOf(invalidParentSha)
            )

            val exception = assertThrows<IllegalArgumentException> {
                listOf(childVec).toDomain(repository)
            }

            assertThat(exception.message).contains("Invalid SHA")
        }

        @Test
        fun `batch toDomain handles commit with multiple parents`() {
            val parent1 = createBasicCommitVec(validSha, message = "Parent 1")
            val parent2 = createBasicCommitVec(anotherValidSha, message = "Parent 2")
            val mergeCommit = createBasicCommitVec(
                sha = thirdValidSha,
                message = "Merge commit",
                parents = listOf(validSha, anotherValidSha)
            )

            val result = listOf(parent1, parent2, mergeCommit).toDomain(repository)

            val merge = result.find { it.sha == thirdValidSha }!!
            assertThat(merge.parents).hasSize(2)
            assertThat(merge.parents.map { it.sha }).containsExactlyInAnyOrder(validSha, anotherValidSha)
        }

        @Test
        fun `batch toDomain reuses existing commits from repository`() {
            val testCommitter = User(name = "Test Committer", repository = repository)
            val existingCommit = Commit(
                sha = validSha,
                commitDateTime = LocalDateTime.of(2023, 1, 1, 0, 0),
                message = "Existing",
                repository = repository,
                committer = testCommitter
            )

            val vec1 = createBasicCommitVec(validSha, message = "New")
            val vec2 = createBasicCommitVec(anotherValidSha, message = "Another")

            val result = listOf(vec1, vec2).toDomain(repository)

            assertThat(result).hasSize(2)
            assertThat(result.find { it.sha == validSha }).isSameAs(existingCommit)
            assertThat(result.find { it.sha == validSha }?.message).isEqualTo("Existing") // Keeps existing data
        }

        @Test
        fun `batch toDomain handles complex graph with shared parents`() {
            val root = createBasicCommitVec("1".repeat(40), message = "Root")
            val child1 = createBasicCommitVec("2".repeat(40), message = "Child 1", parents = listOf("1".repeat(40)))
            val child2 = createBasicCommitVec("3".repeat(40), message = "Child 2", parents = listOf("1".repeat(40)))
            val merge = createBasicCommitVec(
                "4".repeat(40),
                message = "Merge",
                parents = listOf("2".repeat(40), "3".repeat(40))
            )

            val result = listOf(root, child1, child2, merge).toDomain(repository)

            val rootCommit = result.find { it.sha == "1".repeat(40) }!!
            val mergeCommit = result.find { it.sha == "4".repeat(40) }!!

            // Root should have 2 children (both branches)
            assertThat(rootCommit.children).hasSize(2)
            // Merge should have 2 parents
            assertThat(mergeCommit.parents).hasSize(2)
        }

        @Test
        fun `batch toDomain handles cyclic prevention - commit cannot be its own parent`() {
            // This should be prevented by Commit.parents.add validation
            val selfParentVec = createBasicCommitVec(
                sha = validSha,
                message = "Self parent",
                parents = listOf(validSha)
            )

            assertThrows<IllegalArgumentException> {
                listOf(selfParentVec).toDomain(repository)
            }
        }

        @Test
        fun `batch toDomain identity preservation - same SHA appears once in result`() {
            val vec1 = createBasicCommitVec(validSha, message = "First appearance")
            val vec2 = createBasicCommitVec(validSha, message = "Second appearance")

            val result = listOf(vec1, vec2).toDomain(repository)

            // Should preserve order but deduplicate by identity
            assertThat(result).hasSize(2)
            assertThat(result[0]).isSameAs(result[1]) // Same identity
            assertThat(result[0].message).isEqualTo("First appearance") // First wins
        }

        @Test
        fun `batch toDomain enriches existing commit with author`() {
            val testCommitter = User(name = "Test Committer", repository = repository)
            val existingCommit = Commit(
                sha = validSha,
                commitDateTime = LocalDateTime.of(2023, 1, 1, 0, 0),
                repository = repository,
                committer = testCommitter
            )
            assertThat(existingCommit.author).isNull()

            val vec = GixCommit(
                oid = validSha,
                message = "test",
                author = GixSignature("Author", "a@test.com", testTime),
                committer = GixSignature("Committer", "c@test.com", testTime),
                branch = null,
                parents = emptyList(),
                fileTree = emptyList()
            )

            val result = listOf(vec).toDomain(repository)

            assertThat(result[0]).isSameAs(existingCommit)
            assertThat(result[0].author?.name).isEqualTo("Author")
            assertThat(result[0].committer.name).isEqualTo("Test Committer")
        }

        @Test
        fun `batch toDomain handles linear history chain`() {
            val commit1 = createBasicCommitVec("1".repeat(40), message = "Initial")
            val commit2 = createBasicCommitVec("2".repeat(40), message = "Second", parents = listOf("1".repeat(40)))
            val commit3 = createBasicCommitVec("3".repeat(40), message = "Third", parents = listOf("2".repeat(40)))

            val result = listOf(commit1, commit2, commit3).toDomain(repository)

            val c1 = result.find { it.sha == "1".repeat(40) }!!
            val c2 = result.find { it.sha == "2".repeat(40) }!!
            val c3 = result.find { it.sha == "3".repeat(40) }!!

            assertThat(c2.parents).containsOnly(c1)
            assertThat(c3.parents).containsOnly(c2)
            assertThat(c1.children).containsOnly(c2)
            assertThat(c2.children).containsOnly(c3)
        }
    }

    // ========== Edge Cases ==========

    @Nested
    inner class EdgeCases {

        @Test
        fun `handles all lowercase SHA`() {
            val sha = "abcdef1234567890" + "a".repeat(24)
            val vec = createBasicCommitVec(sha)

            val result = vec.toDomain(repository)

            assertThat(result.sha).isEqualTo(sha)
        }

        @Test
        fun `handles all uppercase SHA`() {
            val sha = "ABCDEF1234567890" + "A".repeat(24)
            val vec = createBasicCommitVec(sha)

            val result = vec.toDomain(repository)

            assertThat(result.sha).isEqualTo(sha)
        }

        @Test
        fun `handles mixed case SHA`() {
            val sha = "AbCdEf1234567890" + "aA".repeat(12)
            val vec = createBasicCommitVec(sha)

            val result = vec.toDomain(repository)

            assertThat(result.sha).isEqualTo(sha)
        }

        @ParameterizedTest
        @ValueSource(strings = ["0000000000000000000000000000000000000000", "ffffffffffffffffffffffffffffffffffffffff"])
        fun `handles boundary SHA values`(sha: String) {
            val vec = createBasicCommitVec(sha)

            val result = vec.toDomain(repository)

            assertThat(result.sha).isEqualTo(sha)
        }

        @Test
        fun `handles very long commit message`() {
            val longMessage = "A".repeat(10000)
            val vec = createBasicCommitVec(validSha, message = longMessage)

            val result = vec.toDomain(repository)

            assertThat(result.message).isEqualTo(longMessage)
        }

        @Test
        fun `handles commit with special characters in message`() {
            val message = "Fix: 修复 bug\n\nDescription: Émojis 🎉 and symbols !@#$%^&*()"
            val vec = createBasicCommitVec(validSha, message = message)

            val result = vec.toDomain(repository)

            assertThat(result.message).isEqualTo(message)
        }
    }

    // ========== Helper Functions ==========

    private fun createBasicCommitVec(
        sha: String,
        message: String = "Test message",
        parents: List<String> = emptyList()
    ): GixCommit {
        return GixCommit(
            oid = sha,
            message = message,
            author = null,
            committer = GixSignature("Test Committer", "test@example.com", testTime),
            branch = null,
            parents = parents,
            fileTree = emptyList()
        )
    }
}
