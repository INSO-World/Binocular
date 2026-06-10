package com.inso_world.binocular.infrastructure.sql

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.integration.base.InfrastructureDataSetup
import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.infrastructure.sql.integration.service.base.deleteAllEntities
import com.inso_world.binocular.infrastructure.sql.service.ProjectInfrastructurePortImpl
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

/**
 * Sets up and tears down shared test fixtures for SQL-adapter integration tests.
 *
 * [setup] persists [TestDataProvider.testProjects] (the same singleton the tests read), which
 * cascades to their repositories, commits, and branches. [ProjectInfrastructurePortImpl.saveAll]
 * back-propagates JPA-generated IDs onto the domain singletons so tests can use [Branch.id] etc.
 * immediately after setup.
 *
 * [teardown] deletes all projects; JPA [CascadeType.ALL] propagates the delete to repositories,
 * commits, and branches.
 */
@Component
internal class SqlInfrastructureDataSetup(
    private val entityManager: EntityManager,
    private val projectInfrastructurePort: ProjectInfrastructurePortImpl,
) : InfrastructureDataSetup {
    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    companion object {
        private val logger by logger()
    }

    override fun setup() {
        logger.info(">>> SqlInfrastructureDataSetup setup")
        // Reset JPA-assigned ids on Repository singletons so Hibernate always
        // inserts fresh rows rather than attempting a merge on a non-existent id.
        // (Project/Commit/Branch/Developer ids are never copied in toEntity, so
        // they do not need resetting here.)
        TestDataProvider.testRepositories.forEach { it.id = null }
        projectInfrastructurePort.saveAll(TestDataProvider.testProjects)
        logger.info("<<< SqlInfrastructureDataSetup setup")
    }

    override fun teardown() {
        logger.info(">>> SqlInfrastructureDataSetup teardown")

        transactionTemplate.execute {
            projectInfrastructurePort.deleteAllEntities()
            entityManager.flush()
        }

        // Clear persistence context so stale entity references don't cause
        // constraint violations when JPA assigns recycled IDs to new entities
        entityManager.clear()

        logger.info("<<< SqlInfrastructureDataSetup teardown")
    }
}
