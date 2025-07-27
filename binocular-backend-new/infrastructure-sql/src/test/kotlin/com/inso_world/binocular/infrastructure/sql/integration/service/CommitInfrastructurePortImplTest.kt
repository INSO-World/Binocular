package com.inso_world.binocular.infrastructure.sql.integration.service

import com.inso_world.binocular.infrastructure.sql.integration.service.base.BaseServiceTest
import com.inso_world.binocular.infrastructure.sql.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.sql.persistence.dao.RepositoryDao
import com.inso_world.binocular.infrastructure.sql.service.BranchInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.sql.service.CommitInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.sql.service.ProjectInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.sql.service.RepositoryInfrastructurePortImpl
import com.inso_world.binocular.infrastructure.sql.service.UserInfrastructurePortImpl
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import io.mockk.mockk
import io.mockk.verify
import jakarta.validation.ConstraintViolationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
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

internal class CommitInfrastructurePortImplTest : BaseServiceTest() {
    @Autowired
    private lateinit var branchPort: BranchInfrastructurePortImpl

    @Autowired
    private lateinit var userPort: UserInfrastructurePortImpl

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
    private lateinit var branchDomain: Branch
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
        this.branchDomain =
            Branch(
                name = "test branch",
                repositoryId = repository.id,
            )
    }

    companion object {
        @JvmStatic
        fun provideCyclicCommits(): Stream<Arguments> {
            fun user() =
                User(
                    name = "test",
                    email = "test@example.com",
                )

            fun commit1() =
                Commit(
                    sha = "1234567890123456789012345678901234567890",
                    message = "test commit",
                    commitDateTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
                    repositoryId = null,
                    parents = mutableSetOf(),
                    committer = user(),
                )

            fun commit2() =
                Commit(
                    sha = "fedcbafedcbafedcbafedcbafedcbafedcbafedc",
                    message = "yet another commit",
                    commitDateTime = LocalDateTime.of(2021, 1, 1, 0, 0, 0),
                    repositoryId = null,
                    parents = mutableSetOf(),
                    committer = user(),
                )

            fun commit3() =
                Commit(
                    sha = "0987654321098765432109876543210987654321",
                    message = "commit number three",
                    commitDateTime = LocalDateTime.of(2022, 1, 1, 0, 0, 0),
                    repositoryId = null,
                    parents = mutableSetOf(),
                    committer = user(),
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
            fun user() =
                User(
                    name = "user 1",
                    email = "user@example.com",
                )

            fun commit1_pc(): Commit {
                val cmt =
                    Commit(
                        sha = "A".repeat(40),
                        message = "test commit",
                        commitDateTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
                        repositoryId = null,
                        parents = mutableSetOf(),
                        committer = user(),
                    )
                cmt.committer?.committedCommits?.add(cmt)
                return cmt
            }

            fun commit2_pc(): Commit {
                val cmt =
                    Commit(
                        sha = "B".repeat(40),
                        message = "yet another commit",
                        commitDateTime = LocalDateTime.of(2021, 1, 1, 0, 0, 0),
                        repositoryId = null,
                        parents = mutableSetOf(),
                        committer = user(),
                    )

                cmt.committer?.committedCommits?.add(cmt)
                return cmt
            }

            fun commit3_pc(): Commit {
                val cmt =
                    Commit(
                        sha = "C".repeat(40),
                        message = "commit number three",
                        commitDateTime = LocalDateTime.of(2022, 1, 1, 0, 0, 0),
                        repositoryId = null,
                        parents = mutableSetOf(),
                        committer = user(),
                    )

                cmt.committer?.committedCommits?.add(cmt)
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
                // 4, two commits, with relationship
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

    @Nested
    inner class UpdateOperation : BaseServiceTest() {
        private lateinit var savedCommit: Commit

        @BeforeEach
        fun setup() {
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
                    repositoryId = repository.id,
                    parents = mutableSetOf(),
                    branches =
                        mutableSetOf(baseBranch),
                    committer = user,
                )
            user.committedCommits.add(baseCommit)
            baseBranch.commitShas.add(baseCommit.sha)
            repository.commits.add(baseCommit)
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
            newBranch.commitShas.add(savedCommit.sha)

            assertAll(
                "check model",
                { assertThat(savedCommit.branches).hasSize(2) },
                { assertThat(newBranch.commitShas).hasSize(1) },
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
            super.tearDown()

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
                                        name = "test repository",
                                    ),
                            ),
                        )
                    val repository = project.repo!!
                    val cmt =
                        Commit(
                            sha = "C".repeat(40),
                            message = "msg",
                            branches = mutableSetOf(branchA, branchB),
                            commitDateTime = LocalDateTime.now(),
                            repositoryId = repository.id,
                            committer = user,
                        )

                    user.addCommittedCommit(cmt)
                    repository.user.add(user)

                    branchA.commitShas.add(cmt.sha)
                    branchB.commitShas.add(cmt.sha)

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

    @Nested
    inner class SaveOperation : BaseServiceTest() {
        @Autowired
        private lateinit var ctx: MappingContext
//
//        @BeforeEach
//        fun setup() {
//            mappingScope.clear()
//        }

        @ParameterizedTest
        @MethodSource(
            "com.inso_world.binocular.infrastructure.sql.integration.service.CommitInfrastructurePortImplTest#provideCyclicCommits",
        )
        @Disabled("until something clever is implemented for cycle detection")
        fun `save multiple commits with cycle, expect ValidationException`(commitList: List<Commit>) {
            var branch = Branch(
                name = "test branch"
            )

            val repositoryDao = mockk<RepositoryDao>()

            val ex = assertThrows<DataAccessException> {
                commitList.forEach { cmt ->
                    cmt.repositoryId = repository.id
                    cmt.parents.forEach { c ->
                        c.repositoryId = repository.id

                        branch.commitShas.add(c.sha)
                        c.branches.add(branch)

                        c.committer?.committedCommits?.add(c)
                    }

                    branch.commitShas.add(cmt.sha)
                    cmt.branches.add(branch)

                    cmt.committer?.committedCommits?.add(cmt)

                    repository.commits.add(cmt)
                    branch = commitPort.create(cmt).branches.toList()[0]
                }
            }
            assertThat(ex.message).contains("Cyclic dependency detected")
            verify(exactly = 0) { repositoryDao.create(any()) }
        }

        @ParameterizedTest
        @MethodSource(
            "com.inso_world.binocular.infrastructure.sql.integration.service.CommitInfrastructurePortImplTest#provideCommitsAndLists",
        )
        fun `save multiple commits with repository, expecting in database`(commitList: List<Commit>) {
            val savedCommits =
                commitList
                    .map { cmt ->
                        cmt.branches.add(branchDomain)
                        branchDomain.commitShas.add(cmt.sha)
                        cmt.repositoryId = repository.id
                        cmt.committer?.repository = repository
                        cmt.author?.repository = repository
                        cmt.parents.forEach { c ->
                            c.repositoryId = repository.id
                            c.committer?.repository = repository
                            c.committer?.let {
                                repository.user.add(it)
                                it.addCommittedCommit(c)
                            }
                            c.author?.repository = repository
                            c.author?.let {
                                repository.user.add(it)
                                it.addAuthoredCommit(c)
                            }
                            c.branches.add(branchDomain)
                            branchDomain.commitShas.add(c.sha)
                        }
                        repository.commits.add(cmt)
                        cmt.committer?.let {
                            repository.user.add(it)
                            it.addCommittedCommit(cmt)
                        }
                        cmt.author?.let {
                            repository.user.add(it)
                            it.addAuthoredCommit(cmt)
                        }
                        repository.branches.add(branchDomain)

                        assertDoesNotThrow {
                            commitPort.create(cmt)
                        }
                    }.map {
                        commitPort.findById(it.id!!) ?: throw IllegalStateException("must find commit here")
                    }
            repository.commits = savedCommits.toMutableSet()

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
                    ).distinct()
                    .size,
            )
            run {
//        check that branch with same identity map onto same object after mapping
                val allBranches = commitPort.findAll().flatMap { it.branches }
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
                    .isEqualTo(savedCommits)
            }
            run {
                val elements = commitPort.findExistingSha(repository, commitList.map { it.sha })
                assertThat(elements)
                    .usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .ignoringFieldsMatchingRegexes(".*id")
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
                    .ignoringFieldsMatchingRegexes(".*id")
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
            "com.inso_world.binocular.infrastructure.sql.integration.service.CommitInfrastructurePortImplTest#provideCommitsAndLists",
        )
        fun `save multiple commits with repository, verify relationship to repository`(commitList: List<Commit>) {
            val savedEntities =
                commitList
                    .map { cmt ->
                        cmt.branches.add(branchDomain)
                        branchDomain.commitShas.add(cmt.sha)
                        cmt.repositoryId = repository.id
                        cmt.parents.forEach { parent ->
                            parent.repositoryId = repository.id
                            parent.branches.add(branchDomain)
                            branchDomain.commitShas.add(parent.sha)
                        }
                        repository.commits.add(cmt)
                        assertDoesNotThrow {
                            return@map commitPort.create(cmt)
                        }
                    }.map {
                        commitPort.findById(it.id!!) ?: throw IllegalStateException("must find commit here")
                    }
            repository.commits = savedEntities.toMutableSet()

            val expectedCommits =
                (savedEntities + savedEntities.flatMap { it.parents })
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
                    .isEqualTo(expectedCommits)
            }

            run {
                val elem = commitPort.findAll()
                assertThat(elem)
                    .usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .isEqualTo(expectedCommits)
            }
            // do not continue here as it fails anyway then
//            assertAll(
            run {
                assertThat(
                    repositoryPort
                        .findAll()
                        .toList()[0]
                        .commits,
                ).usingRecursiveComparison()
                    .ignoringCollectionOrder()
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
                    .isEqualTo(elements)
            }
//            )
        }

        @ParameterizedTest
        @MethodSource(
            "com.inso_world.binocular.infrastructure.sql.integration.service.CommitInfrastructurePortImplTest#provideCommitsAndLists",
        )
        fun `save multiple commits with repository, verify relationship to project`(commitList: List<Commit>) {
            val savedEntities =
                run {
                    val user =
                        User(
                            name = "test",
                            email = "test@example.com",
                            repository = repository,
                        )
                    repository.user.add(user)
                    val savedCommits =
                        commitList
                            .map { cmt ->
                                user.committedCommits.add(cmt)
                                cmt.committer = user

                                cmt.repositoryId = repository.id
                                cmt.branches.add(branchDomain)
                                branchDomain.commitShas.add(cmt.sha)
                                cmt.parents.forEach { c ->
                                    user.committedCommits.add(c)
                                    c.committer = user

                                    c.repositoryId = repository.id
                                    c.branches.add(branchDomain)
                                    branchDomain.commitShas.add(c.sha)
                                }
                                repository.commits.add(cmt)
                                assertDoesNotThrow {
                                    commitPort.create(cmt)
                                }
                            }.map {
                                commitPort.findById(it.id!!) ?: throw IllegalStateException("must find commit here")
                            }
                    repository.commits = savedCommits.toMutableSet()

                    return@run savedCommits
                }

            val expectedCommits =
                (savedEntities + savedEntities.flatMap { it.parents })
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
                    .isEqualTo(expectedCommits)
            }
            run {
                val elements = commitPort.findExistingSha(repository, expectedCommits.map { it.sha })
                assertThat(elements)
                    .usingRecursiveComparison()
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
                            repositoryId = null,
                            committer = user,
                        )
                    user.addCommittedCommit(cmt)
                    cmt.branches.add(branchDomain)
                    branchDomain.commitShas.add(cmt.sha)
                    cmt.repositoryId = repository.id

                    repository.commits.add(cmt)
                    repository.user.add(user)
                    repository.branches.add(branchDomain)

                    assertAll(
                        "check model",
                        { assertThat(cmt.branches).hasSize(1) },
                        { assertThat(cmt.committer).isNotNull() },
                        { assertThat(branchDomain.commitShas).hasSize(1) },
                        { assertThat(cmt.repositoryId).isNotNull() },
                        { assertThat(user.repository).isNotNull() },
                        { assertThat(cmt.repositoryId).isEqualTo(repository.id) },
                        { assertThat(repository.commits).hasSize(1) },
                        { assertThat(repository.user).hasSize(1) },
                        { assertThat(user.committedCommits).hasSize(1) },
                    )
                    val saved =
                        assertDoesNotThrow {
                            commitPort.create(cmt)
                        }

                    assertAll(
                        "check saved entity",
                        { assertThat(saved.branches).hasSize(1) },
                        { assertThat(saved.branches.map { it.id }).doesNotContainNull() },
                        { assertThat(saved.committer).isNotNull() },
                        { assertThat(saved.author).isNull() },
                        { assertThat(saved.repositoryId).isNotNull() },
                        { assertThat(saved.repositoryId).isEqualTo(repository.id) },
                    )

                    return@run saved
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
                .isEqualTo(savedCommit)

            assertThat(
                commitPort.findAll().toList()[0],
            ).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFieldsMatchingRegexes(".*id")
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
                .ignoringFieldsMatchingRegexes(".*id")
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
                { assertThat(commitPort.findAll().toList()[0].repositoryId).isNotNull() },
                { assertThat(commitPort.findAll().toList()[0].repositoryId).isEqualTo(project.repo?.id) },
                { assertThat(commitPort.findAll().toList()[0].repositoryId).isEqualTo(repository.id) },
            )
        }

        @Test
        fun `save 1 commit with repository, verify relationship to repository`() {
            val savedCommit =
                run {
                    val user =
                        User(
                            name = "test",
                            email = "test",
                        )
                    val cmt =
                        Commit(
                            sha = "1234567890123456789012345678901234567890",
                            message = "test commit",
                            commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
                            repositoryId = repository.id,
                            branches = mutableSetOf(branchDomain),
                            committer = user,
                        )
                    user.committedCommits.add(cmt)
                    branchDomain.commitShas.add(cmt.sha)

                    assertDoesNotThrow {
                        return@run commitPort.create(cmt)
                    }
                }

            assertAll(
                "check database numbers",
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(branchPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(1) },
                { assertThat(userPort.findAll()).hasSize(1) },
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
                    val user =
                        User(
                            name = "test",
                            email = "test@example.com",
                        )
                    val cmt =
                        Commit(
                            sha = "1234567890123456789012345678901234567890",
                            message = "test commit",
                            commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
                            repositoryId = repository.id,
                            branches = mutableSetOf(branchDomain),
                            committer = user,
                        )
                    user.committedCommits.add(cmt)
                    branchDomain.commitShas.add(cmt.sha)
                    repository.commits.add(cmt)
                    repository.user.add(user)

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
                            ?.toList()[0]
                    assertThat(elem)
                        .usingRecursiveComparison()
                        .ignoringCollectionOrder()
                        .isEqualTo(savedCommit)
                },
            )
        }
    }

    @Nested
    inner class Validation : BaseServiceTest() {
        @Test
        fun `save 1 commit without branch, expect Constraint`() {
            assertThrows<ConstraintViolationException> {
                commitPort.create(
                    Commit(
                        sha = "B".repeat(40),
                    ),
                )
            }
        }
    }
}
