package com.inso_world.binocular.infrastructure.test.commit

import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import com.inso_world.binocular.model.vcs.ReferenceCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

/**
 * Tests for updating commits through CommitInfrastructurePort.
 *
 * Since #454 every aggregate is persisted through its own port and parent/child relations are
 * expressed as [Commit.parentShas] / [Commit.childShas]. The sha sets have no counterpart in
 * `CommitEntity` yet, so these tests assert that an update round-trips the commit's own state
 * and keeps the sha links on the domain object, not that the edges reach the database.
 *
 * A commit must be authored by a developer that already exists in the database, because
 * `CommitDao.create` resolves author and committer by id. There is no `DeveloperInfrastructurePort`
 * and `IDeveloperDao` is internal to `infrastructure-arangodb`, so a database-agnostic test cannot
 * persist a developer of its own. These tests therefore work on the seeded
 * [TestDataProvider.repository] with [TestDataProvider.developer1] / [TestDataProvider.developer2]
 * as authors, and express every count assertion relative to the baseline captured in `@BeforeEach`.
 */
internal class CommitUpdateOperationTest : BaseInfrastructureSpringTest() {
    @Autowired
    private lateinit var branchPort: BranchInfrastructurePort

    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePort

    @Autowired
    private lateinit var commitPort: CommitInfrastructurePort

    private val repository: Repository = TestDataProvider.repository

    private lateinit var savedCommit: Commit

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

        this.savedCommit =
            commitPort.create(
                commit(
                    sha = "1234567890123456789012345678901234567890",
                    message = "test commit",
                    developer = TestDataProvider.developer1,
                    timestamp = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
                ),
            )

        // A branch is only a reference to a commit, so it is created on its own port.
        branchPort.create(
            Branch(
                name = "fixed branch",
                fullName = "refs/heads/fixed-branch",
                category = ReferenceCategory.LOCAL_BRANCH,
                repositoryId = repository.iid,
                headSha = savedCommit.sha,
            ),
        )
    }

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

    @Test
    fun `update commit unchanged, should not fail`() {
        assertDoesNotThrow {
            commitPort.update(savedCommit)
        }
        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(baseProjects) },
            { assertThat(repositoryPort.findAll()).hasSize(baseRepositories) },
            { assertThat(commitPort.findAll()).hasSize(baseCommits + 1) },
            { assertThat(branchPort.findAll()).hasSize(baseBranches + 1) },
        )
    }

    @Test
    fun `update commit, changed message is persisted`() {
        val changed =
            commit(
                sha = savedCommit.sha,
                message = "updated commit message",
                developer = TestDataProvider.developer1,
                timestamp = savedCommit.authorDateTime,
                iid = savedCommit.iid,
            ).also { it.id = savedCommit.id }

        assertDoesNotThrow { commitPort.update(changed) }

        val reloaded = commitPort.findByIid(savedCommit.iid)
        assertAll(
            { assertThat(reloaded).isNotNull() },
            { assertThat(reloaded?.message).isEqualTo("updated commit message") },
            { assertThat(reloaded?.sha).isEqualTo(savedCommit.sha) },
            { assertThat(commitPort.findAll()).hasSize(baseCommits + 1) },
        )
    }

    @Test
    fun `update commit, add parent sha`() {
        val parent =
            commitPort.create(
                commit(
                    sha = "0".repeat(40),
                    message = "parent commit",
                    developer = TestDataProvider.developer2,
                    timestamp = LocalDateTime.of(2019, 12, 31, 23, 59, 59),
                ),
            )

        savedCommit.parentShas.add(parent.sha)
        parent.childShas.add(savedCommit.sha)

        assertDoesNotThrow { commitPort.update(savedCommit) }

        assertAll(
            "check parent-child relationship on the domain objects",
            { assertThat(savedCommit.parentShas).containsExactly(parent.sha) },
            { assertThat(parent.childShas).containsExactly(savedCommit.sha) },
        )
        assertThat(commitPort.findAll()).hasSize(baseCommits + 2)
    }

    @Test
    fun `update commit with complex parent graph`() {
        val parent1 =
            commitPort.create(
                commit(
                    sha = "a".repeat(40),
                    message = "parent 1",
                    developer = TestDataProvider.developer1,
                    timestamp = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
                ),
            )
        val parent2 =
            commitPort.create(
                commit(
                    sha = "b".repeat(40),
                    message = "parent 2",
                    developer = TestDataProvider.developer2,
                    timestamp = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
                ),
            )

        savedCommit.parentShas.add(parent1.sha)
        savedCommit.parentShas.add(parent2.sha)

        assertDoesNotThrow { commitPort.update(savedCommit) }

        assertAll(
            "check parent relationships on the domain object",
            { assertThat(savedCommit.parentShas).hasSize(2) },
            { assertThat(savedCommit.parentShas).containsExactlyInAnyOrder(parent1.sha, parent2.sha) },
        )
        assertThat(commitPort.findAll()).hasSize(baseCommits + 3)
    }

    private fun commit(
        sha: String,
        message: String,
        developer: Developer,
        timestamp: LocalDateTime,
        iid: Commit.Id = Commit.Id(kotlin.uuid.Uuid.random()),
    ): Commit =
        Commit(
            sha = sha,
            message = message,
            authorSignature = Signature(developerId = developer.iid, developer = developer, timestamp = timestamp),
            repositoryId = repository.iid,
            iid = iid,
        )
}
