package com.inso_world.binocular.cli.integration.persistence.dao.sql.base

import com.inso_world.binocular.cli.BinocularCommandLineApplication
import com.inso_world.binocular.cli.entity.Repository
import com.inso_world.binocular.cli.index.vcs.toVcsRepository
import com.inso_world.binocular.cli.integration.TestDataSetupService
import com.inso_world.binocular.cli.integration.utils.generateCommits
import com.inso_world.binocular.cli.integration.utils.setupRepoConfig
import com.inso_world.binocular.cli.persistence.repository.sql.CommitRepository
import com.inso_world.binocular.cli.persistence.repository.sql.ProjectRepository
import com.inso_world.binocular.cli.persistence.repository.sql.RepositoryRepository
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.support.TransactionTemplate

@ExtendWith(SpringExtension::class)
@DataJpaTest
@ContextConfiguration(classes = [BinocularCommandLineApplication::class])
@Import(BinocularCommandLineApplication::class)
@ComponentScan(basePackages = ["com.inso_world.binocular.cli.persistence.dao.sql"])
internal class BasePersistenceWithDataTest : BaseFixturesIntegrationTest() {
//    @PersistenceContext
//    protected lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var testDataSetupService: TestDataSetupService

    @Autowired
    lateinit var commitRepository: CommitRepository

//    @Autowired
//    lateinit var userRepository: UserRepository

    @Autowired
    internal lateinit var repositoryRepository: RepositoryRepository

    @Autowired
    private lateinit var projectRepository: ProjectRepository

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    protected lateinit var simpleRepo: Repository
    protected lateinit var octoRepo: Repository

    @BeforeEach
    fun setupBase() {
        val simpleRepoConfig =
            setupRepoConfig(
                "${FIXTURES_PATH}/${SIMPLE_REPO}",
                "HEAD",
                projectName = SIMPLE_PROJECT_NAME,
            )
        this.simpleRepo = simpleRepoConfig.repo.toVcsRepository().toEntity(simpleRepoConfig.project)
        simpleRepoConfig.project.repo = this.simpleRepo
        generateCommits(simpleRepoConfig, simpleRepo)
//        transactionTemplate.execute {
        projectRepository.save(simpleRepoConfig.project)
        this.simpleRepo = this.repositoryRepository.save(this.simpleRepo)
//        }

        val octoRepoConfig =
            setupRepoConfig(
                "${FIXTURES_PATH}/${OCTO_REPO}",
                "HEAD",
                projectName = OCTO_PROJECT_NAME,
            )
        this.octoRepo = octoRepoConfig.repo.toVcsRepository().toEntity(octoRepoConfig.project)
        octoRepoConfig.project.repo = this.octoRepo
        generateCommits(octoRepoConfig, octoRepo)
//        transactionTemplate.execute {
        projectRepository.save(octoRepoConfig.project)
        this.octoRepo = this.repositoryRepository.save(this.octoRepo)
//        }
    }

    @AfterEach
    fun cleanup() {
        testDataSetupService.clearAllData()
//        entityManager.clear()
//        projectRepository.deleteAll()
//        repositoryRepository.deleteAll()
//        commitRepository.deleteAll()
//        userRepository.deleteAll()
    }
}
