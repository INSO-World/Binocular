package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.validation.base.ValidationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class BranchValidationTest : ValidationTest() {
    @Test
    fun `branch with empty commits, violates NotEmpty`() {
        val branch = Branch(
            name = "name",
            repository = Repository(
                localPath = "test repo",
                project = Project(name = "test project")
            )
        )

        val violation =
            run {
                val violations = validator.validate(branch)
                assertThat(violations).hasSize(1)
                violations.first()
            }
        assertThat(violation.propertyPath.toString()).isEqualTo("commits")
    }
}
