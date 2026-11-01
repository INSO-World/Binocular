package com.inso_world.binocular.infrastructure.test.repository

import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.CommitInfrastructurePort
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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

/**
 * Tests for saving repositories through RepositoryInfrastructurePort.
 *
 * Since #454 saving a repository does not cascade into its commits, branches and developers:
 * each is persisted through its own port and linked by [Commit.repositoryId] /
 * [Branch.repositoryId]. These tests assert both halves of that contract — creating a repository
 * alone leaves the commit and branch collections empty, and the explicitly created aggregates
 * come back pointing at the right repository.
 *
 * The plain repository case still creates its own project and repository, but a commit must be
 * authored by a developer that already exists in the database: `CommitDao.create` resolves author
 * and committer by id, there is no `DeveloperInfrastructurePort` and `IDeveloperDao` is internal
 * to `infrastructure-arangodb`, so a database-agnostic test cannot persist a developer of its own
 * (`RepositoryMapper` does not persist `Repository.developers` either). The commit-authoring cases
 * therefore use the seeded [TestDataProvider.repository] with [TestDataProvider.developer1] /
 * [TestDataProvider.developer2]. Since that fixture is present from the start, every count
 * assertion is expressed relative to the baseline captured in `@BeforeEach`.
 */
internal class RepositorySaveOperationTest : BaseInfrastructureSpringTest() {
    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePort

    @Autowired
    private lateinit var commitPort: CommitInfrastructurePort

    @Autowired
    private lateinit var branchPort: BranchInfrastructurePort

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
    fun `save plain repository, expecting in database`() {
        val project = projectPort.create(Project(name = "test-project"))
        val repository = Repository(localPath = "path/to/repo", projectId = project.iid)

        assertDoesNotThrow {
            repositoryPort.create(repository)
        }

        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(baseProjects + 1) },
            { assertThat(repositoryPort.findAll()).hasSize(baseRepositories + 1) },
            { assertThat(commitPort.findAll()).hasSize(baseCommits) },
            { assertThat(branchPort.findAll()).hasSize(baseBranches) },
        )

        val loaded = repositoryPort.findAll().single { it.iid == repository.iid }
        assertAll(
            "check repository round trip",
            { assertThat(loaded.id).isNotNull() },
            { assertThat(loaded.localPath).isEqualTo(repository.localPath) },
            { assertThat(loaded.projectId).isEqualTo(project.iid) },
        )
    }

    @Test
    fun `save repository with one commit, expecting in database`() {
        val repository = TestDataProvider.repository
        val commit =
            commitPort.create(
                commit(repository, sha = "a".repeat(40), message = "Initial commit", developer = TestDataProvider.developer1),
            )
        branchPort.create(branch(repository, name = "one-commit", fullName = "refs/heads/one-commit", headSha = commit.sha))

        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(baseProjects) },
            { assertThat(repositoryPort.findAll()).hasSize(baseRepositories) },
            { assertThat(commitPort.findAll()).hasSize(baseCommits + 1) },
            { assertThat(branchPort.findAll()).hasSize(baseBranches + 1) },
        )

        val loaded = commitPort.findAll().single { it.sha == commit.sha }
        assertAll(
            "check commit relationship",
            { assertThat(loaded.id).isNotNull() },
            { assertThat(loaded.sha).isEqualTo(commit.sha) },
            { assertThat(loaded.message).isEqualTo(commit.message) },
            { assertThat(loaded.repositoryId).isEqualTo(repository.iid) },
        )
    }

    @Test
    fun `save repository with one commit with one parent, expecting both in database`() {
        val repository = TestDataProvider.repository

        val parent =
            commitPort.create(
                commit(repository, sha = "b".repeat(40), message = "Parent commit", developer = TestDataProvider.developer2),
            )
        val child =
            commit(repository, sha = "a".repeat(40), message = "Child commit", developer = TestDataProvider.developer1)
                .apply { parentShas.add(parent.sha) }
        commitPort.create(child)

        branchPort.create(
            branch(repository, name = "feature/test", fullName = "refs/heads/feature/test", headSha = child.sha),
        )

        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(baseProjects) },
            { assertThat(repositoryPort.findAll()).hasSize(baseRepositories) },
            { assertThat(commitPort.findAll()).hasSize(baseCommits + 2) },
            { assertThat(branchPort.findAll()).hasSize(baseBranches + 1) },
        )

        val created = commitPort.findAll().filter { it.sha == parent.sha || it.sha == child.sha }
        assertAll(
            "check commit relationship",
            { assertThat(created.map { it.id }).doesNotContainNull() },
            { assertThat(created.map { it.repositoryId }).containsOnly(repository.iid) },
            {
                assertThat(created.map { it.sha })
                    .containsExactlyInAnyOrder("a".repeat(40), "b".repeat(40))
            },
        )
    }

    @Test
    fun `save repository with one commit with two parents, expecting all in database`() {
        val repository = TestDataProvider.repository

        val parent1 =
            commitPort.create(
                commit(repository, sha = "b".repeat(40), message = "Parent 1", developer = TestDataProvider.developer2),
            )
        val parent2 =
            commitPort.create(
                commit(repository, sha = "c".repeat(40), message = "Parent 2", developer = TestDataProvider.developer1),
            )
        val merge =
            commit(repository, sha = "a".repeat(40), message = "Merge commit", developer = TestDataProvider.developer1).apply {
                parentShas.add(parent1.sha)
                parentShas.add(parent2.sha)
            }
        commitPort.create(merge)

        branchPort.create(branch(repository, name = "two-parents", fullName = "refs/heads/two-parents", headSha = merge.sha))

        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(baseProjects) },
            { assertThat(repositoryPort.findAll()).hasSize(baseRepositories) },
            { assertThat(commitPort.findAll()).hasSize(baseCommits + 3) },
            { assertThat(branchPort.findAll()).hasSize(baseBranches + 1) },
        )

        val loadedMerge = commitPort.findAll().single { it.sha == merge.sha }
        assertAll(
            "merge commit round trip",
            { assertThat(loadedMerge.message).isEqualTo("Merge commit") },
            { assertThat(loadedMerge.repositoryId).isEqualTo(repository.iid) },
            { assertThat(loadedMerge.committerSignature).isEqualTo(loadedMerge.authorSignature) },
        )
    }

    @Test
    fun `save repository with two commits, expecting in database`() {
        val repository = TestDataProvider.repository

        val first =
            commitPort.create(
                commit(repository, sha = "a".repeat(40), message = "First commit", developer = TestDataProvider.developer1),
            )
        val second =
            commit(repository, sha = "b".repeat(40), message = "Second commit", developer = TestDataProvider.developer2)
                .apply { parentShas.add(first.sha) }
        commitPort.create(second)

        val savedBranch =
            branchPort.create(
                branch(repository, name = "test branch", fullName = "refs/heads/test-branch", headSha = second.sha),
            )

        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(baseProjects) },
            { assertThat(repositoryPort.findAll()).hasSize(baseRepositories) },
            { assertThat(commitPort.findAll()).hasSize(baseCommits + 2) },
            { assertThat(branchPort.findAll()).hasSize(baseBranches + 1) },
        )

        val loadedBranch = branchPort.findAll().single { it.iid == savedBranch.iid }
        assertAll(
            "check branch points at the newest commit",
            { assertThat(savedBranch.headSha).isEqualTo(second.sha) },
            { assertThat(loadedBranch.headSha).isEqualTo(second.sha) },
            { assertThat(loadedBranch.repositoryId).isEqualTo(repository.iid) },
        )
    }

    private fun commit(
        repository: Repository,
        sha: String,
        message: String,
        developer: Developer,
        timestamp: LocalDateTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
    ): Commit =
        Commit(
            sha = sha,
            message = message,
            authorSignature = Signature(developerId = developer.iid, developer = developer, timestamp = timestamp),
            repositoryId = repository.iid,
        )

    private fun branch(
        repository: Repository,
        name: String,
        fullName: String,
        headSha: String,
    ): Branch =
        Branch(
            name = name,
            fullName = fullName,
            category = ReferenceCategory.LOCAL_BRANCH,
            repositoryId = repository.iid,
            headSha = headSha,
        )
}
