package com.inso_world.binocular.infrastructure.sql.integration.service

import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.TestData
import com.inso_world.binocular.infrastructure.sql.integration.service.base.BaseServiceTest
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Revision
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Stats
import com.inso_world.binocular.model.User
import com.inso_world.binocular.model.vcs.ReferenceCategory
import com.inso_world.binocular.model.vcs.Remote
import jakarta.validation.ConstraintViolationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi

internal class RepositoryInfrastructurePortImplTest : BaseServiceTest() {
    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePort

    @Autowired
    private lateinit var commitPort: CommitInfrastructurePort

    @Autowired
    private lateinit var userPort: UserInfrastructurePort

    @Autowired
    private lateinit var branchPort: BranchInfrastructurePort

    private lateinit var testProject: Project

    @BeforeEach
    fun setup() {
//        setUp()
        testProject = projectPort.create(TestData.Domain.testProject(name = "Test Project", id = null))
    }

    @AfterEach
    fun cleanup() {
        tearDown()
    }

    private fun createCommit(
        repository: Repository,
        sha: String,
        commitDateTime: LocalDateTime,
        authorDateTime: LocalDateTime? = null,
        message: String? = null,
        committer: User? = null,
    ): Commit {
        val resolvedCommitter = committer ?: User(
            name = "Committer-${sha.take(6)}", repository = repository
        ).apply {
            email = "${sha.take(6)}@integration.test"
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

    @Nested
    @DisplayName("Create Operations")
    inner class CreateOperations {
        @Test
        fun `create empty repository with minimal required fields`() {
            val repository = Repository(
                localPath = "/path/to/repo", project = testProject
            )

            val created = repositoryPort.create(repository)

            assertAll(
                "Verify created repository",
                { assertThat(created.localPath).isEqualTo("/path/to/repo") },
                { assertThat(created.project).isEqualTo(testProject) },
                { assertThat(created.id).isNotNull() },
                { assertThat(created.iid).isNotNull() },
                { assertThat(created.commits).isEmpty() },
                { assertThat(created.branches).isEmpty() },
                { assertThat(created.user).isEmpty() },
                { assertThat(created.remotes).isEmpty() })

            assertThat(repositoryPort.findAll()).hasSize(1)
        }

        @Test
        fun `create repository with single commit`() {
            val repository = Repository(
                localPath = "/repo/path", project = testProject
            )
            val commit = createCommit(
                sha = "a".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 1, 12, 0),
                authorDateTime = LocalDateTime.of(2024, 1, 1, 11, 0),
                message = "Initial commit",
                repository = repository
            )

            val created = repositoryPort.create(repository)

            assertAll(
                "Verify repository with commit",
                { assertThat(created.commits).hasSize(1) },
                { assertThat(created.commits.first().sha).isEqualTo("a".repeat(40)) },
                { assertThat(created.commits.first().message).isEqualTo("Initial commit") })
            assertThat(commitPort.findAll()).hasSize(1)
        }

        @Test
        fun `create repository with commit, branch, and user`() {
            val repository = Repository(
                localPath = "/complex/repo", project = testProject
            )
            val user = User(name = "John Doe", repository = repository).apply {
                email = "john@example.com"
            }
            val commit = createCommit(
                sha = "b".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 2, 15, 10, 30),
                message = "Feature commit",
                repository = repository,
                committer = user
            )
            commit.author = user

            val branch = Branch(
                name = "main",
                fullName = "refs/heads/main",
                category = ReferenceCategory.LOCAL_BRANCH,
                repository = repository,
                head = commit
            )

            val created = repositoryPort.create(repository)

            assertAll(
                "Verify complex repository structure",
                { assertThat(created.commits).hasSize(1) },
                { assertThat(created.branches).hasSize(1) },
                { assertThat(created.user).hasSize(1) },
                { assertThat(created.branches.first().name).isEqualTo("main") },
                { assertThat(created.branches.first().head).isEqualTo(created.commits.first()) },
                { assertThat(created.user.first().name).isEqualTo("John Doe") })
            assertAll(
                "Verify persistence layer counts",
                { assertThat(repositoryPort.findAll()).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(1) },
                { assertThat(branchPort.findAll()).hasSize(1) },
                { assertThat(userPort.findAll()).hasSize(1) })
        }

        @Test
        fun `create repository with parent-child commit relationship`() {
            val repository = Repository(
                localPath = "/parent-child/repo", project = testProject
            )
            val parentCommit = createCommit(
                sha = "1".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 1, 10, 0),
                message = "Parent",
                repository = repository
            )
            val childCommit = createCommit(
                sha = "2".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 2, 10, 0),
                message = "Child",
                repository = repository
            )
            childCommit.parents.add(parentCommit)

            val created = repositoryPort.create(repository)

            assertAll("Verify parent-child relationship", { assertThat(created.commits).hasSize(2) }, {
                val child = created.commits.find { it.sha == "2".repeat(40) }
                assertThat(child?.parents).hasSize(1)
            }, {
                val parent = created.commits.find { it.sha == "1".repeat(40) }
                assertThat(parent?.children).hasSize(1)
            })
        }

        @Test
        fun `create repository with merge commit (two parents)`() {
            val repository = Repository(
                localPath = "/merge/repo", project = testProject
            )
            val parent1 = createCommit(
                sha = "a".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 1, 10, 0),
                message = "Parent 1",
                repository = repository
            )
            val parent2 = createCommit(
                sha = "b".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 1, 11, 0),
                message = "Parent 2",
                repository = repository
            )
            val mergeCommit = createCommit(
                sha = "c".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 2, 10, 0),
                message = "Merge commit",
                repository = repository
            )
            mergeCommit.parents.addAll(listOf(parent1, parent2))

            val created = repositoryPort.create(repository)

            assertAll("Verify merge commit structure", { assertThat(created.commits).hasSize(3) }, {
                val merge = created.commits.find { it.sha == "c".repeat(40) }
                assertThat(merge?.parents).hasSize(2)
            }, {
                val p1 = created.commits.find { it.sha == "a".repeat(40) }
                assertThat(p1?.children).hasSize(1)
            }, {
                val p2 = created.commits.find { it.sha == "b".repeat(40) }
                assertThat(p2?.children).hasSize(1)
            })
        }

        @Test
        fun `create repository with multiple branches pointing to same commit`() {
            val repository = Repository(
                localPath = "/multi-branch/repo", project = testProject
            )
            val commit = createCommit(
                sha = "f".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 1, 12, 0),
                message = "Shared commit",
                repository = repository
            )
            val branch1 = Branch(
                name = "main",
                fullName = "refs/heads/main",
                category = ReferenceCategory.LOCAL_BRANCH,
                repository = repository,
                head = commit
            )
            val branch2 = Branch(
                name = "develop",
                fullName = "refs/heads/develop",
                category = ReferenceCategory.LOCAL_BRANCH,
                repository = repository,
                head = commit
            )

            val created = repositoryPort.create(repository)

            assertAll(
                "Verify multiple branches",
                { assertThat(created.commits).hasSize(1) },
                { assertThat(created.branches).hasSize(2) },
                {
                    val branchNames = created.branches.map { it.name }
                    assertThat(branchNames).containsExactlyInAnyOrder("main", "develop")
                },
                {
                    created.branches.forEach { branch ->
                        assertThat(branch.head.sha).isEqualTo("f".repeat(40))
                    }
                })
        }

        @Test
        fun `create repository with remote`() {
            val repository = Repository(
                localPath = "/remote/repo", project = testProject
            )
            val remote = Remote(
                name = "origin", url = "https://github.com/example/repo.git", repository = repository
            )

            val created = repositoryPort.create(repository)

            assertAll(
                "Verify remote",
                { assertThat(created.remotes).hasSize(1) },
                { assertThat(created.remotes.first().name).isEqualTo("origin") },
                { assertThat(created.remotes.first().url).isEqualTo("https://github.com/example/repo.git") })
        }
    }

    @Nested
    @DisplayName("Update Operations")
    inner class UpdateOperations {
        @Test
        fun `update repository by adding new commit`() {
            val repository = repositoryPort.create(
                Repository(
                    localPath = "/update/repo", project = testProject
                )
            )

            val newCommit = createCommit(
                sha = "f".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 3, 1, 10, 0),
                message = "New commit",
                repository = repository
            )

            val updated = repositoryPort.update(repository)

            assertAll(
                "Verify updated repository",
                { assertThat(updated.commits).hasSize(1) },
                { assertThat(updated.commits.first().sha).isEqualTo("f".repeat(40)) })
        }

        @Test
        fun `update repository by adding branch to existing commit`() {
            val repository = Repository(
                localPath = "/branch-update/repo", project = testProject
            )
            val commit = createCommit(
                sha = "e".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 1, 12, 0),
                message = "Commit",
                repository = repository
            )
            val created = repositoryPort.create(repository)

            val newBranch = Branch(
                name = "feature",
                fullName = "refs/heads/feature",
                category = ReferenceCategory.LOCAL_BRANCH,
                repository = created,
                head = created.commits.first()
            )

            val updated = repositoryPort.update(created)

            assertAll(
                "Verify branch addition",
                { assertThat(updated.branches).hasSize(1) },
                { assertThat(updated.branches.first().name).isEqualTo("feature") },
                { assertThat(updated.commits).hasSize(1) })
        }

        @Test
        fun `update repository idempotency - updating with same data`() {
            val repository = Repository(
                localPath = "/idempotent/repo", project = testProject
            )
            val commit = createCommit(
                sha = "e".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 1, 12, 0),
                message = "Commit",
                repository = repository
            )
            val created = repositoryPort.create(repository)

            val firstUpdate = repositoryPort.update(created)
            val secondUpdate = repositoryPort.update(firstUpdate)

            assertAll(
                "Verify idempotency",
                { assertThat(firstUpdate.commits).hasSize(1) },
                { assertThat(secondUpdate.commits).hasSize(1) },
                { assertThat(commitPort.findAll()).hasSize(1) },
                {
                    assertThat(firstUpdate).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(secondUpdate)
                })
        }

        @Test
        fun `update repository with commit graph expansion`() {
            val repository = Repository(
                localPath = "/expand/repo", project = testProject
            )
            val commit1 = createCommit(
                sha = "f".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 1, 10, 0),
                message = "First",
                repository = repository
            )
            val created = repositoryPort.create(repository)

            val commit2 = createCommit(
                sha = "e".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 2, 10, 0),
                message = "Second",
                repository = created
            )
            commit2.parents.add(commit1)

            val commit3 = createCommit(
                sha = "c".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 3, 10, 0),
                message = "Third",
                repository = created
            )
            commit3.parents.add(commit2)

            val updated = repositoryPort.update(created)

            assertAll("Verify commit graph expansion", { assertThat(updated.commits).hasSize(3) }, {
                val c3 = updated.commits.find { it.sha == "c".repeat(40) }
                assertThat(c3?.parents).hasSize(1)
                assertThat(c3?.parents?.first()?.sha).isEqualTo("e".repeat(40))
            }, {
                val c2 = updated.commits.find { it.sha == "e".repeat(40) }
                assertThat(c2?.parents).hasSize(1)
                assertThat(c2?.children).hasSize(1)
            }, {
                val c1 = updated.commits.find { it.sha == "f".repeat(40) }
                assertThat(c1?.children).hasSize(1)
            })
        }

        @Test
        fun `update repository with multiple users`() {
            val repository = Repository(
                localPath = "/multi-user/repo", project = testProject
            )
            val user1 = User(name = "Alice", repository = repository).apply { email = "alice@example.com" }
            val commit = createCommit(
                sha = "e".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 1, 12, 0),
                message = "Commit",
                repository = repository,
                committer = user1
            )
            commit.author = user1

            val created = repositoryPort.create(repository)

            val user2 = User(name = "Bob", repository = created).apply { email = "bob@example.com" }
            val commit2 = createCommit(
                sha = "c".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 2, 12, 0),
                message = "Second commit",
                repository = created,
                committer = user2
            )
            commit2.author = user2

            val updated = repositoryPort.update(created)

            assertAll(
                "Verify multiple users",
                { assertThat(updated.user).hasSize(2) },
                { assertThat(updated.commits).hasSize(2) },
                {
                    val userNames = updated.user.map { it.name }
                    assertThat(userNames).containsExactlyInAnyOrder("Alice", "Bob")
                })
        }

        @Test
        fun `update repository by adding remote`() {
            val repository = repositoryPort.create(
                Repository(
                    localPath = "/remote-update/repo", project = testProject
                )
            )

            val remote = Remote(
                name = "upstream", url = "https://github.com/upstream/repo.git", repository = repository
            )

            val updated = repositoryPort.update(repository)

            assertAll(
                "Verify remote addition",
                { assertThat(updated.remotes).hasSize(1) },
                { assertThat(updated.remotes.first().name).isEqualTo("upstream") })
        }
    }

    @Nested
    @DisplayName("Read Operations")
    inner class ReadOperations {
        @Test
        fun `findAll returns all repositories`() {
            repositoryPort.create(Repository(localPath = "/repo1", project = testProject))
            with(projectPort.create(testProject.copy(name = "Test 2"))) {
                repositoryPort.create(Repository(localPath = "/repo2", project = this))
            }
            with(projectPort.create(testProject.copy(name = "Test 3"))) {
                repositoryPort.create(Repository(localPath = "/repo3", project = this))
            }

            val all = repositoryPort.findAll()

            assertThat(all).hasSize(3)
        }

        @Test
        fun `findByIid returns correct repository`() {
            val created = repositoryPort.create(
                Repository(
                    localPath = "/specific/repo", project = testProject
                )
            )

            val found = repositoryPort.findByIid(created.iid)

            assertAll(
                "Verify found repository",
                { assertThat(found).isNotNull },
                { assertThat(found?.localPath).isEqualTo("/specific/repo") },
                { assertThat(found?.iid).isEqualTo(created.iid) })
        }

        @OptIn(ExperimentalUuidApi::class)
        @Test
        fun `findByIid returns null for non-existent iid`() {
            val nonExistentIid = Repository.Id(kotlin.uuid.Uuid.random())
            val found = repositoryPort.findByIid(nonExistentIid)

            assertThat(found).isNull()
        }

        @Test
        fun `findByName returns correct repository`() {
            repositoryPort.create(Repository(localPath = "target-repo", project = testProject))
            with(projectPort.create(testProject.copy(name = "Test 2"))) {
                repositoryPort.create(
                    Repository(
                        localPath = "other-repo",
                        project = this
                    )
                )
            }

            val found = repositoryPort.findByName("target-repo")

            assertAll(
                "Verify found repository",
                { assertThat(found).isNotNull },
                { assertThat(found?.localPath).isEqualTo("target-repo") })
        }

        @Test
        fun `findByName returns null for non-existent name`() {
            val found = repositoryPort.findByName("non-existent")

            assertThat(found).isNull()
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    inner class DeleteOperations {
        @Test
        fun `delete repository by entity`() {
            val repository = repositoryPort.create(
                Repository(
                    localPath = "/delete/repo", project = testProject
                )
            )

            assertThrows<UnsupportedOperationException> { repositoryPort.delete(repository) }

//            assertAll(
//                "Verify deletion",
//                { assertThat(repositoryPort.findAll()).isEmpty() },
//                { assertThat(repositoryPort.findByIid(repository.iid)).isNull() })
        }

        @Test
        fun `delete repository by id`() {
            val repository = repositoryPort.create(
                Repository(
                    localPath = "/delete-by-id/repo", project = testProject
                )
            )
            val id = repository.id!!

            assertThrows<UnsupportedOperationException> { repositoryPort.deleteById(id) }

//            assertAll(
//                "Verify deletion by id",
//                { assertThat(repositoryPort.findAll()).isEmpty() },
//                { assertThat(repositoryPort.findByIid(repository.iid)).isNull() })
        }

        @Test
        fun `deleteAll removes all repositories`() {
            repositoryPort.create(Repository(localPath = "/repo1", project = testProject))
            with(testProject.copy(name = "Test 2")) {
                projectPort.create(this)
                repositoryPort.create(Repository(localPath = "/repo2", project = this))
            }
            with(testProject.copy(name = "Test 3")) {
                projectPort.create(this)
                repositoryPort.create(Repository(localPath = "/repo3", project = this))
            }

            assertThrows<UnsupportedOperationException> { repositoryPort.deleteAll() }

//            assertThat(repositoryPort.findAll()).isEmpty()
        }

        @Test
        fun `delete repository with commits deletes cascaded entities`() {
            val repository = Repository(
                localPath = "/cascade/repo", project = testProject
            )
            val commit = createCommit(
                sha = "d".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 1, 12, 0),
                message = "Commit",
                repository = repository
            )
            val created = repositoryPort.create(repository)

            assertThrows<UnsupportedOperationException> { repositoryPort.delete(created) }

//            assertAll(
//                "Verify cascaded deletion",
//                { assertThat(repositoryPort.findAll()).isEmpty() },
//                { assertThat(commitPort.findAll()).isEmpty() })
        }
    }


    @Nested
    @DisplayName("Domain Invariant Tests")
    inner class DomainInvariantTests {
        @Test
        fun `repository enforces non-blank localPath`() {
            assertThrows<IllegalArgumentException> {
                Repository(
                    localPath = "   ", project = testProject
                )
            }
        }

        @Test
        fun `repository links itself to project on construction`() {
            val project = projectPort.create(TestData.Domain.testProject(name = "Link Test", id = null))
            val repository = Repository(
                localPath = "/link/test", project = project
            )

            assertThat(project.repo).isEqualTo(repository)
        }

        @Test
        fun `repository commits collection is add-only`() {
            val repository = Repository(
                localPath = "/add-only/repo", project = testProject
            )
            val commit = createCommit(
                sha = "f".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 1, 12, 0),
                message = "Test",
                repository = repository
            )

            assertThrows<UnsupportedOperationException> {
                repository.commits.remove(commit)
            }
        }

        @Test
        fun `repository rejects commit from different repository`() {
            val repo1 = Repository(localPath = "/repo1", project = testProject)
            val repo2 = with(testProject.copy(name = testProject.name + "2")) {
                Repository(localPath = "/repo2", project = this)
            }
            val commit = createCommit(
                sha = "f".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 1, 12, 0),
                message = "Test",
                repository = repo1
            )

            assertThrows<IllegalArgumentException> {
                repo2.commits.add(commit)
            }
        }

        @Test
        fun `repository commits collection deduplicates by uniqueKey`() {
            val repository = Repository(
                localPath = "/dedup/repo", project = testProject
            )
            val commit1 = createCommit(
                sha = "f".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 1, 12, 0),
                message = "First",
                repository = repository
            )
            val commit2 = createCommit(
                sha = "f".repeat(40), // Same SHA
                commitDateTime = LocalDateTime.of(2024, 1, 1, 12, 0), message = "Second", repository = repository
            )

            val added1 = repository.commits.add(commit1)
            val added2 = repository.commits.add(commit2)

            assertAll(
                "Verify deduplication",
                { assertThat(commit1).isNotSameAs(commit2) },
                { assertThat(repository.commits).hasSize(1) },
                { assertThat(repository.commits.first()).isSameAs(commit1) },
            )
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    inner class EdgeCasesAndBoundaryConditions {
        @Test
        fun `create repository with 255 character long path, should succeed`() {
            val longPath = "2".repeat(255)
            val repository = Repository(
                localPath = longPath, project = testProject
            )

            val created = repositoryPort.create(repository)

            assertThat(created.localPath).isEqualTo(longPath)
        }

        @Test
        fun `create repository with 256 character long path, should fail`() {
            val longPath = "2".repeat(256)
            val repository = Repository(
                localPath = longPath, project = testProject
            )

            assertThrows<ConstraintViolationException> {
                repositoryPort.create(repository)
            }
        }

        @Test
        fun `create repository with special characters in path`() {
            val specialPath = "/path/with-special_chars.@#$%/repo"
            val repository = Repository(
                localPath = specialPath, project = testProject
            )

            val created = repositoryPort.create(repository)

            assertThat(created.localPath).isEqualTo(specialPath)
        }

        @Test
        fun `create repository with complex commit graph`() {
            val repository = Repository(
                localPath = "/complex-graph/repo", project = testProject
            )

            // Create diamond-shaped commit graph
            //     c1
            //    /  \
            //   c2  c3
            //    \  /
            //     c4
            val c1 = createCommit(
                sha = "1".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 1, 10, 0),
                message = "C1",
                repository = repository
            )
            val c2 = createCommit(
                sha = "2".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 2, 10, 0),
                message = "C2",
                repository = repository
            )
            val c3 = createCommit(
                sha = "3".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 3, 10, 0),
                message = "C3",
                repository = repository
            )
            val c4 = createCommit(
                sha = "4".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 4, 10, 0),
                message = "C4",
                repository = repository
            )

            c2.parents.add(c1)
            c3.parents.add(c1)
            c4.parents.addAll(listOf(c2, c3))

            val created = repositoryPort.create(repository)

            assertAll("Verify diamond graph structure", { assertThat(created.commits).hasSize(4) }, {
                val commit1 = created.commits.find { it.sha == "1".repeat(40) }
                assertThat(commit1?.children).hasSize(2)
            }, {
                val commit4 = created.commits.find { it.sha == "4".repeat(40) }
                assertThat(commit4?.parents).hasSize(2)
            })
        }

        @Test
        fun `update repository with large number of commits`() {
            val repository = repositoryPort.create(
                Repository(
                    localPath = "/large/repo", project = testProject
                )
            )

            // Add 100 commits
            repeat(100) { index ->
                createCommit(
                    sha = index.toString().padStart(40, '0'),
                    commitDateTime = LocalDateTime.of(2024, 1, 1, 0, 0).plusMinutes(index.toLong()),
                    message = "Commit $index",
                    repository = repository
                )
            }

            val updated = repositoryPort.update(repository)

            assertThat(updated.commits).hasSize(100)
        }

        @Test
        fun `commit with null message is allowed`() {
            val repository = Repository(
                localPath = "/null-message/repo", project = testProject
            )
            val commit = createCommit(
                sha = "e".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 1, 12, 0),
                message = null,
                repository = repository
            )

            val created = repositoryPort.create(repository)

            assertThat(created.commits.first().message).isNull()
        }

        @Test
        fun `commit with empty string message is allowed`() {
            val repository = Repository(
                localPath = "/empty-message/repo", project = testProject
            )
            val commit = createCommit(
                sha = "e".repeat(40),
                commitDateTime = LocalDateTime.of(2024, 1, 1, 12, 0),
                message = "",
                repository = repository
            )

            val created = repositoryPort.create(repository)

            assertThat(created.commits.first().message).isEmpty()
        }
    }
}
