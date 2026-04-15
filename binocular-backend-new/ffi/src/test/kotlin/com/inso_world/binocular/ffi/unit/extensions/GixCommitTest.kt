package com.inso_world.binocular.ffi.unit.extensions

import com.inso_world.binocular.core.unit.base.BaseUnitTest
import com.inso_world.binocular.ffi.extensions.toDomain
import com.inso_world.binocular.ffi.internal.GixCommit
import com.inso_world.binocular.ffi.internal.GixSignature
import com.inso_world.binocular.ffi.internal.GixTime
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.time.ZoneOffset

class GixCommitTest : BaseUnitTest() {

    private lateinit var repository: Repository

    private val validSha = "a".repeat(40)
    private val parentSha = "b".repeat(40)
    private val authorTime = GixTime(seconds = 1704067200L, offset = 0) // 2024-01-01
    private val committerTime = GixTime(seconds = 1704153600L, offset = 0) // 2024-01-02

    @BeforeEach
    fun setUp() {
        repository = Repository(
            localPath = "/path/to/repo",
            project = Project(name = "test-project")
        )
    }

    private fun gixCommit(
        sha: String = validSha,
        message: String = "msg",
        author: GixSignature = GixSignature("Author", "author@test.com", authorTime),
        committer: GixSignature = GixSignature("Committer", "committer@test.com", committerTime),
        parents: List<String> = emptyList(),
    ): GixCommit =
        GixCommit(
            oid = sha,
            message = message,
            committer = committer,
            author = author,
            branch = null,
            parents = parents,
            fileTree = emptyList()
        )

    private fun GixTime.toLocalDateTime() =
        Instant.ofEpochSecond(this.seconds)
            .atOffset(ZoneOffset.ofTotalSeconds(this.offset.coerceIn(-18 * 3600, 18 * 3600)))
            .toLocalDateTime()

    @Nested
    inner class SingleItemMapper {

        @Test
        fun `creates commit with author and committer signatures`() {
            val result = gixCommit().toDomain(repository)

            assertThat(result.sha).isEqualTo(validSha)
            assertThat(result.author.name).isEqualTo("Author")
            assertThat(result.committer.name).isEqualTo("Committer")
            assertThat(result.authorDateTime).isEqualTo(authorTime.toLocalDateTime())
            assertThat(result.commitDateTime).isEqualTo(committerTime.toLocalDateTime())
            assertThat(repository.commits).contains(result)
            assertThat(repository.developers).hasSize(2)
        }

        @Test
        fun `defaults committer to author when signatures match`() {
            val sig = GixSignature("Same Person", "same@test.com", authorTime)

            val result = gixCommit(author = sig, committer = sig).toDomain(repository)

            assertThat(result.author).isSameAs(result.committer)
            assertThat(result.commitDateTime).isEqualTo(authorTime.toLocalDateTime())
        }

        @Test
        fun `reuses existing commit by sha`() {
            val existing = Commit(
                sha = validSha,
                authorSignature = com.inso_world.binocular.model.Signature(
                    developer = com.inso_world.binocular.model.Developer(
                        name = "Existing",
                        email = "existing@test.com",
                        repository = repository
                    ),
                    timestamp = authorTime.toLocalDateTime()
                ),
                repository = repository
            )

            val result = gixCommit().toDomain(repository)

            assertThat(result).isSameAs(existing)
        }

        @Test
        fun `rejects invalid sha`() {
            val invalid = gixCommit(sha = "short")

            assertThrows<IllegalArgumentException> {
                invalid.toDomain(repository)
            }
        }
    }

    @Nested
    inner class BatchMapper {

        @Test
        fun `wires parents bidirectionally`() {
            val parentVec = gixCommit(sha = parentSha)
            val childVec = gixCommit(sha = validSha, parents = listOf(parentSha))

            val results = listOf(parentVec, childVec).toDomain(repository)

            val parent = results.first { it.sha == parentSha }
            val child = results.first { it.sha == validSha }

            assertThat(child.parents).containsExactly(parent)
            assertThat(parent.children).containsExactly(child)
        }
    }
}
