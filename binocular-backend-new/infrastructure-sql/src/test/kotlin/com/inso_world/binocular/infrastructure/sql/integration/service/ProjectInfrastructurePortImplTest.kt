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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
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

    @Nested
    inner class SaveOperation : BaseServiceTest() {
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
            val createdProject =
                projectPort.create(
                    Project(
                        name = "test project",
                        repo = repository,
                    ),
                )

            assertThat(projectPort.findAll()).hasSize(1)
            run {
                val elem = projectPort.findAll().toList()[0]
                assertThat(elem.id).isEqualTo(elem.repo?.project?.id)
                assertThat(elem)
                    .usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(createdProject)
                assertThat(elem.repo).isNotNull()
                assertThat(elem.repo?.id).isNotNull()
                assertThat(elem.repo)
                    .usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .ignoringFields("id", "project")
                    .isEqualTo(repository)
            }
            assertAll(
                "check database numbers",
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(0) },
                { assertThat(branchPort.findAll()).hasSize(0) },
                { assertThat(userPort.findAll()).hasSize(0) },
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
            run {
                val elem = commitPort.findAll().toList()[0]
                assertThat(elem)
                    .usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .ignoringFieldsMatchingRegexes(".*id", ".*repositoryId", ".*project")
                    .isEqualTo(cmt)
            }
            run {
                assertThat(
                    commitPort.findAll().toList()[0],
                ).usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .ignoringFieldsMatchingRegexes(".*id", ".*repositoryId", ".*project")
                    .isEqualTo(repositoryProject.repo?.commits?.toList()[0])
            }
            assertAll(
                "check ids",
                { assertThat(commitPort.findAll().toList()[0].id).isNotNull() },
                { assertThat(commitPort.findAll().toList()[0].repositoryId).isNotNull() },
                { assertThat(commitPort.findAll().toList()[0].repositoryId).isEqualTo(repositoryProject.repo?.id) },
            )
        }
    }

    @Nested
    inner class UpdateOperation : BaseServiceTest() {
        private lateinit var existingProject: Project

        @BeforeEach
        fun setup() {
            val p = Project(
                name = "test project",
                repo = repository
            )
            existingProject = projectPort.create(p)
        }

        @Test
        fun `update project, unchanged, should not fail`() {
            assertDoesNotThrow {
                projectPort.update(existingProject)
            }

            assertAll(
                "check database numbers",
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(0) },
                { assertThat(branchPort.findAll()).hasSize(0) },
                { assertThat(userPort.findAll()).hasSize(0) },
            )
        }

        @Test
        fun `update project, add empty repository`() {
            super.tearDown()
            assertThat(projectPort.findAll()).hasSize(0)
            assertThat(repositoryPort.findAll()).hasSize(0)

            existingProject = run {
                val p = Project(
                    name = "test project",
                    repo = null
                )
                val saved = projectPort.create(p)
                assertThat(saved.repo).isNull()
                assertAll(
                    "check database numbers",
                    { assertThat(projectPort.findAll()).hasSize(1) },
                    { assertThat(repositoryPort.findAll()).hasSize(0) },
                    { assertThat(commitPort.findAll()).hasSize(0) },
                    { assertThat(branchPort.findAll()).hasSize(0) },
                    { assertThat(userPort.findAll()).hasSize(0) },
                )

                return@run saved
            }

            existingProject.repo = repository
            repository.project = existingProject

            val saved = projectPort.update(existingProject)
            assertThat(saved.repo).isNotNull()
            assertAll(
                "check database numbers",
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(0) },
                { assertThat(branchPort.findAll()).hasSize(0) },
                { assertThat(userPort.findAll()).hasSize(0) },
            )
        }

        @Test
        fun `update project, remove repository, should not update anything`() {
            existingProject.repo = null
            assertDoesNotThrow {
                projectPort.update(existingProject)
            }
            assertAll(
                "check database numbers",
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(0) },
                { assertThat(branchPort.findAll()).hasSize(0) },
                { assertThat(userPort.findAll()).hasSize(0) },
            )
        }

        @Test
        fun `update project, change repository, should fail`() {
            existingProject.repo = Repository(
                name = "new repository",
                project = existingProject
            )
            assertThrows<IllegalArgumentException> {
                projectPort.update(existingProject)
            }
            assertAll(
                "check database numbers",
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(0) },
                { assertThat(branchPort.findAll()).hasSize(0) },
                { assertThat(userPort.findAll()).hasSize(0) },
            )
        }

        @Test
        fun `update project, add commits to repository, expect in database`() {
            run {
                val user =
                    User(
                        name = "test",
                        email = "test@example.com",
                        repository = existingProject.repo,
                    )
                val branch =
                    Branch(
                        name = "test branch",
                        repositoryId = existingProject.repo?.id,
                    )
                val cmt =
                    Commit(
                        sha = "1234567890123456789012345678901234567890",
                        message = "test commit",
                        commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
                        repositoryId = existingProject.repo?.id,
                        branches = mutableSetOf(branch),
                    )
                user.addCommittedCommit(cmt)
                existingProject.repo?.user?.add(user)
                branch.commitShas.add(cmt.sha)
                existingProject.repo?.commits?.add(cmt)
                existingProject.repo?.branches?.add(branch)
                assertAll(
                    "check model",
                    { assertThat(existingProject.repo).isNotNull() },
                    { assertThat(existingProject.repo?.commits).hasSize(1) },
                    { assertThat(existingProject.repo?.user).hasSize(1) },
                    { assertThat(existingProject.repo?.branches).hasSize(1) },
                )
            }

            assertDoesNotThrow {
                projectPort.update(existingProject)
            }
            assertAll(
                "check database numbers",
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(1) },
                { assertThat(branchPort.findAll()).hasSize(1) },
                { assertThat(userPort.findAll()).hasSize(1) },
            )
        }
    }
}
