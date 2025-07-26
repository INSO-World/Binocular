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
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class RepositoryInfrastructurePortImplTest : BaseServiceTest() {
    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePortImpl

    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePortImpl

    @Autowired
    private lateinit var commitPort: CommitInfrastructurePortImpl

    @Autowired
    private lateinit var userPort: UserInfrastructurePortImpl

    @Autowired
    private lateinit var branchPort: BranchInfrastructurePort

    private lateinit var repositoryProject: Project

    @BeforeEach
    @Transactional
    fun setup() {
        repositoryProject =
            projectPort.create(
                Project(
                    name = "test project",
                ),
            )
    }

    @AfterEach
    fun teardown() {
        transactionTemplate.execute { entityManager.clear() }
    }

    @Nested
    inner class SaveOperation : BaseServiceTest() {
        @Test
        fun `save repository with one commit, expecting in database`() {
            val savedRepo =
                run {
                    val repository =
                        Repository(
                            name = "test repository",
                            project = repositoryProject,
                        )
                    val user =
                        User(
                            name = "test",
                            email = "test@example.com",
                            repository = repository,
                        )

                    repository.project = repositoryProject
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
                            committer = user,
                        )
                    user.addCommittedCommit(cmt)

                    branch.commitShas.add(cmt.sha)
                    repository.branches.add(branch)
                    repository.commits.add(cmt)
                    repository.user.add(user)

                    assertAll(
                        "check model",
                        { assertThat(branch.commitShas).hasSize(1) },
                        { assertThat(cmt.branches).hasSize(1) },
                        { assertThat(repository.branches).hasSize(1) },
                        { assertThat(repository.user).hasSize(1) },
                        { assertThat(repository.commits).hasSize(1) },
                    )

                    return@run repositoryPort.create(repository)
                }

            assertAll(
                "check database numbers",
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(1) },
                { assertThat(branchPort.findAll()).hasSize(1) },
                { assertThat(userPort.findAll()).hasSize(1) },
            )
            assertThat(commitPort.findAll().toList()[0])
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(savedRepo.commits.toList()[0])
            assertAll(
                "check commit relationship",
                { assertThat(commitPort.findAll().toList()[0].id).isNotNull() },
                { assertThat(commitPort.findAll().toList()[0].repositoryId).isNotNull() },
                { assertThat(commitPort.findAll().toList()[0].repositoryId).isEqualTo(savedRepo.id) },
            )
        }

        @Test
        fun `save repository with one commit with one parent, expecting both in database`() {
            val savedRepo =
                run {
                    val repository =
                        Repository(
                            name = "test repository",
                            project = repositoryProject,
                        )
                    val user =
                        User(
                            name = "test",
                            email = "test@example.com",
                            repository = repository,
                        )

                    repository.project = repositoryProject
                    val branch =
                        Branch(
                            name = "test branch",
                            repositoryId = repository.id,
                        )
                    val parent = Commit(
                        sha = "0".repeat(40),
                        message = "test commit 2",
                        commitDateTime = LocalDateTime.of(2025, 5, 13, 1, 1),
                        repositoryId = repository.id,
                        branches = mutableSetOf(branch),
                        committer = user,
                        parents = mutableSetOf()
                    )
                    val cmt =
                        Commit(
                            sha = "1".repeat(40),
                            message = "test commit",
                            commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
                            repositoryId = repository.id,
                            branches = mutableSetOf(branch),
                            committer = user,
                            parents = mutableSetOf(parent)
                        )
                    parent.children.add(cmt)
                    user.addCommittedCommit(cmt)
                    user.addCommittedCommit(parent)

                    branch.commitShas.add(cmt.sha)
                    repository.branches.add(branch)
                    repository.commits.add(cmt)
                    repository.user.add(user)

                    assertAll(
                        "check model",
                        { assertThat(branch.commitShas).hasSize(1) },
                        { assertThat(cmt.parents).hasSize(1) },
                        { assertThat(cmt.branches).hasSize(1) },
                        { assertThat(repository.branches).hasSize(1) },
                        { assertThat(repository.user).hasSize(1) },
                        { assertThat(repository.commits).hasSize(1) },
                    )

                    return@run repositoryPort.create(repository)
                }

            assertAll(
                "check database numbers",
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(2) },
                { assertThat(branchPort.findAll()).hasSize(1) },
                { assertThat(userPort.findAll()).hasSize(1) },
            )
//            assert parent
            assertThat(commitPort.findAll().find { it.sha == "0".repeat(40) })
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(savedRepo.commits.find { it.sha == "0".repeat(40) })
//            assert child
            assertThat(commitPort.findAll().find { it.sha == "1".repeat(40) })
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(savedRepo.commits.find { it.sha == "1".repeat(40) })
            assertAll(
                "check commit relationship",
                { assertThat(commitPort.findAll().map { it.id }).doesNotContainNull() },
                { assertThat(commitPort.findAll().map { it.repositoryId }).doesNotContainNull() },
                { assertThat(commitPort.findAll().map { it.repositoryId }).containsOnly(savedRepo.id) },
            )
        }

        @Test
        fun `save repository with one commit with two parents, expecting both in database`() {
            val savedRepo =
                run {
                    val repository =
                        Repository(
                            name = "test repository",
                            project = repositoryProject,
                        )
                    val user =
                        User(
                            name = "test",
                            email = "test@example.com",
                            repository = repository,
                        )

                    repository.project = repositoryProject
                    val branch =
                        Branch(
                            name = "test branch",
                            repositoryId = repository.id,
                        )
                    val parent1 = Commit(
                        sha = "1".repeat(40),
                        message = "parent1",
                        commitDateTime = LocalDateTime.of(2025, 5, 13, 1, 1),
                        repositoryId = repository.id,
                        branches = mutableSetOf(branch),
                        committer = user,
                        parents = mutableSetOf()
                    )
                    val parent2 = Commit(
                        sha = "2".repeat(40),
                        message = "parent2",
                        commitDateTime = LocalDateTime.of(2025, 5, 13, 1, 1),
                        repositoryId = repository.id,
                        branches = mutableSetOf(branch),
                        committer = user,
                        parents = mutableSetOf()
                    )
                    val cmt =
                        Commit(
                            sha = "c".repeat(40),
                            message = "test commit",
                            commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
                            repositoryId = repository.id,
                            branches = mutableSetOf(branch),
                            committer = user,
                            parents = mutableSetOf(parent1, parent2)
                        )
                    parent1.children.add(cmt)
                    parent2.children.add(cmt)
                    user.addCommittedCommit(cmt)
                    user.addCommittedCommit(parent1)
                    user.addCommittedCommit(parent2)

                    branch.commitShas.add(cmt.sha)
                    repository.branches.add(branch)
                    repository.commits.add(cmt)
                    repository.user.add(user)

                    assertAll(
                        "check model",
                        { assertThat(branch.commitShas).hasSize(1) },
                        { assertThat(cmt.parents).hasSize(2) },
                        { assertThat(cmt.branches).hasSize(1) },
                        { assertThat(repository.branches).hasSize(1) },
                        { assertThat(repository.user).hasSize(1) },
                        { assertThat(repository.commits).hasSize(1) },
                    )

                    return@run repositoryPort.create(repository)
                }

            assertAll(
                "check database numbers",
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(3) },
                { assertThat(branchPort.findAll()).hasSize(1) },
                { assertThat(userPort.findAll()).hasSize(1) },
            )
//            assert parent1
            assertThat(commitPort.findAll().find { it.sha == "1".repeat(40) })
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(savedRepo.commits.find { it.sha == "1".repeat(40) })
//            assert parent2
            assertThat(commitPort.findAll().find { it.sha == "2".repeat(40) })
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(savedRepo.commits.find { it.sha == "2".repeat(40) })
//            assert child
            assertThat(commitPort.findAll().find { it.sha == "c".repeat(40) })
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(savedRepo.commits.find { it.sha == "c".repeat(40) })
//            check relationship of child
            run {
                val child = savedRepo.commits.find { it.sha == "c".repeat(40) } ?: throw IllegalStateException("child must be found here")

                assertThat(child.parents).hasSize(2)
                assertThat(child.children).isEmpty()
            }
            assertAll(
                "check commit relationship",
                { assertThat(commitPort.findAll().map { it.id }).doesNotContainNull() },
                { assertThat(commitPort.findAll().map { it.repositoryId }).doesNotContainNull() },
                { assertThat(commitPort.findAll().map { it.repositoryId }).containsOnly(savedRepo.id) },
            )
        }

        @Test
        fun `save plain repository, expecting in database`() {
            val repository =
                Repository(
                    name = "test repository",
                    project = repositoryProject,
                )

            repositoryProject.repo = repository
            assertDoesNotThrow {
                repositoryPort.create(repository)
            }

            assertAll(
                "check database numbers",
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(0) },
                { assertThat(branchPort.findAll()).hasSize(0) },
            )
        }

        @Test
        fun `save repository with two commits, expecting in database`() {
            val repository =
                Repository(
                    name = "test repository",
                    project = repositoryProject,
                )

            val user =
                User(
                    name = "test",
                    email = "test@example.com",
                    repository = repository,
                )

            repository.project = repositoryProject
            val branch =
                Branch(
                    name = "test branch",
                    repositoryId = repository.id,
                )
            val cmtA =
                Commit(
                    sha = "A".repeat(40),
                    message = "test commit",
                    commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
                    repositoryId = repository.id,
                    branches = mutableSetOf(branch),
                )
            val cmtB =
                Commit(
                    sha = "B".repeat(40),
                    message = "test commit",
                    commitDateTime = LocalDateTime.of(2024, 8, 13, 1, 1),
                    repositoryId = repository.id,
                    branches = mutableSetOf(branch),
                )
            user.addCommittedCommit(cmtA)
            user.addCommittedCommit(cmtB)

            branch.commitShas.add(cmtA.sha)
            branch.commitShas.add(cmtB.sha)

            repository.branches.add(branch)
            repository.commits.add(cmtA)
            repository.commits.add(cmtB)
            repository.user.add(user)

            assertAll(
                "check model",
                { assertThat(branch.commitShas).hasSize(2) },
                { assertThat(cmtA.branches).hasSize(1) },
                { assertThat(cmtB.branches).hasSize(1) },
                { assertThat(repository.branches).hasSize(1) },
                { assertThat(repository.commits).hasSize(2) },
                { assertThat(repository.user).hasSize(1) },
            )

            val savedEntity =
                assertDoesNotThrow {
                    repositoryPort.create(repository)
                }

            assertAll(
                "check saved entity",
                { assertThat(savedEntity.branches).hasSize(1) },
                { assertThat(savedEntity.commits).hasSize(2) },
            )

            assertAll(
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(2) },
                { assertThat(branchPort.findAll()).hasSize(1) },
                { assertThat(userPort.findAll()).hasSize(1) },
            )
        }
    }

    @Nested
    inner class UpdateOperations : BaseServiceTest() {
        @Test
        fun `save empty repository, update with two commits, one branch`() {
            val repository =
                assertDoesNotThrow {
                    repositoryPort.create(
                        Repository(
                            name = "test repository",
                            project = repositoryProject,
                        ),
                    )
                }

            assertAll(
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(0) },
                { assertThat(branchPort.findAll()).hasSize(0) },
            )
            repository.project = repositoryProject

            val branch =
                Branch(
                    name = "test branch",
                    repositoryId = repository.id,
                )
            val user =
                User(
                    name = "test",
                    email = "test@example.com",
                    repository = repository,
                )
            val cmtA =
                Commit(
                    sha = "A".repeat(40),
                    message = "test commit",
                    commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
                    repositoryId = repository.id,
                    branches = mutableSetOf(branch),
                )
            val cmtB =
                Commit(
                    sha = "B".repeat(40),
                    message = "test commit",
                    commitDateTime = LocalDateTime.of(2024, 8, 13, 1, 1),
                    repositoryId = repository.id,
                    branches = mutableSetOf(branch),
                )
            user.addCommittedCommit(cmtA)
            user.addCommittedCommit(cmtB)

            branch.commitShas.add(cmtA.sha)
            branch.commitShas.add(cmtB.sha)

            repository.branches.add(branch)
            repository.commits.add(cmtA)
            repository.commits.add(cmtB)
            repository.user.add(user)

            assertAll(
                "check model",
                { assertThat(branch.commitShas).hasSize(2) },
                { assertThat(repository.commits).hasSize(2) },
                { assertThat(repository.branches).hasSize(1) },
                { assertThat(repository.user).hasSize(1) },
            )

            val updatedEntity =
                assertDoesNotThrow {
                    repositoryPort.update(repository)
                }
            assertAll(
                "Check updated entity",
                { assertThat(updatedEntity.commits).hasSize(2) },
                { assertThat(updatedEntity.branches).hasSize(1) },
                { assertThat(updatedEntity.user).hasSize(1) },
            )
            assertThat(updatedEntity.branches.toList()[0].commitShas).hasSize(2)

            assertAll(
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(2) },
                { assertThat(branchPort.findAll()).hasSize(1) },
                { assertThat(userPort.findAll()).hasSize(1) },
            )

            assertThat(repositoryPort.findAll().toList()[0])
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(updatedEntity)
        }

        @Test
        fun `save empty repository, update with one commit, no parent`() {
            val repository =
                assertDoesNotThrow {
                    repositoryPort.create(
                        Repository(
                            name = "test repository",
                            project = repositoryProject,
                        ),
                    )
                }

            assertAll(
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(0) },
                { assertThat(branchPort.findAll()).hasSize(0) },
                { assertThat(userPort.findAll()).hasSize(0) },
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
            val user =
                User(
                    name = "test",
                    email = "test@example.com",
                    repository = repository,
                )
            user.addCommittedCommit(cmt)
            branch.commitShas.add(cmt.sha)
            repository.branches.add(branch)
            repository.commits.add(cmt)
            repository.user.add(user)

            assertAll(
                "Check model",
                { assertThat(branch.commitShas).hasSize(1) },
                { assertThat(repository.branches).hasSize(1) },
                { assertThat(repository.commits).hasSize(1) },
                { assertThat(repository.user).hasSize(1) },
            )

            val updatedEntity =
                assertDoesNotThrow {
                    repositoryPort.update(repository)
                }
            assertAll(
                "Check updated entity",
                { assertThat(updatedEntity.commits).hasSize(1) },
                { assertThat(updatedEntity.branches).hasSize(1) },
                { assertThat(updatedEntity.user).hasSize(1) },
            )

            assertAll(
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(1) },
                { assertThat(branchPort.findAll()).hasSize(1) },
                { assertThat(userPort.findAll()).hasSize(1) },
            )

            assertThat(repositoryPort.findAll().toList()[0])
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(updatedEntity)
        }

        @Test
        fun `save empty repository, update with one commit, one parent`() {
            val repository =
                assertDoesNotThrow {
                    repositoryPort.create(
                        Repository(
                            name = "test repository",
                            project = repositoryProject,
                        ),
                    )
                }

            assertAll(
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(0) },
                { assertThat(branchPort.findAll()).hasSize(0) },
                { assertThat(userPort.findAll()).hasSize(0) },
            )

            val branch =
                Branch(
                    name = "test branch",
                    repositoryId = repository.id,
                )
            val parent =
                Commit(
                    sha = "1".repeat(40),
                    message = "test commit",
                    commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
                    repositoryId = repository.id,
                    branches = mutableSetOf(branch),
                )
            val cmt =
                Commit(
                    sha = "c".repeat(40),
                    message = "test commit",
                    commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
                    repositoryId = repository.id,
                    branches = mutableSetOf(branch),
                    parents = mutableSetOf(parent)
                )
            val user =
                User(
                    name = "test",
                    email = "test@example.com",
                    repository = repository,
                )
            user.addCommittedCommit(cmt)
            user.addCommittedCommit(parent)
            branch.commitShas.add(cmt.sha)
            repository.branches.add(branch)
            repository.commits.add(cmt)
            repository.user.add(user)

            assertAll(
                "Check model",
                {assertThat(cmt.parents).hasSize(1)},
                { assertThat(branch.commitShas).hasSize(1) },
                { assertThat(repository.branches).hasSize(1) },
                { assertThat(repository.commits).hasSize(1) },
                { assertThat(repository.user).hasSize(1) },
            )

            val updatedEntity =
                assertDoesNotThrow {
                    repositoryPort.update(repository)
                }
            assertAll(
                "Check updated entity",
                { assertThat(updatedEntity.commits).hasSize(2) },
                { assertThat(updatedEntity.commits.find { it.sha == "c".repeat(40) }?.parents).hasSize(1) },
                { assertThat(updatedEntity.commits.find { it.sha == "c".repeat(40) }?.children).isEmpty() },
                { assertThat(updatedEntity.commits.find { it.sha == "1".repeat(40) }?.children).hasSize(1) },
                { assertThat(updatedEntity.commits.find { it.sha == "1".repeat(40) }?.parents).isEmpty() },
                { assertThat(updatedEntity.branches).hasSize(1) },
                { assertThat(updatedEntity.user).hasSize(1) },
            )

            assertAll(
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(2) },
                { assertThat(branchPort.findAll()).hasSize(1) },
                { assertThat(userPort.findAll()).hasSize(1) },
            )

            assertThat(repositoryPort.findAll().toList()[0])
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(updatedEntity)
        }

        @Test
        fun `save empty repository, update with one commit, two parents`() {
            val repository =
                assertDoesNotThrow {
                    repositoryPort.create(
                        Repository(
                            name = "test repository",
                            project = repositoryProject,
                        ),
                    )
                }

            assertAll(
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(0) },
                { assertThat(branchPort.findAll()).hasSize(0) },
                { assertThat(userPort.findAll()).hasSize(0) },
            )

            val branch =
                Branch(
                    name = "test branch",
                    repositoryId = repository.id,
                )
            val parent1 =
                Commit(
                    sha = "1".repeat(40),
                    message = "parent1",
                    commitDateTime = LocalDateTime.of(2025, 6, 13, 1, 1),
                    repositoryId = repository.id,
                    branches = mutableSetOf(branch),
                )
            val parent2 =
                Commit(
                    sha = "2".repeat(40),
                    message = "parent2",
                    commitDateTime = LocalDateTime.of(2025, 6, 14, 1, 1),
                    repositoryId = repository.id,
                    branches = mutableSetOf(branch),
                )
            val cmt =
                Commit(
                    sha = "c".repeat(40),
                    message = "test commit",
                    commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
                    repositoryId = repository.id,
                    branches = mutableSetOf(branch),
                    parents = mutableSetOf(parent1, parent2)
                )
            val user =
                User(
                    name = "test",
                    email = "test@example.com",
                    repository = repository,
                )
            user.addCommittedCommit(cmt)
            user.addCommittedCommit(parent1)
            user.addCommittedCommit(parent2)
            branch.commitShas.add(cmt.sha)
            repository.branches.add(branch)
            repository.commits.add(cmt)
            repository.user.add(user)

            assertAll(
                "Check model",
                {assertThat(cmt.parents).hasSize(2)},
                { assertThat(branch.commitShas).hasSize(1) },
                { assertThat(repository.branches).hasSize(1) },
                { assertThat(repository.commits).hasSize(1) },
                { assertThat(repository.user).hasSize(1) },
            )

            val updatedEntity =
                assertDoesNotThrow {
                    repositoryPort.update(repository)
                }
            assertAll(
                "Check updated entity",
                { assertThat(updatedEntity.commits).hasSize(3) },
                { assertThat(updatedEntity.commits.find { it.sha == "c".repeat(40) }?.parents).hasSize(2) },
                { assertThat(updatedEntity.commits.find { it.sha == "c".repeat(40) }?.children).isEmpty() },
                { assertThat(updatedEntity.commits.find { it.sha == "1".repeat(40) }?.children).hasSize(1) },
                { assertThat(updatedEntity.commits.find { it.sha == "1".repeat(40) }?.parents).isEmpty() },
                { assertThat(updatedEntity.commits.find { it.sha == "2".repeat(40) }?.children).hasSize(1) },
                { assertThat(updatedEntity.commits.find { it.sha == "2".repeat(40) }?.parents).isEmpty() },
                { assertThat(updatedEntity.branches).hasSize(1) },
                { assertThat(updatedEntity.user).hasSize(1) },
            )

            assertAll(
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(3) },
                { assertThat(branchPort.findAll()).hasSize(1) },
                { assertThat(userPort.findAll()).hasSize(1) },
            )

            assertThat(repositoryPort.findAll().toList()[0])
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(updatedEntity)
        }

        @Test
        fun `save empty repository, update with one commit, update second time unchanged`() {
            val repository =
                assertDoesNotThrow {
                    repositoryPort.create(
                        Repository(
                            name = "test repository",
                            project = repositoryProject,
                        ),
                    )
                }

            assertAll(
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(0) },
                { assertThat(branchPort.findAll()).hasSize(0) },
                { assertThat(userPort.findAll()).hasSize(0) },
            )

            val branch =
                Branch(
                    name = "test branch",
                    repositoryId = repository.id,
                )
            val user =
                User(
                    name = "test",
                    email = "test@example.com",
                    repository = repository,
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
            branch.commitShas.add(cmt.sha)
            repository.branches.add(branch)
            repository.commits.add(cmt)
            repository.user.add(user)

            assertDoesNotThrow {
                repositoryPort.update(repository)
            }

            assertAll(
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(1) },
                { assertThat(branchPort.findAll()).hasSize(1) },
                { assertThat(userPort.findAll()).hasSize(1) },
            )

            val updatedEntity =
                assertDoesNotThrow {
                    repositoryPort.update(repository)
                }

            assertAll(
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(1) },
                { assertThat(branchPort.findAll()).hasSize(1) },
                { assertThat(userPort.findAll()).hasSize(1) },
            )

            assertThat(repositoryPort.findAll().toList()[0])
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(updatedEntity)
        }

        @Test
        fun `save empty repository, update with one commit, update again with new commit`() {
            val repository =
                assertDoesNotThrow {
                    repositoryPort.create(
                        Repository(
                            name = "test repository",
                            project = repositoryProject,
                        ),
                    )
                }

            assertAll(
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(0) },
                { assertThat(branchPort.findAll()).hasSize(0) },
                { assertThat(userPort.findAll()).hasSize(0) },
            )

            val branch =
                Branch(
                    name = "test branch",
                    repositoryId = repository.id,
                )

            run {
                val cmt =
                    Commit(
                        sha = "1234567890123456789012345678901234567890",
                        message = "test commit",
                        commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
                        repositoryId = repository.id,
                        branches = mutableSetOf(branch),
                    )
                val user =
                    User(
                        name = "test",
                        email = "test@example.com",
                        repository = repository,
                    )
                user.addCommittedCommit(cmt)
                branch.commitShas.add(cmt.sha)
                repository.branches.add(branch)
                repository.commits.add(cmt)
                repository.user.add(user)

                assertDoesNotThrow {
                    repositoryPort.update(repository)
                }

                assertAll(
                    { assertThat(projectPort.findAll()).hasSize(1) },
                    { assertThat(repositoryPort.findAll()).hasSize(1) },
                    { assertThat(commitPort.findAll()).hasSize(1) },
                    { assertThat(branchPort.findAll()).hasSize(1) },
                    { assertThat(userPort.findAll()).hasSize(1) },
                )
            }

            run {
                val cmt =
                    Commit(
                        sha = "B".repeat(40),
                        message = "test commit 2",
                        commitDateTime = LocalDateTime.of(2024, 1, 26, 1, 1),
                        repositoryId = repository.id,
                        branches = mutableSetOf(branch),
                    )
                val user =
                    User(
                        name = "test2",
                        email = "test2@example.com",
                        repository = repository,
                    )
                user.addCommittedCommit(cmt)
                branch.commitShas.add(cmt.sha)
                repository.branches.add(branch)
                repository.commits.add(cmt)
                repository.user.add(user)

                val updatedEntity =
                    assertDoesNotThrow {
                        repositoryPort.update(repository)
                    }

                assertAll(
                    { assertThat(projectPort.findAll()).hasSize(1) },
                    { assertThat(repositoryPort.findAll()).hasSize(1) },
                    { assertThat(commitPort.findAll()).hasSize(2) },
                    { assertThat(branchPort.findAll()).hasSize(1) },
                    { assertThat(userPort.findAll()).hasSize(2) },
                )

                assertThat(repositoryPort.findAll().toList()[0])
                    .usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .isEqualTo(updatedEntity)
            }
        }

        @Test
        fun `save empty repository, update with two commits, two branches`() {
            val repository =
                assertDoesNotThrow {
                    repositoryPort.create(
                        Repository(
                            name = "test repository",
                            project = repositoryProject,
                        ),
                    )
                }

            assertAll(
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(0) },
                { assertThat(branchPort.findAll()).hasSize(0) },
                { assertThat(userPort.findAll()).hasSize(0) },
            )

            run {
                val branch =
                    Branch(
                        name = "test branch 1",
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
                val user =
                    User(
                        name = "test",
                        email = "test@example.com",
                        repository = repository,
                    )
                user.addCommittedCommit(cmt)
                branch.commitShas.add(cmt.sha)
                repository.branches.add(branch)
                repository.commits.add(cmt)
                repository.user.add(user)

                val updatedEntity =
                    assertDoesNotThrow {
                        repositoryPort.update(repository)
                    }
                assertAll(
                    "Check updated entity",
                    { assertThat(updatedEntity.commits).hasSize(1) },
                    { assertThat(updatedEntity.branches).hasSize(1) },
                    { assertThat(updatedEntity.user).hasSize(1) },
                )

                assertAll(
                    { assertThat(projectPort.findAll()).hasSize(1) },
                    { assertThat(repositoryPort.findAll()).hasSize(1) },
                    { assertThat(commitPort.findAll()).hasSize(1) },
                    { assertThat(branchPort.findAll()).hasSize(1) },
                    { assertThat(userPort.findAll()).hasSize(1) },
                )
            }

            run {
                val branch =
                    Branch(
                        name = "test branch 2",
                        repositoryId = repository.id,
                    )

                val cmt =
                    Commit(
                        sha = "B".repeat(40),
                        message = "test commit 2",
                        commitDateTime = LocalDateTime.of(2024, 1, 26, 1, 1),
                        repositoryId = repository.id,
                        branches = mutableSetOf(branch),
                    )
                val user = // same as before
                    User(
                        name = "test",
                        email = "test@example.com",
                        repository = repository,
                    )
                user.addCommittedCommit(cmt)
                branch.commitShas.add(cmt.sha)
                repository.branches.add(branch)
                repository.commits.add(cmt)
                repository.user.add(user)

                val updatedEntity =
                    assertDoesNotThrow {
                        repositoryPort.update(repository)
                    }
                assertAll(
                    "Check updated entity",
                    { assertThat(updatedEntity.commits).hasSize(2) },
                    { assertThat(updatedEntity.branches).hasSize(2) },
                    { assertThat(updatedEntity.user).hasSize(1) },
                )

                assertAll(
                    { assertThat(projectPort.findAll()).hasSize(1) },
                    { assertThat(repositoryPort.findAll()).hasSize(1) },
                    { assertThat(commitPort.findAll()).hasSize(2) },
                    { assertThat(branchPort.findAll()).hasSize(2) },
                    { assertThat(userPort.findAll()).hasSize(1) },
                )

                assertThat(repositoryPort.findAll().toList()[0])
                    .usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .isEqualTo(updatedEntity)
            }
        }

        @Test
        fun `save empty repository, update with one commits, add new branch to commit`() {
            val repository =
                assertDoesNotThrow {
                    repositoryPort.create(
                        Repository(
                            name = "test repository",
                            project = repositoryProject,
                        ),
                    )
                }

            assertAll(
                "Check database numbers",
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(0) },
                { assertThat(branchPort.findAll()).hasSize(0) },
            )

            val cmt =
                Commit(
                    sha = "1234567890123456789012345678901234567890",
                    message = "test commit",
                    commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
                    repositoryId = repository.id,
                    branches = mutableSetOf(),
                )
            val user =
                User(
                    name = "test",
                    email = "test@example.com",
                    repository = repository,
                )
            user.addCommittedCommit(cmt)

            run {
                val branch =
                    Branch(
                        name = "test branch 1",
                        repositoryId = repository.id,
                    )

                cmt.branches.add(branch)
                branch.commitShas.add(cmt.sha)
                repository.branches.add(branch)
                repository.commits.add(cmt)
                repository.user.add(user)

                assertAll(
                    "check model",
                    { assertThat(cmt.branches).hasSize(1) },
                    { assertThat(branch.commitShas).hasSize(1) },
                    { assertThat(repository.branches).hasSize(1) },
                    { assertThat(repository.commits).hasSize(1) },
                )

                val updatedEntity =
                    assertDoesNotThrow {
                        repositoryPort.update(repository)
                    }

                assertAll(
                    "check updated entity",
                    { assertThat(updatedEntity.branches).hasSize(1) },
                    { assertThat(updatedEntity.commits).hasSize(1) },
                    { assertThat(updatedEntity.user).hasSize(1) },
                )

                assertAll(
                    "Check database numbers",
                    { assertThat(projectPort.findAll()).hasSize(1) },
                    { assertThat(repositoryPort.findAll()).hasSize(1) },
                    { assertThat(commitPort.findAll()).hasSize(1) },
                    { assertThat(branchPort.findAll()).hasSize(1) },
                    { assertThat(userPort.findAll()).hasSize(1) },
                )
            }

            run {
                val branch =
                    Branch(
                        name = "test branch 2",
                        repositoryId = repository.id,
                    )

                cmt.branches.add(branch)
                branch.commitShas.add(cmt.sha)
                repository.branches.add(branch)
                repository.commits.add(cmt)

                assertAll(
                    "check model",
                    { assertThat(cmt.branches).hasSize(2) },
                    { assertThat(branch.commitShas).hasSize(1) },
                    { assertThat(repository.branches).hasSize(2) },
                    { assertThat(repository.commits).hasSize(1) },
                    { assertThat(repository.user).hasSize(1) },
                )

                val updatedEntity =
                    assertDoesNotThrow {
                        repositoryPort.update(repository)
                    }
                transactionTemplate.execute { entityManager.flush() }
                assertAll(
                    "check updated entity",
                    { assertThat(updatedEntity.branches).hasSize(2) },
                    { assertThat(updatedEntity.commits).hasSize(1) },
                    { assertThat(updatedEntity.commits.toList()[0].branches).hasSize(2) },
                )

                assertAll(
                    "Check database numbers",
                    { assertThat(projectPort.findAll()).hasSize(1) },
                    { assertThat(repositoryPort.findAll()).hasSize(1) },
                    { assertThat(commitPort.findAll()).hasSize(1) },
                    { assertThat(branchPort.findAll()).hasSize(2) },
                    { assertThat(userPort.findAll()).hasSize(1) },
                )
                assertThat(
                    commitPort.findAll().toList()[0].branches,
                ).hasSize(2)

                assertThat(repositoryPort.findAll().toList()[0])
                    .usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .isEqualTo(updatedEntity)
            }
        }

        @Test
        fun `save empty repository, update with commit, update with same commit again`() {
            val repository =
                assertDoesNotThrow {
                    repositoryPort.create(
                        Repository(
                            name = "test repository",
                            project = repositoryProject,
                        ),
                    )
                }

            assertAll(
                "Check database numbers",
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(0) },
                { assertThat(branchPort.findAll()).hasSize(0) },
                { assertThat(userPort.findAll()).hasSize(0) },
            )

            val branch =
                Branch(
                    name = "test branch 1",
                    repositoryId = repository.id,
                )
            val user =
                User(
                    name = "test",
                    email = "test@example.com",
                    repository = repository,
                )
            run {
                val cmt =
                    Commit(
                        sha = "1234567890123456789012345678901234567890",
                        message = "test commit",
                        commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
                        repositoryId = repository.id,
                        branches = mutableSetOf(branch),
                    )
                user.addCommittedCommit(cmt)
                assertAll(
                    "Adding new commit",
                    { assertTrue(branch.commitShas.add(cmt.sha)) },
                    { assertTrue(repository.branches.add(branch)) },
                    { assertTrue(repository.commits.add(cmt)) },
                    { assertTrue(repository.user.add(user)) },
                )

                assertDoesNotThrow {
                    repositoryPort.update(repository)
                }

                assertAll(
                    "Check database numbers",
                    { assertThat(projectPort.findAll()).hasSize(1) },
                    { assertThat(repositoryPort.findAll()).hasSize(1) },
                    { assertThat(commitPort.findAll()).hasSize(1) },
                    { assertThat(branchPort.findAll()).hasSize(1) },
                    { assertThat(userPort.findAll()).hasSize(1) },
                )
            }

            run {
                val cmt =
                    Commit(
                        // same commit as above, but new object
                        sha = "1234567890123456789012345678901234567890",
                        message = "test commit",
                        commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
                        repositoryId = repository.id,
                        branches = mutableSetOf(branch),
                    )
                user.addCommittedCommit(cmt)
                assertAll(
                    "Add same commit again, same hashCode",
                    { assertFalse(branch.commitShas.add(cmt.sha)) }, // sha already in
                    { assertFalse(repository.branches.add(branch)) }, // branch already in
                    { assertFalse(repository.commits.add(cmt)) }, // cmt is in based on hashCode
                    { assertFalse(repository.user.add(user)) }, // user is in based on hashCode
                )
                assertThat(repository.commits).hasSize(1)
                assertThat(repository.user).hasSize(1)

                val savedRepo =
                    assertDoesNotThrow {
                        repositoryPort.update(repository)
                    }

                assertAll(
                    "Check database numbers",
                    { assertThat(projectPort.findAll()).hasSize(1) },
                    { assertThat(repositoryPort.findAll()).hasSize(1) },
                    { assertThat(commitPort.findAll()).hasSize(1) },
                    { assertThat(branchPort.findAll()).hasSize(1) },
                    { assertThat(userPort.findAll()).hasSize(1) },
                )

                assertThat(repositoryPort.findAll().toList()[0])
                    .usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .isEqualTo(savedRepo)
            }
        }

        @Test
        fun `save empty repository, update with commit, update with same commit again but changed`() {
            val repository =
                assertDoesNotThrow {
                    repositoryPort.create(
                        Repository(
                            name = "test repository",
                            project = repositoryProject,
                        ),
                    )
                }

            assertAll(
                "Check database numbers",
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(0) },
                { assertThat(branchPort.findAll()).hasSize(0) },
                { assertThat(userPort.findAll()).hasSize(0) },
            )

            val branch =
                Branch(
                    name = "test branch 1",
                    repositoryId = repository.id,
                )
            run {
                val cmt =
                    Commit(
                        sha = "1234567890123456789012345678901234567890",
                        message = "test commit",
                        commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
                        repositoryId = repository.id,
                        branches = mutableSetOf(branch),
                    )
                val user =
                    User(
                        name = "test",
                        email = "test@example.com",
                        repository = repository,
                    )
                user.addCommittedCommit(cmt)

                assertAll(
                    "Adding new commit",
                    { assertTrue(branch.commitShas.add(cmt.sha)) },
                    { assertTrue(repository.branches.add(branch)) },
                    { assertTrue(repository.commits.add(cmt)) },
                    { assertTrue(repository.user.add(user)) },
                )

                val savedRepo =
                    assertDoesNotThrow {
                        repositoryPort.update(repository)
                    }

                assertAll(
                    "Check database numbers",
                    { assertThat(projectPort.findAll()).hasSize(1) },
                    { assertThat(repositoryPort.findAll()).hasSize(1) },
                    { assertThat(commitPort.findAll()).hasSize(1) },
                    { assertThat(branchPort.findAll()).hasSize(1) },
                    { assertThat(userPort.findAll()).hasSize(1) },
                )

                assertThat(repositoryPort.findAll().toList()[0])
                    .usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .isEqualTo(savedRepo)
            }

            run {
                val cmt =
                    Commit(
                        sha = "1234567890123456789012345678901234567890",
                        message = "msg changed", // message property changed!
                        commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
                        repositoryId = repository.id,
                        branches = mutableSetOf(branch),
                    )
                assertAll(
                    "Add same commit again, same hashCode",
                    { assertFalse(branch.commitShas.add(cmt.sha)) }, // sha already in
                    { assertFalse(repository.branches.add(branch)) }, // branch already in
                    { assertTrue(repository.commits.add(cmt)) }, // cmt is NOT in based on hashCode, changed message
                )
                assertThat(repository.commits).hasSize(2)

                val savedRepo =
                    assertDoesNotThrow {
                        repositoryPort.update(repository)
                    }

                assertAll(
                    "Check database numbers",
                    { assertThat(projectPort.findAll()).hasSize(1) },
                    { assertThat(repositoryPort.findAll()).hasSize(1) },
                    { assertThat(commitPort.findAll()).hasSize(1) },
                    { assertThat(branchPort.findAll()).hasSize(1) },
                    { assertThat(userPort.findAll()).hasSize(1) },
                )
                assertThat(repositoryPort.findAll().toList()[0])
                    .usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .isEqualTo(savedRepo)
            }
        }

        @Test
        fun `save repository with two commits, remove commit and update`() {
            val savedRepo =
                run {
                    val repository =
                        Repository(
                            name = "test repository",
                            project = repositoryProject,
                        )
                    val user =
                        User(
                            name = "test",
                            email = "test@example.com",
                            repository = repository,
                        )
                    val branch =
                        Branch(
                            name = "test branch 1",
                            repositoryId = repository.id,
                        )

                    val cmtA =
                        Commit(
                            sha = "A".repeat(40),
                            message = "test commit",
                            commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
                            repositoryId = repository.id,
                            branches = mutableSetOf(branch),
                        )
                    val cmtB =
                        Commit(
                            sha = "B".repeat(40),
                            message = "test commit",
                            commitDateTime = LocalDateTime.of(2024, 8, 13, 1, 1),
                            repositoryId = repository.id,
                            branches = mutableSetOf(branch),
                        )
                    user.addCommittedCommit(cmtA)
                    user.addCommittedCommit(cmtB)

                    branch.commitShas.add(cmtA.sha)
                    branch.commitShas.add(cmtB.sha)
                    repository.branches.add(branch)
                    repository.commits.add(cmtA)
                    repository.commits.add(cmtB)
                    repository.user.add(user)

                    assertAll(
                        "check model",
                        { assertThat(branch.commitShas).hasSize(2) },
                        { assertThat(cmtA.branches).hasSize(1) },
                        { assertThat(cmtB.branches).hasSize(1) },
                        { assertThat(repository.branches).hasSize(1) },
                        { assertThat(repository.commits).hasSize(2) },
                        { assertThat(repository.user).hasSize(1) },
                    )

                    val updatedEntity =
                        assertDoesNotThrow {
                            repositoryPort.create(repository)
                        }
                    assertAll(
                        "Check created entity",
                        { assertThat(updatedEntity.commits).hasSize(2) },
                        { assertThat(updatedEntity.branches).hasSize(1) },
                    )

                    assertAll(
                        { assertThat(projectPort.findAll()).hasSize(1) },
                        { assertThat(repositoryPort.findAll()).hasSize(1) },
                        { assertThat(commitPort.findAll()).hasSize(2) },
                        { assertThat(branchPort.findAll()).hasSize(1) },
                        { assertThat(userPort.findAll()).hasSize(1) },
                    )

                    return@run updatedEntity
                }

            val updatedRepo =
                run {
                    assertDoesNotThrow {
                        savedRepo.removeCommitBySha("A".repeat(40))
                    }
                    assertThat(savedRepo.commits).hasSize(1)
                    assertThat(savedRepo.branches).hasSize(1)
                    assertThat(savedRepo.user).hasSize(1)
                    assertThat(savedRepo.branches.toList()[0].commitShas).hasSize(1)

                    val updatedRepo =
                        assertDoesNotThrow {
                            return@assertDoesNotThrow repositoryPort.update(savedRepo)
                        }

                    assertThat(updatedRepo.commits).hasSize(1)
                    assertThat(updatedRepo.branches).hasSize(1)
                    assertThat(updatedRepo.user).hasSize(1)
                    assertThat(updatedRepo.branches.toList()[0].commitShas).hasSize(1)

                    return@run updatedRepo
                }

            assertThat(updatedRepo.commits).hasSize(1)
            assertThat(updatedRepo.branches).hasSize(1)
            assertThat(updatedRepo.user).hasSize(1)
            assertThat(commitPort.findAll()).hasSize(1)
            assertThat(userPort.findAll()).hasSize(1)
        }
    }
}
