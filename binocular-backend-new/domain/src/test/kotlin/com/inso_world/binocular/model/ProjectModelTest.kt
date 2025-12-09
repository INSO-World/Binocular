package com.inso_world.binocular.model

import com.inso_world.binocular.model.utils.ReflectionUtils.Companion.setField
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ProjectModelTest {
    @Test
    fun `create empty project, checks that iid is created automatically`() {
        val project = Project(name = "test-project")

        assertThat(project.iid).isNotNull()
        assertThat(project.repo).isNull()
    }

    @Test
    fun `create user, validate hashCode is same based on iid`() {
        val project = Project(name = "test-project")

        assertThat(project.hashCode()).isEqualTo(project.iid.hashCode())
    }

    @Test
    fun `create user, validate uniqueKey`() {
        val project = Project(name = "test-project")

        assertAll(
            { assertThat(project.uniqueKey).isEqualTo(Project.Key("test-project")) },
            { assertThat(project.uniqueKey.name).isSameAs(project.name) }
        )
    }

    @Test
    fun `create projects, check that equals uses iid only`() {
        val projectA = Project(name = "test-project")
        val projectB = Project(name = "test-project") // same name

        assertThat(projectA).isNotEqualTo(projectB)
    }

    @Test
    fun `create projects via copy, check that equals they are equal`() {
        val projectA = Project(name = "test-project")
        val originIid = projectA.iid
        val projectB = projectA.copy()
        setField(
            projectB.javaClass.superclass.getDeclaredField("iid").apply { isAccessible = true },
            projectB,
            originIid
        )

        assertThat(projectA).isNotSameAs(projectB)
        assertThat(projectA.iid).isEqualTo(originIid)
        assertThat(projectA.iid).isEqualTo(projectB.iid)
        assertThat(projectA).isEqualTo(projectB)
    }

    @Test
    fun `create project with repository, should link correctly`() {
        val project = Project(name = "test-project").apply {
            this.repo = Repository(
                localPath = "test",
                project = this,
            )
        }

        // check reference
        assertThat(project.repo).isNotNull()
        assertThat(requireNotNull(project.repo).project).isSameAs(project)
        assertThat(requireNotNull(project.repo).project.repo).isSameAs(project.repo)
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.domain.data.DummyTestData#provideBlankStrings")
    fun `create project with blank name, should fail`(
        name: String,
    ) {
        assertThrows<IllegalArgumentException> { Project(name) }
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.domain.data.DummyTestData#provideAllowedStrings")
    fun `create project with allowed names, should pass`(
        name: String,
    ) {
        assertDoesNotThrow { Project(name) }
    }

    @Test
    fun `create project with description`() {
        val project = Project(name = "test-project").apply {
            description = "test-description"
        }

        assertThat(project.description).isEqualTo("test-description")
    }

    @Test
    fun `create project with explicit null repo`() {
        assertThrows<IllegalArgumentException> {
            Project(name = "test-project").apply {
                this.repo = null
            }
        }
    }
}
