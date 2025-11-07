package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.data.MockTestDataProvider
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class RepositoryTest : BaseInfrastructureSpringTest() {
    @Autowired
    lateinit var repositoryPort: RepositoryInfrastructurePort

    private lateinit var project: Project
    private lateinit var mockData: MockTestDataProvider

    @Autowired
    lateinit var projectPort: ProjectInfrastructurePort

    @BeforeEach
    fun setUp() {
        mockData = MockTestDataProvider()
        project = projectPort.create(Project(name = "proj-repo-rt-001"))
    }

    @Test
    fun `find all repositories, expect non empty list`() {
        assertThat(repositoryPort.findAll()).hasSize(6)
    }

    @Test
    fun `create repository and find by name`() {
        val repo = Repository(localPath = "repo-rt-001", project = project)
        val created = repositoryPort.create(repo)
        assertNotNull(created.id)

        val loadedById = repositoryPort.findById(requireNotNull(created.id))
        assertNotNull(loadedById)
        assertEquals(created.id, loadedById!!.id)
        assertEquals(repo.localPath, loadedById.localPath)
        assertEquals(project.id, loadedById.project?.id)

        val loadedByName = repositoryPort.findByName(repo.localPath)
        assertNotNull(loadedByName)
        assertEquals(created.id, loadedByName!!.id)
    }
}
