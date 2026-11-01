package com.inso_world.binocular.infrastructure.test.commit

import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

/**
 * Tests for saving commits through CommitInfrastructurePort.
 *
 * Since #454 a commit is persisted through its own port and references its repository by
 * [Commit.repositoryId]; `repositoryPort.update()` no longer cascades into `repository.commits`.
 * Parent/child relations are expressed as [Commit.parentShas] / [Commit.childShas].
 *
 * The commit-graph scenarios below therefore assert that every commit of a topology is persisted
 * and round-trips with its own properties. They deliberately do **not** assert that the parent
 * edges are persisted: `parentShas`/`childShas` have no counterpart in `CommitEntity` yet (the
 * entity still carries `parents`/`children` object lists), which `DomainModelAlignmentTest`
 * reports as open drift.
 *
 * A commit must be authored by a developer that already exists in the database, because
 * `CommitDao.create` resolves author and committer by id. There is no `DeveloperInfrastructurePort`
 * and `IDeveloperDao` is internal to `infrastructure-arangodb`, so a database-agnostic test cannot
 * persist a developer of its own. The only developers available are the ones seeded by
 * `InfrastructureDataSetup.setup()`, hence these tests use the seeded
 * [TestDataProvider.repository] together with [TestDataProvider.developer1] /
 * [TestDataProvider.developer2]. Because that fixture is present from the start, every count
 * assertion is expressed relative to the baseline captured in `@BeforeEach`.
 */
internal class CommitSaveOperationTest : BaseInfrastructureSpringTest() {
    @Autowired
    private lateinit var branchPort: BranchInfrastructurePort

    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePort

    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    private lateinit var commitPort: CommitInfrastructurePort

    private val repository: Repository = TestDataProvider.repository
    private val developer: Developer = TestDataProvider.developer1

    private var baseProjects: Int = 0
    private var baseRepositories: Int = 0
    private var baseCommits: Int = 0
    private var baseBranches: Int = 0

    @BeforeEach
    fun setup() {
        reseedFixture()
        baseProjects = projectPort.findAll().count()
        baseRepositories = repositoryPort.findAll().count()
        baseCommits = commitPort.findAll().count()
        baseBranches = branchPort.findAll().count()
    }

    // --- Helpers ---

    /**
     * Writes the seeded fixture a second time, replacing — not removing — it.
     *
     * The very first seeding of a fresh database stores project documents whose `@Ref` collections
     * are read back as `null`, which makes `ProjectMapper.toDomain` throw a `NullPointerException`
     * on `projectPort.findAll()`. Re-seeding stores them into the now existing collections, so the
     * tests no longer depend on another test class having absorbed that failure first. The fixture
     * itself — and with it the only persisted developers — stays available.
     */
    private fun reseedFixture() {
        infrastructureDataSetup.teardown()
        infrastructureDataSetup.setup()
    }

    private fun ts(year: Int): LocalDateTime = LocalDateTime.of(year, 1, 1, 0, 0, 0)

    /**
     * Builds a sha that is unique per graph scenario, so that the scenarios of one `@TestFactory`
     * do not collide on the unique sha index: `@BeforeEach` runs once per factory method, not once
     * per [DynamicTest], so the commits of a scenario stay in the database for the next one.
     */
    private fun scenarioSha(
        scenarioIndex: Int,
        commitIndex: Int,
    ): String = "%02x%02x".format(scenarioIndex, commitIndex) + "0".repeat(36)

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
            authorSignature = Signature(developerId = developer.iid, developer = developer, timestamp = ts),
            repositoryId = repo.iid,
        )

    /**
     * Invokes [block] with the seeded repository and the seeded developer that authors its commits.
     *
     * Used by `@TestFactory` dynamic tests because `@BeforeEach` runs once per factory method, not
     * per [DynamicTest]. The fixture is deliberately **not** torn down: without it there would be
     * no persisted developer to author a commit with.
     */
    private fun withFixtureRepository(block: (Repository, Developer) -> Unit) {
        block(TestDataProvider.repository, TestDataProvider.developer1)
    }

    /**
     * Commit-graph scenario definitions for multi-commit tests.
     *
     * Each pair is (scenario name, lambda that builds the graph against a given repository, its
     * persisted developer and a scenario-local sha factory, and returns all commits that are
     * expected to be persisted).
     */
    private fun commitGraphScenarios(): List<Pair<String, (Repository, Developer, (Int) -> String) -> List<Commit>>> =
        listOf(
            "single commit" to { repo, dev, sha ->
                listOf(createCommit(sha(1), dev, repo, ts = ts(2020)))
            },
            "two independent commits" to { repo, dev, sha ->
                listOf(
                    createCommit(sha(1), dev, repo, ts = ts(2020)),
                    createCommit(sha(2), dev, repo, ts = ts(2021)),
                )
            },
            "three independent commits" to { repo, dev, sha ->
                listOf(
                    createCommit(sha(1), dev, repo, ts = ts(2020)),
                    createCommit(sha(2), dev, repo, ts = ts(2021)),
                    createCommit(sha(3), dev, repo, ts = ts(2022)),
                )
            },
            "parent-child: c1 -> c2" to { repo, dev, sha ->
                val c2 = createCommit(sha(2), dev, repo, ts = ts(2020))
                val c1 = createCommit(sha(1), dev, repo, ts = ts(2021))
                c1.parentShas.add(c2.sha)
                c2.childShas.add(c1.sha)
                listOf(c1, c2)
            },
            "c1 -> c2 + c3 standalone" to { repo, dev, sha ->
                val c2 = createCommit(sha(2), dev, repo, ts = ts(2020))
                val c1 = createCommit(sha(1), dev, repo, ts = ts(2021))
                val c3 = createCommit(sha(3), dev, repo, ts = ts(2022))
                c1.parentShas.add(c2.sha)
                listOf(c1, c2, c3)
            },
            "octopus merge: c1 -> {c2, c3}" to { repo, dev, sha ->
                val c2 = createCommit(sha(2), dev, repo, ts = ts(2020))
                val c3 = createCommit(sha(3), dev, repo, ts = ts(2022))
                val c1 = createCommit(sha(1), dev, repo, ts = ts(2021))
                c1.parentShas.add(c2.sha)
                c1.parentShas.add(c3.sha)
                listOf(c1, c2, c3)
            },
            "diamond: c1 -> {c3, c2}, c3 -> c2" to { repo, dev, sha ->
                val c2 = createCommit(sha(2), dev, repo, ts = ts(2020))
                val c3 = createCommit(sha(3), dev, repo, ts = ts(2022))
                val c1 = createCommit(sha(1), dev, repo, ts = ts(2021))
                c1.parentShas.add(c3.sha)
                c1.parentShas.add(c2.sha)
                c3.parentShas.add(c2.sha)
                listOf(c1, c2, c3)
            },
            "reverse diamond: c1 -> {c2, c3}, c2 -> c3" to { repo, dev, sha ->
                val c3 = createCommit(sha(3), dev, repo, ts = ts(2022))
                val c2 = createCommit(sha(2), dev, repo, ts = ts(2021))
                val c1 = createCommit(sha(1), dev, repo, ts = ts(2020))
                c1.parentShas.add(c2.sha)
                c1.parentShas.add(c3.sha)
                c2.parentShas.add(c3.sha)
                listOf(c1, c2, c3)
            },
        )

    @Nested
    inner class SingleCommitSave {
        @Test
        fun `saved commit exists in database with correct counts and properties`() {
            val developer = this@CommitSaveOperationTest.developer
            val cmt =
                createCommit(
                    sha = "1234567890123456789012345678901234567890",
                    developer = developer,
                    repo = repository,
                    ts = LocalDateTime.of(2025, 7, 13, 1, 1),
                )

            val saved = assertDoesNotThrow { commitPort.create(cmt) }

            assertAll(
                "post-save model checks",
                { assertThat(saved.repositoryId).isEqualTo(repository.iid) },
                { assertThat(saved.authorSignature.developerId).isEqualTo(developer.iid) },
                { assertThat(saved.committerSignature).isEqualTo(saved.authorSignature) },
            )

            assertAll(
                "database counts",
                { assertThat(projectPort.findAll()).hasSize(baseProjects) },
                { assertThat(repositoryPort.findAll()).hasSize(baseRepositories) },
                { assertThat(commitPort.findAll()).hasSize(baseCommits + 1) },
                { assertThat(branchPort.findAll()).hasSize(baseBranches) },
            )

            val fromDb = commitPort.findAll().single { it.sha == cmt.sha }
            assertAll(
                "commit round trip",
                { assertThat(fromDb.id).isNotNull() },
                { assertThat(fromDb.iid).isEqualTo(cmt.iid) },
                { assertThat(fromDb.sha).isEqualTo(cmt.sha) },
                { assertThat(fromDb.message).isEqualTo(cmt.message) },
                { assertThat(fromDb.repositoryId).isEqualTo(repository.iid) },
                { assertThat(fromDb.authorDateTime).isEqualTo(cmt.authorDateTime) },
            )
        }

        @Test
        fun `commit is findable by its own iid`() {
            val developer = this@CommitSaveOperationTest.developer
            val created =
                commitPort.create(
                    createCommit(
                        sha = "2345678901234567890123456789012345678901",
                        developer = developer,
                        repo = repository,
                        ts = LocalDateTime.of(2025, 7, 13, 1, 1),
                    ),
                )

            val loaded = commitPort.findByIid(created.iid)
            assertAll(
                { assertThat(loaded).isNotNull() },
                { assertThat(loaded?.sha).isEqualTo(created.sha) },
                { assertThat(loaded?.repositoryId).isEqualTo(repository.iid) },
                { assertThat(commitPort.findAll()).hasSize(baseCommits + 1) },
            )
        }
    }

    @Nested
    inner class MultipleCommitSave {
        @TestFactory
        fun `commits persisted for various graph topologies`(): List<DynamicTest> =
            commitGraphScenarios().mapIndexed { scenarioIndex, (name, buildGraph) ->
                DynamicTest.dynamicTest(name) {
                    withFixtureRepository { repo, dev ->
                        val commits = buildGraph(repo, dev) { commitIndex -> scenarioSha(scenarioIndex, commitIndex) }
                        val commitsBefore = commitPort.findAll().count()
                        commitPort.saveAll(commits)

                        assertAll(
                            "database counts",
                            { assertThat(projectPort.findAll()).hasSize(baseProjects) },
                            { assertThat(repositoryPort.findAll()).hasSize(baseRepositories) },
                            { assertThat(branchPort.findAll()).hasSize(baseBranches) },
                            { assertThat(commitPort.findAll().toList()).hasSize(commitsBefore + commits.size) },
                        )
                    }
                }
            }

        @TestFactory
        fun `persisted commits keep their sha and repository reference`(): List<DynamicTest> =
            commitGraphScenarios().mapIndexed { scenarioIndex, (name, buildGraph) ->
                DynamicTest.dynamicTest(name) {
                    withFixtureRepository { repo, dev ->
                        val commits = buildGraph(repo, dev) { commitIndex -> scenarioSha(scenarioIndex, commitIndex) }
                        val expectedShas = commits.map { it.sha }.toSet()
                        commitPort.saveAll(commits)

                        val loaded = commitPort.findAll().filter { it.sha in expectedShas }
                        assertAll(
                            { assertThat(loaded.map { it.sha }).containsExactlyInAnyOrderElementsOf(expectedShas) },
                            { assertThat(loaded.map { it.repositoryId }.toSet()).containsOnly(repo.iid) },
                            { assertThat(loaded.map { it.id }).doesNotContainNull() },
                        )
                    }
                }
            }
    }
}
