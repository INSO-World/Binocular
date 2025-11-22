package com.inso_world.binocular.infrastructure.sql.unit.mapper

import com.inso_world.binocular.core.extension.reset
import com.inso_world.binocular.data.MockTestDataProvider
import com.inso_world.binocular.infrastructure.sql.TestData
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.unit.mapper.base.BaseMapperTest
import com.inso_world.binocular.model.NonRemovingMutableSet
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Reference
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
import org.junit.jupiter.params.provider.Arguments
import java.time.LocalDateTime
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal class RepositoryMapperTest : BaseMapperTest() {

    private lateinit var projectEntity: ProjectEntity

    private lateinit var mockTestDataProvider: MockTestDataProvider

    @BeforeEach
    fun setup() {
        mockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntityKt")
        mockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntityKt")
        this.projectEntity =
            ProjectEntity(
                name = "TestProject",
                iid = Project.Id(Uuid.random())
            ).apply {
                id = 1L
                description = "A test project"
            }
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntityKt")
        unmockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntityKt")
    }

    companion object {
        val IGNORED_FIELDS = ProjectMapperTest.IGNORED_FIELDS.map { "project.$it" } + listOf(
            "issues",
            "commits",
            "branches",
            "user",
            "project.repo.issues",
            "project.repo.commits",
            "project.repo.branches",
            "project.repo.user",
        )

        @JvmStatic
        fun commitList(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    listOf(
                        run {
                            val repository = TestData.Domain.testRepository()

                            val cmt = TestData.Domain.testCommit(
                                sha = "a".repeat(40),
                                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                                message = "Valid commit 1",
                                repository = repository
                            ).apply {
                                this.id = "1"
                            }
//                                val branch = branchModel(cmt, repository)
                            cmt.parents.add(
                                TestData.Domain.testCommit(
                                    sha = "b".repeat(40),
                                    authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                    commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                    message = "Valid commit 2",
                                    repository = repository
                                ).apply {
                                    this.id = "2"
                                }
                            )
                            cmt
                        },
                        run {
                            val repository = TestData.Domain.testRepository()
                            val cmt = TestData.Domain.testCommit(
                                sha = "b".repeat(40),
                                authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                message = "Valid commit 2",
                                repository = repository
                            ).apply {
                                this.id = "2"
                            }
//                                this.commits.add(cmt)
                            cmt
                        },
                    ),
                    2,
                ),
                Arguments.of(
                    listOf(
                        run {
                            val repository = TestData.Domain.testRepository()
                            val cmt = TestData.Domain.testCommit(
                                sha = "a".repeat(40),
                                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                                message = "Valid commit 1",
                                repository = repository
                            ).apply {
                                this.id = "3"
                            }
//                                this.head = cmt
                            cmt.parents.add(
                                TestData.Domain.testCommit(
                                    sha = "b".repeat(40),
                                    authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                    commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                    message = "Valid commit 2",
                                    repository = repository
                                ).apply {
                                    this.id = "4"
                                }
                            )
                            cmt
                        },
                        run {
                            val repository = TestData.Domain.testRepository()
                            val cmt = TestData.Domain.testCommit(
                                sha = "c".repeat(40),
                                authorDateTime = LocalDateTime.of(2020, 1, 4, 1, 0, 0, 0),
                                commitDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                message = "Valid commit 3",
                                repository = repository
                            ).apply {
                                this.id = "5"
                            }
//                                this.head = cmt
                            cmt
                        },
                    ),
                    3,
                ),
                Arguments.of(
                    listOf(
                        run {
                            val repository = TestData.Domain.testRepository()
                            val cmt = TestData.Domain.testCommit(
                                sha = "a".repeat(40),
                                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                                message = "Valid commit 1",
                                repository = repository
                            ).apply {
                                this.id = "6"
                            }
//                                this.head = cmt
                            cmt.parents.add(
                                TestData.Domain.testCommit(
                                    sha = "b".repeat(40),
                                    authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                    commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                    message = "Valid commit 2",
                                    repository = repository
                                ).apply {
                                    this.id = "7"
                                }
                            )
                            cmt
                        },
                        run {
                            val repository = TestData.Domain.testRepository()
                            val cmt = TestData.Domain.testCommit(
                                sha = "c".repeat(40),
                                authorDateTime = LocalDateTime.of(2020, 1, 4, 1, 0, 0, 0),
                                commitDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                message = "Valid commit 3",
                                repository = repository
                            ).apply {
                                this.id = "8"
                            }
//                                this.head = cmt
                            cmt.parents.add(
                                TestData.Domain.testCommit(
                                    sha = "b".repeat(40),
                                    authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                    commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                    message = "Valid commit 2",
                                    repository = repository
                                ).apply {
                                    this.id = "7"
                                }
                            )
                            cmt
                        },
                    ),
                    3,
                ),
                Arguments.of(
                    listOf(
                        run {
                            val repository = TestData.Domain.testRepository()
                            val cmt = TestData.Domain.testCommit(
                                sha = "a".repeat(40),
                                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                                message = "Valid commit 1",
                                repository = repository
                            ).apply {
                                this.id = "9"
                            }
//                                this.head = cmt
                            cmt.parents.add(
                                TestData.Domain.testCommit(
                                    sha = "b".repeat(40),
                                    authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                    commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                                    message = "Valid commit 2",
                                    repository = repository
                                ).apply {
                                    this.id = "10"
                                }
                            )
                            cmt
                        },
                        run {
                            val repository = TestData.Domain.testRepository()
                            val cmt = TestData.Domain.testCommit(
                                sha = "c".repeat(40),
                                authorDateTime = LocalDateTime.of(2020, 1, 4, 1, 0, 0, 0),
                                commitDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
                                message = "Valid commit 3",
                                repository = repository
                            ).apply {
                                this.id = "11"
                            }
//                                this.head = cmt
                            cmt.parents.add(
                                TestData.Domain.testCommit(
                                    sha = "d".repeat(40),
                                    authorDateTime = LocalDateTime.of(2020, 1, 5, 1, 0, 0, 0),
                                    commitDateTime = LocalDateTime.of(2020, 1, 4, 1, 0, 0, 0),
                                    message = "Valid commit 4",
                                    repository = repository
                                ).apply {
                                    this.id = "12"
                                }
                            )
                            cmt
                        },
                    ),
                    4,
                ),
            )
    }

    @Nested
    inner class ToEntity {

        private lateinit var domain: Repository

        @BeforeEach
        fun setUp() {
            // Create a minimal valid Repository domain object with matching project from parent setup
            val project = TestData.Domain.testProject(
                name = projectEntity.name,
                id = projectEntity.id?.toString(),
                description = projectEntity.description
            )
            domain = TestData.Domain.testRepository(
                localPath = "TestRepo",
                id = "10",
                project = project
            )
            val projectEntity = TestData.Entity.testProjectEntity(
                name = domain.project.name,
                id = domain.project.id?.toLong(),
                description = domain.project.description
            )
            ctx.remember(domain.project, projectEntity)
            clearMocks(ctx, recordedCalls = true) // ensure recorded calls are clear after setup
        }

        @Test
        fun `map with commits, no branch`() {
            val commit = TestData.Domain.testCommit(
                sha = "a".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repository = domain,
                id = "1"
            )

            domain.commits.add(commit)

            // Map to entity
            val entity = repositoryMapper.toEntity(domain)

            assertAll(
                { assertThat(entity.user).hasSize(0) },
                { assertThat(entity.commits).hasSize(0) },
                { assertThat(entity.branches).hasSize(0) }
            )
        }

        @Test
        fun `map minimal valid repository, check properties only`() {
            // Map to entity
            val entity = repositoryMapper.toEntity(domain)

            // Assert mapping
            assertAll(
                { assertEquals(entity.localPath, domain.localPath) },
                { assertThat(entity.project).isNotNull() },
                { assertThat(entity.project.repo).isSameAs(entity) },
                { assertThat(entity.iid).isNotSameAs(domain.iid) },
                { assertThat(entity.iid).isEqualTo(domain.iid) }
            )
        }

        @Test
        fun `map minimal valid repository, check equality`() {
            // Map to entity
            val entity = repositoryMapper.toEntity(domain)

            // Assert mapping
            assertThat(entity).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFieldsMatchingRegexes(".*id")
                .isEqualTo(domain)
        }

        @Test
        fun `map minimal valid repository, verify calls`() {
            // Map to entity
            val entity = repositoryMapper.toEntity(domain)

            verifyOrder {
                ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(domain)
                with(domain.project) {
                    ctx.findEntity<Project.Key, Project, ProjectEntity>(this)
                }
                with(domain) {
                    ctx.remember(this, entity)
                }
            }

            confirmVerified(ctx)
        }

        @Test
        fun `map with one commits and branch no relationships, verify calls`() {
            mockTestDataProvider = MockTestDataProvider(domain)
            val commit = with(mockTestDataProvider) {
                (domain.commits as NonRemovingMutableSet<*>).reset()
                (domain.branches as NonRemovingMutableSet<*>).reset()
                val commit = this.commitBySha.getValue("a".repeat(40))
                domain.commits.add(commit)

                with(this.branchByName.getValue("origin/feature/test")) {
                    this.head = commit
                    domain.branches.add(this)
//                    this
                }
                commit
            }
            assertAll(
                "check model",
                { assertThat(domain.commits).hasSize(1) },
                { assertThat(domain.branches).hasSize(1) }
            )

            // Map to entity
            val entity = repositoryMapper.toEntity(domain)

            // Assert mapping
            assertAll(
                { assertEquals(entity.localPath, domain.localPath) },
                { assertThat(entity.commits).hasSize(0) },// commits are not mapped, done by assembler
            )

            verifyOrder {
                ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(domain)
                ctx.findEntity<Project.Key, Project, ProjectEntity>(domain.project)
                ctx.remember(domain, entity)
            }

            confirmVerified(ctx)
        }

        @Test
        fun `map with id is null, should not fail`() {
            domain.apply { id = null }
            assertNull(domain.id)

            assertDoesNotThrow {
                repositoryMapper.toEntity(domain)
            }
        }

        @Test
        fun `map with id containing whitespace, should trim and parse correctly`() {
            domain.apply { id = "  42  " }

            val entity = repositoryMapper.toEntity(domain)

            assertThat(entity.id).isEqualTo(42L)
        }

        @Test
        fun `map with id as empty string, should result in null entity id`() {
            domain.apply { id = "" }

            val entity = repositoryMapper.toEntity(domain)

            assertThat(entity.id).isNull()
        }

        @Test
        fun `map with id as whitespace only, should result in null entity id`() {
            domain.apply { id = "   " }

            val entity = repositoryMapper.toEntity(domain)

            assertThat(entity.id).isNull()
        }

        @Test
        fun `map with id as non-numeric string, should result in null entity id`() {
            domain.apply { id = "not-a-number" }

            val entity = repositoryMapper.toEntity(domain)

            assertThat(entity.id).isNull()
        }

        @Test
        fun `map same domain twice, should return cached entity from context`() {
            val entity1 = repositoryMapper.toEntity(domain)
            val entity2 = repositoryMapper.toEntity(domain)

            assertAll(
                { assertThat(entity1).isSameAs(entity2) },
                { verify(atLeast = 2) { ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(domain) } },
                { verify(exactly = 1) { ctx.remember(domain, entity1) } }
            )
        }

        @Test
        fun `map with localPath that has leading and trailing whitespace`() {
            // Note: The domain constructor validates localPath.trim().isNotBlank()
            // but doesn't modify the actual localPath value
            val project = TestData.Domain.testProject(
                name = projectEntity.name,
                id = projectEntity.id?.toString(),
                description = projectEntity.description
            )
            val domainWithWhitespace = TestData.Domain.testRepository(
                localPath = "  /path/to/repo  ",
                id = "10",
                project = project
            )

            val entity = repositoryMapper.toEntity(domainWithWhitespace)

            assertThat(entity.localPath).isEqualTo("/path/to/repo")
        }

        @Test
        fun `map repository with different project, should use project from context`() {
            val anotherProject = TestData.Domain.testProject(
                name = "AnotherProject",
                id = "20"
            )
            val anotherProjectEntity = TestData.Entity.testProjectEntity(
                name = anotherProject.name,
                id = 20L,
                iid = anotherProject.iid
            )
            ctx.remember(anotherProject, anotherProjectEntity)

            val domainWithDifferentProject = TestData.Domain.testRepository(
                localPath = "/different/path",
                id = "15",
                project = anotherProject
            )

            val entity = repositoryMapper.toEntity(domainWithDifferentProject)

            assertAll(
                { assertThat(entity.project).isSameAs(anotherProjectEntity) },
                { assertThat(entity.project.name).isEqualTo("AnotherProject") }
            )
        }

        @Test
        fun `clear context, expect IllegalStateException since Project is not in context`() {
            ctx.reset()

            val ex = assertThrows<IllegalStateException> {
                repositoryMapper.toEntity(domain)
            }

            assertThat(ex.message).contains("ProjectEntity must be mapped before RepositoryEntity.")
        }
    }

    @Nested
    inner class ToDomain : BaseMapperTest() {

        private lateinit var repositoryEntity: RepositoryEntity
        private lateinit var branchEntity: BranchEntity

        private lateinit var commitEntityA: CommitEntity
//        private lateinit var commitEntityB: CommitEntity

        @BeforeEach
        fun setup() {
            val projectEntity = TestData.Entity.testProjectEntity(
                name = "TestProject",
                id = 1L,
                description = "A test project"
            )

            this.repositoryEntity = TestData.Entity.testRepositoryEntity(
                localPath = "TestRepository",
                id = 1L,
                project = projectEntity
            )

            val project = TestData.Domain.testProject(
                name = repositoryEntity.project.name,
                description = repositoryEntity.project.description
            )
            ctx.remember(project, repositoryEntity.project)
            clearMocks(ctx, recordedCalls = true) // ensure recorded calls are clear after setup

            this.commitEntityA = TestData.Entity.testCommitEntity(
                sha = "A".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repository = this.repositoryEntity,
                id = 1
            )

//            this.commitEntityB =
//                CommitEntity(
//                    sha = "B".repeat(40),
//                    authorDateTime = LocalDateTime.of(2020, 1, 3, 1, 0, 0, 0),
//                    commitDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
//                    message = "Valid commit 2",
//                    repository = this.repositoryEntity,
//                ).apply { id = 2 }

            this.branchEntity =
                BranchEntity(
                    name = "testBranch",
                    fullName = "refs/heads/testBranch",
                    category = ReferenceCategory.LOCAL_BRANCH,
                    repository = this.repositoryEntity,
                    head = commitEntityA,
                    iid = Reference.Id(Uuid.random())
                )
        }

        @Test
        fun `minimal valid entity, check properties only`() {
            assertNotNull(repositoryEntity.id)
            val domain = repositoryMapper.toDomain(repositoryEntity)

            assertAll(
                { assertThat(domain.localPath).isEqualTo(repositoryEntity.localPath) },
                { assertThat(domain).isSameAs(domain.project.repo) },
                { assertThat(domain.commits).isEmpty() },
                { assertThat(domain.branches).isEmpty() },
                { assertThat(domain.user).isEmpty() },
                { assertThat(domain.iid).isNotSameAs(repositoryEntity.iid) },
                { assertThat(domain.iid).isEqualTo(repositoryEntity.iid) }
            )
        }

        @Test
        fun `minimal valid entity, check equality`() {
            val domain = repositoryMapper.toDomain(repositoryEntity)

            assertThat(domain).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFields(*IGNORED_FIELDS.toTypedArray())
                // ignore collections since they are mapped in assembler
                .ignoringFieldsMatchingRegexes(".*id")
                .ignoringActualNullFields()
                .isEqualTo(repositoryEntity)
        }

        @Test
        fun `minimal valid entity, verify calls`() {
            val domain = repositoryMapper.toDomain(repositoryEntity)

            verifyOrder {
                ctx.findDomain<Repository, RepositoryEntity>(repositoryEntity)
                ctx.findDomain<Project, ProjectEntity>(repositoryEntity.project)
                ctx.remember(domain, repositoryEntity)
            }

            confirmVerified(ctx)
        }

        @Test
        fun `map with id is null, should not fail`() {
            repositoryEntity.apply { id = null }
            assertNull(repositoryEntity.id)

            assertDoesNotThrow {
                repositoryMapper.toDomain(repositoryEntity)
            }
        }

        @Test
        fun `map with id is null, domain id should be null`() {
            repositoryEntity.apply { id = null }

            val domain = repositoryMapper.toDomain(repositoryEntity)

            assertThat(domain.id).isNull()
        }

        @Test
        fun `map same entity twice, should return cached domain from context`() {
            val domain1 = repositoryMapper.toDomain(repositoryEntity)
            val domain2 = repositoryMapper.toDomain(repositoryEntity)

            assertAll(
                { assertThat(domain1).isSameAs(domain2) },
                { verify(atLeast = 2) { ctx.findDomain<Repository, RepositoryEntity>(repositoryEntity) } },
                { verify(exactly = 1) { ctx.remember(domain1, repositoryEntity) } }
            )
        }

        @Test
        fun `map entity with id as Long MAX_VALUE, should convert correctly`() {
            repositoryEntity.apply { id = Long.MAX_VALUE }

            val domain = repositoryMapper.toDomain(repositoryEntity)

            assertThat(domain.id).isEqualTo(Long.MAX_VALUE.toString())
        }

        @Test
        fun `map entity, iid should be set correctly via reflection`() {
            val expectedIid = repositoryEntity.iid

            val domain = repositoryMapper.toDomain(repositoryEntity)

            assertAll(
                { assertThat(domain.iid).isEqualTo(expectedIid) },
                { assertThat(domain.iid.value).isEqualTo(expectedIid.value) }
            )
        }

        @Test
        fun `map entity with localPath having whitespace, should preserve it`() {
            val projectEntity = TestData.Entity.testProjectEntity(
                name = "TestProject",
                id = 1L,
                description = "A test project"
            )
            val entityWithWhitespace = TestData.Entity.testRepositoryEntity(
                localPath = "  /path/to/repo  ",
                id = 1L,
                project = projectEntity
            )

            val domain = repositoryMapper.toDomain(entityWithWhitespace)

            assertThat(domain.localPath).isEqualTo("/path/to/repo")
        }

        @Test
        fun `clear context, expect IllegalStateException since Project is not in context`() {
            ctx.reset()

            val ex = assertThrows<IllegalStateException> {
                repositoryMapper.toDomain(repositoryEntity)
            }

            assertThat(ex.message).contains("Project must be mapped before Repository.")
        }
    }

    @Nested
    inner class RefreshDomain {
        private lateinit var entity: RepositoryEntity
        private lateinit var domain: Repository

        @BeforeEach
        fun setup() {
            val projectEntity = TestData.Entity.testProjectEntity(
                name = "TestProject",
                id = 1L,
                description = "A test project"
            )

            entity = TestData.Entity.testRepositoryEntity(
                localPath = "TestRepository",
                id = 1L,
                project = projectEntity
            )

            val project = TestData.Domain.testProject(
                name = entity.project.name,
                id = null,
                description = entity.project.description
            )

            domain = TestData.Domain.testRepository(
                localPath = "TestRepo",
                id = null,
                project = project
            )
        }

        @Test
        fun `refresh domain object, verify mapper and ctx were not called`() {
            repositoryMapper.refreshDomain(domain, entity)

            verify { ctx wasNot Called }
            verify { commitMapper wasNot Called }
            verify { branchMapper wasNot Called }
            verify { userMapper wasNot Called }

            confirmVerified(ctx)
            confirmVerified(commitMapper)
            confirmVerified(branchMapper)
            confirmVerified(userMapper)
        }

        @Test
        fun `refresh domain object, check that id is set`() {
            assertAll(
                { assertNull(domain.id) },
                { assertNotNull(entity.id) }
            )

            repositoryMapper.refreshDomain(domain, entity)

            assertAll(
                { assertNotNull(domain.id) },
                { assertThat(domain.id).isEqualTo(entity.id.toString()) },
            )
        }

        @Test
        fun `refresh domain object, check that project-id is still null`() {
            assertNull(domain.project.id)

            repositoryMapper.refreshDomain(domain, entity)

            assertNull(domain.project.id)
        }

        @Test
        fun `refresh domain object with commit, verify no calls were made`() {
            val commitEntity = TestData.Entity.testCommitEntity(
                sha = "A".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repository = entity
            )
            entity.commits.add(commitEntity)
//            entity.user.addAll(listOfNotNull(commitEntity.committer, commitEntity.author))

            val committerDomain = requireNotNull(commitEntity.committer).toDomain(domain)
            val commitDomain = commitEntity.toDomain(domain, committerDomain)
//            domain.user.addAll(listOfNotNull(commitDomain.committer, commitDomain.author))

            repositoryMapper.refreshDomain(domain, entity)

            verify { ctx wasNot Called }
            verify { commitMapper wasNot Called }
            verify { branchMapper wasNot Called }
            verify { userMapper wasNot Called }

            confirmVerified(ctx)
            confirmVerified(commitMapper)
            confirmVerified(branchMapper)
            confirmVerified(userMapper)
        }

        @Test
        fun `refresh domain object with commit and user, verify no calls were made`() {
            val commitEntity = TestData.Entity.testCommitEntity(
                sha = "A".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repository = entity,
                committer = null
            ).apply {
                committer = TestData.Entity.testUserEntity(
                    name = "TestUser1",
                    email = "test1@example.com",
                    repository = entity
                )
                author = TestData.Entity.testUserEntity(
                    name = "TestUser2",
                    email = "test2@example.com",
                    repository = entity
                )
            }
            entity.commits.add(commitEntity)
            entity.user.addAll(listOfNotNull(commitEntity.committer, commitEntity.author))

            val committerDomain = requireNotNull(commitEntity.committer).toDomain(domain)
            val commitDomain = commitEntity.toDomain(domain, committerDomain).apply {
                author = commitEntity.author?.toDomain(domain)
            }
            domain.user.addAll(listOfNotNull(commitDomain.committer, commitDomain.author))

            repositoryMapper.refreshDomain(domain, entity)

            verify { ctx wasNot Called }
            verify { commitMapper wasNot Called }
            verify { branchMapper wasNot Called }
            verify { userMapper wasNot Called }

            confirmVerified(ctx)
            confirmVerified(commitMapper)
            confirmVerified(branchMapper)
            confirmVerified(userMapper)
        }

        @Test
        fun `refresh domain object with commit, check nested ids are null`() {
            val commitEntity = TestData.Entity.testCommitEntity(
                sha = "A".repeat(40),
                authorDateTime = LocalDateTime.of(2020, 1, 2, 1, 0, 0, 0),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 1, 0, 0, 0),
                message = "Valid commit 1",
                repository = entity,
                committer = null
            ).apply {
                committer = TestData.Entity.testUserEntity(
                    name = "TestUser1",
                    email = "test1@example.com",
                    repository = entity
                )
                author = TestData.Entity.testUserEntity(
                    name = "TestUser2",
                    email = "test2@example.com",
                    repository = entity
                )
            }
            entity.commits.add(commitEntity)
            entity.user.addAll(listOfNotNull(commitEntity.committer, commitEntity.author))

            val committerDomain = requireNotNull(commitEntity.committer).toDomain(domain)
            val commitDomain = commitEntity.toDomain(domain, committerDomain).apply {
                author = commitEntity.author?.toDomain(domain)
            }
            domain.user.addAll(listOfNotNull(commitDomain.committer, commitDomain.author))

            repositoryMapper.refreshDomain(domain, entity)

            assertAll(
                { assertNotNull(domain.id) },
                { assertThat(domain.commits.map { it.id }).containsOnlyNulls() },
                { assertThat(domain.user.map { it.id }).containsOnlyNulls() },
                // check committer
                { assertNotNull(domain.commits.first().committer) },
                { assertNull(domain.commits.first().committer?.id) },
                // check author
                { assertNotNull(domain.commits.first().author) },
                { assertNull(domain.commits.first().author?.id) },
            )
        }

        @Test
        fun `refresh domain with entity id null, domain id should be set to null string`() {
            val projectEntity = TestData.Entity.testProjectEntity(name = "TestProject", id = 1L)
            entity = TestData.Entity.testRepositoryEntity(id = null, project = projectEntity)

            val project = TestData.Domain.testProject(name = entity.project.name)
            domain = TestData.Domain.testRepository(id = "42", project = project)

            assertAll(
                { assertThat(domain.id).isEqualTo("42") },
                { assertNull(entity.id) }
            )

            repositoryMapper.refreshDomain(domain, entity)

            assertThat(domain.id).isNull()
        }

        @Test
        fun `refresh domain with large entity id, should convert correctly`() {
            val projectEntity = TestData.Entity.testProjectEntity(name = "TestProject", id = 1L)
            entity = TestData.Entity.testRepositoryEntity(id = Long.MAX_VALUE, project = projectEntity)

            val project = TestData.Domain.testProject(name = entity.project.name)
            domain = TestData.Domain.testRepository(id = null, project = project)

            repositoryMapper.refreshDomain(domain, entity)

            assertThat(domain.id).isEqualTo(Long.MAX_VALUE.toString())
        }

        @Test
        fun `refresh domain with zero entity id, should set domain id to zero string`() {
            val projectEntity = TestData.Entity.testProjectEntity(name = "TestProject", id = 1L)
            entity = TestData.Entity.testRepositoryEntity(id = 0L, project = projectEntity)

            val project = TestData.Domain.testProject(name = entity.project.name)
            domain = TestData.Domain.testRepository(id = null, project = project)

            repositoryMapper.refreshDomain(domain, entity)

            assertThat(domain.id).isEqualTo("0")
        }

        @Test
        fun `refresh domain overwrites existing domain id`() {
            val projectEntity = TestData.Entity.testProjectEntity(name = "TestProject", id = 1L)
            entity = TestData.Entity.testRepositoryEntity(id = 100L, project = projectEntity)

            val project = TestData.Domain.testProject(name = entity.project.name)
            domain = TestData.Domain.testRepository(id = "42", project = project)

            assertThat(domain.id).isEqualTo("42")

            repositoryMapper.refreshDomain(domain, entity)

            assertThat(domain.id).isEqualTo("100")
        }

        @Test
        fun `refresh domain preserves other domain fields`() {
            val originalLocalPath = domain.localPath
            val originalProjectName = domain.project.name

            repositoryMapper.refreshDomain(domain, entity)

            assertAll(
                { assertThat(domain.localPath).isEqualTo(originalLocalPath) },
                { assertThat(domain.project.name).isEqualTo(originalProjectName) },
                { assertThat(domain.id).isEqualTo(entity.id.toString()) }
            )
        }

        @Test
        fun `refresh domain returns same domain instance`() {
            val result = repositoryMapper.refreshDomain(domain, entity)

            assertThat(result).isSameAs(domain)
        }
    }
}
