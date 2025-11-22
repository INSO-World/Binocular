package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.data.MockTestDataProvider
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

/**
 * Integration tests for Repository persistence via RepositoryInfrastructurePort.
 * Tests verify that domain model semantics (particularly bidirectional relationships)
 * are preserved through the infrastructure layer.
 */
internal class RepositoryTest : BaseInfrastructureSpringTest() {
    @Autowired
    lateinit var repositoryPort: RepositoryInfrastructurePort

    private lateinit var project: Project
    private lateinit var mockData: MockTestDataProvider

    @BeforeEach
    fun setUp() {
        mockData = MockTestDataProvider()
        project = requireNotNull(mockData.projectsByName["proj-pg-0"])
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

    @Test
    fun `create repository, verify automatic registration with project`() {
        val newProject = Project(name = "test-project-for-repo")
        val repo = Repository(localPath = "repo-rt-002", project = newProject)

        // Repository auto-registers with project during construction
        assertNotNull(newProject.repo)
        assertEquals(repo, newProject.repo)

        val created = repositoryPort.create(repo)
        assertNotNull(created.id)

        // Verify bidirectional relationship persists
        assertEquals(newProject.id, created.project?.id)
    }

    @Test
    fun `repository commits collection is add-only`() {
        val repo = Repository(localPath = "repo-rt-003", project = Project(name = "test-project-2"))
        val committer = User(name = "Test Committer", repository = repo).apply { email = "committer@test.com" }
        val commit = Commit(
            sha = "d".repeat(40),
            message = "test commit",
            commitDateTime = LocalDateTime.now(),
            repository = repo,
            committer = committer,
        )

        // Commits auto-register with repository during construction
        assertEquals(1, repo.commits.size)
        assert(repo.commits.contains(commit))

        // Removal operations should throw UnsupportedOperationException
        org.junit.jupiter.api.assertThrows<UnsupportedOperationException> {
            repo.commits.remove(commit)
        }

        org.junit.jupiter.api.assertThrows<UnsupportedOperationException> {
            repo.commits.clear()
        }
    }
}
