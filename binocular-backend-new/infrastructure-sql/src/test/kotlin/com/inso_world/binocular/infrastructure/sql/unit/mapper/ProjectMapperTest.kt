package com.inso_world.binocular.infrastructure.sql.unit.mapper

import com.inso_world.binocular.data.MockTestDataProvider
import com.inso_world.binocular.infrastructure.sql.TestData
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.toEntity
import com.inso_world.binocular.infrastructure.sql.unit.mapper.ProjectMapperTest.Companion.IGNORED_FIELDS
import com.inso_world.binocular.infrastructure.sql.unit.mapper.base.BaseMapperTest
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import io.mockk.confirmVerified
import io.mockk.mockkStatic
import io.mockk.spyk
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
import java.util.function.BiPredicate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
internal class ProjectMapperTest : BaseMapperTest() {

    companion object {
        val IGNORED_FIELDS = listOf(
            "id", "issues"
        )

        /**
         * Configures recursive comparison for Project domain-entity equality checks.
         *
         * ### Configuration
         * - Ignores collection order for flexible list/set comparisons
         * - Uses custom equality for [Project.Id] value class (compares wrapped UUID values)
         * - Ignores fields: [IGNORED_FIELDS] (id, issues)
         *
         * ### Usage
         * ```kotlin
         * assertThat(domain)
         *     .usingRecursiveComparisonForProject()
         *     .isEqualTo(entity)
         * ```
         *
         * @receiver The AssertJ ObjectAssert for a Project domain object
         * @return Configured RecursiveComparisonAssert ready for `.isEqualTo(entity)`
         */
        fun org.assertj.core.api.ObjectAssert<Project>.usingRecursiveComparisonForProject() =
            this.usingRecursiveComparison()
                .ignoringCollectionOrder()
                .withEqualsForFields(
                    BiPredicate<Any?, Any?> { actual, expected ->
                        when (actual) {
                            is Project.Id if expected is Project.Id -> actual.value == expected.value
                            is Project.Id if expected is Uuid -> actual.value == expected
                            is Uuid if expected is Project.Id -> actual == expected.value
                            else -> actual == expected
                        }
                    },
                    "iid"
                )
                .ignoringFields(*IGNORED_FIELDS.toTypedArray())

    }

    @BeforeEach
    fun setup() {
        // edit fields before spying on super properties
        projectMapper = spyk(super.projectMapper)
    }

    @Nested
    inner class ToEntity {
        private lateinit var mockTestDataProvider: MockTestDataProvider

        @BeforeEach
        fun setup() {
            mockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntityKt")
            mockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntityKt")
            mockTestDataProvider = MockTestDataProvider(
                Repository(localPath = "./", project = TestData.Domain.testProject())
            )
        }

        @AfterEach
        fun tearDown() {
            unmockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntityKt")
            unmockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntityKt")
        }

        @Test
        fun `map with id is null, should not fail`() {
            val domain = TestData.Domain.testProject(id = null)

            assertAll(
                { assertNull(domain.id) },
                { assertDoesNotThrow { projectMapper.toEntity(domain) } }
            )
        }

        @Test
        fun `map with id containing whitespace, should trim and parse correctly`() {
            val domain = TestData.Domain.testProject(id = "  42  ")

            val entity = projectMapper.toEntity(domain)

            assertThat(entity.id).isEqualTo(42L)
        }

        @Test
        fun `map with id as empty string, should result in null entity id`() {
            val domain = TestData.Domain.testProject(id = "")

            val entity = projectMapper.toEntity(domain)

            assertThat(entity.id).isNull()
        }

        @Test
        fun `map with id as whitespace only, should result in null entity id`() {
            val domain = TestData.Domain.testProject(id = "   ")

            val entity = projectMapper.toEntity(domain)

            assertThat(entity.id).isNull()
        }

        @Test
        fun `map with id as non-numeric string, should result in null entity id`() {
            val domain = TestData.Domain.testProject(id = "not-a-number")

            val entity = projectMapper.toEntity(domain)

            assertThat(entity.id).isNull()
        }

        @Test
        fun `map without description, should have null description in entity`() {
            val domain = TestData.Domain.testProject(id = "1", description = null)

            val entity = projectMapper.toEntity(domain)

            assertAll(
                { assertThat(entity.name).isEqualTo(domain.name) },
                { assertThat(entity.description).isNull() }
            )
        }

        @Test
        fun `map same domain twice, should return cached entity from context`() {
            val domain = TestData.Domain.testProject(id = "1")

            val entity1 = projectMapper.toEntity(domain)
            val entity2 = projectMapper.toEntity(domain)

            assertAll(
                { assertThat(entity1).isSameAs(entity2) },
                { verify(exactly = 2) { ctx.findEntity<Project.Key, Project, ProjectEntity>(domain) } },
                { verify(exactly = 1) { ctx.remember(domain, entity1) } },
                { verify(exactly = 1) { domain.toEntity() } }
            )
        }

        @Test
        fun `minimal valid example, verify calls`() {
            val domain = TestData.Domain.testProject(id = "1")

            val entity = projectMapper.toEntity(domain)

            assertAll(
                { assertThat(entity.id).isEqualTo(domain.id?.toLong()) },
                { assertThat(entity.name).isEqualTo(domain.name) },
                { assertThat(entity.description).isEqualTo(domain.description) },
                { assertThat(entity.repo).isNull() }
            )

            verify(exactly = 1) { ctx.findEntity<Project.Key, Project, ProjectEntity>(domain) }
            verify(exactly = 1) { ctx.remember(domain, entity) }
            verify(exactly = 0) { ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(ofType<Repository>()) }
            verify(exactly = 1) { domain.toEntity() }

            confirmVerified(ctx)
        }

        @Test
        fun `map project with repository`() {
            val domain = TestData.Domain.testProject(id = "1").apply {
                repo = Repository(localPath = "./", project = this)
            }

            val entity = projectMapper.toEntity(domain)

            assertThat(entity.name).isEqualTo(domain.name)
            assertThat(entity.repo).isNull()
        }

        @Test
        fun `map project with repository, check equality`() {
            val domain = TestData.Domain.testProject(id = "1").apply {
                repo = Repository(localPath = "./", project = this)
            }

            val entity = projectMapper.toEntity(domain)

            assertThat(entity).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFieldsMatchingRegexes(".*id")
                .ignoringActualNullFields()
                .isEqualTo(domain)
        }

        @Test
        fun `map project with repository, check iid is equal`() {
            val domain = TestData.Domain.testProject(id = "1").apply {
                repo = Repository(localPath = "./", project = this)
            }

            val entity = projectMapper.toEntity(domain)

            assertAll(
                { assertThat(domain.iid).isEqualTo(entity.iid) },
                { assertThat(domain.iid).isNotSameAs(entity.iid) }
            )
        }

        @Test
        fun `map project with repository, verify calls`() {
            val domain = TestData.Domain.testProject(id = "1").apply {
                repo = Repository(localPath = "./", project = this)
            }

            val entity = projectMapper.toEntity(domain)

            verifyOrder {
                ctx.findEntity<Project.Key, Project, ProjectEntity>(domain)
                domain.toEntity()
                ctx.remember(domain, entity)
            }

            confirmVerified(ctx)
        }
    }

    @Nested
    inner class ToDomain {
        private lateinit var mockTestDataProvider: MockTestDataProvider

        @BeforeEach
        fun setup() {
            mockTestDataProvider = MockTestDataProvider(
                Repository(localPath = "./", project = TestData.Domain.testProject())
            )
        }

        @Test
        fun `map with id is null, should not fail`() {
            val entity = TestData.Entity.testProjectEntity(id = null)

            assertAll(
                { assertNull(entity.id) },
                { assertDoesNotThrow { projectMapper.toDomain(entity) } }
            )
        }

        @Test
        fun `map with id is null, domain id should be null`() {
            val entity = TestData.Entity.testProjectEntity(id = null)

            val domain = projectMapper.toDomain(entity)

            assertThat(domain.id).isNull()
        }

        @Test
        fun `map without description, domain description should be null`() {
            val entity = TestData.Entity.testProjectEntity(description = null)

            val domain = projectMapper.toDomain(entity)

            assertAll(
                { assertThat(domain.name).isEqualTo(entity.name) },
                { assertThat(domain.description).isNull() }
            )
        }

        @Test
        fun `map same entity twice, should return cached domain from context`() {
            val entity = spyk(TestData.Entity.testProjectEntity())

            val domain1 = projectMapper.toDomain(entity)
            val domain2 = projectMapper.toDomain(entity)

            assertAll(
                { assertThat(domain1).isSameAs(domain2) },
                { verify(exactly = 2) { ctx.findDomain<Project, ProjectEntity>(entity) } },
                { verify(exactly = 1) { ctx.remember(domain1, entity) } },
                { verify(exactly = 1) { entity.toDomain() } }
            )

            confirmVerified(ctx)
        }

        @Test
        fun `map entity with id as Long MAX_VALUE, should convert correctly`() {
            val entity = TestData.Entity.testProjectEntity(id = Long.MAX_VALUE)

            val domain = projectMapper.toDomain(entity)

            assertThat(domain.id).isEqualTo(Long.MAX_VALUE.toString())
        }

        @Test
        fun `map entity, iid should be set correctly via reflection`() {
            val expectedIid = Project.Id(Uuid.random())
            val entity = TestData.Entity.testProjectEntity(iid = expectedIid)

            val domain = projectMapper.toDomain(entity)

            assertAll(
                { assertThat(domain.iid).isEqualTo(expectedIid) },
                { assertThat(domain.iid.value).isEqualTo(expectedIid.value) }
            )
        }

        @Test
        fun `minimal valid example without repository, check equality`() {
            val entity = TestData.Entity.testProjectEntity()

            val domain = projectMapper.toDomain(entity)

            assertThat(domain)
                .usingRecursiveComparisonForProject()
                .isEqualTo(entity)
        }

        @Test
        fun `minimal valid example, verify calls`() {
            val entity = spyk(TestData.Entity.testProjectEntity())

            val domain = projectMapper.toDomain(entity)

            verify(exactly = 1) { ctx.findDomain<Project, ProjectEntity>(entity) }
            verify(exactly = 1) { ctx.remember(domain, entity) }
            verify(exactly = 0) { ctx.findDomain<Repository, RepositoryEntity>(ofType<RepositoryEntity>()) }
            verify(exactly = 1) { entity.toDomain() }

            confirmVerified(ctx)
        }

        @Test
        fun `minimal valid example, check equality`() {
            val entity = TestData.Entity.testProjectEntity()

            val domain = projectMapper.toDomain(entity)

            assertThat(domain)
                .usingRecursiveComparisonForProject()
                .isEqualTo(entity)
        }

        @Test
        fun `map project with repository, verify calls`() {
            // Arrange: real instances wrapped in spies
            val entity = ProjectEntity(name = "test project", iid = Project.Id(Uuid.random())).apply { id = 1L }
            run {
                val repoEntity =
                    RepositoryEntity(
                        localPath = "./",
                        project = entity,
                        iid = Repository.Id(Uuid.random())
                    ).apply { id = 1L }
                assertThat(entity.repo).isSameAs(repoEntity)
            }

            val domain = projectMapper.toDomain(entity)

            // Verify
            verifyOrder {
                ctx.findDomain<Project, ProjectEntity>(entity)
                ctx.remember(domain, entity)
            }

            confirmVerified(ctx)
        }

        @Test
        fun `map project with repository`() {
            val entity =
                ProjectEntity(
                    name = "test project",
                    iid = Project.Id(Uuid.random())
                ).apply {
                    id = 1
                    repo = RepositoryEntity(localPath = "./", project = this, iid = Repository.Id(Uuid.random()))
                }

            val domain = projectMapper.toDomain(entity)

            assertThat(domain.name).isEqualTo(entity.name)
            assertThat(domain.repo).isNull()
        }

        @Test
        fun `map project with repository, check equality`() {
            val entity =
                ProjectEntity(
                    name = "test project",
                    iid = Project.Id(Uuid.random())
                ).apply {
                    id = 1
                    repo = RepositoryEntity(localPath = "./", project = this, iid = Repository.Id(Uuid.random()))
                }

            val domain = projectMapper.toDomain(entity)

            assertThat(domain.repo).isNull()
            assertThat(domain)
                .usingRecursiveComparisonForProject()
                .ignoringActualNullFields() // domain.repo is null from mapper
                .isEqualTo(entity)
        }

        @Test
        fun `map project with repository, check iid is equal`() {
            val entity =
                ProjectEntity(
                    name = "test project",
                    iid = Project.Id(Uuid.random())
                ).apply {
                    id = 1
                    repo = RepositoryEntity(localPath = "./", project = this, iid = Repository.Id(Uuid.random()))
                }

            val domain = projectMapper.toDomain(entity)

            assertAll(
                { assertThat(entity.iid).isEqualTo(domain.iid) },
                { assertThat(entity.iid).isNotSameAs(domain.iid) }
            )
        }
    }

    @Nested
    inner class RefreshDomain {
        private lateinit var entity: ProjectEntity
        private lateinit var domain: Project

        @BeforeEach
        fun setup() {
            entity = TestData.Entity.testProjectEntity()
            domain = TestData.Domain.testProject(id = null)
        }

        @Test
        fun `refresh domain object, check that id is set`() {
            assertAll(
                { assertNull(domain.id) },
                { assertNotNull(entity.id) }
            )

            projectMapper.refreshDomain(domain, entity)

            assertAll(
                { assertNotNull(domain.id) },
                { assertThat(domain.id).isEqualTo(entity.id.toString()) },
            )
        }

        @Test
        fun `refresh domain with entity id null, domain id should remain null`() {
            entity = TestData.Entity.testProjectEntity(id = null)
            domain = TestData.Domain.testProject(id = "42")

            assertAll(
                { assertThat(domain.id).isEqualTo("42") },
                { assertNull(entity.id) }
            )

            projectMapper.refreshDomain(domain, entity)

            assertThat(domain.id).isNull()
        }

        @Test
        fun `refresh domain with large entity id, should convert correctly`() {
            entity = TestData.Entity.testProjectEntity(id = Long.MAX_VALUE)
            domain = TestData.Domain.testProject(id = null)

            projectMapper.refreshDomain(domain, entity)

            assertThat(domain.id).isEqualTo(Long.MAX_VALUE.toString())
        }

        @Test
        fun `refresh domain with zero entity id, should set domain id to zero string`() {
            entity = TestData.Entity.testProjectEntity(id = 0L)
            domain = TestData.Domain.testProject(id = null)

            projectMapper.refreshDomain(domain, entity)

            assertThat(domain.id).isEqualTo("0")
        }

        @Test
        fun `refresh domain overwrites existing domain id`() {
            entity = TestData.Entity.testProjectEntity(id = 100L)
            domain = TestData.Domain.testProject(id = "42")

            assertThat(domain.id).isEqualTo("42")

            projectMapper.refreshDomain(domain, entity)

            assertThat(domain.id).isEqualTo("100")
        }

        @Test
        fun `refresh domain preserves other domain fields`() {
            val originalName = domain.name
            val originalDescription = domain.description

            projectMapper.refreshDomain(domain, entity)

            assertAll(
                { assertThat(domain.name).isEqualTo(originalName) },
                { assertThat(domain.description).isEqualTo(originalDescription) },
                { assertThat(domain.id).isEqualTo(entity.id.toString()) }
            )
        }
    }

}
