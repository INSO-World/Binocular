package com.inso_world.binocular.infrastructure.test.repository.base

import com.inso_world.binocular.core.index.GitIndexer
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest
import com.inso_world.binocular.core.integration.base.InfrastructureDataSetup
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.infrastructure.test.config.LocalArangodbConfig
import com.inso_world.binocular.infrastructure.test.config.LocalGixConfig
import com.inso_world.binocular.infrastructure.test.config.LocalPostgresConfig
import com.inso_world.binocular.model.Project
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ContextConfiguration
import kotlin.io.path.Path

/**
 * Base test class for infrastructure tests that require actual Git repository data.
 * Provides utilities to index Git repositories and prepare test data.
 */
@SpringBootTest
@ContextConfiguration(
    classes = [LocalArangodbConfig::class, LocalPostgresConfig::class, LocalGixConfig::class],
    initializers = [
        com.inso_world.binocular.infrastructure.arangodb.ArangodbTestConfig.Initializer::class,
        com.inso_world.binocular.infrastructure.sql.SqlTestConfig.Initializer::class
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

    /**
     * Prepares a Git repository for testing by indexing a branch and creating a project.
     *
     * @param path Local filesystem path to the Git repository
     * @param projectName Name for the project that will own this repository
     * @param branchName Name of the branch to traverse (e.g., "main", "refs/heads/main")
     * @return The persisted Project with repository and commit data
     */
    protected fun prepare(path: String, projectName: String, branchName: String): Project {
        val project = Project(name = projectName)
        val repo = indexer.findRepo(Path(path), project)

        // Traverse the specified branch to get all its commits
        val (branch, commits) = indexer.traverseBranch(repo, branchName)

        // Repository automatically registers commits when they're created
        // Branch automatically registers with repository during construction
        // All relationships are now established

        return projectPort.create(project)
    }

    @AfterEach
    fun cleanup() {
        testDataSetupService.teardown()
    }
}
