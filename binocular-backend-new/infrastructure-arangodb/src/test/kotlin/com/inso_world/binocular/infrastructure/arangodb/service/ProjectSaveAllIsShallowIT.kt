package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.core.integration.base.BaseIntegrationTest
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.infrastructure.arangodb.ArangodbTestConfig
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.CommitRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.UserRepository
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
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
 * Pins the shallow-bulk semantics of [ProjectInfrastructurePortImpl.saveAll]: bulk seeding
 * persists only the project and repository documents — repository children (developers,
 * commits, branches) and derived legacy users are the caller's responsibility.
 * Regression net for UserControllerWebTest/DbExportControllerTest (web, arangodb-only).
 */
@SpringBootTest
@EnableAutoConfiguration
@ContextConfiguration(
    classes = [ArangodbTestConfig::class],
    initializers = [ArangodbTestConfig.Initializer::class],
)
@ExtendWith(SpringExtension::class)
@OptIn(ExperimentalUuidApi::class)
internal class ProjectSaveAllIsShallowIT : BaseIntegrationTest() {
    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var commitRepository: CommitRepository

    @BeforeEach
    fun clean() {
        userRepository.deleteAll()
        commitRepository.deleteAll()
    }

    /** saveAll() on a project graph with a developer-authored commit must not write user or commit documents. */
    @Test
    fun `saveAll persists project and repository documents only`() {
        val project = Project(name = "shallow-${Uuid.random()}")
        val repository = Repository(localPath = "/probe/shallow-${Uuid.random()}", project = project)
        val developer = Developer(name = "dev", email = "dev-${Uuid.random()}@example.com", repository = repository)
        Commit(
            sha =
                Uuid
                    .random()
                    .toString()
                    .replace("-", "")
                    .padEnd(40, '0')
                    .substring(0, 40),
            message = "seed commit",
            authorSignature = Signature(developer = developer, timestamp = LocalDateTime.of(2025, 7, 13, 1, 1)),
            repository = repository,
        )

        val saved = projectPort.saveAll(listOf(project)).toList()

        assertAll(
            { assertThat(saved.single().id).isNotNull() },
            { assertThat(saved.single().repo?.id).isNotNull() },
            { assertThat(userRepository.count()).isEqualTo(0L) },
            { assertThat(commitRepository.count()).isEqualTo(0L) },
        )
    }
}
