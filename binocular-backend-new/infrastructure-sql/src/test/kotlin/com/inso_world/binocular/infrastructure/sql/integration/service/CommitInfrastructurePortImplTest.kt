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
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.stream.Stream

internal class CommitInfrastructurePortImplTest : BaseServiceTest() {
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
    private lateinit var project: Project

    @BeforeEach
    fun setup() {
        this.project =
            projectPort.create(
                Project(
                    name = "test project",
                    repo = repository,
                ),
            )
        this.repository = this.project.repo ?: throw IllegalStateException("test repository can not be null")
    }

    companion object {
        @JvmStatic
        fun provideCyclicCommits(): Stream<Arguments> {
            val commit1 =
                Commit(
                    sha = "1234567890123456789012345678901234567890",
                    message = "test commit",
                    commitDateTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
                    repositoryId = null,
                    parents = emptyList(),
                )
            val commit2 =
                Commit(
                    sha = "fedcbafedcbafedcbafedcbafedcbafedcbafedc",
                    message = "yet another commit",
                    commitDateTime = LocalDateTime.of(2021, 1, 1, 0, 0, 0),
                    repositoryId = null,
                    parents = emptyList(),
                )
            val commit3 =
                Commit(
                    sha = "0987654321098765432109876543210987654321",
                    message = "commit number three",
                    commitDateTime = LocalDateTime.of(2022, 1, 1, 0, 0, 0),
                    repositoryId = null,
                    parents = emptyList(),
                )

            return Stream.of(
                // one commit, self referencing
                Arguments.of(
                    listOf(
                        commit1.copy().apply { parents = listOf(commit1) },
                    ),
                ),
                Arguments.of(
                    listOf(
                        commit1.copy().apply {
                            val prnt = commit2.copy().apply { parents = listOf(commit2) }
                            parents = listOf(prnt)
                        },
                    ),
                ),
                Arguments.of(
                    listOf(
                        commit1.copy().apply {
                            val prnt = commit2.copy().apply { parents = listOf(commit1) }
                            parents = listOf(prnt)
                        },
                    ),
                ),
                Arguments.of(
                    listOf(
                        commit1.copy().apply {
                            val prnt = commit2.copy().apply { parents = listOf(commit1, commit3) }
                            parents = listOf(prnt)
                        },
                    ),
                ),
                Arguments.of(
                    listOf(
                        commit1.copy().apply {
                            val prnt = commit2.apply { parents = listOf(commit2) }
                            parents = listOf(prnt)
                        },
                        commit3,
                    ),
                ),
                Arguments.of(
                    listOf(
                        commit3,
                        commit1.copy().apply {
                            val prnt = commit2.apply { parents = listOf(commit2) }
                            parents = listOf(prnt)
                        },
                    ),
                ),
            )
        }

        @JvmStatic
        fun provideCommitsAndLists(): Stream<Arguments> {
            val commit1 =
                Commit(
                    sha = "1234567890123456789012345678901234567890",
                    message = "test commit",
                    commitDateTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
                    repositoryId = null,
                    parents = emptyList(),
                )
            val commit2 =
                Commit(
                    sha = "fedcbafedcbafedcbafedcbafedcbafedcbafedc",
                    message = "yet another commit",
                    commitDateTime = LocalDateTime.of(2021, 1, 1, 0, 0, 0),
                    repositoryId = null,
                    parents = emptyList(),
                )
            val commit3 =
                Commit(
                    sha = "0987654321098765432109876543210987654321",
                    message = "commit number three",
                    commitDateTime = LocalDateTime.of(2022, 1, 1, 0, 0, 0),
                    repositoryId = null,
                    parents = emptyList(),
                )

            return Stream.of(
                // Single commit
                Arguments.of(
                    listOf(commit1),
                    // two commits, no relationship
                ),
                Arguments.of(
                    listOf(
                        commit1,
                        commit2,
                    ),
                ),
                Arguments.of(
                    listOf(
                        commit1,
                        commit2,
                        commit3,
                    ),
                ),
                // two commits, with relationship
                Arguments.of(
                    listOf(
                        commit1.copy().apply { parents = listOf(commit2) },
                    ),
                ),
                // second commit without extra
                Arguments.of(
                    listOf(
                        commit1.copy().apply { parents = listOf(commit2) },
                    ),
                ),
                // two commits, with relationship, with extra
                Arguments.of(
                    listOf(
                        commit1.copy().apply { parents = listOf(commit2) },
                        commit2,
                    ),
                ),
            )
        }
    }

    @ParameterizedTest
    @MethodSource("provideCyclicCommits")
    fun `save multiple commits with cycle, expect ValidationException`(commitList: List<Commit>) {
        assertThrows<jakarta.validation.ConstraintViolationException> {
            commitList.forEach { cmt ->
                cmt.repositoryId = repository.id
                cmt.parents.forEach { c -> c.repositoryId = repository.id }
                repository.commits.add(cmt)
                commitPort.create(cmt)
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideCommitsAndLists")
    fun `save multiple commits with repository, expecting in database`(commitList: List<Commit>) {
        commitList.forEach { cmt ->
            cmt.repositoryId = repository.id
            cmt.parents.forEach { c -> c.repositoryId = repository.id }
            repository.commits.add(cmt)
            assertDoesNotThrow {
                commitPort.create(cmt)
            }
        }

        transactionTemplate.execute {
            assertThat(projectPort.findAll()).hasSize(1)
            assertThat(repositoryPort.findAll()).hasSize(1)
            assertThat(commitPort.findAll()).hasSize(
                commitList
                    .map { it.sha }
                    .union(
                        commitList.flatMap { it.parents.map { parent -> parent.sha } },
                    ).distinct()
                    .size,
            )
            // do not continue here as it fails anyway then
            assertAll(
                {
                    val elements = commitPort.findExistingSha(repository, commitList.map { it.sha })
                    assertThat(elements)
                        .usingRecursiveComparison()
                        .ignoringFields("id", "parents.repositoryId", "parents.id")
                        .ignoringCollectionOrder()
                        .isEqualTo(commitList)
                },
                {
                    val elements = commitPort.findExistingSha(repository, commitList.map { it.sha })
                    assertThat(elements)
                        .usingRecursiveComparison()
                        .ignoringFields("id", "parents.id")
                        .ignoringCollectionOrder()
                        .isEqualTo(
                            project.repo
                                ?.commits,
                        )
                },
                {
                    val elements = commitPort.findExistingSha(repository, commitList.map { it.sha })
                    assertThat(elements)
                        .usingRecursiveComparison()
                        .ignoringFields("id", "parents.id")
                        .ignoringCollectionOrder()
                        .isEqualTo(
                            repository
                                .commits,
//                                .toList(),
                        )
                },
            )
        }
    }

    @ParameterizedTest
    @MethodSource("provideCommitsAndLists")
    fun `save multiple commits with repository, verify relationship to repository`(commitList: List<Commit>) {
        commitList.forEach { cmt ->
            cmt.repositoryId = repository.id
            cmt.parents.forEach { parent -> parent.repositoryId = repository.id }
            repository.commits.add(cmt)
            assertDoesNotThrow {
                val saved = commitPort.create(cmt)
            }
        }

        val expectedCommits =
            (commitList + commitList.flatMap { it.parents })
                .distinctBy { it.sha }

        transactionTemplate.execute {
            assertThat(repositoryPort.findAll()).hasSize(1)
            assertThat(commitPort.findAll()).hasSize(expectedCommits.size)
            // do not continue here as it fails anyway then
            assertAll(
                {
                    val elem = repositoryPort.findAll().toList()[0]
                    assertThat(elem.commits).hasSize(expectedCommits.size)
                },
                {
                    assertThat(expectedCommits)
                        .usingRecursiveComparison()
                        .ignoringFields("id", "parents.id")
                        .ignoringCollectionOrder()
                        .isEqualTo(
                            repositoryPort
                                .findAll()
                                .toList()[0]
                                .commits,
                        )
                },
                {
                    val elements = commitPort.findExistingSha(repository, expectedCommits.map { it.sha })
                    assertThat(elements)
                        .usingRecursiveComparison()
                        .ignoringFields("id", "parents.id")
                        .ignoringCollectionOrder()
                        .isEqualTo(
                            repositoryPort
                                .findAll()
                                .toList()[0]
                                .commits,
                        )
                },
            )
        }
    }

    @ParameterizedTest
    @MethodSource("provideCommitsAndLists")
    fun `save multiple commits with repository, verify relationship to project`(commitList: List<Commit>) {
        commitList.forEach { cmt ->
            cmt.repositoryId = repository.id
            cmt.parents.forEach { c -> c.repositoryId = repository.id }
            repository.commits.add(cmt)
            assertDoesNotThrow {
                commitPort.create(cmt)
            }
        }

        val expectedCommits =
            (commitList + commitList.flatMap { it.parents })
                .distinctBy { it.sha }
        transactionTemplate.execute {
            assertThat(projectPort.findAll()).hasSize(1)
            assertThat(repositoryPort.findAll()).hasSize(1)
            assertThat(commitPort.findAll()).hasSize(expectedCommits.size)
            // do not continue here as it fails anyway then
            assertAll(
                {
                    val elem = projectPort.findAll().toList()[0]
                    assertThat(elem.repo?.commits).hasSize(expectedCommits.size)
                },
                {
                    assertThat(expectedCommits)
                        .usingRecursiveComparison()
                        .ignoringFields("id", "parents.id")
                        .ignoringCollectionOrder()
                        .isEqualTo(
                            projectPort
                                .findAll()
                                .toList()[0]
                                .repo
                                ?.commits,
                        )
                },
                {
                    val elements = commitPort.findExistingSha(repository, expectedCommits.map { it.sha })
                    assertThat(elements)
                        .usingRecursiveComparison()
                        .ignoringFields("id", "parents.id")
                        .ignoringCollectionOrder()
                        .isEqualTo(
                            projectPort
                                .findAll()
                                .toList()[0]
                                .repo
                                ?.commits,
                        )
                },
            )
        }
    }

    @Test
    fun `save 1 commit with repository, expecting in database`() {
        val cmt =
            Commit(
                sha = "1234567890123456789012345678901234567890",
                message = "test commit",
                commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
                repositoryId = null,
            )
        cmt.repositoryId = repository.id
        repository.commits.add(cmt)

        val savedCommit = commitPort.create(cmt)

        transactionTemplate.execute {
            assertThat(projectPort.findAll()).hasSize(1)
            assertThat(repositoryPort.findAll()).hasSize(1)
            assertThat(commitPort.findAll()).hasSize(1)
            assertAll(
                {
                    val elem = commitPort.findAll().toList()[0]
                    assertThat(elem)
                        .usingRecursiveComparison()
                        .ignoringFields("id")
                        .isEqualTo(cmt)
                },
                {
                    val elem = commitPort.findAll().toList()[0]
                    assertThat(elem).isEqualTo(savedCommit)
                },
                {
                    assertThat(
                        commitPort.findAll().toList()[0],
                    ).usingRecursiveComparison().ignoringFields("id").isEqualTo(
                        project.repo
                            ?.commits
                            ?.toList()
                            ?.get(0),
                    )
                },
                {
                    assertThat(
                        commitPort.findAll().toList()[0],
                    ).usingRecursiveComparison().ignoringFields("id").isEqualTo(repository.commits.toList()[0])
                },
                {
                    assertThat(
                        commitPort.findAll().toList()[0],
                    ).usingRecursiveComparison().ignoringFields("id").isEqualTo(cmt)
                },
                { assertThat(commitPort.findAll().toList()[0].id).isNotNull() },
                { assertThat(commitPort.findAll().toList()[0].repositoryId).isNotNull() },
                { assertThat(commitPort.findAll().toList()[0].repositoryId).isEqualTo(project.repo?.id) },
                { assertThat(commitPort.findAll().toList()[0].repositoryId).isEqualTo(repository.id) },
            )
        }
    }

    @Test
    fun `save 1 commit with repository, verify relationship to repository`() {
        val cmt =
            Commit(
                sha = "1234567890123456789012345678901234567890",
                message = "test commit",
                commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
                repositoryId = repository.id,
            )
        repository.commits.add(cmt)

        assertDoesNotThrow {
            commitPort.create(cmt)
        }

        transactionTemplate.execute {
            assertThat(repositoryPort.findAll()).hasSize(1)
            // do not continue with assertAll as list access will be wrong
            assertAll(
                {
                    val elem = repositoryPort.findAll().toList()[0]
                    assertThat(elem.commits).hasSize(1)
                },
                {
                    val elem =
                        repositoryPort
                            .findAll()
                            .toList()[0]
                            .commits
                            .toList()[0]
                    assertThat(elem).usingRecursiveComparison().ignoringFields("id").isEqualTo(cmt)
                },
            )
        }
    }

    @Test
    fun `save 1 commit with repository, verify relationship to project`() {
        val cmt =
            Commit(
                sha = "1234567890123456789012345678901234567890",
                message = "test commit",
                commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
                repositoryId = repository.id,
            )
        repository.commits.add(cmt)

        assertDoesNotThrow {
            commitPort.create(cmt)
        }

        transactionTemplate.execute {
            assertThat(projectPort.findAll()).hasSize(1)
            assertThat(projectPort.findAll().toList()[0]).isNotNull()
            assertThat(projectPort.findAll().toList()[0].repo).isNotNull()
            // do not continue with assertAll as list access will be wrong
            assertAll(
                {
                    val elem = projectPort.findAll().toList()[0]
                    assertThat(elem.repo?.commits).hasSize(1)
                },
                {
                    val elem =
                        projectPort
                            .findAll()
                            .toList()[0]
                            .repo
                            ?.commits
                            ?.toList()[0]
                    assertThat(elem).usingRecursiveComparison().ignoringFields("id").isEqualTo(cmt)
                },
            )
        }
    }
}
