package com.inso_world.binocular.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi

/**
 * BDD tests for Commit model with new Signature-based author/committer semantics.
 *
 * Key changes from previous model:
 * - author is now required (via authorSignature)
 * - committer is optional (via committerSignature), defaults to author if not provided
 * - Both use Signature value objects containing Developer + timestamp
 */
@OptIn(ExperimentalUuidApi::class)
class CommitSignatureModelTest {

    private lateinit var repository: Repository
    private lateinit var author: Developer
    private lateinit var committer: Developer

    @BeforeEach
    fun setUp() {
        val project = Project(name = "test-project")
        repository = Repository(localPath = "test-repo", project = project)
        author = Developer(name = "Author Name", email = "author@example.com", repository = repository)
        committer = Developer(name = "Committer Name", email = "committer@example.com", repository = repository)
    }

    @Nested
    inner class AuthorSignature {

        @Test
        fun `given valid authorSignature, when creating commit, then author should be set`() {
            // Given
            val authorTimestamp = LocalDateTime.now().minusSeconds(10)
            val authorSignature = Signature(developer = author, timestamp = authorTimestamp)

            // When
            val commit = Commit(
                sha = "a".repeat(40),
                authorSignature = authorSignature,
                repository = repository
            )

            // Then
            assertAll(
                { assertThat(commit.authorSignature).isEqualTo(authorSignature) },
                { assertThat(commit.authorSignature.developer).isSameAs(author) },
                { assertThat(commit.authorSignature.timestamp).isEqualTo(authorTimestamp) }
            )
        }

        @Test
        fun `given commit with authorSignature, when author is accessed via convenience property, then it should return the developer`() {
            // Given
            val authorSignature = Signature(developer = author, timestamp = LocalDateTime.now().minusSeconds(1))
            val commit = Commit(
                sha = "b".repeat(40),
                authorSignature = authorSignature,
                repository = repository
            )

            // When
            val commitAuthor = commit.author

            // Then
            assertThat(commitAuthor).isSameAs(author)
        }

        @Test
        fun `given commit creation, when checking author's authoredCommits, then commit should be added`() {
            // Given
            val authorSignature = Signature(developer = author, timestamp = LocalDateTime.now().minusSeconds(1))

            // When
            val commit = Commit(
                sha = "c".repeat(40),
                authorSignature = authorSignature,
                repository = repository
            )

            // Then
            assertThat(author.authoredCommits).contains(commit)
        }
    }

    @Nested
    inner class CommitterSignature {

        @Test
        fun `given no committerSignature, when creating commit, then committer should default to author`() {
            // Given
            val authorSignature = Signature(developer = author, timestamp = LocalDateTime.now().minusSeconds(1))

            // When
            val commit = Commit(
                sha = "d".repeat(40),
                authorSignature = authorSignature,
                repository = repository
                // committerSignature not provided
            )

            // Then
            assertAll(
                { assertThat(commit.committerSignature).isEqualTo(authorSignature) },
                { assertThat(commit.committer).isSameAs(author) }
            )
        }

        @Test
        fun `given explicit committerSignature, when creating commit, then committer should be different from author`() {
            // Given
            val authorTimestamp = LocalDateTime.now().minusSeconds(10)
            val committerTimestamp = LocalDateTime.now().minusSeconds(5)
            val authorSignature = Signature(developer = author, timestamp = authorTimestamp)
            val committerSignature = Signature(developer = committer, timestamp = committerTimestamp)

            // When
            val commit = Commit(
                sha = "e".repeat(40),
                authorSignature = authorSignature,
                committerSignature = committerSignature,
                repository = repository
            )

            // Then
            assertAll(
                { assertThat(commit.authorSignature).isEqualTo(authorSignature) },
                { assertThat(commit.committerSignature).isEqualTo(committerSignature) },
                { assertThat(commit.author).isSameAs(author) },
                { assertThat(commit.committer).isSameAs(committer) },
                { assertThat(commit.author).isNotSameAs(commit.committer) }
            )
        }

        @Test
        fun `given commit with explicit committerSignature, when checking committer's committedCommits, then commit should be added`() {
            // Given
            val authorSignature = Signature(developer = author, timestamp = LocalDateTime.now().minusSeconds(10))
            val committerSignature = Signature(developer = committer, timestamp = LocalDateTime.now().minusSeconds(5))

            // When
            val commit = Commit(
                sha = "f".repeat(40),
                authorSignature = authorSignature,
                committerSignature = committerSignature,
                repository = repository
            )

            // Then
            assertThat(committer.committedCommits).contains(commit)
        }

        @Test
        fun `given same person as author and committer, when creating commit, then both should reference same developer`() {
            // Given
            val authorTimestamp = LocalDateTime.now().minusSeconds(10)
            val committerTimestamp = LocalDateTime.now().minusSeconds(5)
            val authorSignature = Signature(developer = author, timestamp = authorTimestamp)
            val committerSignature = Signature(developer = author, timestamp = committerTimestamp) // same developer

            // When
            val commit = Commit(
                sha = "1".repeat(40),
                authorSignature = authorSignature,
                committerSignature = committerSignature,
                repository = repository
            )

            // Then
            assertAll(
                { assertThat(commit.author).isSameAs(commit.committer) },
                { assertThat(commit.authorSignature.timestamp).isNotEqualTo(commit.committerSignature!!.timestamp) }
            )
        }
    }

    @Nested
    inner class RepositoryConsistency {

        @Test
        fun `given authorSignature with developer from different repository, when creating commit, then it should throw`() {
            // Given
            val otherProject = Project(name = "other-project")
            val otherRepository = Repository(localPath = "other-repo", project = otherProject)
            val otherDeveloper = Developer(name = "Other", email = "other@example.com", repository = otherRepository)
            val authorSignature = Signature(developer = otherDeveloper, timestamp = LocalDateTime.now().minusSeconds(1))

            // When & Then
            assertThrows<IllegalArgumentException> {
                Commit(
                    sha = "2".repeat(40),
                    authorSignature = authorSignature,
                    repository = repository
                )
            }
        }

        @Test
        fun `given committerSignature with developer from different repository, when creating commit, then it should throw`() {
            // Given
            val otherProject = Project(name = "other-project")
            val otherRepository = Repository(localPath = "other-repo", project = otherProject)
            val otherDeveloper = Developer(name = "Other", email = "other@example.com", repository = otherRepository)
            val authorSignature = Signature(developer = author, timestamp = LocalDateTime.now().minusSeconds(10))
            val committerSignature = Signature(developer = otherDeveloper, timestamp = LocalDateTime.now().minusSeconds(5))

            // When & Then
            assertThrows<IllegalArgumentException> {
                Commit(
                    sha = "3".repeat(40),
                    authorSignature = authorSignature,
                    committerSignature = committerSignature,
                    repository = repository
                )
            }
        }
    }

    @Nested
    inner class CommitDateTime {

        @Test
        fun `given commit without explicit commitDateTime, when accessing commitDateTime, then it should use committerSignature timestamp`() {
            // Given
            val authorTimestamp = LocalDateTime.now().minusSeconds(10)
            val committerTimestamp = LocalDateTime.now().minusSeconds(5)
            val authorSignature = Signature(developer = author, timestamp = authorTimestamp)
            val committerSignature = Signature(developer = committer, timestamp = committerTimestamp)

            // When
            val commit = Commit(
                sha = "4".repeat(40),
                authorSignature = authorSignature,
                committerSignature = committerSignature,
                repository = repository
            )

            // Then
            assertThat(commit.commitDateTime).isEqualTo(committerTimestamp)
        }

        @Test
        fun `given commit without committerSignature, when accessing commitDateTime, then it should use authorSignature timestamp`() {
            // Given
            val authorTimestamp = LocalDateTime.now().minusSeconds(5)
            val authorSignature = Signature(developer = author, timestamp = authorTimestamp)

            // When
            val commit = Commit(
                sha = "5".repeat(40),
                authorSignature = authorSignature,
                repository = repository
            )

            // Then
            assertThat(commit.commitDateTime).isEqualTo(authorTimestamp)
        }
    }

    @Nested
    inner class AuthorDateTime {

        @Test
        fun `given commit, when accessing authorDateTime, then it should return authorSignature timestamp`() {
            // Given
            val authorTimestamp = LocalDateTime.now().minusSeconds(10)
            val authorSignature = Signature(developer = author, timestamp = authorTimestamp)

            // When
            val commit = Commit(
                sha = "6".repeat(40),
                authorSignature = authorSignature,
                repository = repository
            )

            // Then
            assertThat(commit.authorDateTime).isEqualTo(authorTimestamp)
        }
    }

    @Nested
    inner class BackwardsCompatibility {

        @Test
        fun `given commit, when accessing deprecated author property, then it should return authorSignature developer`() {
            // Given
            val authorSignature = Signature(developer = author, timestamp = LocalDateTime.now().minusSeconds(1))
            val commit = Commit(
                sha = "7".repeat(40),
                authorSignature = authorSignature,
                repository = repository
            )

            // When & Then
            assertThat(commit.author).isSameAs(author)
        }

        @Test
        fun `given commit, when accessing deprecated committer property, then it should return committerSignature developer`() {
            // Given
            val authorSignature = Signature(developer = author, timestamp = LocalDateTime.now().minusSeconds(10))
            val committerSignature = Signature(developer = committer, timestamp = LocalDateTime.now().minusSeconds(5))
            val commit = Commit(
                sha = "8".repeat(40),
                authorSignature = authorSignature,
                committerSignature = committerSignature,
                repository = repository
            )

            // When & Then
            assertThat(commit.committer).isSameAs(committer)
        }
    }
}
