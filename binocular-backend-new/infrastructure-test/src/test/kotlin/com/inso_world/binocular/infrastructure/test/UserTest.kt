package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

/**
 * Integration tests for User persistence via UserInfrastructurePort.
 * Tests verify that domain model semantics are preserved through the infrastructure layer.
 */
internal class UserTest : BaseInfrastructureSpringTest() {
    @Autowired
    lateinit var userPort: UserInfrastructurePort

    @Autowired
    lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    lateinit var repositoryPort: RepositoryInfrastructurePort

    private lateinit var repository: Repository

    @BeforeEach
    fun setup() {
        // clean and baseline
        userPort.deleteAll()
        repositoryPort.deleteAll()
        projectPort.deleteAll()
        val project = projectPort.create(Project(name = "proj-user-001"))
        repository = repositoryPort.create(Repository(localPath = "repo-user-001", project = project))
    }

    @AfterEach
    fun cleanup() {
        userPort.deleteAll()
        repositoryPort.deleteAll()
        projectPort.deleteAll()
    }

    @Test
    fun `create user and load it by id`() {
        val user = User(name = "Jane Doe", repository = repository).apply { email = "jane@example.com" }
        val created = userPort.create(user)
        val id = requireNotNull(created.id)
        assertNotNull(id)

        val loaded = userPort.findById(id)
        assertNotNull(loaded)
        loaded!!
        assertEquals(created.id, loaded.id)
        assertEquals(user.name, loaded.name)
        assertEquals(user.email, loaded.email)
        assertEquals(repository.id, loaded.repository?.id)
    }

    @Test
    fun `create user, verify automatic registration with repository`() {
        val user = User(name = "Alice", repository = repository).apply { email = "alice@example.com" }

        // User auto-registers with repository during construction
        assertEquals(1, repository.user.size)
        assert(repository.user.contains(user))

        val created = userPort.create(user)
        val loaded = userPort.findById(requireNotNull(created.id))

        assertNotNull(loaded)
        assertEquals(user.name, loaded!!.name)
        assertEquals(repository.id, loaded.repository?.id)
    }

    @Test
    fun `findAll returns created users`() {
        val u1 = userPort.create(User(name = "A", repository = repository).apply { email = "a@example.com" })
        val u2 = userPort.create(User(name = "B", repository = repository).apply { email = "b@example.com" })
        val all = userPort.findAll().toList()
        // at least 2 (could include other users if DB not fully isolated); ensure ours are present
        val ids = all.mapNotNull { it.id }.toSet()
        assert(ids.contains(u1.id) && ids.contains(u2.id))
    }

    @Test
    fun `create user with blank email throws exception`() {
        val user = User(name = "Bob", repository = repository)

        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            user.email = ""
        }

        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            user.email = "   "
        }
    }
}
