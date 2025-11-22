package com.inso_world.binocular.infrastructure.sql.unit.mapper

import com.inso_world.binocular.core.extension.reset
import com.inso_world.binocular.infrastructure.sql.TestData
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import com.inso_world.binocular.infrastructure.sql.unit.mapper.base.BaseMapperTest
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
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
internal class UserMapperTest : BaseMapperTest() {

    companion object {
        val IGNORED_FIELDS = RepositoryMapperTest.IGNORED_FIELDS.map { "repository.$it" } +
                CommitMapperTest.IGNORED_FIELDS.map { "authoredCommits.$it" } +
                CommitMapperTest.IGNORED_FIELDS.map { "committedCommits.$it" } +
                listOf(
                    "iid",
                    "issues",
                    "files",
                    "gitSignature",
                    "repository.user.iid",
                    "repository.user.issues",
                    "repository.user.files",
                    "repository.user.gitSignature"
                )
    }

    @BeforeEach
    fun setup() {
        mockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntityKt")
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntityKt")
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
            val domain = TestData.Domain.testUser(
                name = "John Doe",
                email = "john@example.com",
                repository = repositoryDomain,
                id = null
            )

            val entity = assertDoesNotThrow {
                userMapper.toEntity(domain)
            }

            assertAll(
                "entity properties",
                { assertThat(entity.name).isEqualTo(domain.name) },
                { assertThat(entity.email).isEqualTo(domain.email) },
                { assertThat(entity.repository).isSameAs(repositoryEntity) },
                { assertThat(entity.iid).isNotSameAs(domain.iid) },
                { assertThat(entity.iid).isEqualTo(domain.iid) },
            )

        }
        @Test
        fun `minimal valid example, check equality`() {
            val user = TestData.Domain.testUser(
                name = "John Doe",
                email = "john@example.com",
                repository = repositoryDomain,
                id = null
            )

            val entity = assertDoesNotThrow {
                userMapper.toEntity(user)
            }

            assertThat(entity).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFields(*IGNORED_FIELDS.toTypedArray())
                .ignoringFieldsMatchingRegexes(".*id")
                .isEqualTo(user)
        }

        @Test
        fun `minimal valid example, verify calls`() {
            val user = TestData.Domain.testUser(
                name = "John Doe",
                email = "john@example.com",
                repository = repositoryDomain,
                id = null
            )

            val entity = assertDoesNotThrow {
                userMapper.toEntity(user)
            }

            verifyOrder {
                ctx.findEntity<User.Key, User, UserEntity>(user)
                ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(repositoryDomain)
                ctx.remember(user, entity)
            }

            confirmVerified(ctx)
        }

        @Test
        fun `map with id is null, should not fail`() {
            val user = TestData.Domain.testUser(
                name = "John Doe",
                email = "john@example.com",
                repository = repositoryDomain,
                id = null
            )

            assertNull(user.id)

            assertDoesNotThrow {
                userMapper.toEntity(user)
            }
        }

        @Test
        fun `clear context, expect IllegalStateException since Repository is not in context`() {
            ctx.reset()

            val user = TestData.Domain.testUser(
                name = "John Doe",
                email = "john@example.com",
                repository = repositoryDomain,
                id = null
            )

            val ex = assertThrows<IllegalStateException> {
                userMapper.toEntity(user)
            }

            assertThat(ex.message).contains("RepositoryEntity must be mapped before UserEntity.")
        }

        @Test
        fun `fast-path when user already in context, returns cached entity`() {
            val user = TestData.Domain.testUser(
                name = "John Doe",
                email = "john@example.com",
                repository = repositoryDomain,
                id = null
            )

            // First call: map the user
            val entity1 = userMapper.toEntity(user)

            clearMocks(ctx, recordedCalls = true)

            // Second call: should return cached entity
            val entity2 = userMapper.toEntity(user)

            assertAll(
                { assertThat(entity2).isSameAs(entity1) },
                { verify(exactly = 1) { ctx.findEntity<User.Key, User, UserEntity>(user) } },
                { verify(exactly = 0) { ctx.remember(any<User>(), any<UserEntity>()) } }
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

            clearMocks(ctx, recordedCalls = true) // ensure recorded calls are clear after setup
        }

        @Test
        fun `minimal valid example, check properties`() {
            val userEntity = TestData.Entity.testUserEntity(
                name = "Jane Doe",
                email = "jane@example.com",
                repository = repositoryEntity,
                id = 1L
            )

            val domain = userMapper.toDomain(userEntity)

            assertAll(
                { assertThat(domain.name).isEqualTo(userEntity.name) },
                { assertThat(domain.email).isEqualTo(userEntity.email) },
                { assertThat(domain.repository).isSameAs(repositoryDomain) },
                { assertThat(domain.iid).isNotSameAs(userEntity.iid) },
                { assertThat(domain.iid).isEqualTo(userEntity.iid) },
            )
        }

        @Test
        fun `minimal valid example, check equality`() {
            val userEntity = TestData.Entity.testUserEntity(
                name = "Jane Doe",
                email = "jane@example.com",
                repository = repositoryEntity,
                id = 1L
            )

            val domain = userMapper.toDomain(userEntity)
            assertThat(domain).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFields(*IGNORED_FIELDS.toTypedArray())
                .ignoringFieldsMatchingRegexes(".*id")
                .isEqualTo(userEntity)
        }

        @Test
        fun `minimal valid example, verify calls`() {
            val userEntity = TestData.Entity.testUserEntity(
                name = "Jane Doe",
                email = "jane@example.com",
                repository = repositoryEntity,
                id = 1L
            )

            val domain = userMapper.toDomain(userEntity)

            verifyOrder {
                ctx.findDomain<User, UserEntity>(userEntity)
                ctx.findDomain<Repository, RepositoryEntity>(userEntity.repository)
                ctx.remember(domain, userEntity)
            }

            confirmVerified(ctx)
        }

        @Test
        fun `map with id is null, should not fail`() {
            val userEntity = TestData.Entity.testUserEntity(
                name = "Jane Doe",
                email = "jane@example.com",
                repository = repositoryEntity,
                id = null
            )

            assertNull(userEntity.id)

            assertDoesNotThrow {
                userMapper.toDomain(userEntity)
            }
        }

        @Test
        fun `clear context, expect IllegalStateException since Repository is not in context`() {
            ctx.reset()

            val userEntity = TestData.Entity.testUserEntity(
                name = "Jane Doe",
                email = "jane@example.com",
                repository = repositoryEntity,
                id = 1L
            )

            val ex = assertThrows<IllegalStateException> {
                userMapper.toDomain(userEntity)
            }

            assertThat(ex.message).contains("Repository must be mapped before User.")
        }

        @Test
        fun `fast-path when user already in context, returns cached domain`() {
            val userEntity = TestData.Entity.testUserEntity(
                name = "Jane Doe",
                email = "jane@example.com",
                repository = repositoryEntity,
                id = 1L
            )

            // First call: map the user
            val domain1 = userMapper.toDomain(userEntity)

            clearMocks(ctx, recordedCalls = true)

            // Second call: should return cached domain
            val domain2 = userMapper.toDomain(userEntity)

            assertAll(
                { assertThat(domain2).isSameAs(domain1) },
                { verify(exactly = 1) { ctx.findDomain<User, UserEntity>(userEntity) } },
                { verify(exactly = 0) { ctx.remember(any<User>(), any<UserEntity>()) } }
            )

            confirmVerified(ctx)
        }
    }

    @Nested
    inner class RefreshDomain {
        private lateinit var entity: UserEntity
        private lateinit var domain: User

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

            this.entity = TestData.Entity.testUserEntity(
                name = "John Doe",
                email = "john@example.com",
                repository = repositoryEntity,
                id = 1L
            )

            this.domain = TestData.Domain.testUser(
                name = "John Doe",
                email = "john@example.com",
                repository = repositoryDomain,
                id = null
            )
        }

        @Test
        fun `refresh domain object, verify ctx was not called`() {
            userMapper.refreshDomain(domain, entity)

            verify { ctx wasNot Called }

            confirmVerified(ctx)
        }

        @Test
        fun `refresh domain object, check that id is set`() {
            assertAll(
                { assertNull(domain.id) },
                { assertNotNull(entity.id) }
            )

            userMapper.refreshDomain(domain, entity)

            assertAll(
                { assertNotNull(domain.id) },
                { assertThat(domain.id).isEqualTo(entity.id.toString()) },
            )
        }

        @Test
        fun `refresh domain object with same id, returns immediately without changes`() {
            domain.apply { id = entity.id.toString() }

            val result = userMapper.refreshDomain(domain, entity)

            assertAll(
                { assertThat(result).isSameAs(domain) },
                { assertThat(result.id).isEqualTo(entity.id.toString()) }
            )
        }

        @Test
        fun `refresh domain object, check that nested objects remain unchanged`() {
            val originalRepoId = domain.repository.id
            val originalName = domain.name
            val originalEmail = domain.email

            userMapper.refreshDomain(domain, entity)

            assertAll(
                { assertThat(domain.repository.id).isEqualTo(originalRepoId) },
                { assertThat(domain.name).isEqualTo(originalName) },
                { assertThat(domain.email).isEqualTo(originalEmail) },
                { assertThat(domain.committedCommits).isEmpty() },
                { assertThat(domain.authoredCommits).isEmpty() }
            )
        }
    }
}
