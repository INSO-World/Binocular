package com.inso_world.binocular.cli.integration.persistence.dao.sql

import com.inso_world.binocular.cli.BinocularCommandLineApplication
import com.inso_world.binocular.cli.index.vcs.toVcsRepository
import com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceNoDataTest
import com.inso_world.binocular.cli.integration.utils.RepositoryConfig
import com.inso_world.binocular.cli.integration.utils.generateCommits
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest.Companion.FIXTURES_PATH
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest.Companion.SIMPLE_PROJECT_NAME
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest.Companion.SIMPLE_REPO
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.ffi.BinocularFfi
import com.inso_world.binocular.model.Project
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired

internal class RepositoryDaoTestWithSimpleData(
    @Autowired val repositoryDao: RepositoryInfrastructurePort,
    @Autowired val commitDao: CommitInfrastructurePort,
    @Autowired val userDao: UserInfrastructurePort,
    @Autowired val branchDao: BranchInfrastructurePort,
) : BasePersistenceNoDataTest() {
    @Autowired
    private lateinit var projectDao: ProjectInfrastructurePort

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
        val project = projectDao.save(simpleRepoConfig.project)
        project.repo?.let { this.simpleRepo = it }
            ?: throw IllegalStateException("Repo for project ${simpleRepoConfig.project.name} not found")
    }

    @AfterEach
    fun afterEach() {
        this.simpleRepo.projectId?.let { projectDao.deleteById(it) }
    }

    @Test
    fun check_not_null() {
        assertThat(this.simpleRepo).isNotNull()
    }

    @Test
    fun check_created_exists() {
        val created = this.repositoryDao.findAll()

        assertAll(
            { assertThat(created).isNotEmpty() },
            { assertThat(created).hasSize(1) },
        )
    }

    @Test
    fun check_simple_repository_properties() {
        val created = this.repositoryDao.findAll().toList()[0]

        assertAll(
            { assertThat(created.id).isNotNull() },
            { assertThat(created.name).isEqualTo("${FIXTURES_PATH}/${SIMPLE_REPO}/.git") },
        )
    }

    @Test
    fun find_simple_repository() {
        val simple = this.repositoryDao.findById(simpleRepo.id!!)!!

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
        val commits = generateCommits(simpleRepoConfig, simpleRepo)

        simpleRepo.commits.addAll(commits)
        val saved = this.repositoryDao.update(simpleRepo)
        val commitRepoIds = saved.commits.mapNotNull { it.repositoryId }
        val userRepoIds =
            listOf(
                saved.commits.mapNotNull { it.committer },
                saved.commits.mapNotNull { it.author },
            ).flatten()

        assertAll(
            { assertThat(commitRepoIds).isNotEmpty() },
            { assertThat(commitRepoIds).doesNotContainNull() },
            { assertThat(commitRepoIds).allMatch { it == saved.id } },
            { assertThat(userRepoIds).isNotEmpty() },
            { assertThat(userRepoIds).hasSize(28) },
            { assertThat(userRepoIds).doesNotContainNull() },
            { assertThat(saved.id).isNotNull() },
            { assertThat(saved.commits).isNotEmpty() },
            { assertThat(saved.commits).hasSize(14) },
        )
    }

    @Test
    fun delete_repository() {
        val commits = generateCommits(simpleRepoConfig, simpleRepo)
        simpleRepo.commits.addAll(commits)
        val saved = this.repositoryDao.update(simpleRepo)
        this.repositoryDao.delete(saved)

        assertAll(
            { assertThat(this.repositoryDao.findAll()).isEmpty() },
            { assertThat(this.commitDao.findAll()).isEmpty() },
            { assertThat(this.userDao.findAll()).isEmpty() },
            { assertThat(this.projectDao.findAll()).hasSize(1) },
        )
    }

    @Test
    fun save_full_repository() {
        val commits = generateCommits(simpleRepoConfig, simpleRepo)

        simpleRepo.commits.addAll(commits)
        this.repositoryDao.update(simpleRepo)

        assertAll(
            { assertThat(this.repositoryDao.findAll()).hasSize(1) },
            { assertThat(this.commitDao.findAll()).hasSize(14) },
            { assertThat(this.userDao.findAll()).hasSize(3) },
            { assertThat(this.branchDao.findAll()).hasSize(1) },
        )
    }
}
