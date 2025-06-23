package com.inso_world.binocular.cli.integration.service.base

import com.inso_world.binocular.cli.BinocularCommandLineApplication
import com.inso_world.binocular.cli.entity.Repository
import com.inso_world.binocular.cli.index.vcs.toVcsRepository
import com.inso_world.binocular.cli.integration.utils.generateCommits
import com.inso_world.binocular.cli.integration.utils.setupRepoConfig
import com.inso_world.binocular.cli.persistence.repository.sql.BranchRepository
import com.inso_world.binocular.cli.persistence.repository.sql.CommitRepository
import com.inso_world.binocular.cli.persistence.repository.sql.RepositoryRepository
import com.inso_world.binocular.cli.persistence.repository.sql.UserRepository
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest
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
    internal lateinit var commitRepository: CommitRepository

    @Autowired
    internal lateinit var userRepository: UserRepository

    @Autowired
    internal lateinit var repositoryRepository: RepositoryRepository

    @Autowired
    internal lateinit var branchRepository: BranchRepository

    protected lateinit var simpleRepo: Repository

    @BeforeEach
    fun setupBase() {
        val simpleRepoConfig =
            setupRepoConfig(
                "${FIXTURES_PATH}/${SIMPLE_REPO}",
                "HEAD",
            )
        this.simpleRepo = simpleRepoConfig.repo.toVcsRepository().toEntity()
        generateCommits(simpleRepoConfig, simpleRepo)
        this.simpleRepo = this.repositoryRepository.save(this.simpleRepo)
    }

    @AfterEach
    fun cleanup() {
        repositoryRepository.deleteAll()
        branchRepository.deleteAll()
        commitRepository.deleteAll()
        userRepository.deleteAll()
    }
}
