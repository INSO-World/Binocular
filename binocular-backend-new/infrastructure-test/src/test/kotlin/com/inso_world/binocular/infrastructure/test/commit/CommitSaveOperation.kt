package com.inso_world.binocular.infrastructure.test.commit

import com.inso_world.binocular.core.integration.base.InfrastructureDataSetup
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import com.inso_world.binocular.model.vcs.ReferenceCategory
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import java.time.LocalDateTime
import java.util.stream.Stream

internal class CommitSaveOperation : BaseInfrastructureSpringTest() {
    @Autowired
    private lateinit var userPort: UserInfrastructurePort

    @Autowired
    private lateinit var branchPort: BranchInfrastructurePort

    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePort

    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    private lateinit var commitPort: CommitInfrastructurePort

    companion object {
        @JvmStatic
        fun provideCyclicCommits(): Stream<Arguments> {
            val repository = run {
                val project = Project(name = "proj-valid")
                Repository(localPath = "repo-valid", project = project)
            }

            fun developer() =
                Developer(
                    name = "test",
                    email = "test@example.com",
                    repository = repository
                )

            fun commit1() =
                Commit(
                    sha = "1234567890123456789012345678901234567890",
                    message = "test commit",
                    authorSignature = Signature(developer = developer(), timestamp = LocalDateTime.of(2020, 1, 1, 0, 0, 0)),
                    repository = repository,
                )

            fun commit2() =
                Commit(
                    sha = "fedcbafedcbafedcbafedcbafedcbafedcbafedc",
                    message = "yet another commit",
                    authorSignature = Signature(developer = developer(), timestamp = LocalDateTime.of(2021, 1, 1, 0, 0, 0)),
                    repository = repository,
                )

            fun commit3() =
                Commit(
                    sha = "0987654321098765432109876543210987654321",
                    message = "commit number three",
                    authorSignature = Signature(developer = developer(), timestamp = LocalDateTime.of(2022, 1, 1, 0, 0, 0)),
                    repository = repository,
                )

            return Stream.of(
                // 1, one commit, self referencing
                Arguments.of(
                    run {
                        val c1 = commit1()
                        c1.parents.add(c1)
                        listOf(
                            c1
                        )
                    }
                ),
//                2
                Arguments.of(
                    run {
                        val c1 = commit1()
                        val c2 = commit2()
                        c1.parents.add(c2)
                        c2.parents.add(c2)

                        listOf(c1)
                    }
                ),
//                3
                Arguments.of(
                    run {
                        val c1 = commit1()
                        val c2 = commit2()

                        c1.parents.add(c2)
                        c2.parents.add(c1)

                        listOf(c1)
                    }
                ),
//                4
                Arguments.of(
                    run {
                        val c1 = commit1()
                        val c2 = commit2()
                        val c3 = commit3()

                        c1.parents.add(c2)
                        c2.parents.add(c3)
                        c2.parents.add(c1)

                        listOf(c1)
                    }
                ),
//                5
                Arguments.of(
                    run {
                        val c1 = commit1()
                        val c2 = commit2()
                        val c3 = commit3()

                        c1.parents.add(c2)
                        c2.parents.add(c2)

                        listOf(c1, c3)
                    }
                ),
//                6, same as 5 but reversed order
                Arguments.of(
                    run {
                        val c1 = commit1()
                        val c2 = commit2()
                        val c3 = commit3()

                        c1.parents.add(c2)
                        c2.parents.add(c2)

                        listOf(c3, c1)
                    }
                ),
//                7, just save middle commit c2
                Arguments.of(
                    run {
                        val c1 = commit1()
                        val c2 = commit2()
                        val c3 = commit3()

                        c1.parents.add(c2)
                        c2.parents.add(c3)
                        c3.parents.add(c1)

                        listOf(c2)
                    }
                ),
//                8, just save first commit c1
                Arguments.of(
                    run {
                        val c1 = commit1()
                        val c2 = commit2()
                        val c3 = commit3()

                        c1.parents.add(c2)
                        c2.parents.add(c3)
                        c3.parents.add(c1)

                        listOf(c1)
                    }
                ),
//                9, just save last commit c1
                Arguments.of(
                    run {
                        val c1 = commit1()
                        val c2 = commit2()
                        val c3 = commit3()

                        c1.parents.add(c2)
                        c2.parents.add(c3)
                        c3.parents.add(c1)

                        listOf(c3)
                    }
                ),
            )
        }

        @JvmStatic
        fun provideCommitsAndLists(): Stream<Arguments> {
            val repository = run {
                val project = Project(name = "proj-valid")
                Repository(localPath = "repo-valid", project = project)
            }

            fun developer() =
                Developer(
                    name = "user 1",
                    email = "user@example.com",
                    repository = repository
                )

            fun commit1_pc(): Commit {
                val cmt =
                    Commit(
                        sha = "1".repeat(40),
                        message = "test commit",
                        authorSignature = Signature(developer = developer(), timestamp = LocalDateTime.of(2020, 1, 1, 0, 0, 0)),
                        repository = repository,
                    )
                return cmt
            }

            fun commit2_pc(): Commit {
                val cmt =
                    Commit(
                        sha = "2".repeat(40),
                        message = "yet another commit",
                        authorSignature = Signature(developer = developer(), timestamp = LocalDateTime.of(2021, 1, 1, 0, 0, 0)),
                        repository = repository,
                    )
                return cmt
            }

            fun commit3_pc(): Commit {
                val cmt =
                    Commit(
                        sha = "3".repeat(40),
                        message = "commit number three",
                        authorSignature = Signature(developer = developer(), timestamp = LocalDateTime.of(2022, 1, 1, 0, 0, 0)),
                        repository = repository,
                    )
                return cmt
            }

            return Stream.of(
//                1
                Arguments.of(
                    listOf(
                        commit1_pc(),
                    ),
                ),
//                2
                Arguments.of(
                    run {
                        val c1 = commit1_pc()
                        val c2 = commit2_pc()
                        listOf(
                            c1, c2
                        )
                    }
                ),
//                3
                Arguments.of(
                    run {
                        val c1 = commit1_pc()
                        val c2 = commit2_pc()
                        val c3 = commit3_pc()

                        listOf(
                            c1, c2, c3
                        )
                    }
                ),
                // 4, two commits, with relationship c1->c2
                Arguments.of(
                    run {
                        val c1 = commit1_pc()
                        val c2 = commit2_pc()

                        c1.parents.add(c2)

                        listOf(
//                            intentionally missing c2 here
                            c1
                        )
                    }
                ),
                // 4.2, two commits, with relationship c1<-c2
                Arguments.of(
                    run {
                        val c1 = commit1_pc()
                        val c2 = commit2_pc()

                        c1.children.add(c2)

                        listOf(
//                            intentionally missing c2 here
                            c1
                        )
                    }
                ),
                // 5, second commit without extra
                Arguments.of(
                    run {
                        val c1 = commit1_pc()
                        val c2 = commit2_pc()
                        val c3 = commit3_pc()

                        c1.parents.add(c2)

                        listOf(
//                            intentionally missing c2 here
                            c1, c3
                        )
                    }
                ),
                // 6, two commits, with relationship, with extra
                Arguments.of(
                    run {
                        val c1 = commit1_pc()
                        val c2 = commit2_pc()
                        c1.parents.add(c2)

                        listOf(
                            c1, c2
                        )
                    },
                ),
//                7, octopus merge
                Arguments.of(
                    run {
                        val c1 = commit1_pc()
                        c1.parents.add(commit2_pc())
                        c1.parents.add(commit3_pc())

                        listOf(c1)
                    }
                ),
//                8, octopus merge
                Arguments.of(
                    run {
                        val c1 = commit1_pc()
                        val c2 = commit2_pc()
                        val c3 = commit3_pc()

                        c1.parents.add(c3)
                        c1.parents.add(c2)

                        c3.parents.add(c2)
                        listOf(
                            c1, c2, c3
                        )
                    }
                ),
//                9, octopus merge
                Arguments.of(
                    run {
                        val c1 = commit1_pc()
                        val c2 = commit2_pc()
                        val c3 = commit3_pc()

                        c1.parents.add(c2)
                        c1.parents.add(c3)

//                        vice versa to 7
                        c2.parents.add(c3)

                        listOf(
                            c1, c2, c3
                        )
                    }
                ),
            )
        }
    }

    private var repository =
        Repository(
            localPath = "test repository",
            project = Project(name = "proj-valid")
        )

    private lateinit var branchDomain: Branch
    private lateinit var project: Project

    @BeforeEach
    fun setup() {
        infrastructureDataSetup.teardown()
        this.project =
            projectPort.create(
                Project(
                    name = "test project",
                ),
            )
        this.repository = this.project.repo ?: throw IllegalStateException("test repository can not be null")
        val developer = Developer(name = "a", email = "a@example.com", repository = this.repository)
        this.branchDomain =
            Branch(
                name = "test branch",
                fullName = "refs/heads/test branch",
                category = ReferenceCategory.LOCAL_BRANCH,
                repository = this.repository,
                head = Commit(
                    sha = "a".repeat(40),
                    message = "message",
                    authorSignature = Signature(developer = developer, timestamp = LocalDateTime.now()),
                    repository = repository,
                )
            )
    }

    @ParameterizedTest
    @MethodSource(
        "com.inso_world.binocular.infrastructure.test.commit.CommitSaveOperation#provideCyclicCommits",
    )
    @Disabled("until something clever is implemented for cycle detection")
    fun `save multiple commits with cycle, expect ValidationException`(commitList: List<Commit>) {
        var branch =  Branch(
            name = "test branch",
            fullName = "refs/heads/test branch",
            category = ReferenceCategory.LOCAL_BRANCH,
            repository = this.repository,
            head = commitList.first()
        )

        val repositoryDao = mockk<RepositoryInfrastructurePort>()

        val ex = assertThrows<DataAccessException> {
            commitList.forEach { cmt ->
                repository.commits.add(cmt)
            }
        }
        assertThat(ex.message).contains("Cyclic dependency detected")
        verify(exactly = 0) { repositoryDao.create(any()) }
    }

    @ParameterizedTest
    @MethodSource(
        "com.inso_world.binocular.infrastructure.test.commit.CommitSaveOperation#provideCommitsAndLists",
    )
    fun `save multiple commits with repository, expecting in database`(commitList: List<Commit>) {
        repository.commits.addAll(commitList)

        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(1) },
            { assertThat(repositoryPort.findAll()).hasSize(1) },
            { assertThat(branchPort.findAll()).hasSize(1) },
            { assertThat(userPort.findAll()).hasSize(1) },
        )
        assertThat(commitPort.findAll()).hasSize(
            commitList
                .map { it.sha }
                .union(
                    commitList.flatMap { it.parents.map { parent -> parent.sha } },
                ).union(
                    commitList.flatMap { it.children.map { parent -> parent.sha } },
                ).distinct()
                .size,
        )
        run {
//        check that branch with same identity map onto same object after mapping
            val allBranches = repositoryPort.findAll().flatMap { it.branches }
            if (allBranches.isNotEmpty()) {
                val first = allBranches.first()
                assertAll(
                    allBranches.map { branch ->
                        { assertSame(first, branch, "Branch is not the same instance as the first") }
                    },
                )
            }
        }
        run {
            val elements = commitPort.findExistingSha(repository, commitList.map { it.sha })
            assertThat(elements)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFieldsMatchingRegexes(".*id", ".*_*", ".*logger")  // This ignores only fields starting with _
                .isEqualTo(commitList)
        }
        run {
            val elements = commitPort.findExistingSha(repository, commitList.map { it.sha })
            assertThat(elements)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFieldsMatchingRegexes(".*id", ".*_*", ".*logger")
                .ignoringFields(
                    "users", // deprecated field
                ).isEqualTo(
                    project.repo
                        ?.commits,
                )
        }
        run {
            val elements = commitPort.findExistingSha(repository, commitList.map { it.sha })
            assertThat(elements)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(".*id", ".*_*", ".*logger")
                .ignoringFields(
                    "users", // deprecated field
                ).ignoringCollectionOrder()
                .isEqualTo(
                    repository
                        .commits,
                )
        }
    }

    @ParameterizedTest
    @MethodSource(
        "com.inso_world.binocular.infrastructure.test.commit.CommitSaveOperation#provideCommitsAndLists",
    )
    fun `save multiple commits with repository, verify relationship to repository`(commitList: List<Commit>) {
        val savedEntities = commitList
        repository.commits.clear()
        repository.commits.addAll(savedEntities)

        val expectedCommits =
            (savedEntities + savedEntities.flatMap { it.parents } + savedEntities.flatMap { it.children })
                .distinctBy { it.sha }

        assertAll(
            "Check database numbers",
            { assertThat(repositoryPort.findAll()).hasSize(1) },
            { assertThat(commitPort.findAll()).hasSize(expectedCommits.size) },
            { assertThat(userPort.findAll()).hasSize(1) },
        )
        run {
            val elem = repositoryPort.findAll().toList()[0]
            assertThat(elem.commits).hasSize(expectedCommits.size)
        }
        run {
            val elem = repositoryPort.findAll().toList()[0]
            assertThat(elem.commits)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFieldsMatchingRegexes(".*id", ".*_*", ".*logger")
                .isEqualTo(expectedCommits)
        }

        run {
            val elem = commitPort.findAll()
            assertThat(elem)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFieldsMatchingRegexes(".*id", ".*_*", ".*logger")
                .isEqualTo(expectedCommits)
        }
        // do not continue here as it fails anyway then
        run {
            assertThat(
                repositoryPort
                    .findAll()
                    .toList()[0]
                    .commits,
            ).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFieldsMatchingRegexes(".*id", ".*_*", ".*logger")
                .isEqualTo(expectedCommits)
        }
        run {
            val elements = commitPort.findExistingSha(repository, expectedCommits.map { it.sha })
            assertThat(
                repositoryPort
                    .findAll()
                    .toList()[0]
                    .commits,
            ).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFieldsMatchingRegexes(".*id", ".*_*", ".*logger")
                .isEqualTo(elements)
        }
    }

    @ParameterizedTest
    @MethodSource(
        "com.inso_world.binocular.infrastructure.test.commit.CommitSaveOperation#provideCommitsAndLists",
    )
    fun `save multiple commits with repository, verify relationship to project`(commitList: List<Commit>) {
        val savedEntities =
            run {
                val developer = Developer(name = "test", email = "test@example.com", repository = repository)
                repository.developers.add(developer)
                val savedCommits = commitList
                repository.commits.clear()
                repository.commits.addAll(savedCommits)

                return@run savedCommits
            }

        val expectedCommits =
            (savedEntities + savedEntities.flatMap { it.parents } + savedEntities.flatMap { it.children })
                .distinctBy { it.sha }
        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(1) },
            { assertThat(repositoryPort.findAll()).hasSize(1) },
            { assertThat(commitPort.findAll()).hasSize(expectedCommits.size) },
            { assertThat(userPort.findAll()).hasSize(1) },
        )
        // do not continue here as it fails anyway then
        run {
            val elem = projectPort.findAll().toList()[0]
            assertThat(elem.repo?.commits).hasSize(expectedCommits.size)
        }
        run {
            assertThat(
                projectPort
                    .findAll()
                    .toList()[0]
                    .repo
                    ?.commits,
            ).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFieldsMatchingRegexes(".*id", ".*_*", ".*logger")
                .isEqualTo(expectedCommits)
        }
        run {
            val elements = commitPort.findExistingSha(repository, expectedCommits.map { it.sha })
            assertThat(elements)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(".*id", ".*_*", ".*logger")
                .ignoringCollectionOrder()
                .isEqualTo(
                    projectPort
                        .findAll()
                        .toList()[0]
                        .repo
                        ?.commits,
                )
        }
    }

    @Test
    fun `save 1 commit with repository, expecting in database`() {
        val savedCommit =
            run {
                val developer = Developer(name = "test", email = "test@example.com", repository = repository)
                val cmt =
                    Commit(
                        sha = "1234567890123456789012345678901234567890",
                        message = "test commit",
                        authorSignature = Signature(developer = developer, timestamp = LocalDateTime.of(2025, 7, 13, 1, 1)),
                        repository = repository,
                    )

                repository.commits.add(cmt)
                repository.developers.add(developer)
                repository.branches.add(branchDomain)

                assertAll(
                    "check model",
                    { assertThat(cmt.committer).isNotNull() },
                    { assertThat(branchDomain.commits).hasSize(1) },
                    { assertThat(cmt.repository).isNotNull() },
                    { assertThat(cmt.repository.id).isNotNull() },
                    { assertThat(developer.repository).isNotNull() },
                    { assertThat(cmt.repository.id).isEqualTo(repository.id) },
                    { assertThat(repository.commits).hasSize(1) },
                    { assertThat(repository.developers).hasSize(2) }, // includes developer from setup
                    { assertThat(developer.committedCommits).hasSize(1) },
                )
                val saved =
                    assertDoesNotThrow {
                        commitPort.create(cmt)
                    }

                assertAll(
                    "check saved entity",
                    { assertThat(saved.committer).isNotNull() },
                    { assertThat(saved.author).isNotNull() },
                    { assertThat(saved.repository).isNotNull() },
                    { assertThat(saved.repository.id).isNotNull() },
                    { assertThat(saved.repository.id).isEqualTo(repository.id) },
                )

                return@run saved
            }

        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(1) },
            { assertThat(repositoryPort.findAll()).hasSize(1) },
            { assertThat(commitPort.findAll()).hasSize(1) },
            { assertThat(branchPort.findAll()).hasSize(1) },
            { assertThat(userPort.findAll()).hasSize(2) }, // includes developer from setup
        )

        assertThat(commitPort.findAll().toList()[0])
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .ignoringFieldsMatchingRegexes(".*id", ".*_*", ".*logger")
            .isEqualTo(savedCommit)

        assertThat(
            commitPort.findAll().toList()[0],
        ).usingRecursiveComparison()
            .ignoringCollectionOrder()
            .ignoringFieldsMatchingRegexes(".*id", ".*_*", ".*logger")
            .ignoringFields(
                "users", // deprecated field
            ).isEqualTo(
                project.repo
                    ?.commits
                    ?.toList()
                    ?.get(0),
            )
        assertThat(
            commitPort.findAll().toList()[0],
        ).usingRecursiveComparison()
            .ignoringCollectionOrder()
            .ignoringFieldsMatchingRegexes(".*id", ".*_*", ".*logger")
            .ignoringFields(
                "users", // deprecated field
            ).isEqualTo(repository.commits.toList()[0])
        assertThat(
            commitPort.findAll().toList()[0],
        ).usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(savedCommit)

        assertAll(
            "check ids",
            { assertThat(commitPort.findAll().toList()[0].id).isNotNull() },
            { assertThat(commitPort.findAll().toList()[0].repository).isNotNull() },
            { assertThat(commitPort.findAll().toList()[0].repository.id).isNotNull() },
            { assertThat(commitPort.findAll().toList()[0].repository.id).isEqualTo(project.repo?.id) },
            { assertThat(commitPort.findAll().toList()[0].repository.id).isEqualTo(repository.id) },
        )
    }

    @Test
    fun `save 1 commit with repository, verify relationship to repository`() {
        val savedCommit =
            run {
                val developer = Developer(name = "test", email = "test@example.com", repository = repository)
                val cmt =
                    Commit(
                        sha = "1234567890123456789012345678901234567890",
                        message = "test commit",
                        authorSignature = Signature(developer = developer, timestamp = LocalDateTime.of(2025, 7, 13, 1, 1)),
                        repository = repository,
                    )

                assertDoesNotThrow {
                    return@run commitPort.create(cmt)
                }
            }

        assertAll(
            "check database numbers",
            { assertThat(repositoryPort.findAll()).hasSize(1) },
            { assertThat(branchPort.findAll()).hasSize(1) },
            { assertThat(commitPort.findAll()).hasSize(1) },
            { assertThat(userPort.findAll()).hasSize(2) }, // includes developer from setup
        )
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
                assertThat(elem)
                    .usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .isEqualTo(savedCommit)
            },
        )
    }

    @Test
    fun `save 1 commit with repository, verify relationship to project`() {
        val savedCommit =
            run {
                val developer = Developer(name = "test", email = "test@example.com", repository = repository)
                val cmt =
                    Commit(
                        sha = "1234567890123456789012345678901234567890",
                        message = "test commit",
                        authorSignature = Signature(developer = developer, timestamp = LocalDateTime.of(2025, 7, 13, 1, 1)),
                        repository = repository,
                    )

                assertDoesNotThrow {
                    return@run commitPort.create(cmt)
                }
            }

        assertAll(
            "projectport",
            { assertThat(projectPort.findAll()).hasSize(1) },
            { assertThat(projectPort.findAll().toList()[0]).isNotNull() },
            { assertThat(projectPort.findAll().toList()[0].repo).isNotNull() },
        )
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
                        ?.toList()?.get(0)
                assertThat(elem)
                    .usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .isEqualTo(savedCommit)
            },
        )
    }
}