package com.inso_world.binocular.infrastructure.sql.unit.mapper

import com.inso_world.binocular.core.extension.reset
import com.inso_world.binocular.infrastructure.sql.TestData
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.unit.mapper.base.BaseMapperTest
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.vcs.ReferenceCategory
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
internal class BranchMapperTest : BaseMapperTest() {

    companion object {
        val IGNORED_FIELDS = RepositoryMapperTest.IGNORED_FIELDS.map { "repository.$it" } + listOf(
            "iid",
            "active",
            "tracksFileRenames",
            "files",
            "branch",
            "commits"
        ) + CommitMapperTest.IGNORED_FIELDS.map { "head.$it" }
    }

    @BeforeEach
    fun setup() {
        mockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntityKt")
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntityKt")
    }

    @Nested
    inner class ToEntity {
        private lateinit var repositoryEntity: RepositoryEntity
        private lateinit var repositoryDomain: Repository
        private lateinit var commitDomain: Commit
        private lateinit var commitEntity: CommitEntity

        private fun createBranch(name: String = "testBranch"): Branch =
            Branch(
                name = name,
                fullName = "refs/heads/$name",
                category = ReferenceCategory.LOCAL_BRANCH,
                repository = repositoryDomain,
                head = commitDomain,
            )

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
            commitDomain = TestData.Domain.testCommit(
                sha = "a".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repository = repositoryDomain,
                id = "1"
            )
            commitEntity = TestData.Entity.testCommitEntity(
                sha = commitDomain.sha,
                authorDateTime = commitDomain.authorDateTime,
                commitDateTime = commitDomain.commitDateTime,
                message = commitDomain.message,
                repository = repositoryEntity,
                id = commitDomain.id?.toLong()
            )
            ctx.remember(commitDomain, commitEntity)

            clearMocks(ctx, recordedCalls = true)
        }

        @Test
        fun `minimal valid example, check properties`() {
            val domain = createBranch()

            assertAll(
                "validate model",
                { assertThat(domain.commits).hasSize(1) },
                { assertThat(domain.commits).containsOnly(commitDomain) },
            )

            val entity = assertDoesNotThrow {
                branchMapper.toEntity(domain)
            }

            assertAll(
                "entity properties",
                { assertThat(entity.name).isEqualTo(domain.name) },
                { assertThat(entity.fullName).isEqualTo(domain.fullName) },
                { assertThat(entity.category).isEqualTo(domain.category) },
                { assertThat(entity.repository).isSameAs(repositoryEntity) },
                { assertThat(entity.head).isSameAs(commitEntity) },
                { assertThat(entity.iid).isNotSameAs(domain.iid) },
                { assertThat(entity.iid).isEqualTo(domain.iid) },
            )
        }

        @Test
        fun `minimal valid example, check equality`() {
            val branch = createBranch()

            val entity = assertDoesNotThrow {
                branchMapper.toEntity(branch)
            }

            assertThat(entity).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFields(*IGNORED_FIELDS.toTypedArray())
                .ignoringFieldsMatchingRegexes(".*id")
                .isEqualTo(branch)
        }

        @Test
        fun `minimal valid example, verify calls`() {
            val branch = createBranch()

            val entity = assertDoesNotThrow {
                branchMapper.toEntity(branch)
            }

            verifyOrder {
                ctx.findEntity<Branch.Key, Branch, BranchEntity>(branch)
                ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(repositoryDomain)
                ctx.findEntity<Commit.Key, Commit, CommitEntity>(commitDomain)
                ctx.remember(branch, entity)
            }

            confirmVerified(ctx)
        }

        @Test
        fun `map with id is null, should not fail`() {
            val branch = createBranch().apply { id = null }

            assertNull(branch.id)

            assertDoesNotThrow {
                branchMapper.toEntity(branch)
            }
        }

        @Test
        fun `clear context, expect IllegalStateException since Repository is not in context`() {
            ctx.reset()
            ctx.remember(commitDomain, commitEntity)

            val branch = createBranch()

            val ex = assertThrows<IllegalStateException> {
                branchMapper.toEntity(branch)
            }

            assertThat(ex.message).contains("RepositoryEntity must be mapped before BranchEntity.")
        }

        @Test
        fun `clear context, expect IllegalStateException since Commit is not in context`() {
            ctx.reset()
            ctx.remember(repositoryDomain, repositoryEntity)

            val branch = createBranch()

            val ex = assertThrows<IllegalStateException> {
                branchMapper.toEntity(branch)
            }

            assertThat(ex.message).contains("CommitEntity must be mapped before BranchEntity.")
        }

        @Test
        fun `fast-path when branch already in context, returns cached entity`() {
            val branch = createBranch()

            // First call: map the branch
            val entity1 = branchMapper.toEntity(branch)

            clearMocks(ctx, recordedCalls = true)

            // Second call: should return cached entity
            val entity2 = branchMapper.toEntity(branch)

            assertAll(
                { assertThat(entity2).isSameAs(entity1) },
                { verify(exactly = 1) { ctx.findEntity<Branch.Key, Branch, BranchEntity>(branch) } },
                { verify(exactly = 0) { ctx.remember(any<Branch>(), any<BranchEntity>()) } }
            )

            confirmVerified(ctx)
        }
    }

    @Nested
    inner class ToDomain {
        private lateinit var repositoryEntity: RepositoryEntity
        private lateinit var commitEntity: CommitEntity
        private lateinit var repositoryDomain: Repository
        private lateinit var commitDomain: Commit

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

            commitEntity = TestData.Entity.testCommitEntity(
                sha = "d".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "",
                repository = repositoryEntity,
                id = 1L
            )
            commitDomain = TestData.Domain.testCommit(
                sha = commitEntity.sha,
                authorDateTime = commitEntity.authorDateTime,
                commitDateTime = commitEntity.commitDateTime,
                message = commitEntity.message,
                repository = repositoryDomain,
                id = commitEntity.id.toString()
            )
            ctx.remember(commitDomain, commitEntity)

            clearMocks(ctx, recordedCalls = true) // ensure recorded calls are clear after setup
        }

        @Test
        fun `minimal valid example, check properties`() {
            val branchEntity = TestData.Entity.testBranchEntity(
                name = "testBranch",
                repository = repositoryEntity,
                head = commitEntity,
                id = 1
            )

            val domain = branchMapper.toDomain(branchEntity)

            assertAll(
                { assertThat(domain.name).isEqualTo(branchEntity.name) },
                { assertThat(domain.repository).isSameAs(repositoryDomain) },
                { assertThat(domain.head).isSameAs(commitDomain) },
                { assertThat(domain.commits).hasSize(1) },
                { assertThat(domain.commits).containsOnly(commitDomain) },
                { assertThat(domain.iid).isNotSameAs(branchEntity.iid) },
                { assertThat(domain.iid).isEqualTo(branchEntity.iid) },
            )
        }

        @Test
        fun `minimal valid example, check equality`() {
            val branchEntity = TestData.Entity.testBranchEntity(
                name = "testBranch",
                repository = repositoryEntity,
                head = commitEntity,
                id = 1
            )

            val domain = branchMapper.toDomain(branchEntity)
            assertThat(domain).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFields(*IGNORED_FIELDS.toTypedArray())
                .ignoringFieldsMatchingRegexes(".*id")
                .isEqualTo(branchEntity)
        }

        @Test
        fun `minimal valid example, verify calls`() {
            val branchEntity = TestData.Entity.testBranchEntity(
                name = "testBranch",
                repository = repositoryEntity,
                head = commitEntity,
                id = 1
            )

            val domain = branchMapper.toDomain(branchEntity)

            verifyOrder {
                ctx.findDomain<Branch, BranchEntity>(branchEntity)
                ctx.findDomain<Repository, RepositoryEntity>(branchEntity.repository)
                ctx.findDomain<Commit, CommitEntity>(branchEntity.head)
                ctx.remember(domain, branchEntity)
            }

            confirmVerified(ctx)
        }

        @Test
        fun `map with id is null, should not fail`() {
            val branchEntity = TestData.Entity.testBranchEntity(
                name = "testBranch",
                repository = repositoryEntity,
                head = commitEntity,
                id = null
            )

            assertNull(branchEntity.id)

            assertDoesNotThrow {
                branchMapper.toDomain(branchEntity)
            }
        }

        @Test
        fun `clear context, expect IllegalStateException since Repository is not in context`() {
            ctx.reset()

            val branchEntity = TestData.Entity.testBranchEntity(
                name = "testBranch",
                repository = repositoryEntity,
                head = commitEntity,
                id = 1
            )

            val ex = assertThrows<IllegalStateException> {
                branchMapper.toDomain(branchEntity)
            }

            assertThat(ex.message).contains("Repository must be mapped before Branch.")
        }

        @Test
        fun `clear context but keep Repository, expect IllegalStateException since Commit is not in context`() {
            ctx.reset()
            ctx.remember(repositoryDomain, repositoryEntity)

            val branchEntity = TestData.Entity.testBranchEntity(
                name = "testBranch",
                repository = repositoryEntity,
                head = commitEntity,
                id = 1
            )

            val ex = assertThrows<IllegalStateException> {
                branchMapper.toDomain(branchEntity)
            }

            assertThat(ex.message).contains("Commit must be mapped before Branch.")
        }

        @Test
        fun `fast-path when branch already in context, returns cached domain`() {
            val branchEntity = TestData.Entity.testBranchEntity(
                name = "testBranch",
                repository = repositoryEntity,
                head = commitEntity,
                id = 1
            )

            // First call: map the branch
            val domain1 = branchMapper.toDomain(branchEntity)

            clearMocks(ctx, recordedCalls = true)

            // Second call: should return cached domain
            val domain2 = branchMapper.toDomain(branchEntity)

            assertAll(
                { assertThat(domain2).isSameAs(domain1) },
                { verify(exactly = 1) { ctx.findDomain<Branch, BranchEntity>(branchEntity) } },
                { verify(exactly = 0) { ctx.remember(any<Branch>(), any<BranchEntity>()) } }
            )

            confirmVerified(ctx)
        }
    }

    @Nested
    inner class RefreshDomain {
        private lateinit var entity: BranchEntity
        private lateinit var domain: Branch

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

            val commitEntity = TestData.Entity.testCommitEntity(
                sha = "d".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "",
                repository = repositoryEntity,
                id = 1L
            )
            val commitDomain = TestData.Domain.testCommit(
                sha = commitEntity.sha,
                authorDateTime = commitEntity.authorDateTime,
                commitDateTime = commitEntity.commitDateTime,
                message = commitEntity.message,
                repository = repositoryDomain,
                id = commitEntity.id.toString()
            )

            this.entity = TestData.Entity.testBranchEntity(
                name = "testBranch",
                repository = repositoryEntity,
                head = commitEntity,
                id = 1L
            )

            this.domain = TestData.Domain.testBranch(
                name = "testBranch",
                repository = repositoryDomain,
                head = commitDomain,
                id = null
            )
        }

        @Test
        fun `refresh domain object, verify mapper and ctx were not called`() {
            branchMapper.refreshDomain(domain, entity)

            verify { ctx wasNot Called }
            verify { commitMapper wasNot Called }
            verify { repositoryMapper wasNot Called }

            confirmVerified(ctx)
            confirmVerified(commitMapper)
            confirmVerified(repositoryMapper)
        }

        @Test
        fun `refresh domain object, check that id is set`() {
            assertAll(
                { assertNull(domain.id) },
                { assertNotNull(entity.id) }
            )

            branchMapper.refreshDomain(domain, entity)

            assertAll(
                { assertNotNull(domain.id) },
                { assertThat(domain.id).isEqualTo(entity.id.toString()) },
            )
        }

        @Test
        fun `refresh domain object, check that nested ids remain unchanged`() {
            val originalHeadId = domain.head.id
            val originalRepoId = domain.repository.id

            branchMapper.refreshDomain(domain, entity)

            assertAll(
                { assertThat(domain.head.id).isEqualTo(originalHeadId) },
                { assertThat(domain.repository.id).isEqualTo(originalRepoId) }
            )
        }
    }
}
