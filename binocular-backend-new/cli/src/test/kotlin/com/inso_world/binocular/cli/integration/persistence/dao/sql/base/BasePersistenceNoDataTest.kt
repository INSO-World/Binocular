package com.inso_world.binocular.cli.integration.persistence.dao.sql.base

import com.inso_world.binocular.cli.integration.TestDataSetupService
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.model.Repository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate

class BasePersistenceNoDataTest : BasePersistenceTest() {
    @PersistenceContext
    internal lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var commitRepository: CommitInfrastructurePort

    @Autowired
    private lateinit var testDataSetupService: TestDataSetupService

    @Autowired
    private lateinit var repositoryRepository: RepositoryInfrastructurePort

    @Autowired
    private lateinit var projectRepository: ProjectInfrastructurePort

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    protected lateinit var simpleRepo: Repository
    protected lateinit var octoRepo: Repository

    @BeforeEach
    fun setupBase() {
    }

    @AfterEach
    fun cleanup() {
        testDataSetupService.clearAllData()
//        entityManager.clear()
//        projectRepository.deleteAll()
//        repositoryRepository.deleteAll()
//        commitRepository.deleteAll()
//        testDataSetupService.clearAllData()
//        userRepository.deleteAll()
    }
}
