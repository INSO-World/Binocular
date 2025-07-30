package com.inso_world.binocular.cli.integration.persistence.dao.sql

import com.inso_world.binocular.cli.BinocularCommandLineApplication
import com.inso_world.binocular.cli.index.vcs.toVcsRepository
import com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceNoDataTest
import com.inso_world.binocular.cli.integration.utils.RepositoryConfig
import com.inso_world.binocular.cli.integration.utils.generateCommits
import com.inso_world.binocular.cli.service.RepositoryService
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest.Companion.FIXTURES_PATH
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest.Companion.SIMPLE_PROJECT_NAME
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest.Companion.SIMPLE_REPO
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.ffi.BinocularFfi
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy

internal class RepositoryDaoTestWithSimpleData(
    @Autowired val repositoryPort: RepositoryInfrastructurePort,
    @Autowired val commitPort: CommitInfrastructurePort,
    @Autowired val userPort: UserInfrastructurePort,
    @Autowired val branchPort: BranchInfrastructurePort,
) : BasePersistenceNoDataTest() {
    @Autowired
    private lateinit var projectDao: ProjectInfrastructurePort

    @Autowired @Lazy
    private lateinit var repoService: RepositoryService

    companion object {
        internal lateinit var simpleRepoConfig: RepositoryConfig

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            BinocularCommandLineApplication()
            val ffi = BinocularFfi()
            val repo = ffi.findRepo("${FIXTURES_PATH}/${SIMPLE_REPO}")
            val cmt = ffi.findCommit(repo, "HEAD")
            val hashes = ffi.traverse(repo, cmt, null)
            this.simpleRepoConfig =
                RepositoryConfig(
                    repo = repo,
                    startCommit = cmt,
                    hashes = hashes,
                    project =
                        Project(
                            name = SIMPLE_PROJECT_NAME,
                        ),
                )
            this.simpleRepoConfig.hashes.map { it.branch = "master" }
        }
    }

    @BeforeEach
    fun beforeEach() {
        val repo = simpleRepoConfig.repo.toVcsRepository().toDomain()
        repo.project = simpleRepoConfig.project
        simpleRepoConfig.project.repo = repo
        val project = projectDao.create(simpleRepoConfig.project)
        project.repo?.let { this.simpleRepo = it }
            ?: throw IllegalStateException("Repo for project ${simpleRepoConfig.project.name} not found")
    }

    @AfterEach
    fun afterEach() {
        this.simpleRepo.project?.id?.let { projectDao.deleteById(it) }
    }

    @Test
    fun check_not_null() {
        assertThat(this.simpleRepo).isNotNull()
    }

    @Test
    fun check_created_exists() {
        val created = this.repositoryPort.findAll()

        assertAll(
            { assertThat(created).isNotEmpty() },
            { assertThat(created).hasSize(1) },
        )
    }

    @Test
    fun check_simple_repository_properties() {
        val created = this.repositoryPort.findAll().toList()[0]

        assertAll(
            { assertThat(created.id).isNotNull() },
            { assertThat(created.name).isEqualTo("${FIXTURES_PATH}/${SIMPLE_REPO}/.git") },
        )
    }

    @Test
    fun find_simple_repository() {
        val simple = this.repositoryPort.findById(simpleRepo.id!!)!!

        assertAll(
            { assertThat(simple.id).isNotNull() },
            { assertThat(simple.name).isEqualTo("${FIXTURES_PATH}/${SIMPLE_REPO}/.git") },
        )
    }

    @Test
    fun check_if_commits_default_empty() {
        assertThat(simpleRepo.commits).isEmpty()
    }

    @Test
    fun add_all_commits_to_repository() {
        val commits = generateCommits(repoService,simpleRepoConfig, simpleRepo)

        simpleRepo.commits.addAll(commits)
        val saved = this.repositoryPort.update(simpleRepo)
        val commitRepoIds = saved.commits.mapNotNull { it.repository?.id }
        val userRepoIds =
            listOf(
                saved.commits.mapNotNull { it.committer },
                saved.commits.mapNotNull { it.author },
            ).flatten()

        assertAll(
            "commitRepoIds",
            { assertThat(commitRepoIds).isNotEmpty() },
            { assertThat(commitRepoIds).doesNotContainNull() },
            { assertThat(commitRepoIds).allMatch { it == saved.id } },
        )
        assertAll(
            "userRepoIds",
            { assertThat(userRepoIds).isNotEmpty() },
            { assertThat(userRepoIds).hasSize(28) },
            { assertThat(userRepoIds).doesNotContainNull() },
        )
        assertAll(
            "saved.id",
            { assertThat(saved.id).isNotNull() },
            { assertThat(saved.commits).isNotEmpty() },
            { assertThat(saved.commits).hasSize(14) },
        )
    }

    @Test
    fun delete_repository() {
        val commits = generateCommits(repoService,simpleRepoConfig, simpleRepo)
        simpleRepo.commits.addAll(commits)
        val saved = this.repositoryPort.update(simpleRepo)
        this.repositoryPort.delete(saved)

        assertAll(
            { assertThat(this.repositoryPort.findAll()).isEmpty() },
            { assertThat(this.commitPort.findAll()).isEmpty() },
            { assertThat(this.userPort.findAll()).isEmpty() },
            { assertThat(this.projectDao.findAll()).hasSize(1) },
        )
    }

    @Test
    fun `save valid repository`() {
        val commits = generateCommits(repoService,simpleRepoConfig, simpleRepo)

        simpleRepo.commits.addAll(commits)
        this.repositoryPort.update(simpleRepo)

        assertAll(
            "check database numbers",
            { assertThat(this.repositoryPort.findAll()).hasSize(1) },
            { assertThat(this.commitPort.findAll()).hasSize(14) },
            { assertThat(this.userPort.findAll()).hasSize(3) },
            { assertThat(this.branchPort.findAll()).hasSize(1) },
        )
    }
}
