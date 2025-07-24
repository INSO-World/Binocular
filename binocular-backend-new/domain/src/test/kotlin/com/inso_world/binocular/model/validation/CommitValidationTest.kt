package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import java.util.stream.Stream

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

    companion object {
        @JvmStatic
        fun invalidCommitsModels(): Stream<Arguments> = ValidationTestData.invalidCommitsModels()
    }

    @ParameterizedTest
    @MethodSource("invalidCommitsModels")
    fun `invalid property with valid branch`(
        invalidCommit: Commit,
        propertyPath: String,
    ) {
        val dummyBranch =
            Branch(
                name = "branch",
                repositoryId = "1",
            )
        invalidCommit.branches.add(dummyBranch)
        dummyBranch.commitShas.add(invalidCommit.sha)

        val violation =
            run {
                val violations = validator.validate(invalidCommit)
                assertThat(violations).hasSize(1)
                violations.toList()[0]
            }
        assertThat(violation.propertyPath.toString()).isEqualTo(propertyPath)
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
                repositoryId = repository.id,
                branches = mutableSetOf(Branch(name = "b", commitShas = mutableSetOf("a".repeat(40)))),
            )

        // When
        val violations = validator.validate(commit)

        // Then
        assertThat(violations).isEmpty()
    }

    @Test
    fun `should fail validation when repository id is null but repositoryId is not null`() {
        // Given
        val commit =
            Commit(
                sha = "a".repeat(40),
                message = "message",
                commitDateTime = LocalDateTime.now(),
                repositoryId = "some-id",
                branches = mutableSetOf(Branch(name = "b", commitShas = mutableSetOf("a".repeat(40)))),
            )
        val repository = Repository(id = null, name = "test-repo", commits = mutableSetOf(commit), project = Project(name = "test"))

        // When
        val violations = validator.validate(repository)

        // Then
        assertAll(
            { assertThat(violations).isNotEmpty() },
            { assertThat(violations).hasSize(1) },
            {
                assertThat(
                    violations.map {
                        it.message
                    }[0],
                ).isEqualTo(
                    "Repository ID of Commit=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa is null, but commit has a repositoryId=null.",
                )
            },
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
                repositoryId = repository.id,
                branches = mutableSetOf(Branch(name = "b", commitShas = mutableSetOf("a".repeat(40)))),
            )

        // When
        val violations = validator.validate(commit)

        // Then
        assertThat(violations).isEmpty()
    }

    @Test
    fun `should fail validation when repository id is not null but repositoryId does not match`() {
        // Given
        val commit =
            Commit(
                sha = "a".repeat(40),
                message = "message",
                commitDateTime = LocalDateTime.now(),
                repositoryId = "different-id",
                branches = mutableSetOf(Branch(name = "b", commitShas = mutableSetOf("a".repeat(40)))),
            )
        val repository = Repository(id = "repo-123", name = "test-repo", commits = mutableSetOf(commit), project = Project(name = "test"))

        // When
        val violations = validator.validate(repository)

        // Then
        assertAll(
            { assertFalse(violations.isEmpty(), "Should have validation violations") },
            { assertThat(violations).hasSize(1) },
            {
                assertThat(
                    violations.map { it.message }[0],
                ).contains("Commit repositoryId=different-id does not match repository.id=repo-123")
            },
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
                repositoryId = null,
                branches = mutableSetOf(Branch(name = "b", commitShas = mutableSetOf("a".repeat(40)))),
            )

        // When
        val violations = validator.validate(commit)

        // Then
        assertThat(violations).isEmpty()
    }
}
