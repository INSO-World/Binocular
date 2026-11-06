package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.utils.ReflectionUtils.Companion.setField
import com.inso_world.binocular.model.validation.base.ValidationTest
import jakarta.validation.ConstraintViolation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class RepositoryValidationTest : ValidationTest() {
    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.domain.data.DummyTestData#provideBlankStrings")
    fun `should fail when localPath is blank`(
        localPath: String,
    ) {
        // Given
        val project = Project(name = "test-project")
        val repository =
            Repository(
                localPath = "localPath",
                project = project
            )
        // change field via reflection, otherwise constructor check fails
        setField(
            repository.javaClass.getDeclaredField("localPath").apply { isAccessible = true },
            repository,
            localPath
        )
        assertThat(repository.localPath).isEqualTo(localPath)

        // When
        val violations = validator.validate(repository)

        // Then
        assertThat(violations).hasSize(1)
        val violation = violations.first()
        assertThat(violation).isInstanceOf(ConstraintViolation::class.java)
        assertThat(violation.propertyPath.toString()).isEqualTo("localPath")
    }
}
