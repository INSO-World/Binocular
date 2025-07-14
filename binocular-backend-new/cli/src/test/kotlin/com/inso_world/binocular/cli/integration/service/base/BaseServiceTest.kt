package com.inso_world.binocular.cli.integration.service.base

import com.inso_world.binocular.cli.BinocularCommandLineApplication
import com.inso_world.binocular.cli.index.vcs.toVcsRepository
import com.inso_world.binocular.cli.integration.TestDataSetupService
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

    @Autowired
    internal lateinit var commitRepository: CommitInfrastructurePort

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
    private lateinit var projectRepository: ProjectInfrastructurePort

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
        val repo = simpleRepoConfig.repo.toVcsRepository().toDomain()
//        this.simpleProject = this.projectRepository.save(simpleRepoConfig.project)
//        this.simpleRepo = this.repositoryRepository.save(simpleRepoConfig.repo.toVcsRepository().toDomain(this.simpleProject))
//        simpleRepoConfig.project.repo = this.simpleRepo
        repo.project = simpleRepoConfig.project
        repo.project?.repo = repo
        generateCommits(simpleRepoConfig, repo)
//        this.simpleProject.repo = this.simpleRepo
//        simpleRepoConfig.project.repo = repo
        this.simpleProject = projectRepository.save(repo.project!!)
        this.simpleRepo =
            this.simpleProject.repo ?: throw IllegalStateException("Repository must be present at this state")
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
