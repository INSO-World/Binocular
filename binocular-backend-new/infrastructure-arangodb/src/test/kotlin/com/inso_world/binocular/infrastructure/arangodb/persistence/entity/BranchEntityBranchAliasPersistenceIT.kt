package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.core.ArangoOperations
import com.inso_world.binocular.core.integration.base.BaseIntegrationTest
import com.inso_world.binocular.infrastructure.arangodb.ArangodbTestConfig
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.BranchRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.CommitRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.RepositoryRepository
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.vcs.ReferenceCategory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Verifies that [BranchEntity.branch] (the legacy alias for [BranchEntity.name]) is persisted
 * as a `"branch"` field on the raw ArangoDB `branches` document.
 *
 * ### Why this test exists
 * [com.inso_world.binocular.infrastructure.arangodb.service.DbExportPortImpl] exports raw stored
 * documents (no entity/Jackson mapping). The `db-export` JSON contract expects a `"branch"` key
 * equal to `"name"` for backward-compatibility with the legacy
 * [com.inso_world.binocular.model.Branch.branch] domain alias. If `branch` is annotated
 * `@Transient`, Spring Data ArangoDB never writes it, leaving the raw document without a
 * `"branch"` key.
 */
@SpringBootTest
@EnableAutoConfiguration
@ContextConfiguration(
    classes = [ArangodbTestConfig::class],
    initializers = [ArangodbTestConfig.Initializer::class],
)
@ExtendWith(SpringExtension::class)
@OptIn(ExperimentalUuidApi::class)
internal class BranchEntityBranchAliasPersistenceIT : BaseIntegrationTest() {
    @Autowired
    private lateinit var arangoOperations: ArangoOperations

    @Autowired
    private lateinit var repositoryRepository: RepositoryRepository

    @Autowired
    private lateinit var commitRepository: CommitRepository

    @Autowired
    private lateinit var branchRepository: BranchRepository

    private val branchIds = mutableListOf<String>()
    private val commitIds = mutableListOf<String>()
    private val developerIds = mutableListOf<String>()
    private val repositoryIds = mutableListOf<String>()

    @AfterEach
    fun cleanup() {
        branchIds.forEach { runCatching { branchRepository.deleteById(it) } }
        commitIds.forEach { runCatching { commitRepository.deleteById(it) } }
        developerIds.forEach { runCatching { arangoOperations.delete(it, DeveloperEntity::class.java) } }
        repositoryIds.forEach { runCatching { repositoryRepository.deleteById(it) } }
    }

    /**
     * Saves a minimal Repository -> Commit -> Branch graph and asserts that the raw
     * `branches` document's `"branch"` field is non-null and equal to [BranchEntity.name].
     */
    @Test
    fun `branch alias field is persisted on raw branches document`() {
        // --- setup ---
        val repoEntity = RepositoryEntity(iid = Uuid.random(), localPath = "/probe-branch-alias/repo-${Uuid.random()}")
        repositoryRepository.save(repoEntity)
        repositoryIds += repoEntity.id!!

        val devEntity =
            DeveloperEntity(
                gitSignature = "Branch Alias Dev <branch-alias-${Uuid.random()}@test.com>",
                iid = Developer.Id(Uuid.random()),
            ).also { it.repository = repoEntity }
        arangoOperations.insert(devEntity)
        developerIds += devEntity.id!!

        val commitEntity =
            CommitEntity(
                sha = "branch-alias-sha-${Uuid.random()}",
                iid = Uuid.random(),
                authorDateTime = LocalDateTime.now(),
                commitDateTime = LocalDateTime.now(),
            ).also {
                it.author = devEntity
                it.committer = devEntity
                it.repository = repoEntity
            }
        commitRepository.save(commitEntity)
        commitIds += commitEntity.id!!

        val branchEntity =
            BranchEntity(
                iid = Uuid.random(),
                name = "branch-alias-${Uuid.random()}",
                fullName = "refs/heads/branch-alias",
                category = ReferenceCategory.LOCAL_BRANCH.name,
            ).also {
                it.repository = repoEntity
                it.head = commitEntity
            }
        branchRepository.save(branchEntity)
        branchIds += branchEntity.id!!

        // --- probe: raw document must contain the legacy "branch" alias field ---
        val rawValue =
            arangoOperations
                .query(
                    "FOR b IN branches FILTER b._key == @key RETURN b.branch",
                    mapOf("key" to branchEntity.id!!),
                    String::class.java,
                ).first()

        assertNotNull(rawValue, "raw document must contain a non-null 'branch' field")
        assertEquals(branchEntity.name, rawValue, "'branch' field must equal the persisted 'name' value")
    }
}
