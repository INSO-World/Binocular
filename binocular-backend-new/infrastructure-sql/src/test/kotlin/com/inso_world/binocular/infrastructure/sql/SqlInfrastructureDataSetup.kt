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
 * cascades to their repositories, branches, commits, developers, and accounts. [ProjectInfrastructurePortImpl.saveAll]
 * back-propagates JPA-generated IDs onto the domain singletons so tests can use [Branch.id] etc.
 * immediately after setup.
 *
 * [teardown] deletes all projects; JPA [CascadeType.ALL] propagates the delete to repositories,
 * branches, commits, developers, and accounts.
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
        // Null out JPA-assigned ids on the entire persisted graph so Hibernate
        // always inserts fresh rows rather than attempting a merge on stale IDs
        // from a previous test run (the PG container is a static singleton that
        // can retain data across JVM invocations).
        //
        // The toEntity functions in all SQL entity classes copy the domain id
        // into the entity id (for update semantics), so any domain singleton
        // that survived a previous test's back-propagation will carry a stale
        // id. Persisting such an entity with a non-null @Id that has no
        // matching row → detached entity → InvalidDataAccessApiUsageException.
        //
        // Reset order mirrors cascade order: top-level collections first,
        // then children (branches/commits/developers are owned by repositories,
        // accounts are owned by projects).
        TestDataProvider.testProjects.forEach { it.id = null }
        TestDataProvider.testRepositories.forEach { it.id = null }
        TestDataProvider.testBranches.forEach { it.id = null }
        TestDataProvider.testCommits.forEach { it.id = null }
        TestDataProvider.testDevelopers.forEach { it.id = null }
        TestDataProvider.testAccounts.forEach { it.id = null }
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
