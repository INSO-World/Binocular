package com.inso_world.binocular.infrastructure.sql.unit.assembler

import com.inso_world.binocular.core.extension.reset
import com.inso_world.binocular.infrastructure.sql.TestData
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RemoteEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import com.inso_world.binocular.infrastructure.sql.unit.assembler.base.BaseAssemblerTest
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Reference
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import com.inso_world.binocular.model.vcs.Remote
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
 * Comprehensive C4 unit tests for [com.inso_world.binocular.infrastructure.sql.assembler.RepositoryAssembler].
 *
 * Tests cover:
 * - **toEntity()**: Domain → Entity assembly with complete commit graph wiring
 * - **toDomain()**: Entity → Domain assembly with bidirectional relationship handling
 * - Two-pass commit graph assembly (structure first, then parent/child wiring)
 * - Identity preservation via MappingContext
 * - Aggregate boundaries (Repository owns Commits/Branches/Users, references Project)
 * - Standalone vs. ProjectAssembler-driven assembly scenarios
 * - Complex commit graphs (linear, branching, diamond, multiple roots)
 * - Author/committer relationship wiring
 * - Edge cases (empty repository, no branches, circular references prevention)
 */
@OptIn(ExperimentalUuidApi::class)
internal class RepositoryAssemblerTest : BaseAssemblerTest() {
    private fun createCommit(
        sha: String,
        commitDateTime: LocalDateTime,
        repository: Repository,
        authorDateTime: LocalDateTime? = null,
        message: String? = null,
        committer: User? = null,
    ): Commit {
        val resolvedCommitter = committer ?: User(
            name = "Committer-${sha.take(6)}",
            repository = repository
        ).apply {
            email = "${sha.take(6)}@example.com"
        }

        return com.inso_world.binocular.model.Commit(
            sha = sha,
            commitDateTime = commitDateTime,
            authorDateTime = authorDateTime,
            message = message,
            repository = repository,
            committer = resolvedCommitter,
        )
    }

    private fun CommitEntity.withCommitter(
        repository: RepositoryEntity,
        label: String
    ): CommitEntity = apply {
        this.committer = TestData.Entity.testUserEntity(
            name = "Committer-$label",
            email = "$label@example.com",
            repository = repository
        )
    }

    @BeforeEach
    fun setup() {
        mockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntityKt")
        mockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntityKt")
        mockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntityKt")
        mockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntityKt")
        mockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntityKt")
        mockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.RemoteEntityKt")
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntityKt")
        unmockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntityKt")
        unmockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntityKt")
        unmockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntityKt")
        unmockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntityKt")
        unmockkStatic("com.inso_world.binocular.infrastructure.sql.persistence.entity.RemoteEntityKt")
    }

    @Nested
    inner class ToEntity {

        @Nested
        inner class MinimalRepository {

            @Test
            fun `empty Repository without commits or branches, check structure`() {
                val project = Project(name = "TestProject")
                val repository = Repository(
                    localPath = "/test/repo",
                    project = project
                )

                // Pre-map project to context (simulates ProjectAssembler scenario)
                val projectEntity = projectMapper.toEntity(project)

                clearMocks(ctx, recordedCalls = true)

                val entity = assertDoesNotThrow {
                    repositoryAssembler.toEntity(repository)
                }

                assertAll(
                    "entity structure",
                    { assertThat(entity.localPath).isEqualTo(repository.localPath) },
                    { assertThat(entity.project).isSameAs(projectEntity) },
                    { assertThat(entity.commits).isEmpty() },
                    { assertThat(entity.branches).isEmpty() },
                    { assertThat(entity.user).isEmpty() }
                )
            }

            @Test
            fun `empty Repository, verify mapper calls`() {
                val project = Project(name = "TestProject")
                val repository = Repository(localPath = "/test/repo", project = project)

                val projectEntity = projectMapper.toEntity(project)

                clearMocks(ctx, recordedCalls = true)

                val entity = repositoryAssembler.toEntity(repository)

                verifyOrder {
                    // Fast-path check
                    ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(repository)
                    // Project reference check
                    ctx.findEntity<Project.Key, Project, ProjectEntity>(project)
                    // Repository structure mapping
                    repositoryMapper.toEntity(repository)
                }

                // No commits/branches/users to process
                verify(exactly = 0) {
                    commitMapper.toEntity(any())
                    branchMapper.toEntity(any())
                    userMapper.toEntity(any())
                }
            }
        }

        @Nested
        inner class StandaloneAssembly {

            @Test
            fun `standalone assembly without Project in context, creates minimal Project reference`() {
                val project = Project(name = "TestProject")
                val repository = Repository(localPath = "/test/repo", project = project)

                // Don't pre-map project - simulate standalone scenario
                val entity = assertDoesNotThrow {
                    repositoryAssembler.toEntity(repository)
                }

                assertAll(
                    "minimal project reference created",
                    { assertThat(entity.project).isNotNull() },
                    { assertThat(entity.project.name).isEqualTo(project.name) },
                    { assertThat(entity.project.repo).isSameAs(entity) }
                )
            }

            @Test
            fun `standalone assembly, verify Project mapped but not fully built`() {
                val project = Project(name = "TestProject")
                val repository = Repository(localPath = "/test/repo", project = project)

                val entity = repositoryAssembler.toEntity(repository)

                verify(exactly = 1) {
                    // Project gets mapped as minimal reference
                    projectMapper.toEntity(project)
                }

                assertThat(entity.project.repo).isSameAs(entity)
            }
        }

        @Nested
        inner class CommitGraphAssembly {

            @Test
            fun `single commit without parents, check structure`() {
                val project = Project(name = "TestProject")
                val repository = Repository(localPath = "/test/repo", project = project)

                val commit = createCommit(
                    sha = "a".repeat(40),
                    commitDateTime = LocalDateTime.of(2020, 1, 1, 0, 0),
                    repository = repository
                )

                val entity = repositoryAssembler.toEntity(repository)

                assertAll(
                    "single commit",
                    { assertThat(entity.commits).hasSize(1) },
                    { assertThat(entity.commits.first().sha).isEqualTo(commit.sha) },
                    { assertThat(entity.commits.first().parents).isEmpty() },
                    { assertThat(entity.commits.first().children).isEmpty() }
                )
            }

            @Test
            fun `linear commit chain, verify parent-child relationships`() {
                val project = Project(name = "TestProject")
                val repository = Repository(localPath = "/test/repo", project = project)

                // c1 <- c2 <- c3
                val c1 = createCommit(sha = "1".repeat(40), commitDateTime = LocalDateTime.of(2020, 1, 1, 0, 0), repository = repository)
                val c2 = createCommit(sha = "2".repeat(40), commitDateTime = LocalDateTime.of(2020, 1, 2, 0, 0), repository = repository)
                val c3 = createCommit(sha = "3".repeat(40), commitDateTime = LocalDateTime.of(2020, 1, 3, 0, 0), repository = repository)

                c2.parents.add(c1)
                c3.parents.add(c2)

                val entity = repositoryAssembler.toEntity(repository)

                val c1Entity = entity.commits.find { it.sha == c1.sha }!!
                val c2Entity = entity.commits.find { it.sha == c2.sha }!!
                val c3Entity = entity.commits.find { it.sha == c3.sha }!!

                assertAll(
                    "linear chain relationships",
                    // c1: no parents, 1 child
                    { assertThat(c1Entity.parents).isEmpty() },
                    { assertThat(c1Entity.children).containsExactly(c2Entity) },
                    // c2: 1 parent, 1 child
                    { assertThat(c2Entity.parents).containsExactly(c1Entity) },
                    { assertThat(c2Entity.children).containsExactly(c3Entity) },
                    // c3: 1 parent, no children
                    { assertThat(c3Entity.parents).containsExactly(c2Entity) },
                    { assertThat(c3Entity.children).isEmpty() }
                )
            }

            @Test
            fun `branching commit graph, verify relationships`() {
                val project = Project(name = "TestProject")
                val repository = Repository(localPath = "/test/repo", project = project)

                // c1 splits into c2 and c3
                //   c1
                //  /  \
                // c2  c3
                val c1 = createCommit(sha = "1".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)
                val c2 = createCommit(sha = "2".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)
                val c3 = createCommit(sha = "3".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)

                c2.parents.add(c1)
                c3.parents.add(c1)

                val entity = repositoryAssembler.toEntity(repository)

                val c1Entity = entity.commits.find { it.sha == c1.sha }!!
                val c2Entity = entity.commits.find { it.sha == c2.sha }!!
                val c3Entity = entity.commits.find { it.sha == c3.sha }!!

                assertAll(
                    "branching relationships",
                    // c1: no parents, 2 children
                    { assertThat(c1Entity.parents).isEmpty() },
                    { assertThat(c1Entity.children).containsExactlyInAnyOrder(c2Entity, c3Entity) },
                    // c2: 1 parent, no children
                    { assertThat(c2Entity.parents).containsExactly(c1Entity) },
                    { assertThat(c2Entity.children).isEmpty() },
                    // c3: 1 parent, no children
                    { assertThat(c3Entity.parents).containsExactly(c1Entity) },
                    { assertThat(c3Entity.children).isEmpty() }
                )
            }

            @Test
            fun `diamond commit graph, verify merge relationships`() {
                val project = Project(name = "TestProject")
                val repository = Repository(localPath = "/test/repo", project = project)

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

                val entity = repositoryAssembler.toEntity(repository)

                val c1Entity = entity.commits.find { it.sha == c1.sha }!!
                val c2Entity = entity.commits.find { it.sha == c2.sha }!!
                val c3Entity = entity.commits.find { it.sha == c3.sha }!!
                val c4Entity = entity.commits.find { it.sha == c4.sha }!!

                assertAll(
                    "diamond graph relationships",
                    // c1: no parents, 2 children
                    { assertThat(c1Entity.parents).isEmpty() },
                    { assertThat(c1Entity.children).containsExactlyInAnyOrder(c2Entity, c3Entity) },
                    // c2: 1 parent (c1), 1 child (c4)
                    { assertThat(c2Entity.parents).containsExactly(c1Entity) },
                    { assertThat(c2Entity.children).containsExactly(c4Entity) },
                    // c3: 1 parent (c1), 1 child (c4)
                    { assertThat(c3Entity.parents).containsExactly(c1Entity) },
                    { assertThat(c3Entity.children).containsExactly(c4Entity) },
                    // c4: 2 parents (c2, c3), no children
                    { assertThat(c4Entity.parents).containsExactlyInAnyOrder(c2Entity, c3Entity) },
                    { assertThat(c4Entity.children).isEmpty() }
                )
            }

            @Test
            fun `multiple root commits (orphan branches), all wired correctly`() {
                val project = Project(name = "TestProject")
                val repository = Repository(localPath = "/test/repo", project = project)

                // Two independent commit chains
                val root1 = createCommit(sha = "1".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)
                val child1 = createCommit(sha = "2".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)
                child1.parents.add(root1)

                val root2 = createCommit(sha = "3".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)
                val child2 = createCommit(sha = "4".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)
                child2.parents.add(root2)

                val entity = repositoryAssembler.toEntity(repository)

                val root1Entity = entity.commits.find { it.sha == root1.sha }!!
                val child1Entity = entity.commits.find { it.sha == child1.sha }!!
                val root2Entity = entity.commits.find { it.sha == root2.sha }!!
                val child2Entity = entity.commits.find { it.sha == child2.sha }!!

                assertAll(
                    "independent chains",
                    // Chain 1
                    { assertThat(root1Entity.parents).isEmpty() },
                    { assertThat(root1Entity.children).containsExactly(child1Entity) },
                    { assertThat(child1Entity.parents).containsExactly(root1Entity) },
                    { assertThat(child1Entity.children).isEmpty() },
                    // Chain 2
                    { assertThat(root2Entity.parents).isEmpty() },
                    { assertThat(root2Entity.children).containsExactly(child2Entity) },
                    { assertThat(child2Entity.parents).containsExactly(root2Entity) },
                    { assertThat(child2Entity.children).isEmpty() }
                )
            }

            @Test
            fun `complex graph with multiple branches and merges`() {
                val project = Project(name = "TestProject")
                val repository = Repository(localPath = "/test/repo", project = project)

                //       c1
                //      / | \
                //    c2 c3 c4
                //     \ | /
                //       c5
                val c1 = createCommit(sha = "1".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)
                val c2 = createCommit(sha = "2".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)
                val c3 = createCommit(sha = "3".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)
                val c4 = createCommit(sha = "4".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)
                val c5 = createCommit(sha = "5".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)

                c2.parents.add(c1)
                c3.parents.add(c1)
                c4.parents.add(c1)
                c5.parents.add(c2)
                c5.parents.add(c3)
                c5.parents.add(c4)

                val entity = repositoryAssembler.toEntity(repository)

                val c1Entity = entity.commits.find { it.sha == c1.sha }!!
                val c5Entity = entity.commits.find { it.sha == c5.sha }!!

                assertAll(
                    "complex graph",
                    { assertThat(c1Entity.children).hasSize(3) },
                    { assertThat(c5Entity.parents).hasSize(3) }
                )
            }
        }

        @Nested
        inner class AuthorCommitterRelationships {

            @Test
            fun `commit with author and committer, both users wired correctly`() {
                val project = Project(name = "TestProject")
                val repository = Repository(localPath = "/test/repo", project = project)

                val author = User(
                    name = "Author",
                    repository = repository
                ).apply { email = "author@example.com" }

                val committer = User(
                    name = "Committer",
                    repository = repository
                ).apply { email = "committer@example.com" }

                val commit = createCommit(
                    sha = "a".repeat(40),
                    commitDateTime = LocalDateTime.now(),
                    repository = repository,
                    committer = committer
                )

                commit.author = author

                val entity = repositoryAssembler.toEntity(repository)

                val commitEntity = entity.commits.first()

                assertAll(
                    "author/committer relationships",
                    { assertThat(commitEntity.author).isNotNull() },
                    { assertThat(commitEntity.author?.name).isEqualTo("Author") },
                    { assertThat(commitEntity.committer).isNotNull() },
                    { assertThat(commitEntity.committer?.name).isEqualTo("Committer") },
                    // Both users added to repository
                    { assertThat(entity.user).hasSize(2) }
                )
            }

            @Test
            fun `same user as author and committer, only one user entity created`() {
                val project = Project(name = "TestProject")
                val repository = Repository(localPath = "/test/repo", project = project)

                val user = User(
                    name = "Same User",
                    repository = repository
                ).apply { email = "user@example.com" }

                val commit = createCommit(
                    sha = "a".repeat(40),
                    commitDateTime = LocalDateTime.now(),
                    repository = repository,
                    committer = user
                )

                commit.author = user

                val entity = repositoryAssembler.toEntity(repository)

                val commitEntity = entity.commits.first()

                assertAll(
                    "same user for both roles",
                    { assertThat(commitEntity.author).isSameAs(commitEntity.committer) },
                    // Only one user entity in repository (identity preserved)
                    { assertThat(entity.user).hasSize(1) },
                    { assertThat(entity.user.first().name).isEqualTo("Same User") }
                )
            }

            @Test
            fun `multiple commits share same author, identity preserved`() {
                val project = Project(name = "TestProject")
                val repository = Repository(localPath = "/test/repo", project = project)

                val sharedAuthor = User(
                    name = "Shared Author",
                    repository = repository
                ).apply { email = "shared@example.com" }

                val c1 = createCommit(
                    sha = "1".repeat(40),
                    commitDateTime = LocalDateTime.now(),
                    repository = repository,
                    committer = sharedAuthor
                )
                val c2 = createCommit(
                    sha = "2".repeat(40),
                    commitDateTime = LocalDateTime.now(),
                    repository = repository,
                    committer = sharedAuthor
                )

                c1.author = sharedAuthor
                c2.author = sharedAuthor

                val entity = repositoryAssembler.toEntity(repository)

                val c1Entity = entity.commits.find { it.sha == c1.sha }!!
                val c2Entity = entity.commits.find { it.sha == c2.sha }!!

                assertAll(
                    "shared author identity",
                    { assertThat(c1Entity.author).isSameAs(c2Entity.author) },
                    { assertThat(c1Entity.committer).isSameAs(c2Entity.committer) },
                    // Only one user entity created
                    { assertThat(entity.user).hasSize(1) }
                )
            }
        }

        @Nested
        inner class BranchHandling {

            @Test
            fun `repository with single branch, check structure`() {
                val project = Project(name = "TestProject")
                val repository = Repository(localPath = "/test/repo", project = project)

                val commit = createCommit(
                    sha = "a".repeat(40),
                    commitDateTime = LocalDateTime.now(),
                    repository = repository
                )

                val branch = Branch(
                    name = "main",
                    fullName = "refs/heads/main",
                    category = ReferenceCategory.LOCAL_BRANCH,
                    head = commit,
                    repository = repository
                )

                val entity = repositoryAssembler.toEntity(repository)

                assertAll(
                    "branch structure",
                    { assertThat(entity.branches).hasSize(1) },
                    { assertThat(entity.branches.first().name).isEqualTo("main") },
                    { assertThat(entity.branches.first().head.sha).isEqualTo(commit.sha) }
                )
            }

            @Test
            fun `multiple branches pointing to different commits`() {
                val project = Project(name = "TestProject")
                val repository = Repository(localPath = "/test/repo", project = project)

                val c1 = createCommit(sha = "1".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)
                val c2 = createCommit(sha = "2".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)

                val mainBranch = Branch(
                    name = "main",
                    fullName = "refs/heads/main",
                    category = ReferenceCategory.LOCAL_BRANCH,
                    head = c1,
                    repository = repository
                )
                val devBranch = Branch(
                    name = "dev",
                    fullName = "refs/heads/dev",
                    category = ReferenceCategory.LOCAL_BRANCH,
                    head = c2,
                    repository = repository
                )

                val entity = repositoryAssembler.toEntity(repository)

                assertAll(
                    "multiple branches",
                    { assertThat(entity.branches).hasSize(2) },
                    { assertThat(entity.branches.map { it.name }).containsExactlyInAnyOrder("main", "dev") }
                )
            }

            @Test
            fun `multiple branches pointing to same commit`() {
                val project = Project(name = "TestProject")
                val repository = Repository(localPath = "/test/repo", project = project)

                val commit = createCommit(sha = "a".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)

                val main = Branch(
                    name = "main",
                    fullName = "refs/heads/main",
                    category = ReferenceCategory.LOCAL_BRANCH,
                    head = commit,
                    repository = repository
                )
                val release = Branch(
                    name = "release",
                    fullName = "refs/heads/release",
                    category = ReferenceCategory.LOCAL_BRANCH,
                    head = commit,
                    repository = repository
                )

                val entity = repositoryAssembler.toEntity(repository)

                val mainEntity = entity.branches.find { it.name == "main" }!!
                val releaseEntity = entity.branches.find { it.name == "release" }!!

                // Both branches should point to same commit entity (identity preserved)
                assertThat(mainEntity.head).isSameAs(releaseEntity.head)
            }
        }

        @Nested
        inner class RemoteHandling {

            @Test
            fun `repository with remotes maps to entity`() {
                val project = Project(name = "TestProject")
                val repository = Repository(localPath = "/test/repo", project = project)

                Remote(name = "origin", url = "https://example.com/repo.git", repository = repository)

                val entity = repositoryAssembler.toEntity(repository)

                assertAll(
                    { assertThat(entity.remotes).hasSize(1) },
                    { assertThat(entity.remotes.first().name).isEqualTo("origin") },
                    { assertThat(entity.remotes.first().url).isEqualTo("https://example.com/repo.git") }
                )
            }

            @Test
            fun `remote entities map back to domain`() {
                val projectEntity = ProjectEntity(name = "TestProject", iid = Project.Id(
                    Uuid.random()
                )).apply { id = 1L }
                val repositoryEntity = RepositoryEntity(localPath = "/test/repo", project = projectEntity, iid = Repository.Id(
                    Uuid.random()
                )).apply { id = 2L }
                val remoteEntity = RemoteEntity(
                    name = "origin",
                    url = "https://example.com/repo.git",
                    repository = repositoryEntity,
                    iid = Remote.Id(Uuid.random())
                ).apply { id = 3L }

                val domain = repositoryAssembler.toDomain(repositoryEntity)

                assertAll(
                    { assertThat(domain.remotes).hasSize(1) },
                    { assertThat(domain.remotes.first().name).isEqualTo(remoteEntity.name) },
                    { assertThat(domain.remotes.first().url).isEqualTo(remoteEntity.url) }
                )
            }
        }

        @Nested
        inner class IdentityPreservation {

            @Test
            fun `fast-path when Repository already in context, returns cached entity`() {
                val project = Project(name = "TestProject")
                val repository = Repository(localPath = "/test/repo", project = project)

                // First call
                val entity1 = repositoryAssembler.toEntity(repository)

                clearMocks(ctx,repositoryMapper, recordedCalls = true)

                // Second call should return cached
                val entity2 = repositoryAssembler.toEntity(repository)

                assertAll(
                    { assertThat(entity2).isSameAs(entity1) },
                    { verify(exactly = 1) { ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(repository) } },
                    { verify(exactly = 0) { repositoryMapper.toEntity(any()) } }
                )
            }

            @Test
            fun `commits assembled multiple times in same context return same entities`() {
                val project = Project(name = "TestProject")
                val repository = Repository(localPath = "/test/repo", project = project)

                val commit = createCommit(sha = "a".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)

                // First assembly
                val entity1 = repositoryAssembler.toEntity(repository)
                val commitEntity1 = entity1.commits.first()

                clearMocks(ctx, recordedCalls = true)

                // Second assembly should reuse same commit entity
                val entity2 = repositoryAssembler.toEntity(repository)

                assertThat(entity2).isSameAs(entity1)
                assertThat(entity2.commits.first()).isSameAs(commitEntity1)
            }
        }

        @Nested
        inner class TwoPassAssembly {

            @Test
            fun `verify two-pass assembly - commits mapped before relationships wired`() {
                val project = Project(name = "TestProject")
                val repository = Repository(localPath = "/test/repo", project = project)

                val c1 = createCommit(sha = "1".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)
                val c2 = createCommit(sha = "2".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)
                c2.parents.add(c1)

                repositoryAssembler.toEntity(repository)

                verifyOrder {
                    // Phase 1: Map repository structure
                    repositoryMapper.toEntity(repository)

                    // Phase 2: Map all commits (both c1 and c2)
                    commitMapper.toEntity(c1)
                    commitMapper.toEntity(c2)

                    // Phase 2b: Wire relationships (after all commits mapped)
                    // Both commits looked up in context
                    ctx.findEntity<Commit.Key, Commit, CommitEntity>(c2)
                    ctx.findEntity<Commit.Key, Commit, CommitEntity>(c1)
                }
            }

            @Test
            fun `idempotent parent-child wiring - duplicate adds ignored`() {
                val project = Project(name = "TestProject")
                val repository = Repository(localPath = "/test/repo", project = project)

                val c1 = createCommit(sha = "1".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)
                val c2 = createCommit(sha = "2".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)

                // Add parent relationship twice (domain model prevents this, but test assembler resilience)
                c2.parents.add(c1)

                val entity = repositoryAssembler.toEntity(repository)

                val c1Entity = entity.commits.find { it.sha == c1.sha }!!
                val c2Entity = entity.commits.find { it.sha == c2.sha }!!

                // Relationship should only exist once
                assertAll(
                    { assertThat(c2Entity.parents).hasSize(1) },
                    { assertThat(c1Entity.children).hasSize(1) }
                )
            }
        }
    }

    @Nested
    inner class ToDomain {

        @Nested
        inner class MinimalRepository {

            @Test
            fun `empty RepositoryEntity, check structure`() {
                val projectEntity = ProjectEntity(name = "TestProject", iid = Project.Id(
                    Uuid.random()
                )).apply { id = 1L }
                val repositoryEntity = RepositoryEntity(
                    localPath = "/test/repo",
                    project = projectEntity,
                    iid = Repository.Id(
                        Uuid.random()
                    )
                ).apply { id = 2L }

                // Pre-map project
                val project = projectMapper.toDomain(projectEntity)

                clearMocks(ctx, recordedCalls = true)

                val domain = assertDoesNotThrow {
                    repositoryAssembler.toDomain(repositoryEntity)
                }

                assertAll(
                    "domain structure",
                    { assertThat(domain.localPath).isEqualTo(repositoryEntity.localPath) },
                    { assertThat(domain.project).isSameAs(project) },
                    { assertThat(domain.commits).isEmpty() },
                    { assertThat(domain.branches).isEmpty() }
                )
            }
        }

        @Nested
        inner class StandaloneAssembly {

            @Test
            fun `standalone toDomain without Project in context, creates minimal Project`() {
                val projectEntity = ProjectEntity(name = "TestProject",iid = Project.Id(
                    Uuid.random()
                )).apply { id = 1L }
                val repositoryEntity = RepositoryEntity(
                    localPath = "/test/repo",
                    project = projectEntity,
                    iid = Repository.Id(
                        Uuid.random()
                    )
                ).apply { id = 2L }

                // Don't pre-map project
                val domain = assertDoesNotThrow {
                    repositoryAssembler.toDomain(repositoryEntity)
                }

                assertAll(
                    "minimal project created",
                    { assertThat(domain.project).isNotNull() },
                    { assertThat(domain.project.name).isEqualTo(projectEntity.name) },
                    { assertThat(domain.project.repo).isSameAs(domain) }
                )
            }
        }

        @Nested
        inner class CommitGraphAssembly {

            @Test
            fun `single CommitEntity, check domain structure`() {
                val projectEntity = ProjectEntity(name = "TestProject", iid = Project.Id(
                    Uuid.random()
                )).apply { id = 1L }
                val repositoryEntity = RepositoryEntity(localPath = "/test/repo", project = projectEntity, iid = Repository.Id(
                    Uuid.random()
                )).apply { id = 2L }

                val commitEntity = CommitEntity(
                    sha = "a".repeat(40),
                    commitDateTime = LocalDateTime.of(2020, 1, 1, 0, 0),
                    repository = repositoryEntity,
                    iid = Commit.Id(
                        Uuid.random()
                    )
                ).apply { id = 10L }.withCommitter(repositoryEntity, "single")

                val domain = repositoryAssembler.toDomain(repositoryEntity)

                assertAll(
                    "single commit domain",
                    { assertThat(domain.commits).hasSize(1) },
                    { assertThat(domain.commits.first().sha).isEqualTo(commitEntity.sha) },
                    { assertThat(domain.commits.first().parents).isEmpty() },
                    { assertThat(domain.commits.first().children).isEmpty() }
                )
            }

            @Test
            fun `linear commit chain entities, verify domain relationships`() {
                val projectEntity = ProjectEntity(name = "TestProject", iid = Project.Id(
                    Uuid.random()
                )).apply { id = 1L }
                val repositoryEntity = RepositoryEntity(localPath = "/test/repo", project = projectEntity, iid = Repository.Id(
                    Uuid.random()
                )).apply { id = 2L }

                val c1 = CommitEntity(sha = "1".repeat(40), commitDateTime = LocalDateTime.now(), repository = repositoryEntity, iid = Commit.Id(
                    Uuid.random()
                )).apply { id = 10L }.withCommitter(repositoryEntity, "chain1")
                val c2 = CommitEntity(sha = "2".repeat(40), commitDateTime = LocalDateTime.now(), repository = repositoryEntity, iid = Commit.Id(
                    Uuid.random()
                )).apply { id = 11L }.withCommitter(repositoryEntity, "chain2")
                val c3 = CommitEntity(sha = "3".repeat(40), commitDateTime = LocalDateTime.now(), repository = repositoryEntity, iid = Commit.Id(
                    Uuid.random()
                )).apply { id = 12L }.withCommitter(repositoryEntity, "chain3")

                c2.parents.add(c1)
                c1.children.add(c2)
                c3.parents.add(c2)
                c2.children.add(c3)

                val domain = repositoryAssembler.toDomain(repositoryEntity)

                val c1Domain = domain.commits.find { it.sha == c1.sha }!!
                val c2Domain = domain.commits.find { it.sha == c2.sha }!!
                val c3Domain = domain.commits.find { it.sha == c3.sha }!!

                assertAll(
                    "linear chain domain",
                    { assertThat(c1Domain.parents).isEmpty() },
                    { assertThat(c1Domain.children).containsExactly(c2Domain) },
                    { assertThat(c2Domain.parents).containsExactly(c1Domain) },
                    { assertThat(c2Domain.children).containsExactly(c3Domain) },
                    { assertThat(c3Domain.parents).containsExactly(c2Domain) },
                    { assertThat(c3Domain.children).isEmpty() }
                )
            }

            @Test
            fun `diamond graph entities, verify merge domain relationships`() {
                val projectEntity = ProjectEntity(name = "TestProject", iid = Project.Id(
                    Uuid.random()
                )).apply { id = 1L }
                val repositoryEntity = RepositoryEntity(localPath = "/test/repo", project = projectEntity, iid = Repository.Id(
                    Uuid.random()
                )).apply { id = 2L }

                //     c1
                //    /  \
                //   c2  c3
                //    \  /
                //     c4
                val c1 = CommitEntity(sha = "1".repeat(40), commitDateTime = LocalDateTime.now(), repository = repositoryEntity,iid = Commit.Id(
                    Uuid.random()
                )).apply { id = 10L }.withCommitter(repositoryEntity, "diamond1")
                val c2 = CommitEntity(sha = "2".repeat(40), commitDateTime = LocalDateTime.now(), repository = repositoryEntity,iid = Commit.Id(
                    Uuid.random()
                )).apply { id = 11L }.withCommitter(repositoryEntity, "diamond2")
                val c3 = CommitEntity(sha = "3".repeat(40), commitDateTime = LocalDateTime.now(), repository = repositoryEntity,iid = Commit.Id(
                    Uuid.random()
                )).apply { id = 12L }.withCommitter(repositoryEntity, "diamond3")
                val c4 = CommitEntity(sha = "4".repeat(40), commitDateTime = LocalDateTime.now(), repository = repositoryEntity,iid = Commit.Id(
                    Uuid.random()
                )).apply { id = 13L }.withCommitter(repositoryEntity, "diamond4")

                c2.parents.add(c1)
                c1.children.add(c2)
                c3.parents.add(c1)
                c1.children.add(c3)
                c4.parents.add(c2)
                c4.parents.add(c3)
                c2.children.add(c4)
                c3.children.add(c4)

                val domain = repositoryAssembler.toDomain(repositoryEntity)

                val c1Domain = domain.commits.find { it.sha == c1.sha }!!
                val c2Domain = domain.commits.find { it.sha == c2.sha }!!
                val c3Domain = domain.commits.find { it.sha == c3.sha }!!
                val c4Domain = domain.commits.find { it.sha == c4.sha }!!

                assertAll(
                    "diamond domain relationships",
                    { assertThat(c1Domain.children).containsExactlyInAnyOrder(c2Domain, c3Domain) },
                    { assertThat(c2Domain.children).containsExactly(c4Domain) },
                    { assertThat(c3Domain.children).containsExactly(c4Domain) },
                    { assertThat(c4Domain.parents).containsExactlyInAnyOrder(c2Domain, c3Domain) }
                )
            }

            @Test
            fun `bidirectional domain relationships automatically maintained`() {
                val projectEntity = ProjectEntity(name = "TestProject", iid = Project.Id(
                    Uuid.random()
                )).apply { id = 1L }
                val repositoryEntity = RepositoryEntity(localPath = "/test/repo", project = projectEntity, iid = Repository.Id(
                    Uuid.random()
                )).apply { id = 2L }

                val c1 = CommitEntity(sha = "1".repeat(40), commitDateTime = LocalDateTime.now(), repository = repositoryEntity, iid = Commit.Id(
                    Uuid.random()
                )).apply { id = 10L }.withCommitter(repositoryEntity, "bidirectional1")
                val c2 = CommitEntity(sha = "2".repeat(40), commitDateTime = LocalDateTime.now(), repository = repositoryEntity, iid = Commit.Id(
                    Uuid.random()
                )).apply { id = 11L }.withCommitter(repositoryEntity, "bidirectional2")

                c2.parents.add(c1)
                c1.children.add(c2)

                val domain = repositoryAssembler.toDomain(repositoryEntity)

                val c1Domain = domain.commits.find { it.sha == c1.sha }!!
                val c2Domain = domain.commits.find { it.sha == c2.sha }!!

                // Domain model automatically maintains bidirectionality
                assertAll(
                    "automatic bidirectional wiring",
                    { assertThat(c2Domain.parents).contains(c1Domain) },
                    { assertThat(c1Domain.children).contains(c2Domain) }
                )
            }
        }

        @Nested
        inner class AuthorCommitterRelationships {

            @Test
            fun `CommitEntity with author and committer, mapped to domain`() {
                val projectEntity = ProjectEntity(name = "TestProject", iid = Project.Id(
                    Uuid.random()
                )).apply { id = 1L }
                val repositoryEntity = RepositoryEntity(localPath = "/test/repo", project = projectEntity, iid = Repository.Id(
                    Uuid.random()
                )).apply { id = 2L }

                val commitEntity = CommitEntity(
                    sha = "a".repeat(40),
                    commitDateTime = LocalDateTime.now(),
                    repository = repositoryEntity,
                    iid = Commit.Id(
                        Uuid.random()
                    )
                ).apply { id = 10L }

                val authorEntity = UserEntity(
                    name = "Author",
                    email = "author@example.com",
                    repository = repositoryEntity,
                    iid = User.Id(
                        Uuid.random()
                    )
                ).apply { id = 20L }

                val committerEntity = UserEntity(
                    name = "Committer",
                    email = "committer@example.com",
                    repository = repositoryEntity,
                    iid = User.Id(
                        Uuid.random()
                    )
                ).apply { id = 21L }

                commitEntity.author = authorEntity
                commitEntity.committer = committerEntity

                val domain = repositoryAssembler.toDomain(repositoryEntity)

                val commitDomain = domain.commits.first()

                assertAll(
                    "author/committer in domain",
                    { assertThat(commitDomain.author).isNotNull() },
                    { assertThat(commitDomain.author?.name).isEqualTo("Author") },
                    { assertThat(commitDomain.committer).isNotNull() },
                    { assertThat(commitDomain.committer?.name).isEqualTo("Committer") }
                )
            }

            @Test
            fun `same UserEntity for author and committer, identity preserved in domain`() {
                val projectEntity = ProjectEntity(name = "TestProject", iid = Project.Id(
                    Uuid.random()
                )).apply { id = 1L }
                val repositoryEntity = RepositoryEntity(localPath = "/test/repo", project = projectEntity, iid = Repository.Id(
                    Uuid.random()
                )).apply { id = 2L }

                val commitEntity = CommitEntity(
                    sha = "a".repeat(40),
                    commitDateTime = LocalDateTime.now(),
                    repository = repositoryEntity,
                    iid = Commit.Id(
                        Uuid.random()
                    )
                ).apply { id = 10L }

                val userEntity = UserEntity(
                    name = "Same User",
                    email = "user@example.com",
                    repository = repositoryEntity,
                    iid = User.Id(
                        Uuid.random()
                    )
                ).apply { id = 20L }

                commitEntity.author = userEntity
                commitEntity.committer = userEntity

                val domain = repositoryAssembler.toDomain(repositoryEntity)

                val commitDomain = domain.commits.first()

                // Same domain object for both roles
                assertThat(commitDomain.author).isSameAs(commitDomain.committer)
            }
        }

        @Nested
        inner class BranchHandling {

            @Test
            fun `BranchEntity mapped to Branch domain`() {
                val projectEntity = ProjectEntity(name = "TestProject", iid = Project.Id(
                    Uuid.random()
                )).apply { id = 1L }
                val repositoryEntity = RepositoryEntity(localPath = "/test/repo", project = projectEntity, iid = Repository.Id(
                    Uuid.random()
                )).apply { id = 2L }

                val commitEntity = CommitEntity(
                    sha = "a".repeat(40),
                    commitDateTime = LocalDateTime.now(),
                    repository = repositoryEntity,
                    iid = Commit.Id(
                        Uuid.random()
                    )
                ).apply { id = 10L }.withCommitter(repositoryEntity, "branch-domain")

                val branchEntity = BranchEntity(
                    name = "main",
                    fullName = "refs/heads/main",
                    category = ReferenceCategory.LOCAL_BRANCH,
                    head = commitEntity,
                    repository = repositoryEntity,
                    iid = Reference.Id(
                        Uuid.random()
                    )
                ).apply { id = 20L }

                val domain = repositoryAssembler.toDomain(repositoryEntity)

                assertAll(
                    "branch domain",
                    { assertThat(domain.branches).hasSize(1) },
                    { assertThat(domain.branches.first().name).isEqualTo("main") },
                    { assertThat(domain.branches.first().head.sha).isEqualTo(commitEntity.sha) }
                )
            }
        }

        @Nested
        inner class IdentityPreservation {

            @Test
            fun `fast-path when RepositoryEntity already in context, returns cached domain`() {
                val projectEntity = ProjectEntity(name = "TestProject", iid = Project.Id(
                    Uuid.random()
                )).apply { id = 1L }
                val repositoryEntity = RepositoryEntity(localPath = "/test/repo", project = projectEntity, iid = Repository.Id(
                    Uuid.random()
                )).apply { id = 2L }

                // First call
                val domain1 = repositoryAssembler.toDomain(repositoryEntity)

                clearMocks(ctx, repositoryMapper, recordedCalls = true)

                // Second call should return cached
                val domain2 = repositoryAssembler.toDomain(repositoryEntity)

                assertAll(
                    { assertThat(domain2).isSameAs(domain1) },
                    { verify(exactly = 1) { ctx.findDomain<Repository, RepositoryEntity>(repositoryEntity) } },
                    { verify(exactly = 0) { repositoryMapper.toDomain(any()) } }
                )
                confirmVerified(ctx, repositoryMapper)
            }

            @Test
            fun `toEntity followed by toDomain preserves object identity`() {
                val project = Project(name = "TestProject")
                val repository = Repository(localPath = "/test/repo", project = project)

                val commit = createCommit(sha = "a".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)
                val branch = Branch(
                    name = "main",
                    fullName = "refs/heads/main",
                    category = ReferenceCategory.LOCAL_BRANCH,
                    head = commit,
                    repository = repository
                )

                val entity = repositoryAssembler.toEntity(repository)
                val domainFromEntity = repositoryAssembler.toDomain(entity)

                // Should get back same domain objects
                assertAll(
                    { assertThat(domainFromEntity).isSameAs(repository) },
                    { assertThat(domainFromEntity.commits.first()).isSameAs(commit) },
                    { assertThat(domainFromEntity.branches.first()).isSameAs(branch) }
                )
            }

            @Test
            fun `toDomain followed by toEntity preserves object identity`() {
                val projectEntity = ProjectEntity(name = "TestProject", iid = Project.Id(
                    Uuid.random()
                )).apply { id = 1L }
                val repositoryEntity = RepositoryEntity(localPath = "/test/repo", project = projectEntity, iid = Repository.Id(
                    Uuid.random()
                )).apply { id = 2L }

                val commitEntity = CommitEntity(
                    sha = "a".repeat(40),
                    commitDateTime = LocalDateTime.now(),
                    repository = repositoryEntity,
                    iid = Commit.Id(
                        Uuid.random()
                    )
                ).apply { id = 10L }.withCommitter(repositoryEntity, "identity-entity")

                val domain = repositoryAssembler.toDomain(repositoryEntity)
                val entityFromDomain = repositoryAssembler.toEntity(domain)

                // Should get back same entity objects
                assertAll(
                    { assertThat(entityFromDomain).isSameAs(repositoryEntity) },
                    { assertThat(entityFromDomain.commits.first()).isSameAs(commitEntity) }
                )
            }
        }
    }

    @Nested
    inner class EdgeCases {

        @Test
        fun `context reset breaks identity preservation`() {
            val project = Project(name = "TestProject")
            val repository = Repository(localPath = "/test/repo", project = project)

            val entity1 = repositoryAssembler.toEntity(repository)

            ctx.reset()

            val entity2 = repositoryAssembler.toEntity(repository)

            assertThat(entity2).isNotSameAs(entity1)
        }

        @Test
        fun `repository with commits but no branches`() {
            val project = Project(name = "TestProject")
            val repository = Repository(localPath = "/test/repo", project = project)

            val commit = createCommit(sha = "a".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)

            val entity = repositoryAssembler.toEntity(repository)

            assertAll(
                { assertThat(entity.commits).hasSize(1) },
                { assertThat(entity.branches).isEmpty() }
            )
        }

        @Test
        fun `repository with branches but commits only implicitly added`() {
            val project = Project(name = "TestProject")
            val repository = Repository(localPath = "/test/repo", project = project)

            // Commit auto-adds itself to repository.commits in init
            val commit = createCommit(sha = "a".repeat(40), commitDateTime = LocalDateTime.now(), repository = repository)
            val branch = Branch(
                name = "main",
                fullName = "refs/heads/main",
                category = ReferenceCategory.LOCAL_BRANCH,
                head = commit,
                repository = repository
            )

            val entity = repositoryAssembler.toEntity(repository)

            assertAll(
                { assertThat(entity.commits).hasSize(1) },
                { assertThat(entity.branches).hasSize(1) }
            )
        }
    }
}
