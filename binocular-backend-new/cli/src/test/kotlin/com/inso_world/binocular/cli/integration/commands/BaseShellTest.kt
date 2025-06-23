package com.inso_world.binocular.cli.integration.commands

import com.inso_world.binocular.cli.BinocularCommandLineApplication
import com.inso_world.binocular.cli.persistence.repository.sql.BranchRepository
import com.inso_world.binocular.cli.persistence.repository.sql.CommitRepository
import com.inso_world.binocular.cli.persistence.repository.sql.RepositoryRepository
import com.inso_world.binocular.cli.persistence.repository.sql.UserRepository
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest
import org.junit.jupiter.api.AfterEach
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
internal class BaseShellTest : BaseFixturesIntegrationTest() {
    @Autowired
    internal lateinit var commitRepository: CommitRepository

    @Autowired
    internal lateinit var userRepository: UserRepository

    @Autowired
    internal lateinit var repositoryRepository: RepositoryRepository

    @Autowired
    internal lateinit var branchRepository: BranchRepository

    @AfterEach
    fun cleanup() {
        repositoryRepository.deleteAll()
        branchRepository.deleteAll()
        commitRepository.deleteAll()
        userRepository.deleteAll()
    }
}
