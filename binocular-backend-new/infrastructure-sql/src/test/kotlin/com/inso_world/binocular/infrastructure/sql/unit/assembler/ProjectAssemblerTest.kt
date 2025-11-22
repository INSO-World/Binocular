package com.inso_world.binocular.infrastructure.sql.unit.assembler

import com.inso_world.binocular.core.extension.reset
import com.inso_world.binocular.infrastructure.sql.TestData
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import com.inso_world.binocular.infrastructure.sql.unit.assembler.base.BaseAssemblerTest
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Reference
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import com.inso_world.binocular.model.vcs.ReferenceCategory
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
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Comprehensive C4 unit tests for [com.inso_world.binocular.infrastructure.sql.assembler.ProjectAssembler].
 *
 * Tests cover:
 * - **toEntity()**: Domain → Entity assembly with Repository orchestration
 * - **toDomain()**: Entity → Domain assembly with Repository orchestration
 * - Identity preservation via MappingContext
 * - Aggregate boundaries (Project owns Repository)
 * - Edge cases (no repository, cached objects, etc.)
 */
@OptIn(ExperimentalUuidApi::class)
internal class ProjectAssemblerTest : BaseAssemblerTest() {
    private fun createCommit(
        repository: Repository,
        sha: String,
        commitDateTime: LocalDateTime,
        authorDateTime: LocalDateTime? = null,
        message: String? = null,
        committer: User? = null,
    ): Commit {
        val resolvedCommitter = committer ?: User(
            name = "Committer-${sha.take(6)}",
            repository = repository
        ).apply {
            email = "${sha.take(6)}@project.test"
        }

        return com.inso_world.binocular.model.Commit(
            sha = sha,
            authorDateTime = authorDateTime,
            commitDateTime = commitDateTime,
            message = message,
            repository = repository,
            committer = resolvedCommitter,
        )
    }

    @BeforeEach
    fun setup() {
        mockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntityKt")
        mockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntityKt")
        mockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntityKt")
        mockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntityKt")
        mockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntityKt")
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntityKt")
        unmockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntityKt")
        unmockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntityKt")
        unmockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntityKt")
        unmockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntityKt")
    }

    @Nested
    inner class ToEntity {

        @Test
        fun `minimal Project without Repository, check structure`() {
            val project = TestData.Domain.testProject(
                name = "TestProject",
                description = "A test project"
            )

            val entity = assertDoesNotThrow {
                projectAssembler.toEntity(project)
            }

            assertAll(
                "entity structure",
                { assertThat(entity.name).isEqualTo(project.name) },
                { assertThat(entity.description).isEqualTo(project.description) },
                { assertThat(entity.repo).isNull() })
        }

        @Test
        fun `minimal Project without Repository, verify context calls`() {
            val project = TestData.Domain.testProject(
                name = "TestProject",
                description = "A test project"
            )

            val entity = projectAssembler.toEntity(project)

            verifyOrder {
                // Fast-path check
                ctx.findEntity<Project.Key, Project, ProjectEntity>(project)
                // ProjectMapper called to map structure
                projectMapper.toEntity(project)
                // No repositoryAssembler called since no repo
            }

            verify(exactly = 0) {
                repositoryAssembler.toEntity(any())
            }
        }

        @Test
        fun `Project with Repository, check complete structure`() {
            val project = TestData.Domain.testProject(
                name = "TestProject",
                description = "A test project"
            )
            val repository = TestData.Domain.testRepository(
                localPath = "/test/repo",
                project = project
            )
            project.repo = repository

            val entity = assertDoesNotThrow {
                projectAssembler.toEntity(project)
            }

            assertAll(
                "entity structure",
                { assertThat(entity.name).isEqualTo(project.name) },
                { assertThat(entity.description).isEqualTo(project.description) },
                { assertThat(entity.repo).isNotNull() },
                { assertThat(entity.repo?.localPath).isEqualTo(repository.localPath) })
        }

        @Test
        fun `Project with Repository, verify assembler orchestration`() {
            val project = TestData.Domain.testProject(name = "TestProject")
            val repository = TestData.Domain.testRepository(
                localPath = "/test/repo",
                project = project
            )
            project.repo = repository

            val entity = projectAssembler.toEntity(project)

            verifyOrder {
                // Fast-path check
                ctx.findEntity<Project.Key, Project, ProjectEntity>(project)
                // ProjectMapper maps Project structure
                projectMapper.toEntity(project)
                // RepositoryAssembler orchestrates Repository
                repositoryAssembler.toEntity(repository)
            }
        }

        @Test
        fun `Project with Repository containing commits and branches, check full graph`() {
            val project = Project(
                name = "TestProject"
            )
            val repository = Repository(
                localPath = "/test/repo", project = project
            )
            project.repo = repository

            val user = User(
                name = "Test User", repository = repository
            ).apply { email = "test@example.com" }

            val commit1 = createCommit(
                sha = "a".repeat(40),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 0, 0),
                repository = repository,
                committer = user
            )
            val commit2 = createCommit(
                sha = "b".repeat(40),
                commitDateTime = LocalDateTime.of(2020, 1, 2, 0, 0),
                repository = repository,
                committer = user
            )
            commit2.parents.add(commit1)

            val branch = Branch(
                name = "main",
                fullName = "refs/heads/main",
                category = ReferenceCategory.LOCAL_BRANCH,
                head = commit2,
                repository = repository
            )

            commit1.author = user

            val entity = assertDoesNotThrow {
                projectAssembler.toEntity(project)
            }

            assertAll(
                "complete entity graph",
                { assertThat(entity.name).isEqualTo(project.name) },
                { assertThat(entity.repo).isNotNull() },
                { assertThat(entity.repo?.commits).hasSize(2) },
                { assertThat(entity.repo?.branches).hasSize(1) },
                { assertThat(entity.repo?.user).hasSize(1) })
        }

        @Test
        fun `Project with complex commit graph, verify parent-child relationships wired`() {
            val project = Project(name = "TestProject")
            val repository = Repository(localPath = "/test/repo", project = project)
            project.repo = repository

            // Create diamond commit graph:
            //     c1
            //    /  \
            //   c2  c3
            //    \  /
            //     c4
            val c1 = createCommit(sha = "1".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)
            val c2 = createCommit(sha = "2".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)
            val c3 = createCommit(sha = "3".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)
            val c4 = createCommit(sha = "4".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)

            c2.parents.add(c1)
            c3.parents.add(c1)
            c4.parents.add(c2)
            c4.parents.add(c3)

            val entity = assertDoesNotThrow {
                projectAssembler.toEntity(project)
            }

            val c1Entity = entity.repo!!.commits.find { it.sha == c1.sha }!!
            val c2Entity = entity.repo!!.commits.find { it.sha == c2.sha }!!
            val c3Entity = entity.repo!!.commits.find { it.sha == c3.sha }!!
            val c4Entity = entity.repo!!.commits.find { it.sha == c4.sha }!!

            assertAll(
                "parent/child relationships",
                // c1 has no parents, 2 children
                { assertThat(c1Entity.parents).isEmpty() },
                { assertThat(c1Entity.children).containsExactlyInAnyOrder(c2Entity, c3Entity) },
                // c2 has 1 parent, 1 child
                { assertThat(c2Entity.parents).containsExactly(c1Entity) },
                { assertThat(c2Entity.children).containsExactly(c4Entity) },
                // c3 has 1 parent, 1 child
                { assertThat(c3Entity.parents).containsExactly(c1Entity) },
                { assertThat(c3Entity.children).containsExactly(c4Entity) },
                // c4 has 2 parents, no children
                { assertThat(c4Entity.parents).containsExactlyInAnyOrder(c2Entity, c3Entity) },
                { assertThat(c4Entity.children).isEmpty() })
        }

        @Test
        fun `fast-path when Project already in context, returns cached entity`() {
            val project = Project(name = "TestProject")

            // First call
            val entity1 = projectAssembler.toEntity(project)

            clearMocks(ctx, projectMapper, recordedCalls = true)

            // Second call should return cached
            val entity2 = projectAssembler.toEntity(project)

            assertAll(
                { assertThat(entity2).isSameAs(entity1) },
                { verify(exactly = 1) { ctx.findEntity<Project.Key, Project, ProjectEntity>(project) } },
                { verify(exactly = 0) { projectMapper.toEntity(any()) } })

            confirmVerified(ctx, projectMapper)
        }

        @Test
        fun `Project with Repository already assembled, Repository uses cached entity`() {
            val project = Project(name = "TestProject")
            val repository = Repository(localPath = "/test/repo", project = project)
            project.repo = repository

            // Pre-assemble repository
            val repoEntity = repositoryAssembler.toEntity(repository)

            clearMocks(ctx, recordedCalls = true)

            // Assemble project (should find cached repository)
            val projectEntity = projectAssembler.toEntity(project)

            assertThat(projectEntity.repo).isSameAs(repoEntity)

            verify(exactly = 1) {
                repositoryAssembler.toEntity(repository)
            }
        }
    }

    @Nested
    inner class ToDomain {

        @Test
        fun `minimal ProjectEntity without Repository, check structure`() {
            val projectEntity = ProjectEntity(
                iid = Project.Id(Uuid.random()), name = "TestProject"
            ).apply {
                id = 1L
                description = "A test project"
            }

            val domain = assertDoesNotThrow {
                projectAssembler.toDomain(projectEntity)
            }

            assertAll(
                "domain structure",
                { assertThat(domain.name).isEqualTo(projectEntity.name) },
                { assertThat(domain.description).isEqualTo(projectEntity.description) },
                { assertThat(domain.repo).isNull() })
        }

        @Test
        fun `minimal ProjectEntity without Repository, verify context calls`() {
            val projectEntity = ProjectEntity(
                iid = Project.Id(Uuid.random()), name = "TestProject"
            ).apply {
                id = 1L
                description = "A test project"
            }

            val domain = projectAssembler.toDomain(projectEntity)

            verifyOrder {
                // Fast-path check
                ctx.findDomain<Project, ProjectEntity>(projectEntity)
                // ProjectMapper called to map structure
                projectMapper.toDomain(projectEntity)
                // No repositoryAssembler called since no repo
            }

            verify(exactly = 0) {
                repositoryAssembler.toDomain(any())
            }
        }

        @Test
        fun `ProjectEntity with RepositoryEntity, check complete structure`() {
            val projectEntity = ProjectEntity(
                iid = Project.Id(Uuid.random()), name = "TestProject",
            ).apply {
                id = 1L
                description = "A test project"
            }

            val repositoryEntity = RepositoryEntity(
                localPath = "/test/repo", project = projectEntity, iid = Repository.Id(
                    Uuid.random()
                )
            ).apply { id = 2L }

            projectEntity.repo = repositoryEntity

            val domain = assertDoesNotThrow {
                projectAssembler.toDomain(projectEntity)
            }

            assertAll(
                "domain structure",
                { assertThat(domain.name).isEqualTo(projectEntity.name) },
                { assertThat(domain.description).isEqualTo(projectEntity.description) },
                { assertThat(domain.repo).isNotNull() },
                { assertThat(domain.repo?.localPath).isEqualTo(repositoryEntity.localPath) })
        }

        @Test
        fun `ProjectEntity with RepositoryEntity, verify assembler orchestration`() {
            val projectEntity = ProjectEntity(
                iid = Project.Id(Uuid.random()), name = "TestProject"
            ).apply { id = 1L }

            val repositoryEntity = RepositoryEntity(
                localPath = "/test/repo", project = projectEntity, iid = Repository.Id(
                    Uuid.random()
                )
            ).apply { id = 2L }

            projectEntity.repo = repositoryEntity

            val domain = projectAssembler.toDomain(projectEntity)

            verifyOrder {
                // Fast-path check
                ctx.findDomain<Project, ProjectEntity>(projectEntity)
                // ProjectMapper maps Project structure
                projectMapper.toDomain(projectEntity)
                // RepositoryAssembler orchestrates Repository
                repositoryAssembler.toDomain(repositoryEntity)
            }
        }

        @Test
        fun `ProjectEntity with full Repository graph, check complete domain`() {
            val projectEntity = ProjectEntity(iid = Project.Id(Uuid.random()), name = "TestProject").apply { id = 1L }
            val repositoryEntity = RepositoryEntity(
                localPath = "/test/repo", project = projectEntity, iid = Repository.Id(
                    Uuid.random()
                )
            ).apply { id = 2L }
            projectEntity.repo = repositoryEntity

            val commit1Entity = CommitEntity(
                sha = "a".repeat(40),
                commitDateTime = LocalDateTime.of(2020, 1, 1, 0, 0),
                repository = repositoryEntity,
                iid = Commit.Id(
                    Uuid.random()
                )
            ).apply { id = 10L }

            val commit2Entity = CommitEntity(
                sha = "b".repeat(40),
                commitDateTime = LocalDateTime.of(2020, 1, 2, 0, 0),
                repository = repositoryEntity,
                iid = Commit.Id(
                    Uuid.random()
                )
            ).apply { id = 11L }

            commit2Entity.parents.add(commit1Entity)
            commit1Entity.children.add(commit2Entity)

            val branchEntity = BranchEntity(
                name = "main",
                fullName = "refs/heads/main",
                category = ReferenceCategory.LOCAL_BRANCH,
                head = commit2Entity,
                repository = repositoryEntity,
                iid = Reference.Id(
                    Uuid.random()
                )
            ).apply { id = 20L }

            val userEntity = UserEntity(
                name = "Test User", email = "test@example.com", repository = repositoryEntity, iid = User.Id(
                    Uuid.random()
                )
            ).apply { id = 30L }

            commit1Entity.author = userEntity
            commit1Entity.committer = userEntity
            commit2Entity.committer = userEntity

            val domain = assertDoesNotThrow {
                projectAssembler.toDomain(projectEntity)
            }

            assertAll(
                "complete domain graph",
                { assertThat(domain.name).isEqualTo(projectEntity.name) },
                { assertThat(domain.repo).isNotNull() },
                { assertThat(domain.repo?.commits).hasSize(2) },
                { assertThat(domain.repo?.branches).hasSize(1) })
        }

        @Test
        fun `ProjectEntity with complex commit graph, verify parent-child relationships`() {
            val projectEntity = ProjectEntity(iid = Project.Id(Uuid.random()), name = "TestProject").apply { id = 1L }
            val repositoryEntity = RepositoryEntity(
                localPath = "/test/repo", project = projectEntity, iid = Repository.Id(
                    Uuid.random()
                )
            ).apply { id = 2L }
            projectEntity.repo = repositoryEntity

            // Diamond graph
            val c1 = CommitEntity(
                sha = "1".repeat(40),
                commitDateTime = LocalDateTime.now(),
                repository = repositoryEntity,
                iid = Commit.Id(
                    Uuid.random()
                )
            ).apply {
                id = 10L
                committer = TestData.Entity.testUserEntity(
                    name = "Graph Committer 1",
                    email = "graph1@example.com",
                    repository = repositoryEntity
                )
            }
            val c2 = CommitEntity(
                sha = "2".repeat(40),
                commitDateTime = LocalDateTime.now(),
                repository = repositoryEntity,
                iid = Commit.Id(
                    Uuid.random()
                )
            ).apply {
                id = 11L
                committer = TestData.Entity.testUserEntity(
                    name = "Graph Committer 2",
                    email = "graph2@example.com",
                    repository = repositoryEntity
                )
            }
            val c3 = CommitEntity(
                sha = "3".repeat(40),
                commitDateTime = LocalDateTime.now(),
                repository = repositoryEntity,
                iid = Commit.Id(
                    Uuid.random()
                )
            ).apply {
                id = 12L
                committer = TestData.Entity.testUserEntity(
                    name = "Graph Committer 3",
                    email = "graph3@example.com",
                    repository = repositoryEntity
                )
            }
            val c4 = CommitEntity(
                sha = "4".repeat(40),
                commitDateTime = LocalDateTime.now(),
                repository = repositoryEntity,
                iid = Commit.Id(
                    Uuid.random()
                )
            ).apply {
                id = 13L
                committer = TestData.Entity.testUserEntity(
                    name = "Graph Committer 4",
                    email = "graph4@example.com",
                    repository = repositoryEntity
                )
            }

            c2.parents.add(c1)
            c1.children.add(c2)
            c3.parents.add(c1)
            c1.children.add(c3)
            c4.parents.add(c2)
            c4.parents.add(c3)
            c2.children.add(c4)
            c3.children.add(c4)

            val domain = assertDoesNotThrow {
                projectAssembler.toDomain(projectEntity)
            }

            val commits = domain.repo!!.commits
            val c1Domain = commits.find { it.sha == c1.sha }!!
            val c2Domain = commits.find { it.sha == c2.sha }!!
            val c3Domain = commits.find { it.sha == c3.sha }!!
            val c4Domain = commits.find { it.sha == c4.sha }!!

            assertAll(
                "parent/child relationships in domain",
                { assertThat(c1Domain.parents).isEmpty() },
                { assertThat(c1Domain.children).containsExactlyInAnyOrder(c2Domain, c3Domain) },
                { assertThat(c2Domain.parents).containsExactly(c1Domain) },
                { assertThat(c2Domain.children).containsExactly(c4Domain) },
                { assertThat(c3Domain.parents).containsExactly(c1Domain) },
                { assertThat(c3Domain.children).containsExactly(c4Domain) },
                { assertThat(c4Domain.parents).containsExactlyInAnyOrder(c2Domain, c3Domain) },
                { assertThat(c4Domain.children).isEmpty() })
        }

        @Test
        fun `fast-path when ProjectEntity already in context, returns cached domain`() {
            val projectEntity = ProjectEntity(iid = Project.Id(Uuid.random()), name = "TestProject").apply { id = 1L }

            // First call
            val domain1 = projectAssembler.toDomain(projectEntity)

            clearMocks(ctx, projectMapper, recordedCalls = true)

            // Second call should return cached
            val domain2 = projectAssembler.toDomain(projectEntity)

            assertAll(
                { assertThat(domain2).isSameAs(domain1) },
                { verify(exactly = 1) { ctx.findDomain<Project, ProjectEntity>(projectEntity) } },
                { verify(exactly = 0) { projectMapper.toDomain(any()) } })

            confirmVerified(ctx, projectMapper)
        }

        @Test
        fun `ProjectEntity with RepositoryEntity already assembled, uses cached repository`() {
            val projectEntity = ProjectEntity(iid = Project.Id(Uuid.random()), name = "TestProject").apply { id = 1L }
            val repositoryEntity = RepositoryEntity(
                localPath = "/test/repo", project = projectEntity, iid = Repository.Id(
                    Uuid.random()
                )
            ).apply { id = 2L }
            projectEntity.repo = repositoryEntity

            // Pre-assemble repository
            val repoDomain = repositoryAssembler.toDomain(repositoryEntity)

            clearMocks(ctx, recordedCalls = true)

            // Assemble project (should find cached repository)
            val projectDomain = projectAssembler.toDomain(projectEntity)

            assertThat(projectDomain.repo).isSameAs(repoDomain)

            verify(exactly = 1) {
                repositoryAssembler.toDomain(repositoryEntity)
            }
        }
    }

    @Nested
    inner class IdentityPreservation {

        @Test
        fun `toEntity followed by toDomain preserves object identity`() {
            val project = Project(name = "TestProject")
            val repository = Repository(localPath = "/test/repo", project = project)
            project.repo = repository

            val entity = projectAssembler.toEntity(project)
            val domainFromEntity = projectAssembler.toDomain(entity)

            // Should get back exact same domain object
            assertThat(domainFromEntity).isSameAs(project)
            assertThat(domainFromEntity.repo).isSameAs(repository)
        }

        @Test
        fun `toDomain followed by toEntity preserves object identity`() {
            val projectEntity = ProjectEntity(iid = Project.Id(Uuid.random()), name = "TestProject").apply { id = 1L }
            val repositoryEntity = RepositoryEntity(
                localPath = "/test/repo", project = projectEntity, iid = Repository.Id(
                    Uuid.random()
                )
            ).apply { id = 2L }
            projectEntity.repo = repositoryEntity

            val domain = projectAssembler.toDomain(projectEntity)
            val entityFromDomain = projectAssembler.toEntity(domain)

            // Should get back exact same entity object
            assertThat(entityFromDomain).isSameAs(projectEntity)
            assertThat(entityFromDomain.repo).isSameAs(repositoryEntity)
        }

        @Test
        fun `multiple toEntity calls return same entity instance`() {
            val project = Project(name = "TestProject")

            val entity1 = projectAssembler.toEntity(project)
            val entity2 = projectAssembler.toEntity(project)
            val entity3 = projectAssembler.toEntity(project)

            assertAll({ assertThat(entity2).isSameAs(entity1) }, { assertThat(entity3).isSameAs(entity1) })
        }

        @Test
        fun `multiple toDomain calls return same domain instance`() {
            val projectEntity = ProjectEntity(iid = Project.Id(Uuid.random()), name = "TestProject").apply { id = 1L }

            val domain1 = projectAssembler.toDomain(projectEntity)
            val domain2 = projectAssembler.toDomain(projectEntity)
            val domain3 = projectAssembler.toDomain(projectEntity)

            assertAll({ assertThat(domain2).isSameAs(domain1) }, { assertThat(domain3).isSameAs(domain1) })
        }

        @Test
        fun `context reset breaks identity preservation`() {
            val project = Project(name = "TestProject")
            val entity1 = projectAssembler.toEntity(project)

            // Reset context
            ctx.reset()

            val entity2 = projectAssembler.toEntity(project)

            // Should be different instances after reset
            assertThat(entity2).isNotSameAs(entity1)
        }
    }
}
