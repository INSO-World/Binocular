package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.LocalDateTime

class CommitValidationTest {
    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        validator =
            Validation
                .byDefaultProvider()
                .configure()
                .messageInterpolator(ParameterMessageInterpolator())
                .buildValidatorFactory()
                .validator
    }

    @Test
    fun `should pass validation when repository id is null and repositoryId is null`() {
        // Given
        val repository = Repository(id = null, name = "test-repo")
        val commit =
            Commit(
                sha = "a".repeat(40),
                message = "message",
                commitDateTime = LocalDateTime.now(),
                repository = repository,
                repositoryId = null,
            )

        // When
        val violations = validator.validate(commit)

        // Then
        assertThat(violations).isEmpty()
    }

    @Test
    fun `should fail validation when repository id is null but repositoryId is not null`() {
        // Given
        val repository = Repository(id = null, name = "test-repo")
        val commit =
            Commit(
                sha = "a".repeat(40),
                message = "message",
                commitDateTime = LocalDateTime.now(),
                repository = repository,
                repositoryId = "some-id",
            )

        // When
        val violations = validator.validate(commit)

        // Then
        assertAll(
            { assertThat(violations).isNotEmpty() },
            { assertThat(violations).hasSize(1) },
            { assertThat(violations.map { it.message }[0]).contains("Repository ID must be null") },
        )
    }

    @Test
    fun `should pass validation when repository id is not null and repositoryId matches`() {
        // Given
        val repository = Repository(id = "repo-123", name = "test-repo")
        val commit =
            Commit(
                sha = "a".repeat(40),
                message = "message",
                commitDateTime = LocalDateTime.now(),
                repository = repository,
                repositoryId = "repo-123",
            )

        // When
        val violations = validator.validate(commit)

        // Then
        assertThat(violations).isEmpty()
    }

    @Test
    fun `should fail validation when repository id is not null but repositoryId does not match`() {
        // Given
        val repository = Repository(id = "repo-123", name = "test-repo")
        val commit =
            Commit(
                sha = "a".repeat(40),
                message = "message",
                commitDateTime = LocalDateTime.now(),
                repository = repository,
                repositoryId = "different-id",
            )

        // When
        val violations = validator.validate(commit)

        // Then
        assertAll(
            { assertFalse(violations.isEmpty(), "Should have validation violations") },
            { assertThat(violations).hasSize(1) },
            { assertThat(violations.map { it.message }[0]).contains("must match repository ID") },
        )
    }

    @Test
    fun `should pass validation when repository is null`() {
        // Given
        val commit =
            Commit(
                message = "test",
                sha = "a".repeat(40),
                commitDateTime = LocalDateTime.now(),
                repository = null,
                repositoryId = "some-id",
            )

        // When
        val violations = validator.validate(commit)

        // Then
        assertThat(violations).isEmpty()
    }
}
