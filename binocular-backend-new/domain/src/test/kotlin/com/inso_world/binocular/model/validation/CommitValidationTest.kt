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
    private lateinit var repository: Repository

    @BeforeEach
    fun setUp() {
        validator =
            Validation
                .byDefaultProvider()
                .configure()
                .messageInterpolator(ParameterMessageInterpolator())
                .buildValidatorFactory()
                .validator
        repository = Repository(localPath = "test repo", project = Project(name = "test project"))
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
                repository = Repository(
                    localPath = "test repo"
                )
            )
        invalidCommit.branches.add(dummyBranch)
        dummyBranch.commits.add(invalidCommit)

        repository.branches.add(dummyBranch)
        repository.commits.add(invalidCommit)

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
        val repository = Repository(id = null, localPath = "test-repo")
        val branch = Branch(
            name = "b",
//            commits = mutableSetOf(Commit(sha="a".repeat(40))
        )
        val commit =
            Commit(
                sha = "a".repeat(40),
                message = "message",
                commitDateTime = LocalDateTime.now(),
            )
        repository.commits.add(commit)
        branch.commits.add(commit)

        // When
        val violations = validator.validate(commit)

        // Then
        assertThat(violations).isEmpty()
    }

    @Test
    fun `should pass validation when repository id is not null and repositoryId matches`() {
        // Given
        val repository = Repository(id = "repo-123", localPath = "test-repo")
        val branch = Branch(name = "b")
        val commit =
            Commit(
                sha = "a".repeat(40),
                message = "message",
                commitDateTime = LocalDateTime.now(),
            )
        repository.commits.add(commit)
        branch.commits.add(commit)

        // When
        val violations = validator.validate(commit)

        // Then
        assertThat(violations).isEmpty()
    }

    @Test
    fun `should fail validation when repository id is not null but repositoryId does not match`() {
        // Given
        val branch = Branch(name = "b")
        val commit = run {
            val repository = Repository(id = "different-id", localPath = "test-repo")
            val cmt = Commit(
                sha = "a".repeat(40),
                message = "message",
                commitDateTime = LocalDateTime.now(),
            )
            repository.commits.add(cmt)
            cmt
        }
        branch.commits.add(commit)
        val repository = Repository(
            id = "repo-123",
            localPath = "test-repo",
            project = Project(name = "test")
        )
        val repository2 = Repository(
            id = "different-id",
            localPath = "test-repo",
            project = Project(name = "test 2")
        )
        repository.commits.add(commit)
        repository2.commits.add(commit)

        // When
        val violations = validator.validate(repository)

        // Then
        assertAll(
            { assertFalse(violations.isEmpty(), "Should have validation violations") },
            { assertThat(violations).hasSize(1) },
            {
                assertThat(
                    violations.map { it.message }[0],
                ).contains("Commit repository.id=different-id does not match repository.id=repo-123")
            },
        )
    }

    @Test
    fun `should fail validation when repository is null`() {
        // Given
        val branch = Branch(name = "b")
        val commit =
            Commit(
                message = "test",
                sha = "a".repeat(40),
                commitDateTime = LocalDateTime.now(),
                repository = null,
            )
        branch.commits.add(commit)

        // When
        val violations = validator.validate(commit)

        // Then
        assertThat(violations).hasSize(1)
        val message = violations.toList()[0].propertyPath.toString()
        assertThat(message).isEqualTo("repository")
    }
}
