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
import org.junit.jupiter.api.Assertions.assertAll
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
 * Empirical probe: verifies that `@Ref(lazy=false) lateinit var` body properties on a Kotlin
 * `data class` entity are correctly persisted and reloaded by Spring Data ArangoDB 4.6.0.
 *
 * ### Why this test exists
 * Commit `b0ede53ad` moved `@Ref` fields from constructor params to `lateinit var` body props.
 * If Spring Data's write-path skips body `lateinit var` fields (returning null from
 * `PersistentPropertyAccessor.getProperty`), the reference ID is never written to the document,
 * and the read-path leaves the field uninitialized — causing `UninitializedPropertyAccessException`
 * in the mapper layer.
 *
 * ### Three assertions (in one test to share setup cost)
 * 1. **Write-side**: the raw ArangoDB document for a saved [BranchEntity] contains a non-null
 *    `repository` field (the reference was actually persisted).
 * 2. **Read-side**: `branchRepository.findById()` returns an entity whose `::repository` and
 *    `::head` [kotlin.reflect.KProperty0.isInitialized] flags are `true`.
 * 3. **Identity**: the reloaded references point to the exact same documents that were saved.
 */
@SpringBootTest
@EnableAutoConfiguration
@ContextConfiguration(
    classes = [ArangodbTestConfig::class],
    initializers = [ArangodbTestConfig.Initializer::class],
)
@ExtendWith(SpringExtension::class)
@OptIn(ExperimentalUuidApi::class)
internal class RefAnnotationProbeIT : BaseIntegrationTest() {
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

    @Test
    fun `@Ref(lazy=false) lateinit var body field is persisted and reloaded correctly`() {
        // --- setup ---
        val repoEntity = RepositoryEntity(iid = Uuid.random(), localPath = "/probe/repo-${Uuid.random()}")
        repositoryRepository.save(repoEntity)
        repositoryIds += repoEntity.id!!

        val devEntity =
            DeveloperEntity(
                gitSignature = "Probe Dev <probe-${Uuid.random()}@test.com>",
                iid = Developer.Id(Uuid.random()),
            ).also { it.repository = repoEntity }
        arangoOperations.insert(devEntity)
        developerIds += devEntity.id!!

        val commitEntity =
            CommitEntity(
                sha = "probe-sha-${Uuid.random()}",
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
                name = "probe-branch-${Uuid.random()}",
                fullName = "refs/heads/probe-branch",
                category = ReferenceCategory.LOCAL_BRANCH.name,
            ).also {
                it.repository = repoEntity
                it.head = commitEntity
            }
        branchRepository.save(branchEntity)
        branchIds += branchEntity.id!!

        // --- write-side probe: raw document must contain the repository reference ---
        val rawRef =
            arangoOperations
                .query(
                    "FOR b IN branches FILTER b._key == @key RETURN b.repository",
                    mapOf("key" to branchEntity.id!!),
                    String::class.java,
                ).first()

        assertNotNull(rawRef, "write-side: 'repository' field must be persisted in ArangoDB document")

        // --- read-side probe: reload via Spring Data and inspect @Ref fields ---
        val loaded =
            branchRepository.findById(branchEntity.id!!).orElseThrow {
                AssertionError("BranchEntity not found after save — id=${branchEntity.id}")
            }

        // Direct access: throws UninitializedPropertyAccessException if Spring Data left the
        // @Ref field unpopulated — that is exactly the failure mode we are probing for.
        assertAll(
            { assertEquals(repoEntity.id, loaded.repository.id, "read-side: repository identity must match saved entity") },
            { assertEquals(commitEntity.id, loaded.head.id, "read-side: head identity must match saved commit") },
        )
    }

    @Test
    fun `@Ref(lazy=false) lateinit var body field is populated after custom @Query AQL findAll`() {
        // --- setup (same graph as first test) ---
        val repoEntity = RepositoryEntity(iid = Uuid.random(), localPath = "/probe2/repo-${Uuid.random()}")
        repositoryRepository.save(repoEntity)
        repositoryIds += repoEntity.id!!

        val devEntity =
            DeveloperEntity(
                gitSignature = "Probe2 Dev <probe2-${Uuid.random()}@test.com>",
                iid = Developer.Id(Uuid.random()),
            ).also { it.repository = repoEntity }
        arangoOperations.insert(devEntity)
        developerIds += devEntity.id!!

        val commitEntity =
            CommitEntity(
                sha = "probe2-sha-${Uuid.random()}",
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

        val branchName = "probe2-branch-${Uuid.random()}"
        val branchEntity =
            BranchEntity(
                iid = Uuid.random(),
                name = branchName,
                fullName = "refs/heads/$branchName",
                category = ReferenceCategory.LOCAL_BRANCH.name,
            ).also {
                it.repository = repoEntity
                it.head = commitEntity
            }
        branchRepository.save(branchEntity)
        branchIds += branchEntity.id!!

        // --- read via custom AQL @Query (findAllSortedAsc) — the path used in production ---
        val fromAql =
            branchRepository.findByName(branchName)
                ?: error("BranchEntity not found by name '$branchName'")

        // Direct access: throws UninitializedPropertyAccessException if the custom @Query
        // path does NOT resolve @Ref fields the same way findById() does.
        assertAll(
            { assertEquals(repoEntity.id, fromAql.repository.id, "AQL read-side: repository identity must match") },
            { assertEquals(commitEntity.id, fromAql.head.id, "AQL read-side: head identity must match") },
        )
    }
}
