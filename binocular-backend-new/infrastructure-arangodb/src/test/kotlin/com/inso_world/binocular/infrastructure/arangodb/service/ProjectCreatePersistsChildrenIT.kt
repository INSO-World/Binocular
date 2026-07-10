package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.core.integration.base.BaseIntegrationTest
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.infrastructure.arangodb.ArangodbTestConfig
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.BranchRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.CommitRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.DeveloperRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.UserRepository
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import com.inso_world.binocular.model.vcs.ReferenceCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Isolating regression IT for the contract failure
 * `ProjectSaveOperationTest."save project with repository and commits, expecting in database"` (arangodb):
 * create() must persist the repository's developers, commits and branches (Gap A) and a legacy
 * user document so `userPort.findAll()` counts it (Gap B).
 */
@SpringBootTest
@EnableAutoConfiguration
@ContextConfiguration(
    classes = [ArangodbTestConfig::class],
    initializers = [ArangodbTestConfig.Initializer::class],
)
@ExtendWith(SpringExtension::class)
@OptIn(ExperimentalUuidApi::class)
internal class ProjectCreatePersistsChildrenIT : BaseIntegrationTest() {
    @Autowired private lateinit var projectPort: ProjectInfrastructurePort

    @Autowired private lateinit var commitRepository: CommitRepository

    @Autowired private lateinit var branchRepository: BranchRepository

    @Autowired private lateinit var developerRepository: DeveloperRepository

    @Autowired private lateinit var userRepository: UserRepository

    @BeforeEach
    fun clean() {
        commitRepository.deleteAll()
        branchRepository.deleteAll()
        developerRepository.deleteAll()
        userRepository.deleteAll()
    }

    private fun buildAggregate(): Project {
        val project = Project(name = "children-${Uuid.random()}")
        val repository = Repository(localPath = "/probe/children-${Uuid.random()}", project = project)
        val developer = Developer(name = "dev", email = "dev-${Uuid.random()}@example.com", repository = repository)
        val commit =
            Commit(
                sha =
                    Uuid
                        .random()
                        .toString()
                        .replace("-", "")
                        .padEnd(40, '0')
                        .substring(0, 40),
                message = "test commit",
                authorSignature = Signature(developer = developer, timestamp = LocalDateTime.of(2025, 7, 13, 1, 1)),
                repository = repository,
            )
        Branch(
            name = "branch-${Uuid.random()}",
            fullName = "refs/heads/branch",
            category = ReferenceCategory.LOCAL_BRANCH,
            repository = repository,
            head = commit,
        )
        return project
    }

    /** create() on a project whose repository owns one developer/commit/branch must persist all three child documents. */
    @Test
    fun `create persists repository commits branches and developers`() {
        projectPort.create(buildAggregate())

        assertAll(
            { assertThat(developerRepository.count()).isEqualTo(1L) },
            { assertThat(commitRepository.count()).isEqualTo(1L) },
            { assertThat(branchRepository.count()).isEqualTo(1L) },
        )
    }

    /** create() must write one legacy user document per developer so the users collection reflects them. */
    @Test
    fun `create persists a legacy user per developer`() {
        projectPort.create(buildAggregate())

        assertThat(userRepository.count()).isEqualTo(1L)
    }
}
