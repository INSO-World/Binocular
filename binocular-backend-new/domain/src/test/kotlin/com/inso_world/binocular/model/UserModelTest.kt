package com.inso_world.binocular.model

import com.inso_world.binocular.domain.data.MockTestDataProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.uuid.ExperimentalUuidApi

class UserModelTest {
    private lateinit var repository: Repository

    @BeforeEach
    fun setUp() {
        val project = Project(name = "test-project")
        repository =
            Repository(
                localPath = "test",
                project = project,
            )
    }

    @Test
    fun `create user, check that iid is created automatically`() {
        val user = User(name = "test-user", email = "test@example.com", repository)

        assertThat(user.iid).isNotNull()
    }

    @Test
    fun `create user, validate uniqueKey`() {
        val user = User(name = "test-user", email = "test@example.com", repository)

        @OptIn(ExperimentalUuidApi::class)
        assertAll(
            { assertThat(user.uniqueKey).isEqualTo(User.Key(repository.iid, "test-user", email = "test@example.com")) },
            { assertThat(user.uniqueKey.repositoryId).isEqualTo(repository.iid) },
            // compare .value here
            // Because inline classes may be represented both as the underlying value and as a wrapper, referential equality is pointless for them and is therefore prohibited.
            // https://kotlinlang.org/docs/inline-classes.html#representation
            { assertThat(user.uniqueKey.repositoryId.value).isSameAs(repository.iid.value) },
        )
    }

    @Test
    fun `create user, validate hashCode is same based on iid`() {
        val user = User(name = "test-user", email = "test@example.com", repository)

        assertThat(user.hashCode()).isEqualTo(user.iid.hashCode())
    }

    @Test
    fun `create user, is is null`() {
        val user = User(name = "test-user", email = "test@example.com", repository)

        assertThat(user.id).isNull()
    }

    @Test
    fun `create user, check reference to owning repository`() {
        val user = User(name = "test-user", email = "test@example.com", repository)

        assertThat(user.repository).isSameAs(repository)
        assertThat(user.repository.user).containsOnly(user)
        assertThat(repository.user).containsOnly(user)
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.domain.data.DummyTestData#provideBlankStrings")
    fun `create user with invalid name, should fail`(name: String) {
        assertThrows<IllegalArgumentException> {
            User(name, email = "test@example.com", repository)
        }
    }

    @Nested
    inner class CommitRelations {
        @BeforeEach
        fun setUp() {
            this@UserModelTest.setUp()
        }

        @Nested
        inner class CommittedCommits {
            @BeforeEach
            fun setUp() {
                this@CommitRelations.setUp()
            }

            @Test
            fun `create user, validate committedCommits relation is empty`() {
                val user = User(name = "test-user", email = "test@example.com", repository)
                assertThat(user.committedCommits).isEmpty()
            }

            @Test
            fun `create user, add committedCommit with different repository, should fail`() {
                val mockCommit =
                    MockTestDataProvider(this@UserModelTest.repository).commitBySha.getValue("a".repeat(40))

                val differentRepository =
                    Repository(
                        localPath = "different-repository",
                        project = Project(name = "different-project"),
                    )

                val user = User(name = "test-user", email = "test@example.com", differentRepository)

                assertAll(
                    { assertThat(user.repository).isNotEqualTo(mockCommit.repository) },
                    {
                        assertThrows<IllegalArgumentException> {
                            user.committedCommits.add(mockCommit)
                        }
                    },
                )
            }

            @Test
            fun `create user, add committedCommit from same repository but developer committer, should fail`() {
                val mockCommit =
                    MockTestDataProvider(this@UserModelTest.repository).commitBySha.getValue("a".repeat(40))

                val user = User(name = "test-user", email = "test@example.com", repository)

                assertThrows<IllegalArgumentException> {
                    user.committedCommits.add(mockCommit)
                }
            }
        }

        @Nested
        inner class AuthoredCommits {
            @BeforeEach
            fun setUp() {
                this@CommitRelations.setUp()
            }

            @Test
            fun `create user, validate authoredCommits relation is empty`() {
                val user = User(name = "test-user", email = "test@example.com", repository)
                assertThat(user.authoredCommits).isEmpty()
            }

            @Test
            fun `create user, add authoredCommits with different repository, should fail`() {
                val mockCommit =
                    MockTestDataProvider(this@UserModelTest.repository).commitBySha.getValue("a".repeat(40))

                val differentRepository =
                    Repository(
                        localPath = "different-repository",
                        project = Project(name = "different-project"),
                    )

                val user = User(name = "test-user", email = "test@example.com", differentRepository)

                assertAll(
                    { assertThat(user.repository).isNotEqualTo(mockCommit.repository) },
                    {
                        assertThrows<IllegalArgumentException> {
                            user.authoredCommits.add(mockCommit)
                        }
                    },
                )
            }

            @Test
            fun `create user, add authoredCommit from different repository, should fail`() {
                val mockCommit =
                    MockTestDataProvider(this@UserModelTest.repository).commitBySha.getValue("a".repeat(40))

                val differentRepository =
                    Repository(
                        localPath = "different-repository",
                        project = Project(name = "different-project"),
                    )

                val user = User(name = "test-user", email = "test@example.com", differentRepository)

                assertThrows<IllegalArgumentException> {
                    user.authoredCommits.add(mockCommit)
                }
            }

            @Test
            fun `create user, add authoredCommit from same repository, should add without mutating commit author`() {
                val mockCommit =
                    MockTestDataProvider(this@UserModelTest.repository).commitBySha.getValue("a".repeat(40))

                val user = User(name = "test-user", email = "test@example.com", repository)

                assertTrue(user.authoredCommits.add(mockCommit))
                assertAll(
                    { assertThat(user.authoredCommits).containsOnly(mockCommit) },
                    { assertThat(mockCommit.author).isNotEqualTo(user) },
                )
            }
        }
    }

    @Nested
    inner class IssueRelation {
        @BeforeEach
        fun setUp() {
            this@UserModelTest.setUp()
        }

        @Test
        fun `create user, validate issue relation is empty`() {
            val user = User(name = "test-user", email = "test@example.com", repository)
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
            val user = User(name = "test-user", email = "test@example.com", repository)
            assertThat(user.files).isEmpty()
        }
    }
}
