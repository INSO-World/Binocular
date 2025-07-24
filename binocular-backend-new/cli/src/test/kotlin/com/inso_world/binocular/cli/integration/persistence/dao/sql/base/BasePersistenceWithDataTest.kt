package com.inso_world.binocular.cli.integration.persistence.dao.sql.base

import com.inso_world.binocular.cli.BinocularCommandLineApplication
import com.inso_world.binocular.cli.index.vcs.toVcsRepository
import com.inso_world.binocular.cli.integration.TestDataSetupService
import com.inso_world.binocular.cli.integration.utils.RepositoryConfig
import com.inso_world.binocular.cli.integration.utils.generateCommits
import com.inso_world.binocular.cli.integration.utils.setupRepoConfig
import com.inso_world.binocular.cli.service.RepositoryService
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Lazy
import org.springframework.transaction.support.TransactionTemplate

@SpringBootTest(classes = [BinocularCommandLineApplication::class])
class BasePersistenceWithDataTest : BaseFixturesIntegrationTest() {
    @Autowired @Lazy
    private lateinit var repoService: RepositoryService

//    @PersistenceContext
//    protected lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var testDataSetupService: TestDataSetupService

    @Autowired
    lateinit var branchPort: BranchInfrastructurePort

//    @Autowired
//    lateinit var userRepository: UserRepository

    @Autowired
    internal lateinit var repositoryPort: RepositoryInfrastructurePort

    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    protected lateinit var simpleRepo: Repository
    protected lateinit var octoRepo: Repository

    @BeforeEach
    fun setupBase() {
        fun prepare(repoConfig: RepositoryConfig): Project {
            repoConfig.project.repo = repoConfig.repo.toVcsRepository().toDomain()
            repoConfig.project.repo?.project = repoConfig.project

            val project = projectPort.create(repoConfig.project)

            project.repo?.let { generateCommits(repoService,repoConfig, it) } ?: throw IllegalStateException(
                "Repository must not be null at this point",
            )
            return projectPort.update(project)
        }

        prepare(
            setupRepoConfig(
                "${FIXTURES_PATH}/${SIMPLE_REPO}",
                "HEAD",
                projectName = SIMPLE_PROJECT_NAME,
            ),
        ).also { savedProject ->
            savedProject.repo?.let { this.simpleRepo = it }
                ?: throw IllegalStateException("Repository must not be null after save")
        }

        prepare(
            setupRepoConfig(
                "${FIXTURES_PATH}/${OCTO_REPO}",
                "HEAD",
                projectName = OCTO_PROJECT_NAME,
            ),
        ).also { savedProject ->
            savedProject.repo?.let { this.octoRepo = it }
                ?: throw IllegalStateException("Repository must not be null after save")
        }
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
