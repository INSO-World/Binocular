package com.inso_world.binocular.model

import com.inso_world.binocular.data.MockTestDataProvider
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

    @Test
    fun `create commit, check that iid is created automatically`() {
        val commit = Commit(
            sha = "a".repeat(40),
            message = "msg1",
            commitDateTime = LocalDateTime.now(),
            authorDateTime = LocalDateTime.now(),
            repository = repository,
        )

        assertThat(commit.iid).isNotNull()
    }

    @Test
    fun `create commit, check that hashCode is based on iid`() {
        val commit = Commit(
            sha = "a".repeat(40),
            message = "msg1",
            commitDateTime = LocalDateTime.now(),
            authorDateTime = LocalDateTime.now(),
            repository = repository,
        )

        assertThat(commit.hashCode()).isEqualTo(commit.iid.hashCode())
    }

    @Test
    fun `create commit, validate uniqueKey`() {
        val commit = Commit(
            sha = "a".repeat(40),
            message = "msg1",
            commitDateTime = LocalDateTime.now(),
            authorDateTime = LocalDateTime.now(),
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
        val commit = Commit(
            sha = "a".repeat(40),
            message = "msg1",
            commitDateTime = LocalDateTime.now(),
            authorDateTime = LocalDateTime.now(),
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
    fun `create commit, invalid commitDateTime`(
        commitDateTime: LocalDateTime,
    ) {
        assertThrows<IllegalArgumentException> {
            Commit(
                sha = "a".repeat(40),
                message = "msg1",
                commitDateTime = commitDateTime,
                authorDateTime = LocalDateTime.now(),
                repository = repository,
            )
        }
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.data.DummyTestData#provideAllowedPastOrPresentDateTime")
    fun `create commit, valid commitDateTime`(
        commitDateTime: LocalDateTime,
    ) {
        assertDoesNotThrow {
            Commit(
                sha = "a".repeat(40),
                message = "msg1",
                commitDateTime = commitDateTime,
                authorDateTime = LocalDateTime.now(),
                repository = repository,
            )
        }
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.data.DummyTestData#provideAllowedPastOrPresentDateTime")
    fun `create commit, valid authorDateTime`(
        authorDateTime: LocalDateTime,
    ) {
        assertDoesNotThrow {
            Commit(
                sha = "a".repeat(40),
                message = "msg1",
                commitDateTime = LocalDateTime.now(),
                authorDateTime = authorDateTime,
                repository = repository,
            )
        }
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.data.DummyTestData#provideInvalidPastOrPresentDateTime")
    fun `create commit, invalid authorDateTime`(
        authorDateTime: LocalDateTime,
    ) {
        assertThrows<IllegalArgumentException> {
            Commit(
                sha = "a".repeat(40),
                message = "msg1",
                commitDateTime = LocalDateTime.now(),
                authorDateTime = authorDateTime,
                repository = repository,
            )
        }
    }

    @Test
    fun `create two commits, same sha, should not be equal`() {
        val commitA = Commit(
            sha = "a".repeat(40),
            message = "msg1",
            commitDateTime = LocalDateTime.now(),
            authorDateTime = LocalDateTime.now(),
            repository = repository,
        )
        val commitB = Commit(
            sha = "a".repeat(40),
            message = "msg1",
            commitDateTime = LocalDateTime.now(),
            authorDateTime = LocalDateTime.now(),
            repository = repository,
        )

        assertAll(
            { assertThat(commitA.iid).isNotEqualTo(commitB.iid) },
            { assertThat(commitA.uniqueKey).isEqualTo(commitB.uniqueKey) },
            { assertThat(commitA).isNotEqualTo(commitB) })
    }

    @Test
    fun `create commit, then copy, should not be equal`() {
        val commitA = Commit(
            sha = "a".repeat(40),
            message = "msg1",
            commitDateTime = LocalDateTime.now(),
            authorDateTime = LocalDateTime.now(),
            repository = repository,
        )
        val commitB = commitA.copy()

        assertAll(
            { assertThat(commitA.iid).isNotEqualTo(commitB.iid) },
            { assertThat(commitA.uniqueKey).isEqualTo(commitB.uniqueKey) },
            { assertThat(commitA).isNotEqualTo(commitB) })
    }

    @Test
    fun `create commit, then copy, edit iid, should not equal`() {
        val commitA = Commit(
            sha = "a".repeat(40),
            message = "msg1",
            commitDateTime = LocalDateTime.now(),
            authorDateTime = LocalDateTime.now(),
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
    inner class CommitterRelation {
        @BeforeEach
        fun setUp() {
            this@CommitModelTest.setUp()
        }

        @Test
        fun `create commit, set committer, should succeed`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))

            assertThat(commit.committer).isNull()
            val committer = mockTestDataProvider.userByEmail.getValue("a@test.com")

            assertDoesNotThrow { commit.committer = committer }
            assertAll(
                { assertThat(commit.committer).isEqualTo(committer) },
                { assertThat(committer.committedCommits).hasSize(1) },
                { assertThat(committer.committedCommits).containsOnly(commit) },
                { assertThat(committer.authoredCommits).isEmpty() })
        }

        @Test
        fun `create commit, set committer with different repository, should fail`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))

            assertThat(commit.committer).isNull()
            val committer = mockTestDataProvider.userByEmail.getValue("a@test.com")

            val differentRepository = Repository(
                localPath = "test-2",
                project = Project(name = "test-2"),
            )
            setField(
                committer.javaClass.getDeclaredField("repository"), committer, differentRepository
            )
            assertThat(commit.repository).isNotEqualTo(differentRepository)

            assertThrows<IllegalArgumentException> { commit.committer = committer }
            assertAll(
                "validate no changes",
                { assertThat(commit.committer).isNull() },
                { assertThat(commit.author).isNull() },
                { assertThat(committer.committedCommits).isEmpty() },
                { assertThat(committer.authoredCommits).isEmpty() })
        }

        @Test
        fun `create commit, set committer to null, should fail`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))

            assertThat(commit.committer).isNull()

            assertThrows<IllegalArgumentException> { commit.committer = null }
        }

        @Test
        fun `create commit, set committer, set same committer again, should succeed`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))

            assertThat(commit.committer).isNull()
            // first set
            run {
                val committer = mockTestDataProvider.userByEmail.getValue("a@test.com")

                assertDoesNotThrow { commit.committer = committer }
                assertThat(commit.committer).isEqualTo(committer)
                assertThat(committer.committedCommits).hasSize(1)
                assertThat(committer.committedCommits).containsOnly(commit)
                assertThat(committer.authoredCommits).isEmpty()
            }

            // re-set same committer
            run {
                val committer = mockTestDataProvider.userByEmail.getValue("a@test.com")

                assertDoesNotThrow { commit.committer = committer }
                assertAll(
                    { assertThat(commit.committer).isEqualTo(committer) },
                    { assertThat(committer.committedCommits).hasSize(1) },
                    { assertThat(committer.committedCommits).containsOnly(commit) },
                    { assertThat(committer.authoredCommits).isEmpty() })
            }
        }

        @Test
        fun `create commit, set committer, set different committer again, should fail`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))

            assertThat(commit.committer).isNull()
            // first set
            run {
                val committer = mockTestDataProvider.userByEmail.getValue("a@test.com")

                assertDoesNotThrow { commit.committer = committer }
                assertAll(
                    { assertThat(commit.committer).isEqualTo(committer) },
                    { assertThat(committer.committedCommits).hasSize(1) },
                    { assertThat(committer.committedCommits).containsOnly(commit) },
                    { assertThat(committer.authoredCommits).isEmpty() })
            }

            // re-set same committer
            run {
                // different committer now
                val committer = mockTestDataProvider.userByEmail.getValue("b@test.com")

                assertThrows<IllegalArgumentException> { commit.committer = committer }
                assertAll(
                    { assertThat(commit.committer).isNotEqualTo(committer) },
                    { assertThat(committer.committedCommits).isEmpty() },
                    { assertThat(committer.authoredCommits).isEmpty() })
            }
        }
    }

    @Nested
    inner class AuthorRelation {
        @BeforeEach
        fun setUp() {
            this@CommitModelTest.setUp()
        }

        @Test
        fun `create commit, set author, should succeed`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))

            assertThat(commit.author).isNull()
            val author = mockTestDataProvider.userByEmail.getValue("a@test.com")

            assertDoesNotThrow { commit.author = author }
            assertAll(
                { assertThat(commit.author).isEqualTo(author) },
                { assertThat(author.authoredCommits).hasSize(1) },
                { assertThat(author.authoredCommits).containsOnly(commit) },
                { assertThat(author.committedCommits).isEmpty() })
        }

        @Test
        fun `create commit, set author, set same author again, should succeed`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))

            assertThat(commit.author).isNull()
            // first set
            run {
                val author = mockTestDataProvider.userByEmail.getValue("a@test.com")

                assertDoesNotThrow { commit.author = author }
                assertAll(
                    { assertThat(commit.author).isEqualTo(author) },
                    { assertThat(author.authoredCommits).hasSize(1) },
                    { assertThat(author.authoredCommits).containsOnly(commit) },
                    { assertThat(author.committedCommits).isEmpty() })
            }

            // re-set same committer
            run {
                val author = mockTestDataProvider.userByEmail.getValue("a@test.com")

                assertDoesNotThrow { commit.author = author }
                assertAll(
                    { assertThat(commit.author).isEqualTo(author) },
                    { assertThat(author.authoredCommits).hasSize(1) },
                    { assertThat(author.authoredCommits).containsOnly(commit) },
                    { assertThat(author.committedCommits).isEmpty() })
            }
        }

        @Test
        fun `create commit, set committer, set different committer again, should fail`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))

            assertThat(commit.author).isNull()
            // first set
            run {
                val author = mockTestDataProvider.userByEmail.getValue("a@test.com")

                assertDoesNotThrow { commit.author = author }
                assertAll(
                    { assertThat(commit.author).isEqualTo(author) },
                    { assertThat(author.authoredCommits).hasSize(1) },
                    { assertThat(author.authoredCommits).containsOnly(commit) },
                    { assertThat(author.committedCommits).isEmpty() })
            }

            // re-set same committer
            run {
                // different committer now
                val author = mockTestDataProvider.userByEmail.getValue("b@test.com")

                assertThrows<IllegalArgumentException> { commit.author = author }
                assertAll(
                    { assertThat(commit.author).isNotEqualTo(author) },
                    { assertThat(author.committedCommits).isEmpty() },
                    { assertThat(author.committedCommits).isEmpty() },
                )
            }
        }

        @Test
        fun `create commit, set committer with different repository, should fail`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))

            assertThat(commit.author).isNull()
            val author = mockTestDataProvider.userByEmail.getValue("a@test.com")

            val differentRepository = Repository(
                localPath = "test-2",
                project = Project(name = "test-2"),
            )
            setField(
                author.javaClass.getDeclaredField("repository"),
                author,
                differentRepository,
            )
            assertThat(commit.repository).isNotEqualTo(differentRepository)

            assertThrows<IllegalArgumentException> { commit.author = author }
            assertAll(
                "validate no changes",
                { assertThat(commit.committer).isNull() },
                { assertThat(commit.author).isNull() },
                { assertThat(author.committedCommits).isEmpty() },
                { assertThat(author.authoredCommits).isEmpty() })
        }

        @Test
        fun `create commit, set author to null, should fail`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))

            assertThat(commit.author).isNull()

            assertThrows<IllegalArgumentException> { commit.author = null }
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
        fun `create commit, add same parent twice via addAll, should only be added once`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val parent = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            val list = listOf(parent, parent)
            assertThat(list).hasSize(2)

            assertTrue(commit.parents.addAll(list))

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

            assertAll(
                "check inequality of repositories between parent and commit",
                { assertThat(parent.repository).isSameAs(differentRepository) },
                { assertThat(parent.repository).isNotEqualTo(commit.repository) })

            assertThrows<IllegalArgumentException> {
                commit.parents.add(parent)
            }

            assertAll(
                "no changes must happen",
                { assertThat(commit.parents).isEmpty() },
                { assertThat(parent.children).isEmpty() },
            )
        }

        @Test
        fun `create commit, addAll parent with different repository, should fail`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val parent = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            val differentRepository = Repository(
                localPath = "test-2",
                project = Project(name = "test-2"),
            )
            setField(
                parent.javaClass.getDeclaredField("repository"),
                parent,
                differentRepository,
            )

            assertAll(
                "check inequality of repositories between parent and commit",
                { assertThat(parent.repository).isSameAs(differentRepository) },
                { assertThat(parent.repository).isNotEqualTo(commit.repository) })

            assertThrows<IllegalArgumentException> {
                commit.parents.addAll(listOf(parent))
            }

            assertAll(
                "no changes must happen",
                { assertThat(commit.parents).isEmpty() },
                { assertThat(parent.children).isEmpty() },
            )
        }

        @Test
        fun `create commit, add two parents, should succeed`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val parentA = mockTestDataProvider.commitBySha.getValue("b".repeat(40))
            val parentB = mockTestDataProvider.commitBySha.getValue("c".repeat(40))

            assertAll("add two parents", {
                assertDoesNotThrow {
                    commit.parents.add(parentA)
                }
            }, {
                assertDoesNotThrow {
                    commit.parents.add(parentB)
                }
            })

            assertAll(
                "check relationships",
                { assertThat(commit.children).isEmpty() },
                { assertThat(commit.parents).hasSize(2) },
                { assertThat(commit.parents).containsOnly(parentA, parentB) },
            )
            assertAll(
                "check parentA",
                { assertThat(parentA.children).hasSize(1) },
                { assertThat(parentA.children).containsOnly(commit) })
            assertAll(
                "check parentB",
                { assertThat(parentB.children).hasSize(1) },
                { assertThat(parentA.children).containsOnly(commit) })
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
        fun `create commit, addAll same children twice, should only be added once`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val child = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            assertTrue(commit.children.addAll(listOf(child)))
            assertFalse(commit.children.addAll(listOf(child)))
        }

        @Test
        fun `create commit, add same child twice via addAll, should only be added once`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val child = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            val list = listOf(child, child)
            assertThat(list).hasSize(2)

            assertTrue(commit.children.addAll(list))

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

            assertAll(
                "check inequality of repositories between child and commit",
                { assertThat(child.repository).isSameAs(differentRepository) },
                { assertThat(child.repository).isNotEqualTo(commit.repository) })

            assertThrows<IllegalArgumentException> {
                commit.children.add(child)
            }

            assertAll(
                "no changes must happen",
                { assertThat(commit.children).isEmpty() },
                { assertThat(child.parents).isEmpty() },
            )
        }

        @Test
        fun `create commit, addAll child with different repository, should fail`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val child = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            val differentRepository = Repository(
                localPath = "test-2",
                project = Project(name = "test-2"),
            )
            setField(
                child.javaClass.getDeclaredField("repository"),
                child,
                differentRepository,
            )

            assertAll(
                "check inequality of repositories between parent and commit",
                { assertThat(child.repository).isSameAs(differentRepository) },
                { assertThat(child.repository).isNotEqualTo(commit.repository) })

            assertThrows<IllegalArgumentException> {
                commit.children.addAll(listOf(child))
            }

            assertAll(
                "no changes must happen",
                { assertThat(commit.children).isEmpty() },
                { assertThat(child.parents).isEmpty() },
            )
        }

        @Test
        fun `create commit, add two children, should succeed`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val childA = mockTestDataProvider.commitBySha.getValue("b".repeat(40))
            val childB = mockTestDataProvider.commitBySha.getValue("c".repeat(40))

            assertAll("add two children", {
                assertDoesNotThrow {
                    commit.children.add(childA)
                }
            }, {
                assertDoesNotThrow {
                    commit.children.add(childB)
                }
            })

            assertAll(
                "check relationships",
                { assertThat(commit.parents).isEmpty() },
                { assertThat(commit.children).hasSize(2) },
                { assertThat(commit.children).containsOnly(childA, childB) },
            )
            assertAll(
                "check childA",
                { assertThat(childA.parents).hasSize(1) },
                { assertThat(childA.parents).containsOnly(commit) })
            assertAll(
                "check childB",
                { assertThat(childB.parents).hasSize(1) },
                { assertThat(childB.parents).containsOnly(commit) })
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

            assertThat(commit.branches).isEmpty()

            assertTrue(commit.branches.add(branch))

            assertAll(
                "check commit",
                { assertThat(commit.branches).hasSize(1) },
                { assertThat(commit.branches).containsOnly(branch) },
                { assertThat(commit.branches.first()).isSameAs(branch) },
            )
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
            // edit repository field of commit
            setField(
                commit.javaClass.getDeclaredField("repository"),
                commit,
                differentRepository,
            )

            assertAll({ assertThat(commit.repository).isNotSameAs(branch.repository) }, {
                assertThrows<IllegalArgumentException> {
                    commit.branches.add(branch)
                }
            })
        }

        @Test
        fun `create commit with different repository, add to branch, should fail`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branch = mockTestDataProvider.branchByName.getValue("origin/feature/test")

            val differentRepository = Repository(
                localPath = "test-2",
                project = Project(name = "test-2"),
            )
            // edit repository field of commit, not branch
            setField(
                branch.javaClass.getDeclaredField("repository"),
                branch,
                differentRepository,
            )

            assertAll({ assertThat(commit.repository).isNotSameAs(branch.repository) }, {
                assertThrows<IllegalArgumentException> {
                    commit.branches.add(branch)
                }
            })
        }

        @Test
        fun `add commit to two branches, should be referenced correctly`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branchA = mockTestDataProvider.branchByName.getValue("origin/feature/test")
            val branchB = mockTestDataProvider.branchByName.getValue("origin/fixme/123")

            assertAll(
                "add two branches",
                { assertTrue(commit.branches.add(branchA)) },
                { assertTrue(commit.branches.add(branchB)) })

            assertAll(
                "check commit",
                { assertThat(commit.branches).hasSize(2) },
                { assertThat(commit.branches).containsOnly(branchA, branchB) },
            )

            assertAll(
                "check branchA",
                { assertThat(branchA.commits).hasSize(1) },
                { assertThat(branchA.commits).containsOnly(commit) },
                { assertThat(branchA.commits.first()).isSameAs(commit) },
            )

            assertAll(
                "check branchB",
                { assertThat(branchB.commits).hasSize(1) },
                { assertThat(branchB.commits).containsOnly(commit) },
                { assertThat(branchB.commits.first()).isSameAs(commit) },
            )
        }

        @Test
        fun `add commit to two branches via addAll, should be referenced correctly`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branchA = mockTestDataProvider.branchByName.getValue("origin/feature/test")
            val branchB = mockTestDataProvider.branchByName.getValue("origin/fixme/123")

            val list = listOf(branchA, branchB)

            assertThat(list).hasSize(2)

            assertTrue(commit.branches.addAll(list))

            assertAll(
                "check commit",
                { assertThat(commit.branches).hasSize(2) },
                { assertThat(commit.branches).containsOnly(branchA, branchB) },
            )

            assertAll(
                "check branchA",
                { assertThat(branchA.commits).hasSize(1) },
                { assertThat(branchA.commits).containsOnly(commit) },
                { assertThat(branchA.commits.first()).isSameAs(commit) },
            )

            assertAll(
                "check branchB",
                { assertThat(branchB.commits).hasSize(1) },
                { assertThat(branchB.commits).containsOnly(commit) },
                { assertThat(branchB.commits.first()).isSameAs(commit) },
            )
        }

        @Test
        fun `add commit to same branch twice via addAll, should be only be added once`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branchA = mockTestDataProvider.branchByName.getValue("origin/feature/test")
            val branchB = branchA.copy()

            val list = listOf(branchA, branchB)

            assertThat(list).hasSize(2)
            assertThat(branchA).isNotSameAs(branchB)

            assertTrue(commit.branches.addAll(list))

            assertAll(
                "check commit",
                { assertThat(commit.branches).hasSize(1) },
                { assertThat(commit.branches).containsOnly(branchA) },
            )

            assertAll(
                "check branchA",
                { assertThat(branchA.commits).hasSize(1) },
                { assertThat(branchA.commits).containsOnly(commit) },
                { assertThat(branchA.commits.first()).isSameAs(commit) },
            )
        }

        @Test
        fun `add commit to same branch twice, should be only be added once`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branchA = mockTestDataProvider.branchByName.getValue("origin/feature/test")
            val branchB = branchA.copy()

            assertThat(branchA).isNotSameAs(branchB)

            assertTrue(commit.branches.add(branchA))
            assertFalse(commit.branches.add(branchB))

            assertAll(
                "check commit",
                { assertThat(commit.branches).hasSize(1) },
                { assertThat(commit.branches).containsOnly(branchA) },
            )

            assertAll(
                "check branchA",
                { assertThat(branchA.commits).hasSize(1) },
                { assertThat(branchA.commits).containsOnly(commit) },
                { assertThat(branchA.commits.first()).isSameAs(commit) },
            )
        }

        @Test
        fun `add commit to same branch twice via addAll one by one, should be only be added once`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branchA = mockTestDataProvider.branchByName.getValue("origin/feature/test")

            assertTrue(commit.branches.addAll(listOf(branchA)))
            assertFalse(commit.branches.addAll(listOf(branchA)))
        }

        @Test
        fun `try clearing branches, should fail`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branch = mockTestDataProvider.branchByName.getValue("origin/feature/test")

            assertAll(
                { assertTrue(commit.branches.add(branch)) },
                {
                    assertThrows<UnsupportedOperationException> {
                        commit.branches.clear()
                    }
                }
            )
        }

        @Test
        fun `try remove branch, should fail`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branch = mockTestDataProvider.branchByName.getValue("origin/feature/test")

            assertAll(
                { assertTrue(commit.branches.add(branch)) },
                {
                    assertThrows<UnsupportedOperationException> {
                        commit.branches.remove(branch)
                    }
                }
            )
        }
    }
}
