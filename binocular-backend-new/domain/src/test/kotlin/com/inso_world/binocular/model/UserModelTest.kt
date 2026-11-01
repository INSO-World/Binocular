package com.inso_world.binocular.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class UserModelTest {
    private lateinit var repository: Repository

    private fun user(name: String = "test-user"): User =
        User(name = name, repositoryId = repository.iid).apply {
            this.repository = this@UserModelTest.repository
            this.repository?.user?.add(this)
        }

    @BeforeEach
    fun setUp() {
        val project = Project(name = "test-project")
        repository =
            Repository(
                localPath = "test",
                projectId = project.iid,
            ).apply { this.project = project }
    }

    @Test
    fun `create user, check that iid is created automatically`() {
        val user = user()

        assertThat(user.iid).isNotNull()
    }

    @Test
    fun `create user, validate uniqueKey`() {
        val user = user()

        assertAll(
            { assertThat(user.uniqueKey).isEqualTo(User.Key(repository.iid, "test-user <null>")) },
            { assertThat(user.uniqueKey.repositoryId).isEqualTo(repository.iid) },
            // compare .value here
            // Because inline classes may be represented both as the underlying value and as a wrapper, referential equality is pointless for them and is therefore prohibited.
            // https://kotlinlang.org/docs/inline-classes.html#representation
            { assertThat(user.uniqueKey.repositoryId.value).isSameAs(repository.iid.value) },
        )
    }

    @Test
    fun `create user, validate hashCode is same based on iid`() {
        val user = user()

        assertThat(user.hashCode()).isEqualTo(user.iid.hashCode())
    }

    @Test
    fun `create user, is is null`() {
        val user = user()

        assertThat(user.id).isNull()
    }

    @Test
    fun `create user, check reference to owning repository`() {
        val user = user()

        assertThat(user.repository).isSameAs(repository)
        assertThat(user.repository?.user).containsOnly(user)
        assertThat(repository.user).containsOnly(user)
    }

    @Test
    fun `create user, update email`() {
        val user = user()

        assertDoesNotThrow {
            user.email = "test-email"
        }
        assertThat(user.email).isEqualTo("test-email")
    }

    @Test
    fun `create user, update email, check gitSignature`() {
        val user = user()

        assertDoesNotThrow {
            user.email = "test-email"
        }
        assertThat(user.gitSignature).isEqualTo("test-user <test-email>")
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.domain.data.DummyTestData#provideBlankStrings")
    fun `create user, update email with invalid strings, should fail`(email: String) {
        val user = user()

        assertThrows<IllegalArgumentException> {
            user.email = email
        }
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.domain.data.DummyTestData#provideBlankStrings")
    fun `create user with invalid name, should fail`(name: String) {
        assertThrows<IllegalArgumentException> {
            User(name = name, repositoryId = repository.iid)
        }
    }

    @Test
    fun `create user, validate committedCommits relation is empty`() {
        val user = user()
        assertThat(user.committedCommits).isEmpty()
    }

    @Test
    fun `create user, validate authoredCommits relation is empty`() {
        val user = user()
        assertThat(user.authoredCommits).isEmpty()
    }

    @Nested
    inner class IssueRelation {
        @BeforeEach
        fun setUp() {
            this@UserModelTest.setUp()
        }

        @Test
        fun `create user, validate issue relation is empty`() {
            val user = User(name = "test-user", repositoryId = repository.iid).apply { this.repository = repository }
            assertThat(user.issues).isEmpty()
        }
    }

    @Nested
    inner class FileRelation {
        @BeforeEach
        fun setUp() {
            this@UserModelTest.setUp()
        }

        @Test
        fun `create user, validate files relation is empty`() {
            val user = User(name = "test-user", repositoryId = repository.iid).apply { this.repository = repository }
            assertThat(user.files).isEmpty()
        }
    }
}
