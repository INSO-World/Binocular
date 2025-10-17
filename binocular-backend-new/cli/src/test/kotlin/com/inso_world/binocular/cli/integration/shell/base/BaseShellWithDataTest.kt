package com.inso_world.binocular.cli.integration.shell.base

import com.inso_world.binocular.cli.BinocularCommandLineApplication
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest
import com.inso_world.binocular.core.integration.base.InfrastructureDataSetup
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.SqlTestConfig
import com.inso_world.binocular.model.Project
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.shell.test.autoconfigure.AutoConfigureShell
import org.springframework.shell.test.autoconfigure.AutoConfigureShellTestClient
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(
    classes = [BinocularCommandLineApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
@ContextConfiguration(
    initializers = [
        SqlTestConfig.Initializer::class,
    ]
)
@AutoConfigureShell
@AutoConfigureShellTestClient
internal open class BaseShellWithDataTest : BaseFixturesIntegrationTest() {
    @Autowired
    internal lateinit var commitRepository: CommitInfrastructurePort

    @Autowired
    private lateinit var testDataSetupService: InfrastructureDataSetup

    @Autowired
    internal lateinit var repositoryRepository: RepositoryInfrastructurePort

    @Autowired
    internal lateinit var branchRepository: BranchInfrastructurePort

    @Autowired
    internal lateinit var projectRepository: ProjectInfrastructurePort

    protected lateinit var simpleProject: Project
    protected lateinit var advancedProject: Project
    protected lateinit var octoProject: Project

    @BeforeEach
    internal fun setUp() {
        simpleProject =
            this.projectRepository.create(
                Project(
                    name = SIMPLE_PROJECT_NAME,
                    description = "desc",
                ),
            )
        advancedProject =
            this.projectRepository.create(
                Project(
                    name = ADVANCED_PROJECT_NAME,
                    description = "desc",
                ),
            )
        octoProject =
            this.projectRepository.create(
                Project(
                    name = OCTO_PROJECT_NAME,
                    description = "desc",
                ),
            )
    }

    @AfterEach
    fun cleanup() {
        testDataSetupService.teardown()
//        projectRepository.deleteAll()
//        repositoryRepository.deleteAll()
//        branchRepository.deleteAll()
//        commitRepository.deleteAll()
//        userRepository.deleteAll()
    }
}
