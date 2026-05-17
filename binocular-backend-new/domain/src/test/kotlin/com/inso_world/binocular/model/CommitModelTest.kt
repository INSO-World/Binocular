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
            projectId = project.iid,
        )
        mockTestDataProvider = MockTestDataProvider(repository)
    }

    private fun createDeveloper(name: String = "Test Developer", email: String = "dev@test.com") =
        Developer(name = name, email = email)

    private fun createSignature(developer: Developer, timestamp: LocalDateTime = LocalDateTime.now().minusSeconds(1)) =
        Signature(
            developerId = developer.iid,
            gitSignature = developer.gitSignature,
            timestamp = timestamp,
        )

    @Test
    fun `create commit, check that iid is created automatically`() {
        val developer = createDeveloper()
        val signature = createSignature(developer)
        val commit = Commit(
            sha = "a".repeat(40),
            message = "msg1",
            authorSignature = signature,
            repositoryId = repository.iid,
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
            repositoryId = repository.iid,
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
            repositoryId = repository.iid,
        )

        assertAll(
            { assertThat(commit.uniqueKey).isEqualTo(Commit.Key("a".repeat(40))) },
            { assertThat(commit.uniqueKey.sha).isSameAs(commit.sha) }
        )
    }

    @Test
    fun `create commit, validate repositoryId relation`() {
        val project = Project(name = "test-2")
        val repository = Repository(
            localPath = "test-2",
            projectId = project.iid,
        )
        val developer = Developer(name = "Test", email = "test@example.com")
        val signature = Signature(
            developerId = developer.iid,
            gitSignature = developer.gitSignature,
            timestamp = LocalDateTime.now().minusSeconds(1),
        )
        val commit = Commit(
            sha = "a".repeat(40),
            message = "msg1",
            authorSignature = signature,
            repositoryId = repository.iid,
        )

        assertThat(commit.repositoryId).isEqualTo(repository.iid)
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.domain.data.DummyTestData#provideInvalidPastOrPresentDateTime")
    fun `create commit, invalid timestamp in signature`(
        timestamp: LocalDateTime,
    ) {
        val developer = createDeveloper()
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            Signature(
                developerId = developer.iid,
                gitSignature = developer.gitSignature,
                timestamp = timestamp,
            )
        }
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.domain.data.DummyTestData#provideAllowedPastOrPresentDateTime")
    fun `create commit, valid timestamp in signature`(
        timestamp: LocalDateTime,
    ) {
        val developer = createDeveloper()
        assertDoesNotThrow {
            val signature = Signature(
                developerId = developer.iid,
                gitSignature = developer.gitSignature,
                timestamp = timestamp,
            )
            Commit(
                sha = "a".repeat(40),
                message = "msg1",
                authorSignature = signature,
                repositoryId = repository.iid,
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
            repositoryId = repository.iid,
        )
        val commitB = Commit(
            sha = "a".repeat(40),
            message = "msg1",
            authorSignature = signature,
            repositoryId = repository.iid,
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
            repositoryId = repository.iid,
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
            repositoryId = repository.iid,
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
                repositoryId = repository.iid,
            )

            assertAll(
                { assertThat(commit.authorId).isEqualTo(developer.iid) },
                { assertThat(commit.committerId).isEqualTo(developer.iid) },
                { assertThat(commit.authorId).isEqualTo(commit.committerId) }
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
                repositoryId = repository.iid,
            )

            assertAll(
                { assertThat(commit.authorId).isEqualTo(author.iid) },
                { assertThat(commit.committerId).isEqualTo(committer.iid) },
                { assertThat(commit.authorId).isNotEqualTo(commit.committerId) }
            )
        }

        @Test
        fun `commit timestamps come from signatures`() {
            val author = createDeveloper(name = "Author", email = "author@test.com")
            val committer = createDeveloper(name = "Committer", email = "committer@test.com")
            val authorTime = LocalDateTime.of(2024, 1, 1, 10, 0)
            val committerTime = LocalDateTime.of(2024, 1, 1, 11, 0)
            val authorSig = Signature(
                developerId = author.iid,
                gitSignature = author.gitSignature,
                timestamp = authorTime,
            )
            val committerSig = Signature(
                developerId = committer.iid,
                gitSignature = committer.gitSignature,
                timestamp = committerTime,
            )

            val commit = Commit(
                sha = "a".repeat(40),
                message = "msg1",
                authorSignature = authorSig,
                committerSignature = committerSig,
                repositoryId = repository.iid,
            )

            assertAll(
                { assertThat(commit.authorDateTime).isEqualTo(authorTime) },
                { assertThat(commit.commitDateTime).isEqualTo(committerTime) }
            )
        }
    }

    @Nested
    inner class ParentIdsRelation {
        @BeforeEach
        fun setUp() {
            this@CommitModelTest.setUp()
        }

        @Test
        fun `create commit, add parent id, should succeed`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val parent = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            assertTrue(commit.parentIds.add(parent.iid))

            assertAll(
                "parent relation",
                { assertThat(commit.parentIds).hasSize(1) },
                { assertThat(commit.parentIds).containsOnly(parent.iid) },
            )
            assertAll(
                "child relation",
                { assertThat(parent.childIds).hasSize(0) }, // childIds is not auto-wired anymore
            )
        }

        @Test
        fun `create commit, addAll single parent id, should succeed`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val parent = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            assertTrue(commit.parentIds.addAll(listOf(parent.iid)))

            assertAll(
                "commit->parent relation",
                { assertThat(commit.parentIds).hasSize(1) },
                { assertThat(commit.parentIds).containsOnly(parent.iid) },
            )
        }

        @Test
        fun `create commit, add same parent id twice, should only be added once`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val parent = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            assertTrue(commit.parentIds.add(parent.iid))
            assertFalse(commit.parentIds.add(parent.iid))
        }

        @Test
        fun `create commit, addAll same parent id twice, should only be added once`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val parent = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            assertTrue(commit.parentIds.addAll(listOf(parent.iid)))
            assertFalse(commit.parentIds.addAll(listOf(parent.iid)))
        }
    }

    @Nested
    inner class ChildIdsRelation {
        @BeforeEach
        fun setUp() {
            this@CommitModelTest.setUp()
        }

        @Test
        fun `create commit, add child id, should succeed`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val child = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            assertTrue(commit.childIds.add(child.iid))

            assertAll(
                "child relation",
                { assertThat(commit.childIds).hasSize(1) },
                { assertThat(commit.childIds).containsOnly(child.iid) },
            )
        }

        @Test
        fun `create commit, addAll single child id, should succeed`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val child = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            assertTrue(commit.childIds.addAll(listOf(child.iid)))

            assertAll(
                "child relation",
                { assertThat(commit.childIds).hasSize(1) },
                { assertThat(commit.childIds).containsOnly(child.iid) },
            )
        }

        @Test
        fun `create commit, add same child id twice, should only be added once`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val child = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            assertTrue(commit.childIds.add(child.iid))
            assertFalse(commit.childIds.add(child.iid))
        }
    }
}
