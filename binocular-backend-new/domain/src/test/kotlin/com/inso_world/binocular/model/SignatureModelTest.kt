package com.inso_world.binocular.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi

/**
 * BDD tests for Signature value class.
 * Signature represents a Git signature consisting of a Developer and a timestamp,
 * used for author and committer information in commits.
 */
@OptIn(ExperimentalUuidApi::class)
class SignatureModelTest {

    private lateinit var repository: Repository
    private lateinit var developer: Developer

    @BeforeEach
    fun setUp() {
        val project = Project(name = "test-project")
        repository = Repository(localPath = "test-repo", project = project)
        developer = Developer(name = "John Doe", email = "john@example.com", repository = repository)
    }

    @Nested
    inner class Construction {

        @Test
        fun `given valid developer and timestamp, when creating signature, then it should be created successfully`() {
            // Given
            val timestamp = LocalDateTime.now().minusSeconds(1)

            // When
            val signature = Signature(developer = developer, timestamp = timestamp)

            // Then
            assertAll(
                { assertThat(signature.developer).isSameAs(developer) },
                { assertThat(signature.timestamp).isEqualTo(timestamp) }
            )
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.data.DummyTestData#provideAllowedPastOrPresentDateTime")
        fun `given valid past or present timestamp, when creating signature, then it should succeed`(timestamp: LocalDateTime) {
            // When & Then
            val signature = Signature(developer = developer, timestamp = timestamp)
            assertThat(signature.timestamp).isEqualTo(timestamp)
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.data.DummyTestData#provideInvalidPastOrPresentDateTime")
        fun `given future timestamp, when creating signature, then it should throw IllegalArgumentException`(timestamp: LocalDateTime) {
            // When & Then
            assertThrows<IllegalArgumentException> {
                Signature(developer = developer, timestamp = timestamp)
            }
        }
    }

    @Nested
    inner class ValueSemantics {

        @Test
        fun `given two signatures with same developer and timestamp, when comparing, then they should be equal`() {
            // Given
            val timestamp = LocalDateTime.of(2024, 1, 1, 12, 0, 0)
            val signature1 = Signature(developer = developer, timestamp = timestamp)
            val signature2 = Signature(developer = developer, timestamp = timestamp)

            // Then
            assertThat(signature1).isEqualTo(signature2)
        }

        @Test
        fun `given two signatures with different timestamps, when comparing, then they should not be equal`() {
            // Given
            val signature1 = Signature(developer = developer, timestamp = LocalDateTime.of(2024, 1, 1, 12, 0, 0))
            val signature2 = Signature(developer = developer, timestamp = LocalDateTime.of(2024, 1, 1, 13, 0, 0))

            // Then
            assertThat(signature1).isNotEqualTo(signature2)
        }

        @Test
        fun `given two signatures with different developers, when comparing, then they should not be equal`() {
            // Given
            val developer2 = Developer(name = "Jane", email = "jane@example.com", repository = repository)
            val timestamp = LocalDateTime.of(2024, 1, 1, 12, 0, 0)
            val signature1 = Signature(developer = developer, timestamp = timestamp)
            val signature2 = Signature(developer = developer2, timestamp = timestamp)

            // Then
            assertThat(signature1).isNotEqualTo(signature2)
        }

        @Test
        fun `given same signature values, when getting hashCode, then they should be equal`() {
            // Given
            val timestamp = LocalDateTime.of(2024, 1, 1, 12, 0, 0)
            val signature1 = Signature(developer = developer, timestamp = timestamp)
            val signature2 = Signature(developer = developer, timestamp = timestamp)

            // Then
            assertThat(signature1.hashCode()).isEqualTo(signature2.hashCode())
        }
    }

    @Nested
    inner class GitSignatureFormat {

        @Test
        fun `given signature, when getting gitSignature, then it should return formatted string`() {
            // Given
            val timestamp = LocalDateTime.of(2024, 6, 15, 14, 30, 0)
            val signature = Signature(developer = developer, timestamp = timestamp)

            // When
            val gitSig = signature.gitSignature

            // Then - should contain developer's name and email
            assertThat(gitSig).isEqualTo("John Doe <john@example.com>")
        }
    }

    @Nested
    inner class Immutability {

        @Test
        fun `given signature, when accessing properties, then they should be immutable`() {
            // Given
            val timestamp = LocalDateTime.of(2024, 1, 1, 12, 0, 0)
            val signature = Signature(developer = developer, timestamp = timestamp)

            // Then - Signature is a data class with val properties, proving immutability
            assertAll(
                { assertThat(signature.developer).isSameAs(developer) },
                { assertThat(signature.timestamp).isEqualTo(timestamp) }
            )
        }
    }
}
