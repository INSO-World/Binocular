package com.inso_world.binocular.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi

/**
 * BDD tests for Commit model with new Signature-based author/committer semantics.
 *
 * Key changes from previous model:
 * - author is now required (via authorSignature)
 * - committer is optional (via committerSignature), defaults to author if not provided
 * - Both use Signature value objects containing Developer.Id + timestamp
 */
@OptIn(ExperimentalUuidApi::class)
class CommitSignatureModelTest {

    private lateinit var repository: Repository
    private lateinit var author: Developer
    private lateinit var committer: Developer

    @BeforeEach
    fun setUp() {
        val project = Project(name = "test-project")
        repository = Repository(localPath = "test-repo", projectId = project.iid)
        author = Developer(name = "Author Name", email = "author@example.com")
        committer = Developer(name = "Committer Name", email = "committer@example.com")
    }

    @Nested
    inner class AuthorSignature {

        @Test
        fun `given valid authorSignature, when creating commit, then author should be set`() {
            val authorTimestamp = LocalDateTime.now().minusSeconds(10)
            val authorSignature = Signature(
                developerId = author.iid,
                gitSignature = author.gitSignature,
                timestamp = authorTimestamp,
            )

            val commit = Commit(
                sha = "a".repeat(40),
                authorSignature = authorSignature,
                repositoryId = repository.iid,
            )

            assertAll(
                { assertThat(commit.authorSignature).isEqualTo(authorSignature) },
                { assertThat(commit.authorSignature.developerId).isEqualTo(author.iid) },
                { assertThat(commit.authorSignature.timestamp).isEqualTo(authorTimestamp) }
            )
        }

        @Test
        fun `given commit with authorSignature, when authorId is accessed via convenience property, then it should return the developer id`() {
            val authorSignature = Signature(
                developerId = author.iid,
                gitSignature = author.gitSignature,
                timestamp = LocalDateTime.now().minusSeconds(1),
            )
            val commit = Commit(
                sha = "b".repeat(40),
                authorSignature = authorSignature,
                repositoryId = repository.iid,
            )

            assertThat(commit.authorId).isEqualTo(author.iid)
        }
    }

    @Nested
    inner class CommitterSignature {

        @Test
        fun `given no committerSignature, when creating commit, then committer should default to author`() {
            val authorSignature = Signature(
                developerId = author.iid,
                gitSignature = author.gitSignature,
                timestamp = LocalDateTime.now().minusSeconds(1),
            )

            val commit = Commit(
                sha = "d".repeat(40),
                authorSignature = authorSignature,
                repositoryId = repository.iid,
            )

            assertAll(
                { assertThat(commit.committerSignature).isEqualTo(authorSignature) },
                { assertThat(commit.committerId).isEqualTo(author.iid) }
            )
        }

        @Test
        fun `given explicit committerSignature, when creating commit, then committer should be different from author`() {
            val authorTimestamp = LocalDateTime.now().minusSeconds(10)
            val committerTimestamp = LocalDateTime.now().minusSeconds(5)
            val authorSignature = Signature(
                developerId = author.iid,
                gitSignature = author.gitSignature,
                timestamp = authorTimestamp,
            )
            val committerSignature = Signature(
                developerId = committer.iid,
                gitSignature = committer.gitSignature,
                timestamp = committerTimestamp,
            )

            val commit = Commit(
                sha = "e".repeat(40),
                authorSignature = authorSignature,
                committerSignature = committerSignature,
                repositoryId = repository.iid,
            )

            assertAll(
                { assertThat(commit.authorSignature).isEqualTo(authorSignature) },
                { assertThat(commit.committerSignature).isEqualTo(committerSignature) },
                { assertThat(commit.authorId).isEqualTo(author.iid) },
                { assertThat(commit.committerId).isEqualTo(committer.iid) },
                { assertThat(commit.authorId).isNotEqualTo(commit.committerId) }
            )
        }

        @Test
        fun `given same person as author and committer, when creating commit, then both should reference same developer`() {
            val authorTimestamp = LocalDateTime.now().minusSeconds(10)
            val committerTimestamp = LocalDateTime.now().minusSeconds(5)
            val authorSignature = Signature(
                developerId = author.iid,
                gitSignature = author.gitSignature,
                timestamp = authorTimestamp,
            )
            val committerSignature = Signature(
                developerId = author.iid,
                gitSignature = author.gitSignature,
                timestamp = committerTimestamp,
            )

            val commit = Commit(
                sha = "1".repeat(40),
                authorSignature = authorSignature,
                committerSignature = committerSignature,
                repositoryId = repository.iid,
            )

            assertAll(
                { assertThat(commit.authorId).isEqualTo(commit.committerId) },
                { assertThat(commit.authorSignature.timestamp).isNotEqualTo(commit.committerSignature.timestamp) }
            )
        }
    }

    @Nested
    inner class CommitDateTime {

        @Test
        fun `given commit without explicit commitDateTime, when accessing commitDateTime, then it should use committerSignature timestamp`() {
            val authorTimestamp = LocalDateTime.now().minusSeconds(10)
            val committerTimestamp = LocalDateTime.now().minusSeconds(5)
            val authorSignature = Signature(
                developerId = author.iid,
                gitSignature = author.gitSignature,
                timestamp = authorTimestamp,
            )
            val committerSignature = Signature(
                developerId = committer.iid,
                gitSignature = committer.gitSignature,
                timestamp = committerTimestamp,
            )

            val commit = Commit(
                sha = "4".repeat(40),
                authorSignature = authorSignature,
                committerSignature = committerSignature,
                repositoryId = repository.iid,
            )

            assertThat(commit.commitDateTime).isEqualTo(committerTimestamp)
        }

        @Test
        fun `given commit without committerSignature, when accessing commitDateTime, then it should use authorSignature timestamp`() {
            val authorTimestamp = LocalDateTime.now().minusSeconds(5)
            val authorSignature = Signature(
                developerId = author.iid,
                gitSignature = author.gitSignature,
                timestamp = authorTimestamp,
            )

            val commit = Commit(
                sha = "5".repeat(40),
                authorSignature = authorSignature,
                repositoryId = repository.iid,
            )

            assertThat(commit.commitDateTime).isEqualTo(authorTimestamp)
        }
    }

    @Nested
    inner class AuthorDateTime {

        @Test
        fun `given commit, when accessing authorDateTime, then it should return authorSignature timestamp`() {
            val authorTimestamp = LocalDateTime.now().minusSeconds(10)
            val authorSignature = Signature(
                developerId = author.iid,
                gitSignature = author.gitSignature,
                timestamp = authorTimestamp,
            )

            val commit = Commit(
                sha = "6".repeat(40),
                authorSignature = authorSignature,
                repositoryId = repository.iid,
            )

            assertThat(commit.authorDateTime).isEqualTo(authorTimestamp)
        }
    }
}
