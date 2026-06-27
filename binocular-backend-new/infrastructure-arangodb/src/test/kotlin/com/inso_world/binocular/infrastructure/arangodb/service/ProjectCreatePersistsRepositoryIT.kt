package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.core.integration.base.BaseIntegrationTest
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.infrastructure.arangodb.ArangodbTestConfig
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.ProjectRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.RepositoryRepository
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Verifies that [ProjectInfrastructurePortImpl.create] persists the owned [Repository]
 * child aggregate and wires the `@Ref` on the project document.
 *
 * Regression test for the contract-test failure
 * `ProjectSaveOperationTest."save project with repository, expecting in database"` (arangodb profile):
 * create() previously used the structure-only ProjectMapper, so ProjectEntity.repository stayed
 * null and ProjectDao.create()'s repository cascade never fired.
 */
@SpringBootTest
@EnableAutoConfiguration
@ContextConfiguration(
    classes = [ArangodbTestConfig::class],
    initializers = [ArangodbTestConfig.Initializer::class],
)
@ExtendWith(SpringExtension::class)
@OptIn(ExperimentalUuidApi::class)
internal class ProjectCreatePersistsRepositoryIT : BaseIntegrationTest() {
    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    private lateinit var projectRepository: ProjectRepository

    @Autowired
    private lateinit var repositoryRepository: RepositoryRepository

    private val projectIds = mutableListOf<String>()
    private val repositoryIds = mutableListOf<String>()

    @AfterEach
    fun cleanup() {
        projectIds.forEach { runCatching { projectRepository.deleteById(it) } }
        repositoryIds.forEach { runCatching { repositoryRepository.deleteById(it) } }
    }

    /**
     * create() on a project with an attached repository must persist the repository document,
     * wire the @Ref on the project document, and refresh both domain ids.
     */
    @Test
    fun `create project with repository persists repository and wires ref`() {
        val project = Project(name = "create-cascade-${Uuid.random()}")
        val repository = Repository(localPath = "/probe/create-cascade-${Uuid.random()}", project = project)

        val created = projectPort.create(project)
        created.id?.let { projectIds += it }

        val projectEntity = projectRepository.findByName(project.name)
        projectEntity?.repository?.id?.let { repositoryIds += it }

        val repoEntity = repositoryRepository.findByLocalPath(repository.localPath)
        repoEntity?.id?.let { if (it !in repositoryIds) repositoryIds += it }

        assertAll(
            { assertNotNull(created.id, "domain project id must be refreshed after create") },
            { assertNotNull(created.repo?.id, "domain repository id must be refreshed after create") },
            { assertNotNull(repoEntity, "repository document must be persisted") },
            { assertNotNull(projectEntity?.repository, "@Ref repository must be wired on the project document") },
        )
    }

    /**
     * Same as above but with preset document keys (as used by the contract-test mock data,
     * e.g. `p6`/`r6`): the project document must not be repserted with a dangling `@Ref`
     * to a repository document that has not been saved yet — Spring Data ArangoDB eagerly
     * resolves refs when deserializing the repsert result and fails on dangling references.
     */
    @Test
    fun `create project with preset ids persists repository without dangling ref`() {
        val suffix = Uuid.random()
        val project = Project(name = "create-cascade-preset-$suffix")
        project.id = "it-p-$suffix"
        val repository = Repository(localPath = "/probe/create-cascade-preset-$suffix", project = project)
        repository.id = "it-r-$suffix"
        projectIds += "it-p-$suffix"
        repositoryIds += "it-r-$suffix"

        val created = projectPort.create(project)

        val projectEntity = projectRepository.findByName(project.name)
        val repoEntity = repositoryRepository.findByLocalPath(repository.localPath)

        assertAll(
            { assertNotNull(created.repo?.id, "domain repository id must be set after create") },
            { assertNotNull(repoEntity, "repository document must be persisted") },
            { assertNotNull(projectEntity?.repository, "@Ref repository must be wired on the project document") },
        )
    }
}
