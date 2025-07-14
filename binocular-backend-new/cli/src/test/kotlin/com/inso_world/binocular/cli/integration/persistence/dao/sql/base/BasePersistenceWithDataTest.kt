package com.inso_world.binocular.cli.integration.persistence.dao.sql.base

import com.inso_world.binocular.cli.BinocularCommandLineApplication
import com.inso_world.binocular.cli.index.vcs.toVcsRepository
import com.inso_world.binocular.cli.integration.TestDataSetupService
import com.inso_world.binocular.cli.integration.utils.RepositoryConfig
import com.inso_world.binocular.cli.integration.utils.generateCommits
import com.inso_world.binocular.cli.integration.utils.setupRepoConfig
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.support.TransactionTemplate

// @ExtendWith(SpringExtension::class)
// @DataJpaTest
// @ContextConfiguration(classes = [BinocularCommandLineApplication::class])
// @Import(BinocularCommandLineApplication::class)
// @ComponentScan(basePackages = ["com.inso_world.binocular.cli.persistence.dao.sql"])
@SpringBootTest(classes = [BinocularCommandLineApplication::class])
class BasePersistenceWithDataTest : BaseFixturesIntegrationTest() {
//    @PersistenceContext
//    protected lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var testDataSetupService: TestDataSetupService

    @Autowired
    lateinit var commitRepository: CommitInfrastructurePort

//    @Autowired
//    lateinit var userRepository: UserRepository

    @Autowired
    internal lateinit var repositoryRepository: RepositoryInfrastructurePort

    @Autowired
    private lateinit var projectRepository: ProjectInfrastructurePort

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    protected lateinit var simpleRepo: Repository
    protected lateinit var octoRepo: Repository

    @BeforeEach
    fun setupBase() {
        fun prepare(repoConfig: RepositoryConfig): Project {
            repoConfig.project.repo = repoConfig.repo.toVcsRepository().toDomain()
            repoConfig.project.repo?.project = repoConfig.project
            repoConfig.project.repo?.let { generateCommits(repoConfig, it) } ?: throw IllegalStateException(
                "Repository must not be null at this point",
            )
            return projectRepository.save(repoConfig.project)
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
