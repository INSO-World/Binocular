package com.inso_world.binocular.infrastructure.sql.integration.service

import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.SqlTestConfig
import com.inso_world.binocular.infrastructure.sql.integration.service.base.deleteAllEntities
import com.inso_world.binocular.infrastructure.sql.service.ProjectInfrastructurePortImpl
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.support.TransactionTemplate
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

    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    private lateinit var projectInfrastructurePortImpl: ProjectInfrastructurePortImpl

    @Autowired
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

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

    /**
     * Reproduces `UserTest."findAll returns created users"`: a project+repository are persisted
     * (real DB ids assigned), then both rows are cascade-deleted out-of-band (mirroring
     * `SqlInfrastructureDataSetup.teardown()`) without updating the in-memory domain objects.
     * `update()` on the now-stale `repository` must upsert both the project and the repository
     * row instead of throwing `ObjectOptimisticLockingFailureException` on the stale
     * `RepositoryEntity` id.
     */
    @Test
    fun `update upserts missing repository row after cascade-deleted project`() {
        val project = projectPort.create(Project(name = "upsert-repo-${Uuid.random()}"))
        val repository =
            repositoryPort.create(
                Repository(localPath = "/probe/upsert-repo-${Uuid.random()}", project = project)
            )
        val staleRepositoryId = repository.id

        // Cascade-delete project + repository out-of-band, mirroring SqlInfrastructureDataSetup.teardown(),
        // without updating the in-memory `repository`/`project` domain objects' (now stale) ids.
        transactionTemplate.execute {
            projectInfrastructurePortImpl.deleteAllEntities()
            entityManager.flush()
        }
        entityManager.clear()

        val updated = assertDoesNotThrow { repositoryPort.update(repository) }

        assertThat(updated.id).isNotNull().isNotEqualTo(staleRepositoryId)
    }
}
