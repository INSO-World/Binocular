package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Project
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

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

        val loaded = projectPort.findById(requireNotNull(created.id))
        assertNotNull(loaded)
        assertEquals(created.id, loaded!!.id)
        assertEquals(project.name, loaded.name)

        val byName = projectPort.findByName(project.name)
        assertNotNull(byName)
        assertEquals(created.id, byName!!.id)
    }
}
