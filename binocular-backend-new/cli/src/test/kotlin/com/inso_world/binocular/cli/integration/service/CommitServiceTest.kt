package com.inso_world.binocular.cli.integration.service

import com.inso_world.binocular.cli.exception.ServiceException
import com.inso_world.binocular.cli.index.vcs.VcsCommit
import com.inso_world.binocular.cli.integration.service.base.BaseServiceTest
import com.inso_world.binocular.cli.service.CommitService
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.model.Repository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

internal class CommitServiceTest private constructor(
    @Autowired private val commitService: CommitService,
    @Autowired private val commitDao: CommitInfrastructurePort,
) : BaseServiceTest() {
    @BeforeEach
    fun setup() {
    }

    @Test
    fun `load all commits from database, expect all to be found`() {
        val allCommits = commitDao.findAll()

        assertAll(
            "Check Number of saved commits",
            { assertThat(allCommits).isNotEmpty() },
            { assertThat(allCommits).hasSize(14) },
        )
    }

    @Test
    @Transactional
    fun `check if exactly one root commit exists`() {
        val root = commitDao.findAll().filter { it.parents.isEmpty() }
        assertAll(
            "Check if exactly one root commit exists",
            { assertThat(root).isNotEmpty() },
            { assertThat(root).hasSize(1) },
        )
    }

    @Test
    fun `check existing commits, passing empty list`() {
        val existing = commitService.checkExisting(this.simpleRepo, emptyList())

        assertAll(
            { assertThat(existing.first).isEmpty() },
            { assertThat(existing.second).isEmpty() },
        )
    }

    @Test
    fun `check existing commits, passing head commit list, expect 1 existing commit`() {
        val exitingHeadCommits =
            VcsCommit(
                sha = "b51199ab8b83e31f64b631e42b2ee0b1c7e3259a", // head of simple
                message = "",
                branch = "",
                committer = null,
                author = null,
                commitTime = null,
                authorTime = null,
                parents = mutableSetOf(),
            )
        val existing = commitService.checkExisting(this.simpleRepo, listOf(exitingHeadCommits))

        assertAll(
            { assertThat(existing.first).isNotEmpty() },
            { assertThat(existing.first).hasSize(1) },
            { assertThat(existing.second).isEmpty() },
        )
    }

    @Test
    fun `check existing commits, passing new commit and existing, expect 1 new commit, 1 missing`() {
        val headOfOctoRepo =
            VcsCommit(
                sha = "ed167f854e871a1566317302c158704f71f8d16c", // imported branch of octo repo
                message = "",
                branch = "",
                committer = null,
                author = null,
                commitTime = null,
                authorTime = null,
                parents = mutableSetOf(),
            )
        val headOfSimpleRepo =
            VcsCommit(
                sha = "b51199ab8b83e31f64b631e42b2ee0b1c7e3259a", // head of simple
                message = "",
                branch = "",
                committer = null,
                author = null,
                commitTime = null,
                authorTime = null,
                parents = mutableSetOf(),
            )
        val existing = commitService.checkExisting(this.simpleRepo, listOf(headOfSimpleRepo, headOfOctoRepo))

        assertAll(
            { assertThat(existing.first).isNotEmpty() },
            { assertThat(existing.second).isNotEmpty() },
            { assertThat(existing.first).hasSize(1) },
            { assertThat(existing.second).hasSize(1) },
        )
    }

    @Test
    fun `check existing commits, passing new commit, expect 1 new commit`() {
        val exitingHeadCommits =
            VcsCommit(
                sha = "ed167f854e871a1566317302c158704f71f8d16c", // imported branch of octo repo
                message = "",
                branch = "",
                committer = null,
                author = null,
                commitTime = null,
                authorTime = null,
                parents = mutableSetOf(),
            )
        val existing = commitService.checkExisting(this.simpleRepo, listOf(exitingHeadCommits))

        assertAll(
            { assertThat(existing.first).isEmpty() },
            { assertThat(existing.second).isNotEmpty() },
            { assertThat(existing.second).hasSize(1) },
        )
    }

    @Test
    fun `find all commits_for_simple_repository`() {
        val simpleRepoCommits = this.commitService.findAll(this.simpleRepo)

        assertAll(
            { assertThat(simpleRepoCommits).isNotEmpty() },
            { assertThat(simpleRepoCommits).hasSize(14) },
        )
    }

    @Test
    fun `find_all_commits_invalid_repo`() {
        assertThrows<ServiceException> {
            this.commitService.findAll(Repository(id = null, name = "invalid", project = simpleProject))
        }
    }
}
