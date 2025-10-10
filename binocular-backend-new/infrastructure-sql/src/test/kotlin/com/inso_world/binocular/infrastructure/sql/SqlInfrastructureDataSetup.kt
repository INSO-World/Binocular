package com.inso_world.binocular.infrastructure.sql

import com.inso_world.binocular.core.integration.base.InfrastructureDataSetup
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import com.inso_world.binocular.infrastructure.sql.service.BranchInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.sql.service.CommitInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.sql.service.ProjectInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.sql.service.RepositoryInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.sql.service.UserInfrastructurePortImpl
import jakarta.persistence.EntityManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

@Component
internal class SqlInfrastructureDataSetup(
    private val projectInfrastructurePort: ProjectInfrastructurePortImpl,
    private val commitInfrastructurePort: CommitInfrastructurePortImpl,
    private val repositoryInfrastructurePort: RepositoryInfrastructurePortImpl,
//    private val accountRepository: AccountInfrastructurePort,
    private val branchInfrastructurePort: BranchInfrastructurePortImpl,
//    private val buildRepository: BuildInfrastructurePort,
//    private val fileRepository: FileInfrastructurePort,
//    private val issueRepository: IssueInfrastructurePort,
//    private val mergeRequestRepository: MergeRequestInfrastructurePort,
//    private val moduleRepository: ModuleInfrastructurePort,
//    private val noteRepository: NoteInfrastructurePort,
    private val userPort: UserInfrastructurePortImpl,
//    private val milestoneRepository: MilestoneInfrastructurePort,
) : InfrastructureDataSetup {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(SqlInfrastructureDataSetup::class.java)
    }

    @Autowired
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    // Delete children first, then parents (adjust order to your actual FKs)
    private val deleteOrder =
        listOf(
            CommitEntity::class, // depends on User/Branch/Repository
            BranchEntity::class, // depends on Repository
            RepositoryEntity::class, // depends on Project
            ProjectEntity::class,
            UserEntity::class,
        )

    override fun setup() {
        logger.info(">>> SqlInfrastructureDataSetup setup")
        logger.info("<<< SqlInfrastructureDataSetup setup")
    }

    override fun teardown() {
        logger.info(">>> SqlInfrastructureDataSetup teardown")

        projectInfrastructurePort.deleteAll()
        repositoryInfrastructurePort.deleteAll()
        branchInfrastructurePort.deleteAll()
        commitInfrastructurePort.deleteAll()
        userPort.deleteAll()
//        transactionTemplate.executeWithoutResult {
//            deleteOrder.forEach { k ->
//                val entityName = entityManager.metamodel.entity(k.java).name
//                entityManager.createQuery("DELETE FROM $entityName").executeUpdate()
//            }
//            entityManager.flush()
//            entityManager.clear()
//        }

        logger.info("<<< SqlInfrastructureDataSetup teardown")
    }
}
