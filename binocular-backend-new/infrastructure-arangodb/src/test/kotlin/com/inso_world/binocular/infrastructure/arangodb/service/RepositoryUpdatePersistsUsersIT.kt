package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.core.integration.base.BaseIntegrationTest
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.arangodb.ArangodbTestConfig
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.UserRepository
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Isolating regression IT for the contract failure
 * `UserTest."create user, verify automatic registration with repository"` (arangodb):
 * `repositoryPort.update()` must persist the repository's legacy [User] children and
 * write the generated ids back onto the domain objects.
 */
@SpringBootTest
@EnableAutoConfiguration
@ContextConfiguration(
    classes = [ArangodbTestConfig::class],
    initializers = [ArangodbTestConfig.Initializer::class],
)
@ExtendWith(SpringExtension::class)
@OptIn(ExperimentalUuidApi::class)
internal class RepositoryUpdatePersistsUsersIT : BaseIntegrationTest() {
    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePort

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun clean() {
        userRepository.deleteAll()
    }

    /** update() on a repository owning one legacy User must write the user document and refresh its id. */
    @Test
    fun `update persists legacy users and refreshes ids`() {
        val project =
            com.inso_world.binocular.model
                .Project(name = "user-upd-${Uuid.random()}")
        val repository = Repository(localPath = "/probe/user-upd-${Uuid.random()}", project = project)
        projectPort.create(project)

        User(name = "Alice", repository = repository, email = "alice@example.com")
        val updated = repositoryPort.update(repository)

        assertAll(
            { assertThat(updated.user).hasSize(1) },
            { assertThat(updated.user.first().id).isNotNull() },
            { assertThat(userRepository.count()).isEqualTo(1L) },
        )
    }
}
