package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.LocalDateTime

@Disabled
class CommitCycleValidationTest {
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
    fun `should pass validation for commit with no parents`() {
        val commit =
            Commit(
                sha = "a".repeat(40),
                commitDateTime = LocalDateTime.now(),
                message = "Initial commit",
                branches = mutableSetOf(
                    Branch(
                        name = "b", commits = mutableSetOf(
                            Commit(
                                sha = "a".repeat(40)
                            )
                        )
                    )
                ),
            )
        val violations = validator.validate(commit)
        assertAll(
            { assertThat(violations).isEmpty() },
        )
    }

    @Test
    fun `should pass validation for commit with linear ancestry`() {
        val parent =
            Commit(
                sha = "b".repeat(40),
                commitDateTime = LocalDateTime.now(),
                message = "Parent commit",
            )
        val commit =
            Commit(
                sha = "a".repeat(40),
                commitDateTime = LocalDateTime.now(),
                message = "Child commit",
                parents = mutableSetOf(parent),
                branches = mutableSetOf(Branch(name = "b", commits = mutableSetOf(
                            Commit(
                                sha = "a".repeat(40)
                            )
                        ))),
            )
        val violations = validator.validate(commit)
        assertAll(
            { assertThat(violations).isEmpty() },
        )
    }

    @Test
    fun `should pass validation for commit with merge ancestry`() {
        val parent1 =
            Commit(
                sha = "b".repeat(40),
                commitDateTime = LocalDateTime.now(),
                message = "Parent1 commit",
            )
        val parent2 =
            Commit(
                sha = "c".repeat(40),
                commitDateTime = LocalDateTime.now(),
                message = "Parent2 commit",
            )
        val commit =
            Commit(
                sha = "a".repeat(40),
                commitDateTime = LocalDateTime.now(),
                message = "Merge commit",
                parents = mutableSetOf(parent1, parent2),
                branches = mutableSetOf(Branch(name = "b", commits = mutableSetOf(
                            Commit(
                                sha = "a".repeat(40)
                            )
                        ))),
            )
        val violations = validator.validate(commit)
        assertAll(
            { assertThat(violations).isEmpty() },
        )
    }

    @Test
    fun `should fail validation for direct cycle`() {
        val commit =
            Commit(
                sha = "a".repeat(40),
                commitDateTime = LocalDateTime.now(),
                message = "Cyclic commit",
                branches = mutableSetOf(Branch(name = "b", commits = mutableSetOf(
                            Commit(
                                sha = "a".repeat(40)
                            )
                        ))),
            )
        // Direct cycle: commit is its own parent
        commit.parents = mutableSetOf(commit)
        val violation =
            run {
                val violations = validator.validate(commit)
                assertThat(violations).hasSize(1)
                violations.toList()[0]
            }
        assertThat(violation.message).contains("${"a".repeat(40)} -> ${"a".repeat(40)}")
    }

    @Test
    fun `should fail validation for indirect cycle`() {
        val branch = Branch(name = "b")
        val commitA =
            Commit(
                sha = "a".repeat(40),
                commitDateTime = LocalDateTime.now(),
                message = "A",
                branches = mutableSetOf(branch),
            )
        val commitB =
            Commit(
                sha = "b".repeat(40),
                commitDateTime = LocalDateTime.now(),
                message = "B",
                branches = mutableSetOf(branch),
            )
        val commitC =
            Commit(
                sha = "c".repeat(40),
                commitDateTime = LocalDateTime.now(),
                message = "C",
                branches = mutableSetOf(branch),
            )
        branch.commits.add(commitA)
        branch.commits.add(commitB)
        branch.commits.add(commitC)
        // A -> B -> C -> A
        commitA.parents = mutableSetOf(commitB)
        commitB.parents = mutableSetOf(commitC)
        commitC.parents = mutableSetOf(commitA)
        val violation =
            run {
                val violations = validator.validate(commitA)
                assertThat(violations).hasSize(1)
                violations.toList()[0]
            }
        assertThat(violation.message).contains(
            "${"a".repeat(40)} -> ${"b".repeat(40)} -> ${"c".repeat(40)} -> ${
                "a".repeat(
                    40
                )
            }"
        )
    }

    @Test
    fun `should fail validation for deep nested cycle at level 3`() {
        val repository = Repository(
            name = "test repo",
            project = Project(name = "test project")
        )
        val branch = Branch(name = "b")
        repository.branches.add(branch)
        val commitA =
            Commit(
                sha = "a".repeat(40),
                commitDateTime = LocalDateTime.now(),
                message = "A",
                branches = mutableSetOf(branch),
            )
        repository.commits.add(commitA)
        val commitB =
            Commit(
                sha = "b".repeat(40),
                commitDateTime = LocalDateTime.now(),
                message = "B",
                branches = mutableSetOf(branch),
            )
        repository.commits.add(commitB)
        val commitC =
            Commit(
                sha = "c".repeat(40),
                commitDateTime = LocalDateTime.now(),
                message = "C",
                branches = mutableSetOf(branch),
            )
        repository.commits.add(commitC)
        val commitD =
            Commit(
                sha = "d".repeat(40),
                commitDateTime = LocalDateTime.now(),
                message = "D",
                branches = mutableSetOf(branch),
            )
        repository.commits.add(commitD)
        // A -> B -> C -> D -> B (cycle at level 3)
        commitA.parents = mutableSetOf(commitB)
        commitB.parents = mutableSetOf(commitC)
        commitC.parents = mutableSetOf(commitD)
        commitD.parents = mutableSetOf(commitB)
        branch.commits.add(commitA)
        branch.commits.add(commitB)
        branch.commits.add(commitC)
        branch.commits.add(commitD)
        val violation =
            run {
                val violations = validator.validate(repository)
                assertThat(violations).hasSize(1)
                violations.toList()[0]
            }
        assertAll(
            {
                assertThat(
                    violation.message,
                ).contains("${"b".repeat(40)} -> ${"c".repeat(40)} -> ${"d".repeat(40)} -> ${"b".repeat(40)}")
            },
        )
    }

    @Test
    fun `should pass validation for commit with multiple parents and no cycles`() {
        val parent1 =
            Commit(
                sha = "b".repeat(40),
                commitDateTime = LocalDateTime.now(),
                message = "Parent1 commit",
            )
        val parent2 =
            Commit(
                sha = "c".repeat(40),
                commitDateTime = LocalDateTime.now(),
                message = "Parent2 commit",
            )
        val commit =
            Commit(
                sha = "a".repeat(40),
                commitDateTime = LocalDateTime.now(),
                message = "Merge commit",
                parents = mutableSetOf(parent1, parent2),
                branches = mutableSetOf(
                    Branch(
                        name = "b",
                        commits =
                            mutableSetOf(
                                Commit(sha="b".repeat(40)),
                                Commit(sha="c".repeat(40)),
                                Commit(sha="a".repeat(40))
                            )
                    )
                ),
            )
        val violations = validator.validate(commit)
        assertAll(
            { assertThat(violations).isEmpty() },
        )
    }

    @Test
    fun `should fail validation for cycles in multiple parents`() {
        val branch = Branch(name = "b")
        val commitA =
            Commit(
                sha = "a".repeat(40),
                commitDateTime = LocalDateTime.now(),
                message = "A",
                branches = mutableSetOf(branch),
            )
        val commitB =
            Commit(
                sha = "b".repeat(40),
                commitDateTime = LocalDateTime.now(),
                message = "B",
                branches = mutableSetOf(branch),
            )
        val commitC =
            Commit(
                sha = "c".repeat(40),
                commitDateTime = LocalDateTime.now(),
                message = "C",
                branches = mutableSetOf(branch),
            )
        branch.commits.add(commitA)
        branch.commits.add(commitB)
        branch.commits.add(commitC)
        // A -> B, A -> C, B -> C, C -> A (cycle through both parents)
        commitA.parents = mutableSetOf(commitB, commitC)
        commitB.parents = mutableSetOf(commitC)
        commitC.parents = mutableSetOf(commitA)
        val violation =
            run {
                val violations = validator.validate(commitA)
                assertThat(violations).hasSize(1)
                violations.toList()[0]
            }
        assertThat(
            violation.message,
        ).contains("${"a".repeat(40)} -> ${"b".repeat(40)} -> ${"c".repeat(40)} -> ${"a".repeat(40)}")
    }
}
