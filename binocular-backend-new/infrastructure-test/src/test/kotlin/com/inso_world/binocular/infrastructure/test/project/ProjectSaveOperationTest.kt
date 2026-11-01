package com.inso_world.binocular.infrastructure.test.project

import com.inso_world.binocular.core.integration.base.TestDataProvider
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
import com.inso_world.binocular.model.Signature
import com.inso_world.binocular.model.vcs.ReferenceCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

/**
 * Tests for saving projects through ProjectInfrastructurePort.
 *
 * Since #454 saving a project no longer cascades into an attached object graph: every aggregate
 * is persisted through its own port and linked by ID ([Repository.projectId], [Commit.repositoryId],
 * [Branch.repositoryId]). These tests verify exactly that, including that creating a project alone
 * does not create any repository, commit or branch rows.
 *
 * The pure project/repository cases create their own aggregates, but a commit must be authored by
 * a developer that already exists in the database: `CommitDao.create` resolves author and committer
 * by id, there is no `DeveloperInfrastructurePort` and `IDeveloperDao` is internal to
 * `infrastructure-arangodb`, so a database-agnostic test cannot persist a developer of its own.
 * The commit-authoring case therefore uses the seeded [TestDataProvider.repository] and
 * [TestDataProvider.developer1]. Since that fixture is present from the start, every count
 * assertion is expressed relative to the baseline captured in `@BeforeEach`.
 */
internal class ProjectSaveOperationTest : BaseInfrastructureSpringTest() {
    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePort

    @Autowired
    private lateinit var commitPort: CommitInfrastructurePort

    @Autowired
    private lateinit var branchPort: BranchInfrastructurePort

    @Autowired
    private lateinit var userPort: UserInfrastructurePort

    private var baseProjects: Int = 0
    private var baseRepositories: Int = 0
    private var baseCommits: Int = 0
    private var baseBranches: Int = 0
    private var baseUsers: Int = 0

    @BeforeEach
    fun setup() {
        reseedFixture()
        baseProjects = projectPort.findAll().count()
        baseRepositories = repositoryPort.findAll().count()
        baseCommits = commitPort.findAll().count()
        baseBranches = branchPort.findAll().count()
        baseUsers = userPort.findAll().count()
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
    fun `save plain project, expecting in database`() {
        val created = projectPort.create(Project(name = "test project"))

        val loaded = projectPort.findAll().single { it.iid == created.iid }
        assertAll(
            { assertThat(projectPort.findAll()).hasSize(baseProjects + 1) },
            { assertThat(loaded.id).isNotNull() },
            { assertThat(loaded.iid).isEqualTo(created.iid) },
            { assertThat(loaded.name).isEqualTo(created.name) },
            { assertThat(repositoryPort.findAll()).hasSize(baseRepositories) },
            { assertThat(commitPort.findAll()).hasSize(baseCommits) },
            { assertThat(branchPort.findAll()).hasSize(baseBranches) },
        )
    }

    @Test
    fun `save project with repository, check identities`() {
        val createdProject = projectPort.create(Project(name = "test project"))
        val createdRepository =
            repositoryPort.create(Repository(localPath = "test repository", projectId = createdProject.iid))

        assertAll(
            { assertThat(createdRepository.projectId).isEqualTo(createdProject.iid) },
            { assertThat(repositoryPort.findByIid(createdRepository.iid)?.projectId).isEqualTo(createdProject.iid) },
            { assertThat(projectPort.findByIid(createdProject.iid)?.iid).isEqualTo(createdProject.iid) },
        )
    }

    @Test
    fun `save project with repository, expecting in database`() {
        val createdProject = projectPort.create(Project(name = "test project"))
        val createdRepository =
            repositoryPort.create(Repository(localPath = "test repository", projectId = createdProject.iid))

        val loadedRepository = repositoryPort.findAll().single { it.iid == createdRepository.iid }
        assertAll(
            { assertThat(loadedRepository.id).isNotNull() },
            { assertThat(loadedRepository.iid).isEqualTo(createdRepository.iid) },
            { assertThat(loadedRepository.localPath).isEqualTo(createdRepository.localPath) },
            { assertThat(loadedRepository.projectId).isEqualTo(createdProject.iid) },
        )
        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(baseProjects + 1) },
            { assertThat(repositoryPort.findAll()).hasSize(baseRepositories + 1) },
            { assertThat(commitPort.findAll()).hasSize(baseCommits) },
            { assertThat(branchPort.findAll()).hasSize(baseBranches) },
            { assertThat(userPort.findAll()).hasSize(baseUsers) },
        )
    }

    @Test
    fun `save project with repository and commits, expecting in database`() {
        // The seeded fixture is used here because only its developers can author a commit.
        val developer = TestDataProvider.developer1
        val repository = TestDataProvider.repository
        val createdCommit =
            commitPort.create(
                Commit(
                    sha = "1234567890123456789012345678901234567890",
                    message = "test commit",
                    authorSignature =
                        Signature(developerId = developer.iid, timestamp = LocalDateTime.of(2025, 7, 13, 1, 1)),
                    repositoryId = repository.iid,
                ),
            )
        val createdBranch =
            branchPort.create(
                Branch(
                    name = "test branch",
                    fullName = "refs/heads/test-branch",
                    category = ReferenceCategory.LOCAL_BRANCH,
                    repositoryId = repository.iid,
                    headSha = createdCommit.sha,
                ),
            )

        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(baseProjects) },
            { assertThat(repositoryPort.findAll()).hasSize(baseRepositories) },
            { assertThat(commitPort.findAll()).hasSize(baseCommits + 1) },
            { assertThat(branchPort.findAll()).hasSize(baseBranches + 1) },
        )

        val loadedCommit = commitPort.findAll().single { it.sha == createdCommit.sha }
        assertAll(
            "check commit",
            { assertThat(loadedCommit.id).isNotNull() },
            { assertThat(loadedCommit.sha).isEqualTo(createdCommit.sha) },
            { assertThat(loadedCommit.message).isEqualTo(createdCommit.message) },
            { assertThat(loadedCommit.repositoryId).isEqualTo(repository.iid) },
        )

        val loadedBranch = branchPort.findAll().single { it.iid == createdBranch.iid }
        assertAll(
            "check branch",
            { assertThat(loadedBranch.iid).isEqualTo(createdBranch.iid) },
            { assertThat(loadedBranch.headSha).isEqualTo(createdCommit.sha) },
            { assertThat(loadedBranch.repositoryId).isEqualTo(repository.iid) },
        )

        // The repository of the fixture still belongs to the project it was created with.
        assertThat(repositoryPort.findByIid(repository.iid)?.projectId).isEqualTo(TestDataProvider.project.iid)
    }
}
