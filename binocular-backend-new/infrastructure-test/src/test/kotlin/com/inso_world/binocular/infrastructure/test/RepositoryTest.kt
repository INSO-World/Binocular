package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.data.MockTestDataProvider
import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

/**
 * Integration tests for Repository persistence via RepositoryInfrastructurePort.
 *
 * Since #454 a repository references its project through the immutable [Repository.projectId]
 * and commits reference their repository through [Commit.repositoryId]; there is no longer a
 * bidirectional object graph. These tests verify that those ID references survive persistence.
 */
internal class RepositoryTest : BaseInfrastructureSpringTest() {
    @Autowired
    lateinit var repositoryPort: RepositoryInfrastructurePort

    @Autowired
    lateinit var commitPort: CommitInfrastructurePort

    private lateinit var project: Project

    @Autowired
    lateinit var projectPort: ProjectInfrastructurePort

    @BeforeEach
    fun setUp() {
        project = projectPort.create(Project(name = "proj-repo-rt-001"))
    }

    @Test
    fun `find all repositories, expect non empty list`() {
        // the data setup seeds TestDataProvider.repository plus every MockTestDataProvider repository
        val expected = 1 + MockTestDataProvider().testRepositories.size
        assertThat(repositoryPort.findAll()).hasSize(expected)
    }

    @Test
    fun `create repository and find by iid`() {
        val repo = Repository(localPath = "repo-rt-001", projectId = project.iid)
        val created = repositoryPort.create(repo)
        assertNotNull(created.id)

        val loadedByIid = repositoryPort.findByIid(created.iid)
        assertNotNull(loadedByIid)
        assertAll(
            { assertEquals(created.id, loadedByIid!!.id) },
            { assertEquals(repo.localPath, loadedByIid!!.localPath) },
            { assertEquals(project.iid, loadedByIid!!.projectId) },
        )

        val loadedByName = repositoryPort.findByName(repo.localPath)
        assertNotNull(loadedByName)
        assertEquals(created.id, loadedByName!!.id)
    }

    @Test
    fun `create repository, project reference survives the round trip`() {
        val repo = Repository(localPath = "repo-rt-002", projectId = project.iid)

        val created = repositoryPort.create(repo)
        assertNotNull(created.id)

        val reloaded = repositoryPort.findByIid(created.iid)
        assertNotNull(reloaded)
        assertAll(
            { assertEquals(project.iid, created.projectId, "created.projectId") },
            { assertEquals(project.iid, reloaded!!.projectId, "reloaded.projectId") },
            { assertEquals(repo.iid, created.iid, "created.iid") },
        )
    }

    @Test
    fun `commit references its repository via repositoryId`() {
        // CommitDao.create resolves the author by id and there is no DeveloperInfrastructurePort,
        // so only the developers seeded by the infrastructure data setup can author a commit.
        val repo = TestDataProvider.repository
        val developer = TestDataProvider.developer1

        val commit =
            Commit(
                sha = "d".repeat(40),
                message = "test commit",
                authorSignature = Signature(developerId = developer.iid, timestamp = LocalDateTime.now()),
                repositoryId = repo.iid,
            )

        val created = commitPort.create(commit)
        val reloaded = commitPort.findByIid(created.iid)

        assertNotNull(reloaded)
        assertAll(
            { assertEquals(repo.iid, created.repositoryId, "created.repositoryId") },
            { assertEquals(repo.iid, reloaded!!.repositoryId, "reloaded.repositoryId") },
            { assertEquals(commit.sha, reloaded!!.sha, "reloaded.sha") },
        )
    }
}
