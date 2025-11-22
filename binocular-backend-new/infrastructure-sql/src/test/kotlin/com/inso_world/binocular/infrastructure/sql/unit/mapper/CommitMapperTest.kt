package com.inso_world.binocular.infrastructure.sql.unit.mapper

import com.inso_world.binocular.core.extension.reset
import com.inso_world.binocular.infrastructure.sql.TestData
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.unit.mapper.base.BaseMapperTest
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import io.mockk.Called
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.mockk.verifyOrder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
internal class CommitMapperTest : BaseMapperTest() {

    companion object {
        val IGNORED_FIELDS = listOf(
            "issues",
            "modules",
            "stats",
            "builds",
            "files",
            "diffs",
            "branches",
            "parents",
            "children",
            "author",
            "committer",
        ) + RepositoryMapperTest.IGNORED_FIELDS.map { "repository.$it" }
    }

    @BeforeEach
    fun setup() {
        mockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntityKt")
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntityKt")
    }

    @Nested
    inner class ToEntity {
        private lateinit var repositoryEntity: RepositoryEntity
        private lateinit var repositoryDomain: Repository

        @BeforeEach
        fun setup() {
            val projectModel = TestData.Domain.testProject(
                name = "test-project",
                id = null,
                description = null
            )
            repositoryDomain = TestData.Domain.testRepository(
                localPath = "test",
                id = null,
                project = projectModel
            )
            repositoryEntity = TestData.Entity.testRepositoryEntity(
                localPath = repositoryDomain.localPath,
                id = Random.nextLong(),
                project = TestData.Entity.testProjectEntity(
                    name = projectModel.name,
                    id = null,
                    description = null
                )
            )
            ctx.remember(repositoryDomain, repositoryEntity)

            clearMocks(ctx, recordedCalls = true)
        }

        @Test
        fun `minimal valid example, check properties`() {
            val commit = TestData.Domain.testCommit(
                sha = "a".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repository = repositoryDomain,
                id = null
            )

            val entity = assertDoesNotThrow {
                commitMapper.toEntity(commit)
            }

            assertAll(
                "entity properties",
                { assertThat(entity.sha).isEqualTo(commit.sha) },
                { assertThat(entity.message).isEqualTo(commit.message) },
                { assertThat(entity.repository).isSameAs(repositoryEntity) },
                { assertThat(entity.authorDateTime).isEqualTo(commit.authorDateTime) },
                { assertThat(entity.commitDateTime).isEqualTo(commit.commitDateTime) },
                { assertThat(entity.iid).isNotSameAs(commit.iid) },
                { assertThat(entity.iid).isEqualTo(commit.iid) },
            )
        }

        @Test
        fun `minimal valid example, check equality`() {
            val commit = TestData.Domain.testCommit(
                sha = "a".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repository = repositoryDomain,
                id = null
            )

            val entity = assertDoesNotThrow {
                commitMapper.toEntity(commit)
            }

            assertThat(entity).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFields(*IGNORED_FIELDS.toTypedArray())
                .ignoringFieldsMatchingRegexes(".*id")
                .isEqualTo(commit)
        }

        @Test
        fun `minimal valid example, verify calls`() {
            val commit = TestData.Domain.testCommit(
                sha = "a".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repository = repositoryDomain,
                id = null
            )

            val entity = assertDoesNotThrow {
                commitMapper.toEntity(commit)
            }

            verifyOrder {
                ctx.findEntity<Commit.Key, Commit, CommitEntity>(commit)
                ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(repositoryDomain)
                ctx.remember(commit, entity)
            }

            confirmVerified(ctx)
        }

        @Test
        fun `clear context, expect IllegalStateException since Repository is not in context`() {
            ctx.reset()

            val commit = TestData.Domain.testCommit(
                sha = "a".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repository = repositoryDomain,
                id = null
            )

            val ex = assertThrows<IllegalStateException> {
                commitMapper.toEntity(commit)
            }

            assertThat(ex.message).contains("RepositoryEntity must be mapped before CommitEntity.")
        }

        @Test
        fun `map with id is null, should not fail`() {
            val commit = TestData.Domain.testCommit(
                sha = "a".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repository = repositoryDomain,
                id = null
            )

            assertNull(commit.id)

            assertDoesNotThrow {
                commitMapper.toEntity(commit)
            }
        }

        @Test
        fun `fast-path when commit already in context, returns cached entity`() {
            val commit = TestData.Domain.testCommit(
                sha = "a".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repository = repositoryDomain,
                id = null
            )

            // First call: map the commit
            val entity1 = commitMapper.toEntity(commit)

            clearMocks(ctx, recordedCalls = true)

            // Second call: should return cached entity
            val entity2 = commitMapper.toEntity(commit)

            assertAll(
                { assertThat(entity2).isSameAs(entity1) },
                { verify(exactly = 1) { ctx.findEntity<Commit.Key, Commit, CommitEntity>(commit) } },
                { verify(exactly = 0) { ctx.remember(any<Commit>(), any<CommitEntity>()) } }
            )

            confirmVerified(ctx)
        }
    }

    @Nested
    inner class ToDomain {
        private lateinit var repositoryEntity: RepositoryEntity
        private lateinit var repositoryDomain: Repository

        @BeforeEach
        fun setup() {
            val projectEntity = TestData.Entity.testProjectEntity(
                name = "TestProject",
                id = 1L,
                description = "A test project"
            )
            val project = TestData.Domain.testProject(
                name = projectEntity.name,
                id = projectEntity.id.toString(),
                description = projectEntity.description
            )
            ctx.remember(project, projectEntity)

            this.repositoryEntity = TestData.Entity.testRepositoryEntity(
                localPath = "TestRepository",
                id = 1L,
                project = projectEntity
            )
            this.repositoryDomain = TestData.Domain.testRepository(
                localPath = this.repositoryEntity.localPath,
                id = this@ToDomain.repositoryEntity.id.toString(),
                project = project
            )
            ctx.remember(this.repositoryDomain, this.repositoryEntity)

            clearMocks(ctx, recordedCalls = true)
        }

        @Test
        fun `minimal valid example, check properties`() {
            val commitEntity = TestData.Entity.testCommitEntity(
                sha = "a".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repository = repositoryEntity,
                id = 1L
            )

            val domain = commitMapper.toDomain(commitEntity)

            assertAll(
                { assertThat(domain.sha).isEqualTo(commitEntity.sha) },
                { assertThat(domain.message).isEqualTo(commitEntity.message) },
                { assertThat(domain.repository).isSameAs(repositoryDomain) },
                { assertThat(domain.authorDateTime).isEqualTo(commitEntity.authorDateTime) },
                { assertThat(domain.commitDateTime).isEqualTo(commitEntity.commitDateTime) },
                { assertThat(domain.iid).isNotSameAs(commitEntity.iid) },
                { assertThat(domain.iid).isEqualTo(commitEntity.iid) },
                { assertThat(domain.committer).isNotNull() },
                { assertThat(domain.committer.email).isEqualTo(commitEntity.committer?.email) },
            )
        }

        @Test
        fun `minimal valid example, check equality`() {
            val commitEntity = TestData.Entity.testCommitEntity(
                sha = "a".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repository = repositoryEntity,
                id = 1L
            )

            val domain = commitMapper.toDomain(commitEntity)

            assertThat(domain).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFields(*IGNORED_FIELDS.toTypedArray())
                .ignoringFieldsMatchingRegexes(".*id")
                .isEqualTo(commitEntity)
        }

        @Test
        fun `minimal valid example, verify calls`() {
            val commitEntity = TestData.Entity.testCommitEntity(
                sha = "a".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repository = repositoryEntity,
                id = 1L
            )

            val domain = commitMapper.toDomain(commitEntity)

            verifyOrder {
                ctx.findDomain<Commit, CommitEntity>(commitEntity)
                ctx.findDomain<Repository, RepositoryEntity>(commitEntity.repository)
                ctx.findDomain<User, com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity>(
                    requireNotNull(commitEntity.committer)
                )
                ctx.remember(domain.committer, requireNotNull(commitEntity.committer))
                ctx.remember(domain, commitEntity)
            }
        }

        @Test
        fun `map with id is null, should not fail`() {
            val commitEntity = TestData.Entity.testCommitEntity(
                sha = "a".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repository = repositoryEntity,
                id = null
            )

            assertNull(commitEntity.id)

            assertDoesNotThrow {
                commitMapper.toDomain(commitEntity)
            }
        }

        @Test
        fun `commit without committer throws IllegalStateException`() {
            val commitEntity = TestData.Entity.testCommitEntity(
                sha = "missing".repeat(6).take(40),
                authorDateTime = LocalDateTime.of(2020, 5, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 5, 1, 1, 0, 0, 0),
                message = "Commit without committer",
                repository = repositoryEntity,
                committer = null,
                id = 2L
            )

            val ex = assertThrows<IllegalStateException> {
                commitMapper.toDomain(commitEntity)
            }

            assertThat(ex).hasMessageContaining("CommitEntity.committer must not be null")
        }

        @Test
        fun `clear context, expect IllegalStateException since Repository is not in context`() {
            ctx.reset()

            val commitEntity = TestData.Entity.testCommitEntity(
                sha = "a".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repository = repositoryEntity,
                id = 1L
            )

            val ex = assertThrows<IllegalStateException> {
                commitMapper.toDomain(commitEntity)
            }

            assertThat(ex.message).contains("Repository must be mapped before")
        }

        @Test
        fun `fast-path when commit already in context, returns cached domain`() {
            val commitEntity = TestData.Entity.testCommitEntity(
                sha = "a".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repository = repositoryEntity,
                id = 1L
            )

            // First call: map the commit
            val domain1 = commitMapper.toDomain(commitEntity)

            clearMocks(ctx, recordedCalls = true)

            // Second call: should return cached domain
            val domain2 = commitMapper.toDomain(commitEntity)

            assertAll(
                { assertThat(domain2).isSameAs(domain1) },
                { verify(exactly = 1) { ctx.findDomain<Commit, CommitEntity>(commitEntity) } },
                { verify(exactly = 0) { ctx.remember(any<Commit>(), any<CommitEntity>()) } }
            )

            confirmVerified(ctx)
        }
    }

    @Nested
    inner class RefreshDomain {
        private lateinit var entity: CommitEntity
        private lateinit var domain: Commit

        @BeforeEach
        fun setup() {
            val projectEntity = TestData.Entity.testProjectEntity(
                name = "TestProject",
                id = 1L,
                description = "A test project"
            )
            val project = TestData.Domain.testProject(
                name = projectEntity.name,
                id = projectEntity.id.toString(),
                description = projectEntity.description
            )

            val repositoryEntity = TestData.Entity.testRepositoryEntity(
                localPath = "TestRepository",
                id = 1L,
                project = projectEntity
            )
            val repositoryDomain = TestData.Domain.testRepository(
                localPath = repositoryEntity.localPath,
                id = repositoryEntity.id.toString(),
                project = project
            )

            this.entity = TestData.Entity.testCommitEntity(
                sha = "a".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repository = repositoryEntity,
                id = 1L
            )

            this.domain = TestData.Domain.testCommit(
                sha = "a".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repository = repositoryDomain,
                id = null
            )
        }

        @Test
        fun `refresh domain object, verify ctx was not called`() {
            commitMapper.refreshDomain(domain, entity)

            verify { ctx wasNot Called }

            confirmVerified(ctx)
        }

        @Test
        fun `refresh domain object, check that id is set`() {
            assertAll(
                { assertNull(domain.id) },
                { assertNotNull(entity.id) }
            )

            commitMapper.refreshDomain(domain, entity)

            assertAll(
                { assertNotNull(domain.id) },
                { assertThat(domain.id).isEqualTo(entity.id.toString()) },
            )
        }

        @Test
        fun `refresh domain object with same id, returns immediately without changes`() {
            domain.apply { id = entity.id.toString() }

            val result = commitMapper.refreshDomain(domain, entity)

            assertAll(
                { assertThat(result).isSameAs(domain) },
                { assertThat(result.id).isEqualTo(entity.id.toString()) }
            )
        }

        @Test
        fun `refresh domain object, check that nested objects remain unchanged`() {
            val originalRepoId = domain.repository.id
            val originalSha = domain.sha
            val originalMessage = domain.message

            commitMapper.refreshDomain(domain, entity)

            assertAll(
                { assertThat(domain.repository.id).isEqualTo(originalRepoId) },
                { assertThat(domain.sha).isEqualTo(originalSha) },
                { assertThat(domain.message).isEqualTo(originalMessage) },
                { assertThat(domain.parents).isEmpty() },
                { assertThat(domain.children).isEmpty() }
            )
        }
    }
}
