package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.validation.base.ValidationTest
import com.inso_world.binocular.model.vcs.ReferenceCategory
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
                fullName = "refs/heads/branch",
                name = "branch",
                repository = repository,
                head = invalidCommit,
                category = ReferenceCategory.LOCAL_BRANCH
            )

        repository.branches.add(dummyBranch)
        repository.commits.add(invalidCommit)

        val violation =
            run {
                val violations = validator.validate(invalidCommit)
                // when null both `Hexadecimal` and `Size` are violated
                assertThat(violations).hasSizeBetween(1, 2)
                violations.toList()[0]
            }
        assertThat(violation.propertyPath.toString()).isEqualTo(propertyPath)
    }

}
