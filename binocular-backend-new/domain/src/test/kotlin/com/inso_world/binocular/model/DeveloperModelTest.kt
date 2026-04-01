package com.inso_world.binocular.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.uuid.ExperimentalUuidApi

/**
 * BDD tests for Developer domain model.
 * Developer represents a git user scoped to a Repository, extending Stakeholder.
 */
@OptIn(ExperimentalUuidApi::class)
class DeveloperModelTest {

    private lateinit var repository: Repository

    @BeforeEach
    fun setUp() {
        val project = Project(name = "test-project")
        repository = Repository(localPath = "test-repo", project = project)
    }

    @Nested
    inner class Construction {

        @Test
        fun `given valid name and email, when creating developer, then it should be created with iid`() {
            // Given
            val name = "John Doe"
            val email = "john@example.com"

            // When
            val developer = Developer(name = name, email = email, repository = repository)

            // Then
            assertAll(
                { assertThat(developer.name).isEqualTo(name) },
                { assertThat(developer.email).isEqualTo(email) },
                { assertThat(developer.repository).isSameAs(repository) },
                { assertThat(developer.iid).isNotNull() }
            )
        }

        @Test
        fun `given developer creation, when checking repository link, then developer should be in repository developers`() {
            // Given & When
            val developer = Developer(name = "Jane", email = "jane@example.com", repository = repository)

            // Then
            assertThat(repository.developers).contains(developer)
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.domain.data.DummyTestData#provideBlankStrings")
        fun `given blank name, when creating developer, then it should throw IllegalArgumentException`(name: String) {
            // When & Then
            assertThrows<IllegalArgumentException> {
                Developer(name = name, email = "test@example.com", repository = repository)
            }
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.domain.data.DummyTestData#provideBlankStrings")
        fun `given blank email, when creating developer, then it should throw IllegalArgumentException`(email: String) {
            // When & Then
            assertThrows<IllegalArgumentException> {
                Developer(name = "Test", email = email, repository = repository)
            }
        }
    }

    @Nested
    inner class UniqueKey {

        @Test
        fun `given developer, when accessing uniqueKey, then it should contain repositoryId and gitSignature`() {
            // Given
            val developer = Developer(name = "Test User", email = "test@example.com", repository = repository)

            // When
            val key = developer.uniqueKey

            // Then
            assertAll(
                { assertThat(key.repositoryId).isEqualTo(repository.iid) },
                { assertThat(key.gitSignature).isEqualTo("Test User <test@example.com>") }
            )
        }
    }

    @Nested
    inner class GitSignature {

        @Test
        fun `given developer with name and email, when getting gitSignature, then it should return formatted signature`() {
            // Given
            val developer = Developer(name = "John Doe", email = "john@example.com", repository = repository)

            // When
            val signature = developer.gitSignature

            // Then
            assertThat(signature).isEqualTo("John Doe <john@example.com>")
        }

        @Test
        fun `given developer with whitespace in name, when getting gitSignature, then it should trim the name`() {
            // Given
            val developer = Developer(name = "  John Doe  ", email = "john@example.com", repository = repository)

            // When
            val signature = developer.gitSignature

            // Then
            assertThat(signature).isEqualTo("John Doe <john@example.com>")
        }
    }

    @Nested
    inner class Equality {

        @Test
        fun `given same developer instance, when comparing with equals, then it should be equal`() {
            // Given
            val developer = Developer(name = "Test", email = "test@example.com", repository = repository)

            // Then
            assertThat(developer).isEqualTo(developer)
        }

        @Test
        fun `given two different developers, when comparing, then they should not be equal`() {
            // Given
            val developer1 = Developer(name = "Test1", email = "test1@example.com", repository = repository)
            val developer2 = Developer(name = "Test2", email = "test2@example.com", repository = repository)

            // Then
            assertThat(developer1).isNotEqualTo(developer2)
        }

        @Test
        fun `given developer, when getting hashCode, then it should be based on iid`() {
            // Given
            val developer = Developer(name = "Test", email = "test@example.com", repository = repository)

            // Then
            assertThat(developer.hashCode()).isEqualTo(developer.iid.hashCode())
        }
    }

    @Nested
    inner class InheritanceFromStakeholder {

        @Test
        fun `given developer, when checking inheritance, then it should be instance of Stakeholder`() {
            // Given
            val developer = Developer(name = "Test", email = "test@example.com", repository = repository)

            // Then
            assertThat(developer).isInstanceOf(Stakeholder::class.java)
        }
    }

    @Nested
    inner class CommitRelations {

        @Nested
        inner class AuthoredCommits {

            @Test
            fun `given new developer, when checking authoredCommits, then it should be empty`() {
                // Given
                val developer = Developer(name = "Test", email = "test@example.com", repository = repository)

                // Then
                assertThat(developer.authoredCommits).isEmpty()
            }
        }

        @Nested
        inner class CommittedCommits {

            @Test
            fun `given new developer, when checking committedCommits, then it should be empty`() {
                // Given
                val developer = Developer(name = "Test", email = "test@example.com", repository = repository)

                // Then
                assertThat(developer.committedCommits).isEmpty()
            }
        }
    }

    @Nested
    inner class FileAndIssueRelations {

        @Test
        fun `given new developer, when checking files, then it should be empty`() {
            // Given
            val developer = Developer(name = "Test", email = "test@example.com", repository = repository)

            // Then
            assertThat(developer.files).isEmpty()
        }

        @Test
        fun `given new developer, when checking issues, then it should be empty`() {
            // Given
            val developer = Developer(name = "Test", email = "test@example.com", repository = repository)

            // Then
            assertThat(developer.issues).isEmpty()
        }
    }
}
