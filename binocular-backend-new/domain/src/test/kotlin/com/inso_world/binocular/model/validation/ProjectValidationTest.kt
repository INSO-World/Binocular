package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProjectValidationTest {
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
    fun `should pass validation when project id is null and repository projectId is null`() {
        // Given
        val project = Project(id = null, name = "test-project")
        val repository =
            Repository(
                id = null,
                name = "test-repo",
                project = project,
            )
        project.repo = repository

        // When
        val violations = validator.validate(project)

        // Then
        assertThat(violations).isEmpty()
    }

    @Test
    fun `should fail validation when project id is null but repository projectId is not null`() {
        // Given
        val project = Project(id = null, name = "test-project")
        val repository =
            Repository(
                id = null,
                name = "test-repo",
                project = Project(id = "some-id", name = "test-project")
            )
        project.repo = repository

        // When
        val violations = validator.validate(project)

        // Then
        assertThat(violations).isNotEmpty()
        assertThat(violations.any { it.message.contains("Repository must reference back") }).isTrue()
    }

    @Test
    fun `should pass validation when project id is not null and repository projectId matches`() {
        // Given
        val project = Project(id = "123", name = "test-project")
        val repository =
            Repository(
                id = null,
                name = "test-repo",
                project = project,
            )
        project.repo = repository

        // When
        val violations = validator.validate(project)

        // Then
        assertThat(violations).isEmpty()
    }

    @Test
    fun `should fail validation when project id is not null but repository projectId does not match`() {
        // Given
        val project = Project(id = "project-123", name = "test-project")
        val repository =
            Repository(
                id = null,
                name = "test-repo",
                project = Project(id = "different-id", name = "test-project"),
            )
        project.repo = repository

        // When
        val violations = validator.validate(project)

        // Then
        assertThat(violations).isNotEmpty()
        assertThat(violations.any { it.message.contains("Repository must reference back") }).isTrue()
    }

    @Test
    fun `should pass validation when repository is null`() {
        // Given
        val project = Project(id = "project-123", name = "test-project", repo = null)

        // When
        val violations = validator.validate(project)

        // Then
        assertThat(violations).isEmpty()
    }
}
