package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Commit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import java.time.LocalDateTime

class CommitCycleValidationTest {
    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        val validatorFactory = Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(ParameterMessageInterpolator())
            .buildValidatorFactory()
        validator = validatorFactory.validator
    }

    @Test
    fun `should pass validation for commit with no parents`() {
        val commit = Commit(
            sha = "a".repeat(40),
            commitDateTime = LocalDateTime.now(),
            message = "Initial commit"
        )
        val violations = validator.validate(commit)
        assertAll(
            { assertThat(violations).isEmpty() }
        )
    }

    @Test
    fun `should pass validation for commit with linear ancestry`() {
        val parent = Commit(
            sha = "b".repeat(40),
            commitDateTime = LocalDateTime.now(),
            message = "Parent commit"
        )
        val commit = Commit(
            sha = "a".repeat(40),
            commitDateTime = LocalDateTime.now(),
            message = "Child commit",
            parents = listOf(parent)
        )
        val violations = validator.validate(commit)
        assertAll(
            { assertThat(violations).isEmpty() }
        )
    }

    @Test
    fun `should pass validation for commit with merge ancestry`() {
        val parent1 = Commit(
            sha = "b".repeat(40),
            commitDateTime = LocalDateTime.now(),
            message = "Parent1 commit"
        )
        val parent2 = Commit(
            sha = "c".repeat(40),
            commitDateTime = LocalDateTime.now(),
            message = "Parent2 commit"
        )
        val commit = Commit(
            sha = "a".repeat(40),
            commitDateTime = LocalDateTime.now(),
            message = "Merge commit",
            parents = listOf(parent1, parent2)
        )
        val violations = validator.validate(commit)
        assertAll(
            { assertThat(violations).isEmpty() }
        )
    }

    @Test
    fun `should fail validation for direct cycle`() {
        val commit = Commit(
            sha = "a".repeat(40),
            commitDateTime = LocalDateTime.now(),
            message = "Cyclic commit"
        )
        // Direct cycle: commit is its own parent
        commit.parents = listOf(commit)
        val violations = validator.validate(commit)
        assertAll(
            { assertThat(violations).isNotEmpty() },
            { assertThat(violations.first().message).contains("cycle") }
        )
    }

    @Test
    fun `should fail validation for indirect cycle`() {
        val commitA = Commit(
            sha = "a".repeat(40),
            commitDateTime = LocalDateTime.now(),
            message = "A"
        )
        val commitB = Commit(
            sha = "b".repeat(40),
            commitDateTime = LocalDateTime.now(),
            message = "B"
        )
        val commitC = Commit(
            sha = "c".repeat(40),
            commitDateTime = LocalDateTime.now(),
            message = "C"
        )
        // A -> B -> C -> A
        commitA.parents = listOf(commitB)
        commitB.parents = listOf(commitC)
        commitC.parents = listOf(commitA)
        val violations = validator.validate(commitA)
        assertAll(
            { assertThat(violations).isNotEmpty() },
            { assertThat(violations.first().message).contains("cycle") }
        )
    }

    @Test
    fun `should fail validation for deep nested cycle at level 3`() {
        val commitA = Commit(
            sha = "a".repeat(40),
            commitDateTime = LocalDateTime.now(),
            message = "A"
        )
        val commitB = Commit(
            sha = "b".repeat(40),
            commitDateTime = LocalDateTime.now(),
            message = "B"
        )
        val commitC = Commit(
            sha = "c".repeat(40),
            commitDateTime = LocalDateTime.now(),
            message = "C"
        )
        val commitD = Commit(
            sha = "d".repeat(40),
            commitDateTime = LocalDateTime.now(),
            message = "D"
        )
        // A -> B -> C -> D -> B (cycle at level 3)
        commitA.parents = listOf(commitB)
        commitB.parents = listOf(commitC)
        commitC.parents = listOf(commitD)
        commitD.parents = listOf(commitB)
        val violations = validator.validate(commitA)
        assertAll(
            { assertThat(violations).isNotEmpty() },
            { assertThat(violations.first().message).contains("cycle") }
        )
    }

    @Test
    fun `should pass validation for commit with multiple parents and no cycles`() {
        val parent1 = Commit(
            sha = "b".repeat(40),
            commitDateTime = LocalDateTime.now(),
            message = "Parent1 commit"
        )
        val parent2 = Commit(
            sha = "c".repeat(40),
            commitDateTime = LocalDateTime.now(),
            message = "Parent2 commit"
        )
        val commit = Commit(
            sha = "a".repeat(40),
            commitDateTime = LocalDateTime.now(),
            message = "Merge commit",
            parents = listOf(parent1, parent2)
        )
        val violations = validator.validate(commit)
        assertAll(
            { assertThat(violations).isEmpty() }
        )
    }

    @Test
    fun `should fail validation for cycles in multiple parents`() {
        val commitA = Commit(
            sha = "a".repeat(40),
            commitDateTime = LocalDateTime.now(),
            message = "A"
        )
        val commitB = Commit(
            sha = "b".repeat(40),
            commitDateTime = LocalDateTime.now(),
            message = "B"
        )
        val commitC = Commit(
            sha = "c".repeat(40),
            commitDateTime = LocalDateTime.now(),
            message = "C"
        )
        // A -> B, A -> C, B -> C, C -> A (cycle through both parents)
        commitA.parents = listOf(commitB, commitC)
        commitB.parents = listOf(commitC)
        commitC.parents = listOf(commitA)
        val violations = validator.validate(commitA)
        assertAll(
            { assertThat(violations).isNotEmpty() },
            { assertThat(violations.first().message).contains("cycle") }
        )
    }
} 