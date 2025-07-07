package com.inso_world.binocular.cli.integration.service.base

import com.inso_world.binocular.cli.BinocularCommandLineApplication
import com.inso_world.binocular.cli.entity.Project
import com.inso_world.binocular.cli.entity.Repository
import com.inso_world.binocular.cli.index.vcs.toVcsRepository
import com.inso_world.binocular.cli.integration.TestDataSetupService
import com.inso_world.binocular.cli.integration.utils.generateCommits
import com.inso_world.binocular.cli.integration.utils.setupRepoConfig
import com.inso_world.binocular.cli.persistence.repository.sql.ProjectRepository
import com.inso_world.binocular.cli.persistence.repository.sql.RepositoryRepository
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(
    classes = [BinocularCommandLineApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
@ExtendWith(SpringExtension::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@Transactional
internal class BaseServiceTest : BaseFixturesIntegrationTest() {
    @Autowired
    private lateinit var testDataSetupService: TestDataSetupService

//    @Autowired
//    internal lateinit var commitRepository: CommitRepository
//
//    @Autowired
//    internal lateinit var userRepository: UserInfrastructurePort
//
    @Autowired
    internal lateinit var repositoryRepository: RepositoryRepository

//
//    @Autowired
//    internal lateinit var branchRepository: BranchRepository
//
    @Autowired
    internal lateinit var projectRepository: ProjectRepository

    protected lateinit var simpleRepo: Repository

    protected lateinit var simpleProject: Project
    protected lateinit var advancedProject: Project
    protected lateinit var octoProject: Project

    @BeforeEach
    fun setupBase() {
        val simpleRepoConfig =
            setupRepoConfig(
                "${FIXTURES_PATH}/${SIMPLE_REPO}",
                "HEAD",
                projectName = SIMPLE_PROJECT_NAME,
            )
        this.simpleRepo = simpleRepoConfig.repo.toVcsRepository().toEntity(simpleRepoConfig.project)
        generateCommits(simpleRepoConfig, simpleRepo)
        this.simpleProject = this.projectRepository.save(simpleRepoConfig.project)
        this.simpleProject.repo = this.simpleRepo
        this.simpleRepo = this.repositoryRepository.save(this.simpleRepo)
    }

    @AfterEach
    fun cleanup() {
        testDataSetupService.clearAllData()
//        projectRepository.deleteAll()
//        repositoryRepository.deleteAll()
//        branchRepository.deleteAll()
//        commitRepository.deleteAll()
//        userRepository.deleteAll()
    }
}
