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

@SpringBootTest(
    classes = [BinocularCommandLineApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
@ExtendWith(SpringExtension::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
internal class BaseServiceTest : BaseFixturesIntegrationTest() {
    @Autowired
    private lateinit var testDataSetupService: TestDataSetupService

    @Autowired
    internal lateinit var commitPort: CommitInfrastructurePort

    //
//    @Autowired
//    internal lateinit var userRepository: UserInfrastructurePort
//
    @Autowired
    internal lateinit var repositoryPort: RepositoryInfrastructurePort

    //
//    @Autowired
//    internal lateinit var branchRepository: BranchRepository
//
    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePort

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
        repo.projectId = simpleRepoConfig.project.id
        simpleRepoConfig.project.repo = repo
        generateCommits(simpleRepoConfig, repo)
        this.simpleProject = projectPort.create(simpleRepoConfig.project)
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
