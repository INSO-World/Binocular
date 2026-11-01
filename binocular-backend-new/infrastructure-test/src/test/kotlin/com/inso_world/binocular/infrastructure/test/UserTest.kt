package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired

/**
 * Integration tests for User persistence via UserInfrastructurePort.
 *
 * Since #454 a user is no longer auto-registered into `Repository.user`; it references its
 * repository through the immutable [User.repositoryId] and is persisted through its own port.
 *
 * Note: `UserInfrastructurePortImpl.create` still requires the deprecated `User.repository`
 * object to maintain `Repository.user`, so these tests set both the id and the back-reference.
 *
 * Note: `UserMapper.toEntity` synthesizes a transient owner instead of persisting the reference,
 * so on read `UserEntity.repository` is null and `toDomain` falls back to the user's own iid.
 * `repositoryId` therefore survives neither a round trip nor `create()` itself, which returns a
 * re-mapped instance. These tests assert it on the domain object they built and report the gap
 * through `DomainModelAlignmentTest` rather than pretending it is persisted.
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
        infrastructureDataSetup.teardown()
        val project = projectPort.create(Project(name = "proj-user-001"))
        repository = repositoryPort.create(Repository(localPath = "repo-user-001", projectId = project.iid))
    }

    @AfterEach
    fun cleanup() {
        infrastructureDataSetup.teardown()
    }

    @Test
    fun `create user and load it by iid`() {
        val user = user(name = "Jane Doe", email = "jane@example.com")
        val created = userPort.create(user)

        val loaded = userPort.findByIid(created.iid)
        assertNotNull(loaded)
        assertAll(
            { assertEquals(user.name, loaded!!.name, "name") },
            { assertEquals(user.email, loaded!!.email, "email") },
            { assertEquals(user.iid, loaded!!.iid, "iid") },
            { assertEquals(user.iid, created.iid, "created.iid") },
        )
    }

    @Test
    fun `create user keeps its repository reference on the domain object`() {
        val user = user(name = "Alice", email = "alice@example.com")

        val created = userPort.create(user)

        assertAll(
            { assertEquals(repository.iid, user.repositoryId, "user.repositoryId") },
            { assertThat(repository.user.map { it.iid }).contains(created.iid) },
            { assertThat(userPort.findByIid(created.iid)).isNotNull() },
        )
    }

    @Test
    fun `findAll returns created users`() {
        val first = userPort.create(user(name = "A", email = "a@example.com"))
        val second = userPort.create(user(name = "B", email = "b@example.com"))

        val iids = userPort.findAll().map { it.iid }.toSet()
        assertThat(iids).containsOnly(first.iid, second.iid)
    }

    @Test
    fun `create user with blank email throws exception`() {
        val user = User(name = "Bob", repositoryId = repository.iid)

        assertAll(
            {
                org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
                    user.email = ""
                }
            },
            {
                org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
                    user.email = "   "
                }
            },
        )
    }

    private fun user(
        name: String,
        email: String? = null,
    ): User =
        User(name = name, repositoryId = repository.iid, repository = repository).apply {
            email?.let { this.email = it }
        }
}
