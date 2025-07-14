package com.inso_world.binocular.infrastructure.sql.integration.service

import com.inso_world.binocular.infrastructure.sql.integration.service.base.BaseServiceTest
import com.inso_world.binocular.infrastructure.sql.service.CommitInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.sql.service.ProjectInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.sql.service.RepositoryInfrastructurePortImpl
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime

internal class RepositoryInfrastructurePortImplTest : BaseServiceTest() {
    @Autowired
    lateinit var transactionTemplate: TransactionTemplate

    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePortImpl

    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePortImpl

    @Autowired
    private lateinit var commitPort: CommitInfrastructurePortImpl

    private var repository =
        Repository(
            name = "test repository",
        )
    private lateinit var repositoryProject: Project

    @BeforeEach
    fun setup() {
        repositoryProject =
            projectPort.create(
                Project(
                    name = "test project",
                ),
            )
    }

    @Test
    fun `save plain repository, expecting in database`() {
        repository.projectId = repositoryProject.id
        repositoryPort.create(repository)

        transactionTemplate.execute {
            assertAll(
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(0) },
            )
        }
    }

    @Test
    fun `save repository with commits, expecting in database`() {
        repository.projectId = repositoryProject.id
        val cmt =
            Commit(
                sha = "1234567890123456789012345678901234567890",
                message = "test commit",
                commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),,
                repositoryId = repository.id,
            )
        repository.commits.add(cmt)

        val savedRepo = repositoryPort.create(repository)

        transactionTemplate.execute {
            assertAll(
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(1) },
                {
                    val elem = commitPort.findAll().toList()[0]
                    assertThat(elem)
                        .usingRecursiveComparison()
                        .ignoringFields("id", "repositoryId")
                        .isEqualTo(cmt)
                },
                { assertThat(commitPort.findAll().toList()[0]).isEqualTo(savedRepo.commits.toList()[0]) },
                { assertThat(commitPort.findAll().toList()[0].id).isNotNull() },
                { assertThat(commitPort.findAll().toList()[0].repositoryId).isNotNull() },
                { assertThat(commitPort.findAll().toList()[0].repositoryId).isEqualTo(savedRepo.id) },
            )
        }
    }
}
