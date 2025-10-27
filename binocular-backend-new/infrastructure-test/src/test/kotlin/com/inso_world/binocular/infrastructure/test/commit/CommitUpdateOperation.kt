package com.inso_world.binocular.infrastructure.test.commit

import com.inso_world.binocular.core.integration.base.InfrastructureDataSetup
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

internal class CommitUpdateOperation : BaseInfrastructureSpringTest() {
    @Autowired
    private lateinit var infrastructureDataSetup: InfrastructureDataSetup

    @Autowired
    private lateinit var branchPort: BranchInfrastructurePort

    @Autowired
    private lateinit var userPort: UserInfrastructurePort

    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePort

    @Autowired
    private lateinit var commitPort: CommitInfrastructurePort

    private var repository =
        Repository(
            localPath = "test repository",
        )
    private lateinit var branchDomain: Branch
    private lateinit var project: Project

    private lateinit var savedCommit: Commit

    @BeforeEach
    fun setup() {
        infrastructureDataSetup.teardown()
        this.project =
            projectPort.create(
                Project(
                    name = "test project",
                    repo = repository,
                ),
            )
        this.repository = this.project.repo ?: throw IllegalStateException("test repository can not be null")
        this.branchDomain =
            Branch(
                name = "test branch",
                repository = repository,
            )
        val user =
            User(
                name = "user 1",
                email = "user@example.com",
            )
        val baseBranch =
            Branch(
                name = "fixed branch",
            )
        val baseCommit =
            Commit(
                sha = "1234567890123456789012345678901234567890",
                message = "test commit",
                commitDateTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
            )
        repository.commits.add(baseCommit)
        baseBranch.commits.add(baseCommit)
        user.committedCommits.add(baseCommit)
        baseBranch.commits.add(baseCommit)
        repository.branches.add(baseBranch)
        repository.user.add(user)

        this.savedCommit = commitPort.create(baseCommit)
    }

    @Test
    fun `update commit unchanged, should not fail`() {
        assertDoesNotThrow {
            commitPort.update(savedCommit)
        }
        assertAll(
            "check database numbers",
            { assertThat(repositoryPort.findAll()).hasSize(1) },
            { assertThat(commitPort.findAll()).hasSize(1) },
            { assertThat(branchPort.findAll()).hasSize(1) },
            { assertThat(userPort.findAll()).hasSize(1) },
        )
    }

    @Test
    fun `update commit, add new branch`() {
        assertThat(savedCommit.branches).hasSize(1)
        val newBranch =
            Branch(
                name = "new branch",
            )

        savedCommit.branches.add(newBranch)
        newBranch.commits.add(savedCommit)
        repository.branches.add(newBranch)

        assertAll(
            "check model",
            { assertThat(savedCommit.branches).hasSize(2) },
            { assertThat(newBranch.commits).hasSize(1) },
        )

        val updatedEntity =
            assertDoesNotThrow {
                commitPort.update(savedCommit)
            }

        assertThat(updatedEntity.branches).hasSize(2)
    }

    @Test
    fun `update commit, remove branch`() {
        // cleanup
        infrastructureDataSetup.teardown()

        assertAll(
            "check database numbers",
            { assertThat(commitPort.findAll()).hasSize(0) },
            { assertThat(userPort.findAll()).hasSize(0) },
            { assertThat(repositoryPort.findAll()).hasSize(0) },
            { assertThat(branchPort.findAll()).hasSize(0) },
        )

        val branchA =
            Branch(
                name = "A",
            )
        val commit =
            run {
//                just the setup part here
                val user =
                    User(
                        name = "test",
                        email = "test@example.com",
                    )
                val branchB =
                    Branch(
                        name = "B",
                    )
                val project =
                    projectPort.create(
                        Project(
                            name = "test project",
                            repo =
                                Repository(
                                    localPath = "test repository",
                                ),
                        ),
                    )
                val repository = project.repo!!
                val cmt =
                    Commit(
                        sha = "C".repeat(40),
                        message = "msg",
                        commitDateTime = LocalDateTime.now(),
                    )
                repository.commits.add(cmt)
                cmt.branches.addAll(mutableSetOf(branchA, branchB))

                user.committedCommits.add(cmt)
                repository.user.add(user)

                branchA.commits.add(cmt)
                branchB.commits.add(cmt)
                repository.branches.add(branchA)
                repository.branches.add(branchB)

                val savedCommit =
                    assertDoesNotThrow {
                        commitPort.create(cmt)
                    }

                assertAll(
                    "check database numbers",
                    { assertThat(commitPort.findAll()).hasSize(1) },
                    { assertThat(userPort.findAll()).hasSize(1) },
                    { assertThat(repositoryPort.findAll()).hasSize(1) },
                    { assertThat(branchPort.findAll()).hasSize(2) },
                )
//                setup done here
                return@run savedCommit
            }

        run {
            assertTrue(commit.branches.removeIf { it.name == "A" })
            assertThat(commit.branches).hasSize(1)

            val updatedEntity =
                assertDoesNotThrow {
                    commitPort.update(commit)
                }

            assertThat(updatedEntity.branches).hasSize(1)

            assertAll(
                "check database numbers",
                { assertThat(commitPort.findAll()).hasSize(1) },
                { assertThat(userPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(branchPort.findAll()).hasSize(1) },
            )
        }
    }
}
