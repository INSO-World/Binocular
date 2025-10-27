package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.validation.base.ValidationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class CommitValidationTest : ValidationTest() {
    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.model.validation.ValidationTestData#invalidCommitsModels")
    fun `invalid property with valid branch`(
        invalidCommit: Commit,
        propertyPath: String,
    ) {
        val repository = invalidCommit.repository
        val dummyBranch =
            Branch(
                name = "branch",
                repository = repository
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

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.model.validation.ValidationTestData#mockCommitModels")
    fun `valid commits but empty branches`(
        commit: Commit,
    ) {
        val violation =
            run {
                val violations = validator.validate(commit)
                assertThat(violations).hasSize(1)
                violations.first()
            }
        assertThat(violation.propertyPath.toString()).isEqualTo("branches")
    }

}
