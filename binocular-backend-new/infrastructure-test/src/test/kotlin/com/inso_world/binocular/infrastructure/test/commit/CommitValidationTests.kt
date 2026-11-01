package com.inso_world.binocular.infrastructure.test.commit

import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

/**
 * Validation tests for commits persisted through the ports.
 *
 * The former `save 1 commit without branch, expect ConstraintViolationException` case is gone:
 * a commit may exist without a branch, a branch is only a reference to a commit. Since #454 a
 * commit is no longer reachable through the repository object graph either — it carries
 * [Commit.repositoryId] — so these tests check what the model still guarantees.
 *
 * `CommitDao.create` resolves the author by id and there is no `DeveloperInfrastructurePort`
 * (`IDeveloperDao` is internal to the ArangoDB module), so the only developers available to a
 * database-agnostic test are the ones seeded by the infrastructure data setup. These tests
 * therefore build on [TestDataProvider.repository] / [TestDataProvider.developer1] and assert
 * record counts relative to the seeded baseline.
 */
internal class CommitValidationTests : BaseInfrastructureSpringTest() {
    @Autowired
    private lateinit var commitPort: CommitInfrastructurePort

    private lateinit var repository: Repository
    private lateinit var developer: Developer
    private var baseCommits = 0

    @BeforeEach
    fun setUp() {
        repository = TestDataProvider.repository
        developer = TestDataProvider.developer1
        baseCommits = commitPort.findAll().count()
    }

    @Test
    fun `save 1 commit without branch, expect commit to be persisted`() {
        val created = commitPort.create(commit(sha = "b".repeat(40)))

        val loaded = commitPort.findByIid(created.iid)
        assertAll(
            { assertThat(loaded).isNotNull() },
            { assertThat(loaded?.sha).isEqualTo("b".repeat(40)) },
            { assertThat(loaded?.repositoryId).isEqualTo(repository.iid) },
        )
    }

    @Test
    fun `commit sha must be 40 hexadecimal characters`() {
        assertAll(
            { assertThrows<IllegalArgumentException> { commit(sha = "b".repeat(39)) } },
            { assertThrows<IllegalArgumentException> { commit(sha = "z".repeat(40)) } },
        )
    }

    @Test
    fun `committerSignature defaults to the authorSignature`() {
        val created = commitPort.create(commit(sha = "c".repeat(40)))

        assertAll(
            { assertThat(created.committerSignature).isEqualTo(created.authorSignature) },
            { assertThat(created.commitDateTime).isEqualTo(created.authorDateTime) },
        )
    }

    private fun commit(sha: String): Commit =
        Commit(
            sha = sha,
            authorSignature = Signature(developerId = developer.iid, timestamp = LocalDateTime.now()),
            message = null,
            repositoryId = repository.iid,
        )
}
