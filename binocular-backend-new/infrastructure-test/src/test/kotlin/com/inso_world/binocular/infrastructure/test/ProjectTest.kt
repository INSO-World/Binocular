package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.data.MockTestDataProvider
import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired

/**
 * Integration tests for Project persistence via ProjectInfrastructurePort.
 *
 * Since #454 a project no longer owns its repository; the association is expressed by the
 * immutable [Repository.projectId]. These tests therefore verify that the project -> repository
 * link survives a persistence round trip and is established from the repository side only.
 */
internal class ProjectTest : BaseInfrastructureSpringTest() {
    @Autowired
    lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    lateinit var repositoryPort: RepositoryInfrastructurePort

    @Test
    fun `find all projects, expect non empty list`() {
        // the data setup seeds TestDataProvider.project plus every MockTestDataProvider project
        val expected = 1 + MockTestDataProvider().testProjects.size
        assertAll(
            { assertThat(projectPort.findAll()).hasSize(expected) },
            { assertThat(projectPort.findByName(TestDataProvider.project.name)).isNotNull() },
        )
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
    fun `repository references its project via the immutable projectId`() {
        val project = projectPort.create(Project(name = "project-pt-002"))
        val created = repositoryPort.create(Repository(localPath = "repo-pt-002", projectId = project.iid))

        val loaded = repositoryPort.findByIid(created.iid)
        assertNotNull(loaded)
        assertAll(
            { assertEquals(project.iid, created.projectId, "created.projectId") },
            { assertEquals(project.iid, loaded!!.projectId, "loaded.projectId") },
        )
    }

    @Test
    fun `two projects can each own their own repository`() {
        val first = projectPort.create(Project(name = "project-pt-003-a"))
        val second = projectPort.create(Project(name = "project-pt-003-b"))

        val firstRepo = repositoryPort.create(Repository(localPath = "repo-pt-003-a", projectId = first.iid))
        val secondRepo = repositoryPort.create(Repository(localPath = "repo-pt-003-b", projectId = second.iid))

        assertAll(
            { assertEquals(first.iid, firstRepo.projectId, "firstRepo.projectId") },
            { assertEquals(second.iid, secondRepo.projectId, "secondRepo.projectId") },
            { assertThat(firstRepo.iid).isNotEqualTo(secondRepo.iid) },
        )
    }
}
