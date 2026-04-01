package com.inso_world.binocular.infrastructure.test.commit

import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

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

    private lateinit var repository: Repository
    private lateinit var project: Project

    @BeforeEach
    fun setup() {
        infrastructureDataSetup.teardown()
        this.project =
            projectPort.create(
                Project(name = "test project").apply {
                    repository = Repository(localPath = "test repository", project = this)
                },
            )
        this.repository = this.project.repo ?: error("test repository cannot be null")
        this.repositoryPort.create(this.repository)
    }

    // --- Helpers ---

    private fun sha(digit: String): String = digit.repeat(40)

    private fun ts(year: Int): LocalDateTime = LocalDateTime.of(year, 1, 1, 0, 0, 0)

    private fun createDeveloper(
        repo: Repository,
        name: String = "user 1",
        email: String = "user@example.com",
    ): Developer = Developer(name = name, email = email, repository = repo)

    private fun createCommit(
        sha: String,
        developer: Developer,
        repo: Repository,
        ts: LocalDateTime = ts(2020),
        message: String = "test commit",
    ): Commit =
        Commit(
            sha = sha,
            message = message,
            authorSignature = Signature(developer = developer, timestamp = ts),
            repository = repo,
        )

    /** Walks the full commit graph (parents + children) from the given roots. */
    private fun collectAllReachable(commits: List<Commit>): Set<Commit> {
        val visited = mutableSetOf<Commit>()

        fun walk(c: Commit) {
            if (!visited.add(c)) return
            c.parents.forEach { walk(it) }
            c.children.forEach { walk(it) }
        }
        commits.forEach { walk(it) }
        return visited
    }

    /**
     * Tears down existing data and creates a fresh project + repository,
     * then invokes [block] with the new repository.
     *
     * Used by `@TestFactory` dynamic tests because `@BeforeEach` runs once per factory method,
     * not per `DynamicTest`, and `NonRemovingMutableSet` prevents clearing commits between scenarios.
     */
    private fun withFreshRepository(block: (Repository) -> Unit) {
        infrastructureDataSetup.teardown()
        val freshProject =
            projectPort.create(
                Project(name = "test project").apply {
                    repository = Repository(localPath = "test repository", project = this)
                },
            )
        val repo = freshProject.repo ?: error("repository cannot be null")
        repositoryPort.create(repo)
        block(repo)
    }

    /**
     * Commit-graph scenario definitions for multi-commit tests.
     *
     * Each pair is (scenario name, lambda that builds the graph against a given repository
     * and returns the "explicitly provided" commit list).
     */
    private fun commitGraphScenarios(): List<Pair<String, (Repository) -> List<Commit>>> =
        listOf(
            "single commit" to { repo ->
                val dev = createDeveloper(repo)
                listOf(createCommit(sha("1"), dev, repo, ts = ts(2020)))
            },
            "two independent commits" to { repo ->
                val dev = createDeveloper(repo)
                listOf(
                    createCommit(sha("1"), dev, repo, ts = ts(2020)),
                    createCommit(sha("2"), dev, repo, ts = ts(2021)),
                )
            },
            "three independent commits" to { repo ->
                val dev = createDeveloper(repo)
                listOf(
                    createCommit(sha("1"), dev, repo, ts = ts(2020)),
                    createCommit(sha("2"), dev, repo, ts = ts(2021)),
                    createCommit(sha("3"), dev, repo, ts = ts(2022)),
                )
            },
            "parent-child: c1 -> c2 (only c1 in list)" to { repo ->
                val dev = createDeveloper(repo)
                val c2 = createCommit(sha("2"), dev, repo, ts = ts(2020))
                val c1 = createCommit(sha("1"), dev, repo, ts = ts(2021))
                c1.parents.add(c2)
                listOf(c1)
            },
            "child link: c1 <- c2 via children (only c1 in list)" to { repo ->
                val dev = createDeveloper(repo)
                val c2 = createCommit(sha("2"), dev, repo, ts = ts(2020))
                val c1 = createCommit(sha("1"), dev, repo, ts = ts(2021))
                c1.children.add(c2)
                listOf(c1)
            },
            "c1 -> c2 + c3 standalone (c2 not in list)" to { repo ->
                val dev = createDeveloper(repo)
                val c2 = createCommit(sha("2"), dev, repo, ts = ts(2020))
                val c1 = createCommit(sha("1"), dev, repo, ts = ts(2021))
                val c3 = createCommit(sha("3"), dev, repo, ts = ts(2022))
                c1.parents.add(c2)
                listOf(c1, c3)
            },
            "parent-child: c1 -> c2 (both in list)" to { repo ->
                val dev = createDeveloper(repo)
                val c2 = createCommit(sha("2"), dev, repo, ts = ts(2020))
                val c1 = createCommit(sha("1"), dev, repo, ts = ts(2021))
                c1.parents.add(c2)
                listOf(c1, c2)
            },
            "octopus merge: c1 -> {c2, c3}" to { repo ->
                val dev = createDeveloper(repo)
                val c2 = createCommit(sha("2"), dev, repo, ts = ts(2020))
                val c3 = createCommit(sha("3"), dev, repo, ts = ts(2022))
                val c1 = createCommit(sha("1"), dev, repo, ts = ts(2021))
                c1.parents.add(c2)
                c1.parents.add(c3)
                listOf(c1)
            },
            "diamond: c1 -> {c3, c2}, c3 -> c2" to { repo ->
                val dev = createDeveloper(repo)
                val c2 = createCommit(sha("2"), dev, repo, ts = ts(2020))
                val c3 = createCommit(sha("3"), dev, repo, ts = ts(2022))
                val c1 = createCommit(sha("1"), dev, repo, ts = ts(2021))
                c1.parents.add(c3)
                c1.parents.add(c2)
                c3.parents.add(c2)
                listOf(c1, c2, c3)
            },
            "reverse diamond: c1 -> {c2, c3}, c2 -> c3" to { repo ->
                val dev = createDeveloper(repo)
                val c3 = createCommit(sha("3"), dev, repo, ts = ts(2022))
                val c2 = createCommit(sha("2"), dev, repo, ts = ts(2021))
                val c1 = createCommit(sha("1"), dev, repo, ts = ts(2020))
                c1.parents.add(c2)
                c1.parents.add(c3)
                c2.parents.add(c3)
                listOf(c1, c2, c3)
            },
        )

    @Nested
    inner class SingleCommitSave {
        @Test
        fun `saved commit exists in database with correct counts and properties`() {
            val developer = Developer(name = "test", email = "test@example.com", repository = repository)
            val cmt =
                Commit(
                    sha = "1234567890123456789012345678901234567890",
                    message = "test commit",
                    authorSignature =
                        Signature(
                            developer = developer,
                            timestamp = LocalDateTime.of(2025, 7, 13, 1, 1),
                        ),
                    repository = repository,
                )

            repository.commits.add(cmt)
            repository.developers.add(developer)

            assertAll(
                "pre-save model checks",
                { assertThat(cmt.committer).isNotNull() },
                { assertThat(cmt.repository).isNotNull() },
                { assertThat(cmt.repository.iid).isNotNull() },
                { assertThat(developer.repository).isNotNull() },
                { assertThat(cmt.repository.iid).isEqualTo(repository.iid) },
                { assertThat(repository.commits).hasSize(1) },
                { assertThat(repository.developers).hasSize(1) },
                { assertThat(developer.committedCommits).hasSize(1) },
            )

            val saved =
                assertDoesNotThrow {
//                commitPort.create(cmt)
                    repositoryPort.update(repository).commits.find { it.sha == cmt.sha }
                }
            assertNotNull(saved)

            assertAll(
                "post-save entity checks",
                { assertThat(saved.committer).isNotNull() },
                { assertThat(saved.author).isNotNull() },
                { assertThat(saved.repository).isNotNull() },
                { assertThat(saved.repository.iid).isNotNull() },
                { assertThat(saved.repository.iid).isEqualTo(repository.iid) },
            )

            assertAll(
                "database counts",
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(1) },
                { assertThat(branchPort.findAll()).hasSize(0) },
                { assertThat(userPort.findAll()).hasSize(1) },
            )

            val fromDb = commitPort.findAll().toList()[0]

            assertThat(fromDb)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFieldsMatchingRegexes(".*id", ".*_*", ".*logger")
                .isEqualTo(saved)

            assertThat(fromDb)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFieldsMatchingRegexes(".*id", ".*_*", ".*logger")
                .ignoringFields("users")
                .isEqualTo(
                    project.repo
                        ?.commits
                        ?.toList()
                        ?.get(0),
                )

            assertThat(fromDb)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFieldsMatchingRegexes(".*id", ".*_*", ".*logger")
                .ignoringFields("users")
                .isEqualTo(repository.commits.toList()[0])

            assertThat(fromDb)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(saved)

            assertAll(
                "IDs are correctly assigned",
                { assertThat(fromDb.id).isNotNull() },
                { assertThat(fromDb.repository).isNotNull() },
                { assertThat(fromDb.repository.id).isNotNull() },
                { assertThat(fromDb.repository.id).isEqualTo(project.repo?.id) },
                { assertThat(fromDb.repository.id).isEqualTo(repository.id) },
            )
        }

        @Test
        fun `repository from database contains the saved commit`() {
            val developer = Developer(name = "test", email = "test@example.com", repository = repository)
            val cmt =
                Commit(
                    sha = "1234567890123456789012345678901234567890",
                    message = "test commit",
                    authorSignature =
                        Signature(
                            developer = developer,
                            timestamp = LocalDateTime.of(2025, 7, 13, 1, 1),
                        ),
                    repository = repository,
                )

            repositoryPort.update(repository)

            assertAll(
                "database counts",
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(branchPort.findAll()).hasSize(0) },
                { assertThat(commitPort.findAll()).hasSize(1) },
                { assertThat(userPort.findAll()).hasSize(1) },
            )
            run {
                val elem = repositoryPort.findAll().toList()[0]
                assertThat(elem.commits).hasSize(1)
            }
        }

        @Test
        fun `project from database contains the saved commit via repository`() {
            val developer = Developer(name = "test", email = "test@example.com", repository = repository)
            val cmt =
                Commit(
                    sha = "1234567890123456789012345678901234567890",
                    message = "test commit",
                    authorSignature =
                        Signature(
                            developer = developer,
                            timestamp = LocalDateTime.of(2025, 7, 13, 1, 1),
                        ),
                    repository = repository,
                )
            assertDoesNotThrow {
                repositoryPort.update(repository)
            }

            assertAll(
                "project exists",
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(projectPort.findAll().toList()[0]).isNotNull() },
                { assertThat(projectPort.findAll().toList()[0].repo).isNotNull() },
            )

            assertAll(
                {
                    val elem = projectPort.findAll().toList()[0]
                    assertThat(elem.repo?.commits).hasSize(1)
                },
            )
        }
    }

    @Nested
    inner class MultipleCommitSave {
        @TestFactory
        fun `commits persisted for various graph topologies`(): List<DynamicTest> =
            commitGraphScenarios().map { (name, buildGraph) ->
                DynamicTest.dynamicTest(name) {
                    withFreshRepository { repo ->
                        val commitList = buildGraph(repo)
                        repositoryPort.update(repo)

                        val allReachable = collectAllReachable(commitList)
                        val allDevs =
                            allReachable
                                .flatMap { listOf(it.author, it.committer) }
                                .distinctBy { it.iid }

                        assertAll(
                            "database counts",
                            { assertThat(projectPort.findAll()).hasSize(1) },
                            { assertThat(repositoryPort.findAll()).hasSize(1) },
                            { assertThat(branchPort.findAll()).hasSize(0) },
                            { assertThat(userPort.findAll()).hasSize(allDevs.size) },
                        )
                        assertThat(commitPort.findAll().toList()).hasSize(allReachable.size)
                    }
                }
            }

        @TestFactory
        fun `repository from database contains correct commits`(): List<DynamicTest> =
            commitGraphScenarios().map { (name, buildGraph) ->
                DynamicTest.dynamicTest(name) {
                    withFreshRepository { repo ->
                        val commitList = buildGraph(repo)
                        repositoryPort.update(repo)

                        val allReachable = collectAllReachable(commitList)

                        val loaded = repositoryPort.findAll().toList()[0]
                        assertThat(loaded.commits).hasSize(allReachable.size)
                        assertThat(loaded.commits)
                            .usingRecursiveComparison()
                            .ignoringCollectionOrder()
                            .ignoringFieldsMatchingRegexes(".*id", ".*_*", ".*logger")
                            .isEqualTo(allReachable)

                        val bySha = commitPort.findExistingSha(repo, allReachable.map { it.sha })
                        assertThat(loaded.commits)
                            .usingRecursiveComparison()
                            .ignoringCollectionOrder()
                            .ignoringFieldsMatchingRegexes(".*id", ".*_*", ".*logger")
                            .isEqualTo(bySha)
                    }
                }
            }

        @TestFactory
        fun `project from database contains correct commits via repository`(): List<DynamicTest> =
            commitGraphScenarios().map { (name, buildGraph) ->
                DynamicTest.dynamicTest(name) {
                    withFreshRepository { repo ->
                        val commitList = buildGraph(repo)
                        repositoryPort.update(repo)

                        val allReachable = collectAllReachable(commitList)

                        val loadedProject = projectPort.findAll().toList()[0]
                        assertThat(loadedProject.repo?.commits).hasSize(allReachable.size)
                        assertThat(loadedProject.repo?.commits)
                            .usingRecursiveComparison()
                            .ignoringCollectionOrder()
                            .ignoringFieldsMatchingRegexes(".*id", ".*_*", ".*logger")
                            .isEqualTo(allReachable)
                    }
                }
            }
    }

    @Nested
    inner class CyclicCommitValidation {
        /** Domain model rejects self-loops via require() in Commit.parents.add */
        @Test
        fun `commit cannot be its own parent`() {
            val dev = createDeveloper(repository)
            val c1 = createCommit(sha("1"), dev, repository)

            assertThrows<IllegalArgumentException> {
                c1.parents.add(c1)
            }
        }

        /** Domain model rejects 2-cycles: after c1.parents.add(c2), c2.parents.add(c1) fails */
        @Test
        fun `two commits cannot be mutual parents`() {
            val dev = createDeveloper(repository)
            val c1 = createCommit(sha("1"), dev, repository, ts = ts(2020))
            val c2 = createCommit(sha("2"), dev, repository, ts = ts(2021))

            c1.parents.add(c2)

            assertThrows<IllegalArgumentException> {
                c2.parents.add(c1)
            }
        }

        /** 3-node cycles CAN be constructed in the domain model (only 1- and 2-cycles are blocked). */
        @TestFactory
        @Disabled("Infrastructure-level cycle detection not yet implemented")
        fun `three-node cycles are not yet detected by infrastructure`(): List<DynamicTest> =
            listOf(
                DynamicTest.dynamicTest("c1 -> c2 -> c3 -> c1") {
                    withFreshRepository { repo ->
                        val dev = createDeveloper(repo)
                        val c1 = createCommit(sha("1"), dev, repo, ts = ts(2020))
                        val c2 = createCommit(sha("2"), dev, repo, ts = ts(2021))
                        val c3 = createCommit(sha("3"), dev, repo, ts = ts(2022))
                        c1.parents.add(c2)
                        c2.parents.add(c3)
                        c3.parents.add(c1)
                        repositoryPort.update(repo)
                    }
                },
            )
    }
}
