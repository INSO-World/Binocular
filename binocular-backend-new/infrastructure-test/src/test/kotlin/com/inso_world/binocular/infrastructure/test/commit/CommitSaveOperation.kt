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
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import java.time.LocalDateTime

internal class CommitSaveOperation : BaseInfrastructureSpringTest() {

    @Autowired
    private lateinit var infrastructureDataSetup: InfrastructureDataSetup

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

    private var repository =
        Repository(
            localPath = "test repository",
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
                    repo = repository,
                ),
            )
        this.repository = this.project.repo ?: throw IllegalStateException("test repository can not be null")
        this.branchDomain =
            Branch(
                name = "test branch",
                repository = repository,
            )
    }

    @ParameterizedTest
    @MethodSource(
        "com.inso_world.binocular.infrastructure.sql.integration.service.CommitInfrastructurePortImplTest#provideCyclicCommits",
    )
    @Disabled("until something clever is implemented for cycle detection")
    fun `save multiple commits with cycle, expect ValidationException`(commitList: List<Commit>) {
        var branch = Branch(
            name = "test branch"
        )

        val repositoryDao = mockk<RepositoryInfrastructurePort>()

        val ex = assertThrows<DataAccessException> {
            commitList.forEach { cmt ->
                cmt.repository = repository
                cmt.parents.forEach { c ->
                    c.repository = repository

                    branch.commits.add(c)
                    c.branches.add(branch)

                    c.committer?.committedCommits?.add(c)
                }

                branch.commits.add(cmt)
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
                    branchDomain.commits.add(cmt)
                    cmt.repository = repository
                    cmt.committer?.repository = repository
                    cmt.author?.repository = repository
                    (cmt.parents + cmt.children).toSet().forEach { c ->
                        c.repository = repository
                        c.committer?.repository = repository
                        c.committer?.let {
                            repository.user.add(it)
                            it.committedCommits.add(c)
                        }
                        c.author?.repository = repository
                        c.author?.let {
                            repository.user.add(it)
                            it.authoredCommits.add(c)
                        }
                        c.branches.add(branchDomain)
                        branchDomain.commits.add(c)
//                            repository.commits.add(c)
                    }
//                        repository.commits.add(cmt)
                    cmt.committer?.let {
                        repository.user.add(it)
                        it.committedCommits.add(cmt)
                    }
                    cmt.author?.let {
                        repository.user.add(it)
                        it.authoredCommits.add(cmt)
                    }
                    repository.branches.add(branchDomain)

                    assertDoesNotThrow {
                        commitPort.create(cmt)
                    }
                }.map {
                    commitPort.findById(it.id!!) ?: throw IllegalStateException("must find commit here")
                }
        repository.commits.addAll(savedCommits)

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
                .ignoringFieldsMatchingRegexes(".*id", ".*_*", ".*logger")  // This ignores only fields starting with _
                .isEqualTo(savedCommits)
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
        "com.inso_world.binocular.infrastructure.sql.integration.service.CommitInfrastructurePortImplTest#provideCommitsAndLists",
    )
    fun `save multiple commits with repository, verify relationship to repository`(commitList: List<Commit>) {
        val savedEntities =
            commitList
                .map { cmt ->
                    (listOf(cmt) + cmt.parents + cmt.children).forEach { elem ->
                        repository.commits.add(elem)
                        branchDomain.commits.add(elem)
                        elem.committer?.let {
                            repository.user.add(it)
                            elem.committer = it
                        }
                        elem.author?.let {
                            repository.user.add(it)
                            elem.author = it
                        }
                    }
                    assertDoesNotThrow {
                        return@map commitPort.create(cmt)
                    }
                }.map {
                    commitPort.findById(it.id!!) ?: throw IllegalStateException("must find commit here")
                }
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
                            (listOf(cmt) + cmt.parents + cmt.children).forEach { elem ->
                                repository.commits.add(elem)
                                branchDomain.commits.add(elem)
                                elem.committer?.let {
                                    repository.user.add(it)
                                    elem.committer = it
                                }
                                elem.author?.let {
                                    repository.user.add(it)
                                    elem.author = it
                                }
                            }
                            assertDoesNotThrow {
                                commitPort.create(cmt)
                            }
                        }.map {
                            commitPort.findById(it.id!!) ?: throw IllegalStateException("must find commit here")
                        }
                repository.commits.clear()
                repository.commits.addAll(savedCommits)

                return@run savedCommits
            }

        val expectedCommits =
            (savedEntities + savedEntities.flatMap { it.parents }+ savedEntities.flatMap { it.children })
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
                    )
                user.committedCommits.add(cmt)
                cmt.branches.add(branchDomain)
                branchDomain.commits.add(cmt)
                cmt.repository = repository

                repository.commits.add(cmt)
                repository.user.add(user)
                repository.branches.add(branchDomain)

                assertAll(
                    "check model",
                    { assertThat(cmt.branches).hasSize(1) },
                    { assertThat(cmt.committer).isNotNull() },
                    { assertThat(branchDomain.commits).hasSize(1) },
                    { assertThat(cmt.repository).isNotNull() },
                    { assertThat(cmt.repository?.id).isNotNull() },
                    { assertThat(user.repository).isNotNull() },
                    { assertThat(cmt.repository?.id).isEqualTo(repository.id) },
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
                    { assertThat(saved.repository).isNotNull() },
                    { assertThat(saved.repository?.id).isNotNull() },
                    { assertThat(saved.repository?.id).isEqualTo(repository.id) },
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
            { assertThat(commitPort.findAll().toList()[0].repository?.id).isNotNull() },
            { assertThat(commitPort.findAll().toList()[0].repository?.id).isEqualTo(project.repo?.id) },
            { assertThat(commitPort.findAll().toList()[0].repository?.id).isEqualTo(repository.id) },
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
                    )
                repository.commits.add(cmt)
                branchDomain.commits.add(cmt)
                user.committedCommits.add(cmt)
                branchDomain.commits.add(cmt)
                repository.user.add(user)

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
                    )
                repository.commits.add(cmt)
                branchDomain.commits.add(cmt)
                user.committedCommits.add(cmt)
                branchDomain.commits.add(cmt)
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
