package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RepositoryValidationTest {
    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        val validatorFactory =
            Validation
                .byDefaultProvider()
                .configure()
                .messageInterpolator(ParameterMessageInterpolator())
                .buildValidatorFactory()
        validator = validatorFactory.validator
    }

    @Test
    fun `should pass validation when project id is null and projectId is null`() {
        // Given
        val project = Project(id = null, name = "test-project")
        val repository =
            Repository(
                id = null,
                name = "test-repo",
                project = project,
                projectId = null,
            )

        // When
        val violations = validator.validate(repository)

        // Then
        assertThat(violations).isEmpty()
    }

    @Test
    fun `should fail validation when project id is null but projectId is not null`() {
        // Given
        val project = Project(id = null, name = "test-project")
        val repository =
            Repository(
                id = null,
                name = "test-repo",
                project = project,
                projectId = "some-id",
            )

        // When
        val violations = validator.validate(repository)

        // Then
        assertThat(violations).isNotEmpty()
        assertThat(violations.any { it.message.contains("Project ID must be null") }).isTrue()
    }

    @Test
    fun `should pass validation when project id is not null and projectId matches`() {
        // Given
        val project = Project(id = "project-123", name = "test-project")
        val repository =
            Repository(
                id = null,
                name = "test-repo",
                project = project,
                projectId = "project-123",
            )

        // When
        val violations = validator.validate(repository)

        // Then
        assertThat(violations).isEmpty()
    }

    @Test
    fun `should fail validation when project id is not null but projectId does not match`() {
        // Given
        val project = Project(id = "project-123", name = "test-project")
        val repository =
            Repository(
                id = null,
                name = "test-repo",
                project = project,
                projectId = "different-id",
            )

        // When
        val violations = validator.validate(repository)

        // Then
        assertThat(violations).isNotEmpty()
        assertThat(violations.any { it.message.contains("must match project ID") }).isTrue()
    }

    @Test
    fun `should pass validation when project is null`() {
        // Given
        val repository =
            Repository(
                id = null,
                name = "test-repo",
                project = null,
                projectId = "some-id",
            )

        // When
        val violations = validator.validate(repository)

        // Then
        assertThat(violations).isEmpty()
    }
}
