package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

/**
 * Integration tests for Project persistence via ProjectInfrastructurePort.
 * Tests verify that domain model semantics (especially the set-once repo property)
 * are preserved through the infrastructure layer.
 */
internal class ProjectTest : BaseInfrastructureSpringTest() {
    @Autowired
    lateinit var projectPort: ProjectInfrastructurePort

    @Test
    fun `find all projects, expect non empty list`() {
        assertThat(projectPort.findAll()).hasSize(6)
    }

    @Test
    fun `create project and find by name`() {
        val project = Project(name = "project-pt-001")
        val created = projectPort.create(project)
        assertNotNull(created.id)

        val loaded = projectPort.findByIid(created.iid)
        assertNotNull(loaded)
        assertEquals(created.id, loaded!!.id)
        assertEquals(project.name, loaded.name)

        val byName = projectPort.findByName(project.name)
        assertNotNull(byName)
        assertEquals(created.id, byName!!.id)
    }

    @Test
    fun `project repo property is set-once`() {
        val project = Project(name = "project-pt-002")
        val repo1 = Repository(localPath = "repo-1", project = project)

        // Repository auto-registers with project during construction
        assertNotNull(project.repo)
        assertEquals(repo1, project.repo)

        // Attempting to set a different repository should fail
        val repo2 = Repository(localPath = "repo-2", project = Project(name = "other-project"))
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            project.repo = repo2
        }

        // Setting the same repository again should be a no-op
        org.junit.jupiter.api.assertDoesNotThrow {
            project.repo = repo1
        }
    }

    @Test
    fun `project repo property cannot be set to null`() {
        val project = Project(name = "project-pt-003")
        val repo = Repository(localPath = "repo-3", project = project)

        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            project.repo = null
        }
    }
}
