package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class RepositoryTest : BaseInfrastructureSpringTest() {

    @Autowired
    lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    lateinit var repositoryPort: RepositoryInfrastructurePort

    private lateinit var project: Project

    @BeforeEach
    fun setup() {
        repositoryPort.deleteAll()
        projectPort.deleteAll()
        project = projectPort.create(Project(name = "proj-rt-001"))
    }

    @AfterEach
    fun cleanup() {
        repositoryPort.deleteAll()
        projectPort.deleteAll()
    }

    @Test
    fun `create repository and find by name`() {
        val repo = Repository(name = "repo-rt-001", project = project)
        val created = repositoryPort.create(repo)
        assertNotNull(created.id)

        val loadedById = repositoryPort.findById(requireNotNull(created.id))
        assertNotNull(loadedById)
        assertEquals(created.id, loadedById!!.id)
        assertEquals(repo.name, loadedById.name)
        assertEquals(project.id, loadedById.project?.id)

        val loadedByName = repositoryPort.findByName(repo.name)
        assertNotNull(loadedByName)
        assertEquals(created.id, loadedByName!!.id)
    }
}
