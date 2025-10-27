package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.utils.ReflectionUtils.Companion.setField
import com.inso_world.binocular.model.validation.base.ValidationTest
import jakarta.validation.ConstraintViolation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class ProjectValidationTest : ValidationTest() {

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.data.DummyTestData#provideBlankStrings")
    fun `should fail when localPath is blank`(
        name: String,
    ) {
        // Given
        val project = Project(name = "test-project")
        // change field via reflection, otherwise constructor check fails
        setField(project.javaClass.getDeclaredField("name").apply { isAccessible = true }, project, name)
        assertThat(project.name).isEqualTo(name)

        // When
        val violations = validator.validate(project)

        // Then
        assertThat(violations).hasSize(1)
        val violation = violations.first()
        assertThat(violation).isInstanceOf(ConstraintViolation::class.java)
        assertThat(violation.propertyPath.toString()).isEqualTo("name")
    }

}
