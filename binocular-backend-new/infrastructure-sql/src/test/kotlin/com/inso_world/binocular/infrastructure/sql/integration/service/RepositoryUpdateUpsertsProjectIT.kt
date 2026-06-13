package com.inso_world.binocular.infrastructure.sql.integration.service

import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.SqlTestConfig
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
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
 * `UserTest."findAll returns created users"` (postgres): `repositoryPort.update()` on a
 * repository whose project document no longer exists must re-persist (upsert) the project
 * instead of throwing [NotFoundException].
 */
@SpringBootTest
@EnableAutoConfiguration
@ContextConfiguration(
    classes = [SqlTestConfig::class],
    initializers = [SqlTestConfig.Initializer::class],
)
@ExtendWith(SpringExtension::class)
@OptIn(ExperimentalUuidApi::class)
internal class RepositoryUpdateUpsertsProjectIT {
    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePort

    /** update() on a repository whose project does not exist yet must upsert the project and succeed. */
    @Test
    fun `update upserts missing project`() {
        // Create a new project+repo that don't exist in the DB
        val project = Project(name = "upsert-${Uuid.random()}")
        val repository = Repository(localPath = "/probe/upsert-${Uuid.random()}", project = project)
        // No prior persistence on purpose: the project does not exist in the DB.
        val updated = assertDoesNotThrow { repositoryPort.update(repository) }
        assertThat(updated.project.id).isNotNull()
    }
}
