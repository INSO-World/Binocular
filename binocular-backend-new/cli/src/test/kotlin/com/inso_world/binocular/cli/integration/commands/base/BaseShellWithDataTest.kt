package com.inso_world.binocular.cli.integration.commands.base

import com.inso_world.binocular.cli.BinocularCommandLineApplication
import com.inso_world.binocular.cli.entity.Project
import com.inso_world.binocular.cli.integration.TestDataSetupService
import com.inso_world.binocular.cli.persistence.repository.sql.BranchRepository
import com.inso_world.binocular.cli.persistence.repository.sql.CommitRepository
import com.inso_world.binocular.cli.persistence.repository.sql.ProjectRepository
import com.inso_world.binocular.cli.persistence.repository.sql.RepositoryRepository
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.shell.test.autoconfigure.AutoConfigureShell
import org.springframework.shell.test.autoconfigure.AutoConfigureShellTestClient
import org.springframework.test.annotation.DirtiesContext

@AutoConfigureShell
@AutoConfigureShellTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(
    classes = [BinocularCommandLineApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
// @ContextConfiguration(classes = [BinocularCommandLineApplication::class])
// @ShellTest
internal class BaseShellWithDataTest : BaseFixturesIntegrationTest() {
    @Autowired
    internal lateinit var commitRepository: CommitRepository

    @Autowired
    private lateinit var testDataSetupService: TestDataSetupService

    @Autowired
    internal lateinit var repositoryRepository: RepositoryRepository

    @Autowired
    internal lateinit var branchRepository: BranchRepository

    @Autowired
    internal lateinit var projectRepository: ProjectRepository

    protected lateinit var simpleProject: Project
    protected lateinit var advancedProject: Project
    protected lateinit var octoProject: Project

    @BeforeEach
    internal fun setUp() {
        simpleProject =
            this.projectRepository.save(
                Project(
                    name = SIMPLE_PROJECT_NAME,
                    description = "desc",
                ),
            )
        advancedProject =
            this.projectRepository.save(
                Project(
                    name = ADVANCED_PROJECT_NAME,
                    description = "desc",
                ),
            )
        octoProject =
            this.projectRepository.save(
                Project(
                    name = OCTO_PROJECT_NAME,
                    description = "desc",
                ),
            )
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
