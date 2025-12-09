package com.inso_world.binocular.model

import com.inso_world.binocular.domain.data.MockTestDataProvider
import com.inso_world.binocular.model.utils.ReflectionUtils.Companion.setField
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime

class CommitModelTest {
    private lateinit var repository: Repository
    private lateinit var mockTestDataProvider: MockTestDataProvider

    @BeforeEach
    fun setUp() {
        val project = Project(name = "test-project")
        repository = Repository(
            localPath = "test",
            project = project,
        )
        mockTestDataProvider = MockTestDataProvider(repository)
    }

    private fun createDeveloper(name: String = "Test Developer", email: String = "dev@test.com") =
        Developer(name = name, email = email, repository = repository)

    private fun createSignature(developer: Developer, timestamp: LocalDateTime = LocalDateTime.now().minusSeconds(1)) =
        Signature(developer = developer, timestamp = timestamp)

    @Test
    fun `create commit, check that iid is created automatically`() {
        val developer = createDeveloper()
        val signature = createSignature(developer)
        val commit = Commit(
            sha = "a".repeat(40),
            message = "msg1",
            authorSignature = signature,
            repository = repository,
        )

        assertThat(commit.iid).isNotNull()
    }

    @Test
    fun `create commit, check that hashCode is based on iid`() {
        val developer = createDeveloper()
        val signature = createSignature(developer)
        val commit = Commit(
            sha = "a".repeat(40),
            message = "msg1",
            authorSignature = signature,
            repository = repository,
        )

        assertThat(commit.hashCode()).isEqualTo(commit.iid.hashCode())
    }

    @Test
    fun `create commit, validate uniqueKey`() {
        val developer = createDeveloper()
        val signature = createSignature(developer)
        val commit = Commit(
            sha = "a".repeat(40),
            message = "msg1",
            authorSignature = signature,
            repository = repository,
        )

        assertAll(
            { assertThat(commit.uniqueKey).isEqualTo(Commit.Key("a".repeat(40))) },
            { assertThat(commit.uniqueKey.sha).isSameAs(commit.sha) }
        )
    }

    @Test
    fun `create commit, validate repository relation`() {
        val repository = Repository(
            localPath = "test-2",
            project = Project(name = "test-2"),
        )
        val developer = Developer(name = "Test", email = "test@example.com", repository = repository)
        val signature = Signature(developer = developer, timestamp = LocalDateTime.now().minusSeconds(1))
        val commit = Commit(
            sha = "a".repeat(40),
            message = "msg1",
            authorSignature = signature,
            repository = repository,
        )

        assertThat(commit.repository).isSameAs(repository)
        assertAll(
            { assertThat(repository.commits).hasSize(1) },
            { assertThat(repository.commits).containsOnly(commit) },
            { assertThat(repository.commits.first()).isSameAs(commit) })
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.data.DummyTestData#provideInvalidPastOrPresentDateTime")
    fun `create commit, invalid timestamp in signature`(
        timestamp: LocalDateTime,
    ) {
        val developer = createDeveloper()
        assertThrows<IllegalArgumentException> {
            Signature(developer = developer, timestamp = timestamp)
        }
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.data.DummyTestData#provideAllowedPastOrPresentDateTime")
    fun `create commit, valid timestamp in signature`(
        timestamp: LocalDateTime,
    ) {
        val developer = createDeveloper()
        assertDoesNotThrow {
            val signature = Signature(developer = developer, timestamp = timestamp)
            Commit(
                sha = "a".repeat(40),
                message = "msg1",
                authorSignature = signature,
                repository = repository,
            )
        }
    }

    @Test
    fun `create two commits, same sha, should not be equal`() {
        val developer = createDeveloper()
        val signature = createSignature(developer)
        val commitA = Commit(
            sha = "a".repeat(40),
            message = "msg1",
            authorSignature = signature,
            repository = repository,
        )
        val commitB = Commit(
            sha = "a".repeat(40),
            message = "msg1",
            authorSignature = signature,
            repository = repository,
        )

        assertAll(
            { assertThat(commitA.iid).isNotEqualTo(commitB.iid) },
            { assertThat(commitA.uniqueKey).isEqualTo(commitB.uniqueKey) },
            { assertThat(commitA).isNotEqualTo(commitB) })
    }

    @Test
    fun `create commit, then copy, should not be equal`() {
        val developer = createDeveloper()
        val signature = createSignature(developer)
        val commitA = Commit(
            sha = "a".repeat(40),
            message = "msg1",
            authorSignature = signature,
            repository = repository,
        )
        val commitB = commitA.copy()

        assertAll(
            { assertThat(commitA.iid).isNotEqualTo(commitB.iid) },
            { assertThat(commitA.uniqueKey).isEqualTo(commitB.uniqueKey) },
            { assertThat(commitA).isNotEqualTo(commitB) })
    }

    @Test
    fun `create commit, then copy, edit iid, should equal`() {
        val developer = createDeveloper()
        val signature = createSignature(developer)
        val commitA = Commit(
            sha = "a".repeat(40),
            message = "msg1",
            authorSignature = signature,
            repository = repository,
        )
        val commitB = commitA.copy()
        setField(
            commitB.javaClass.superclass.getDeclaredField("iid"), commitB, commitA.iid
        )

        assertThat(commitA.iid).isEqualTo(commitB.iid)

        assertAll(
            { assertThat(commitA.uniqueKey).isEqualTo(commitB.uniqueKey) },
            { assertThat(commitA).isEqualTo(commitB) })
    }

    @Nested
    inner class AuthorAndCommitterValidation {
        @BeforeEach
        fun setUp() {
            this@CommitModelTest.setUp()
        }

        @Test
        fun `create commit with authorSignature only, author and committer should be same`() {
            val developer = createDeveloper()
            val signature = createSignature(developer)

            val commit = Commit(
                sha = "a".repeat(40),
                message = "msg1",
                authorSignature = signature,
                repository = repository,
            )

            assertAll(
                { assertThat(commit.author).isSameAs(developer) },
                { assertThat(commit.committer).isSameAs(developer) },
                { assertThat(commit.author).isSameAs(commit.committer) },
                { assertThat(developer.authoredCommits).contains(commit) },
                { assertThat(developer.committedCommits).contains(commit) }
            )
        }

        @Test
        fun `create commit with separate committerSignature, should have different author and committer`() {
            val author = createDeveloper(name = "Author", email = "author@test.com")
            val committer = createDeveloper(name = "Committer", email = "committer@test.com")
            val authorSig = createSignature(author)
            val committerSig = createSignature(committer)

            val commit = Commit(
                sha = "a".repeat(40),
                message = "msg1",
                authorSignature = authorSig,
                committerSignature = committerSig,
                repository = repository,
            )

            assertAll(
                { assertThat(commit.author).isSameAs(author) },
                { assertThat(commit.committer).isSameAs(committer) },
                { assertThat(commit.author).isNotSameAs(commit.committer) },
                { assertThat(author.authoredCommits).contains(commit) },
                { assertThat(committer.committedCommits).contains(commit) }
            )
        }

        @Test
        fun `create commit with author from different repository, should fail`() {
            val differentRepository = Repository(
                localPath = "test-2",
                project = Project(name = "test-2"),
            )
            val developer = Developer(name = "Test", email = "test@example.com", repository = differentRepository)
            val signature = Signature(developer = developer, timestamp = LocalDateTime.now().minusSeconds(1))

            assertThrows<IllegalArgumentException> {
                Commit(
                    sha = "a".repeat(40),
                    message = "msg1",
                    authorSignature = signature,
                    repository = repository,
                )
            }
        }

        @Test
        fun `create commit with committer from different repository, should fail`() {
            val differentRepository = Repository(
                localPath = "test-2",
                project = Project(name = "test-2"),
            )
            val author = createDeveloper()
            val committer = Developer(name = "Committer", email = "committer@example.com", repository = differentRepository)
            val authorSig = createSignature(author)
            val committerSig = Signature(developer = committer, timestamp = LocalDateTime.now().minusSeconds(1))

            assertThrows<IllegalArgumentException> {
                Commit(
                    sha = "a".repeat(40),
                    message = "msg1",
                    authorSignature = authorSig,
                    committerSignature = committerSig,
                    repository = repository,
                )
            }
        }

        @Test
        fun `commit timestamps come from signatures`() {
            val author = createDeveloper(name = "Author", email = "author@test.com")
            val committer = createDeveloper(name = "Committer", email = "committer@test.com")
            val authorTime = LocalDateTime.of(2024, 1, 1, 10, 0)
            val committerTime = LocalDateTime.of(2024, 1, 1, 11, 0)
            val authorSig = Signature(developer = author, timestamp = authorTime)
            val committerSig = Signature(developer = committer, timestamp = committerTime)

            val commit = Commit(
                sha = "a".repeat(40),
                message = "msg1",
                authorSignature = authorSig,
                committerSignature = committerSig,
                repository = repository,
            )

            assertAll(
                { assertThat(commit.authorDateTime).isEqualTo(authorTime) },
                { assertThat(commit.commitDateTime).isEqualTo(committerTime) }
            )
        }
    }

    @Nested
    inner class ParentsRelation {
        @BeforeEach
        fun setUp() {
            this@CommitModelTest.setUp()
        }

        @Test
        fun `create commit, add parent, should succeed`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val parent = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            assertTrue(commit.parents.add(parent))

            assertAll(
                "parent relation",
                { assertThat(commit.parents).hasSize(1) },
                { assertThat(commit.parents).containsOnly(parent) },
            )
            assertAll(
                "child relation",
                { assertThat(parent.children).hasSize(1) },
                { assertThat(parent.children).containsOnly(commit) },
            )
        }

        @Test
        fun `create commit, addAll single parent, should succeed`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val parent = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            assertTrue(commit.parents.addAll(listOf(parent)))

            assertAll(
                "commit->parent relation",
                { assertThat(commit.parents).hasSize(1) },
                { assertThat(commit.parents).containsOnly(parent) },
            )
            assertAll(
                "parent->commit relation",
                { assertThat(parent.children).hasSize(1) },
                { assertThat(parent.children).containsOnly(commit) },
            )
        }

        @Test
        fun `create commit, add same parent twice, should only be added once`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val parent = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            assertTrue(commit.parents.add(parent))
            assertFalse(commit.parents.add(parent))
        }

        @Test
        fun `create commit, addAll same parent twice, should only be added once`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val parent = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            assertTrue(commit.parents.addAll(listOf(parent)))
            assertFalse(commit.parents.addAll(listOf(parent)))
        }

        @Test
        fun `create commit, add parent with different repository, should fail`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val parent = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            val differentRepository = Repository(
                localPath = "test-2",
                project = Project(name = "test-2"),
            )
            setField(
                parent.javaClass.getDeclaredField("repository"), parent, differentRepository
            )

            assertThrows<IllegalArgumentException> {
                commit.parents.add(parent)
            }
        }

        @Test
        fun `create commit, add same commit to parents, should fail`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))

            val ex = assertThrows<IllegalArgumentException> {
                commit.parents.add(commit)
            }

            assertThat(ex.message).isEqualTo("Commit cannot be its own parent")
        }

        @Test
        fun `create commit, add same commit to children, should fail`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))

            val ex = assertThrows<IllegalArgumentException> {
                commit.children.add(commit)
            }

            assertThat(ex.message).isEqualTo("Commit cannot be its own child")
        }

        @Test
        fun `create commit, add other commit to parents and children, should fail`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val parent = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            assertDoesNotThrow {
                commit.parents.add(parent)
            }
            val ex = assertThrows<IllegalArgumentException> {
                commit.children.add(parent)
            }

            assertThat(ex.message).isEqualTo(
                "${parent.sha} is already present in '${commit.sha}' parent collection. Cannot be added as child too."
            )
        }
    }

    @Nested
    inner class ChildrenRelation {
        @BeforeEach
        fun setUp() {
            this@CommitModelTest.setUp()
        }

        @Test
        fun `create commit, add child, should succeed`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val child = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            assertTrue(commit.children.add(child))

            assertAll(
                "child relation",
                { assertThat(commit.children).hasSize(1) },
                { assertThat(commit.children).containsOnly(child) },
            )
            assertAll(
                "parent relation",
                { assertThat(child.parents).hasSize(1) },
                { assertThat(child.parents).containsOnly(commit) },
            )
        }

        @Test
        fun `create commit, addAll single child, should succeed`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val child = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            assertTrue(commit.children.addAll(listOf(child)))

            assertAll(
                "child relation",
                { assertThat(commit.children).hasSize(1) },
                { assertThat(commit.children).containsOnly(child) },
            )
            assertAll(
                "parent relation",
                { assertThat(child.parents).hasSize(1) },
                { assertThat(child.parents).containsOnly(commit) },
            )
        }

        @Test
        fun `create commit, add same children twice, should only be added once`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val child = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            assertTrue(commit.children.add(child))
            assertFalse(commit.children.add(child))
        }

        @Test
        fun `create commit, add child with different repository, should fail`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val child = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            val differentRepository = Repository(
                localPath = "test-2",
                project = Project(name = "test-2"),
            )
            setField(
                child.javaClass.getDeclaredField("repository"), child, differentRepository
            )

            assertThrows<IllegalArgumentException> {
                commit.children.add(child)
            }
        }
    }

    @Nested
    inner class BranchRelation {
        @BeforeEach
        fun setUp() {
            this@CommitModelTest.setUp()
        }

        @Test
        fun `create commit, add to branch, should succeed`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branch = mockTestDataProvider.branchByName.getValue("origin/feature/test")

            branch.head = commit

            assertAll(
                "check branch relation",
                { assertThat(branch.commits).hasSize(1) },
                { assertThat(branch.commits).containsOnly(commit) },
                { assertThat(branch.commits.first()).isSameAs(commit) },
            )
        }

        @Test
        fun `create commit, add to branch from different repository, should fail`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branch = mockTestDataProvider.branchByName.getValue("origin/feature/test")

            val differentRepository = Repository(
                localPath = "test-2",
                project = Project(name = "test-2"),
            )
            setField(
                commit.javaClass.getDeclaredField("repository"),
                commit,
                differentRepository,
            )

            assertAll({ assertThat(commit.repository).isNotSameAs(branch.repository) }, {
                assertThrows<IllegalArgumentException> {
                    branch.head = commit
                }
            })
        }
    }
}
