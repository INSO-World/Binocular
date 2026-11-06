package com.inso_world.binocular.cli.integration.service

import com.inso_world.binocular.cli.exception.ServiceException
import com.inso_world.binocular.cli.integration.service.base.BaseServiceTest
import com.inso_world.binocular.cli.service.CommitService
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

internal class CommitServiceTest @Autowired constructor(
    private val commitService: CommitService,
    private val commitDao: CommitInfrastructurePort,
) : BaseServiceTest() {

    private lateinit var testDeveloper: Developer

    @BeforeEach
    fun setup() {
        // Get or create a test developer from the repository
        testDeveloper = simpleRepo.developers.firstOrNull()
            ?: Developer(name = "Test Committer", email = "committer@test.com", repository = simpleRepo)
    }

    /**
     * Creates a test commit with the new Signature-based constructor.
     */
    private fun createTestCommit(
        sha: String,
        message: String = "",
        repository: Repository = simpleRepo,
        developer: Developer = testDeveloper,
        timestamp: LocalDateTime = LocalDateTime.now().minusHours(1)
    ): Commit {
        val signature = Signature(developer = developer, timestamp = timestamp)
        return Commit(
            sha = sha,
            message = message,
            authorSignature = signature,
            repository = repository,
        )
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
        val exitingHeadCommits = createTestCommit(
            sha = "b51199ab8b83e31f64b631e42b2ee0b1c7e3259a", // head of simple
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
        val headOfOctoRepo = createTestCommit(
            sha = "ed167f854e871a1566317302c158704f71f8d16c", // imported branch of octo repo
        )
        val headOfSimpleRepo = createTestCommit(
            sha = "b51199ab8b83e31f64b631e42b2ee0b1c7e3259a", // head of simple
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
        val exitingHeadCommits = createTestCommit(
            sha = "ed167f854e871a1566317302c158704f71f8d16c", // imported branch of octo repo
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
    fun `find all commits invalid repo`() {
            assertThat(
                this.commitService.findAll(Repository(localPath = "invalid", project = Project(name = "p")))
            ).isEmpty()
    }
}
