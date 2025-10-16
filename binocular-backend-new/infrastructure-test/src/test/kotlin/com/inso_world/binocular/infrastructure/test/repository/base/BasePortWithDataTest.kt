package com.inso_world.binocular.infrastructure.test.repository.base

import com.inso_world.binocular.core.index.GitIndexer
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest
import com.inso_world.binocular.core.integration.base.InfrastructureDataSetup
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.infrastructure.test.config.LocalArangodbConfig
import com.inso_world.binocular.infrastructure.test.config.LocalGixConfig
import com.inso_world.binocular.infrastructure.test.config.LocalPostgresConfig
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Project
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ContextConfiguration
import kotlin.io.path.Path

@SpringBootTest
@ContextConfiguration(
    classes = [LocalArangodbConfig::class, LocalPostgresConfig::class, LocalGixConfig::class],
    initializers = [
        com.inso_world.binocular.infrastructure.arangodb.ArangodbConfig.Initializer::class,
        com.inso_world.binocular.infrastructure.sql.PostgresConfig.Initializer::class
    ]
)
@ComponentScan(basePackages = ["com.inso_world.binocular.infrastructure.test", "com.inso_world.binocular.core"])
class BasePortWithDataTest : BaseFixturesIntegrationTest() {
    @all:Autowired
    private lateinit var testDataSetupService: InfrastructureDataSetup

    @all:Autowired
    private lateinit var indexer: GitIndexer

    @all:Autowired
    private lateinit var projectPort: ProjectInfrastructurePort

    protected fun prepare(path: String, projectName: String, branch: Branch): Project {
        val repo = indexer.findRepo(Path(path))
        require(repo.branches.add(branch))
        val hashes = indexer.traverseBranch(repo,branch)
        val project =
            Project(
                name = projectName,
            )
        project.repo = repo
        repo.project = project

        repo.branches.add(branch)
        repo.commits.addAll(hashes)

        return projectPort.create(project)
    }

    @AfterEach
    fun cleanup() {
        testDataSetupService.teardown()
//        entityManager.clear()
//        projectRepository.deleteAll()
//        repositoryRepository.deleteAll()
//        commitRepository.deleteAll()
//        userRepository.deleteAll()
    }
}
