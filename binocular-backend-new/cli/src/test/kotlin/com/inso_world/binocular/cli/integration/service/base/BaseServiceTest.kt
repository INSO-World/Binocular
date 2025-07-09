package com.inso_world.binocular.cli.integration.service.base

import com.inso_world.binocular.cli.BinocularCommandLineApplication
import com.inso_world.binocular.cli.index.vcs.toVcsRepository
import com.inso_world.binocular.cli.integration.TestDataSetupService
import com.inso_world.binocular.cli.integration.utils.generateCommits
import com.inso_world.binocular.cli.integration.utils.setupRepoConfig
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
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
    internal lateinit var repositoryRepository: RepositoryInfrastructurePort

//
//    @Autowired
//    internal lateinit var branchRepository: BranchRepository
//
    @Autowired
    internal lateinit var projectRepository: ProjectInfrastructurePort

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
        this.simpleProject = this.projectRepository.save(simpleRepoConfig.project)
        this.simpleRepo = simpleRepoConfig.repo.toVcsRepository().toDomain()
        generateCommits(simpleRepoConfig, simpleRepo)
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
