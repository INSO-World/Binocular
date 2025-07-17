package com.inso_world.binocular.infrastructure.sql.integration.service

import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.integration.service.base.BaseServiceTest
import com.inso_world.binocular.infrastructure.sql.service.CommitInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.sql.service.ProjectInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.sql.service.RepositoryInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.sql.service.UserInfrastructurePortImpl
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

internal class ProjectInfrastructurePortImplTest : BaseServiceTest() {
    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePortImpl

    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePortImpl

    @Autowired
    private lateinit var commitPort: CommitInfrastructurePortImpl

    @Autowired
    private lateinit var branchPort: BranchInfrastructurePort

    @Autowired
    private lateinit var userPort: UserInfrastructurePortImpl

    private var repository =
        Repository(
            name = "test repository",
        )

    @BeforeEach
    fun setup() {
    }

    @Test
    fun `save plain project, expecting in database`() {
        val repositoryProject =
            projectPort.create(
                Project(
                    name = "test project",
                ),
            )
        assertAll(
            { assertThat(projectPort.findAll()).hasSize(1) },
            { assertThat(projectPort.findAll().toList()[0].id).isNotNull() },
            {
                assertThat(projectPort.findAll().toList()[0])
                    .usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(repositoryProject)
            },
            { assertThat(repositoryPort.findAll()).hasSize(0) },
            { assertThat(commitPort.findAll()).hasSize(0) },
            { assertThat(branchPort.findAll()).hasSize(0) },
        )
    }

    @Test
    fun `save project with repository, expecting in database`() {
        val repositoryProject =
            projectPort.create(
                Project(
                    name = "test project",
                    repo = repository,
                ),
            )

        assertThat(projectPort.findAll()).hasSize(1)
        assertAll(
            {
                val elem = projectPort.findAll().toList()[0]
                assertThat(elem.id).isEqualTo(elem.repo?.projectId)
            },
            {
                val elem = projectPort.findAll().toList()[0]
                assertThat(elem)
                    .usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(repositoryProject)
            },
            {
                val elem = projectPort.findAll().toList()[0]
                assertThat(elem.repo).isNotNull()
            },
            {
                val elem = projectPort.findAll().toList()[0]
                assertThat(elem.repo?.id).isNotNull()
            },
            {
                val elem = projectPort.findAll().toList()[0]
                assertThat(elem.repo)
                    .usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .ignoringFields("id", "projectId")
                    .isEqualTo(repository)
            },
            { assertThat(repositoryPort.findAll()).hasSize(1) },
            { assertThat(commitPort.findAll()).hasSize(0) },
            { assertThat(branchPort.findAll()).hasSize(0) },
        )
    }

    @Test
    fun `save project with repository and commits, expecting in database`() {
        val user =
            User(
                name = "test",
                email = "test@example.com",
                repository = repository,
            )
        val branch =
            Branch(
                name = "test branch",
                repositoryId = repository.id,
            )
        val cmt =
            Commit(
                sha = "1234567890123456789012345678901234567890",
                message = "test commit",
                commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
                repositoryId = repository.id,
                branches = mutableSetOf(branch),
            )
        user.addCommittedCommit(cmt)
        repository.user.add(user)
        branch.commitShas.add(cmt.sha)
        repository.commits.add(cmt)
        repository.branches.add(branch)

        val repositoryProject =
            projectPort.create(
                Project(
                    name = "test project",
                    repo = repository,
                ),
            )

        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(1) },
            { assertThat(repositoryPort.findAll()).hasSize(1) },
            { assertThat(commitPort.findAll()).hasSize(1) },
            { assertThat(branchPort.findAll()).hasSize(1) },
            { assertThat(userPort.findAll()).hasSize(1) },
        )
        assertAll(
            {
                val elem = commitPort.findAll().toList()[0]
                assertThat(elem)
                    .usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .ignoringFieldsMatchingRegexes(".*id", ".*repositoryId", ".*projectId")
                    .isEqualTo(cmt)
            },
            {
                assertThat(
                    commitPort.findAll().toList()[0],
                ).usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .ignoringFieldsMatchingRegexes(".*id", ".*repositoryId", ".*projectId")
                    .isEqualTo(repositoryProject.repo?.commits?.toList()[0])
            },
        )
        assertAll(
            "check ids",
            { assertThat(commitPort.findAll().toList()[0].id).isNotNull() },
            { assertThat(commitPort.findAll().toList()[0].repositoryId).isNotNull() },
            { assertThat(commitPort.findAll().toList()[0].repositoryId).isEqualTo(repositoryProject.repo?.id) },
        )
    }
}
