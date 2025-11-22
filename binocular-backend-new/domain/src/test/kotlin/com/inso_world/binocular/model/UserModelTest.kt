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
import kotlin.uuid.ExperimentalUuidApi

class UserModelTest {

    private lateinit var repository: Repository

    @BeforeEach
    fun setUp() {
        val project = Project(name = "test-project")
        repository = Repository(
            localPath = "test",
            project = project,
        )
    }

    @Test
    fun `create user, check that iid is created automatically`() {
        val user = User(name = "test-user", repository)

        assertThat(user.iid).isNotNull()
    }

    @Test
    fun `create user, validate uniqueKey`() {
        val user = User(name = "test-user", repository)

        @OptIn(ExperimentalUuidApi::class)
        assertAll(
            { assertThat(user.uniqueKey).isEqualTo(User.Key(repository.iid, "test-user")) },
            { assertThat(user.uniqueKey.repositoryId).isEqualTo(repository.iid) },
            // compare .value here
            // Because inline classes may be represented both as the underlying value and as a wrapper, referential equality is pointless for them and is therefore prohibited.
            // https://kotlinlang.org/docs/inline-classes.html#representation
            { assertThat(user.uniqueKey.repositoryId.value).isSameAs(repository.iid.value) },
            { assertThat(user.uniqueKey.name).isSameAs(user.name) },
        )
    }

    @Test
    fun `create user, validate hashCode is same based on iid`() {
        val user = User(name = "test-user", repository)

        assertThat(user.hashCode()).isEqualTo(user.iid.hashCode())
    }

    @Test
    fun `create user, is is null`() {
        val user = User(name = "test-user", repository)

        assertThat(user.id).isNull()
    }

    @Test
    fun `create user, check reference to owning repository`() {
        val user = User(name = "test-user", repository)

        assertThat(user.repository).isSameAs(repository)
        assertThat(user.repository.user).containsOnly(user)
        assertThat(repository.user).containsOnly(user)
    }

    @Test
    fun `create user, update email`() {
        val user = User(name = "test-user", repository)

        assertDoesNotThrow {
            user.email = "test-email"
        }
        assertThat(user.email).isEqualTo("test-email")
    }

    @Test
    fun `create user, update email, check gitSignature`() {
        val user = User(name = "test-user", repository)

        assertDoesNotThrow {
            user.email = "test-email"
        }
        assertThat(user.gitSignature).isEqualTo("test-user <test-email>")
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.data.DummyTestData#provideBlankStrings")
    fun `create user, update email with invalid strings, should fail`(
        email: String,
    ) {
        val user = User(name = "test-user", repository)

        assertThrows<IllegalArgumentException> {
            user.email = email
        }
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.data.DummyTestData#provideBlankStrings")
    fun `create user with invalid name, should fail`(
        name: String,
    ) {
        assertThrows<IllegalArgumentException> {
            User(name, repository)
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
                val user = User(name = "test-user", repository)
                assertThat(user.committedCommits).isEmpty()
            }

            @Test
            fun `create user, add committedCommit with different repository, should fail`() {
                val mockCommit =
                    MockTestDataProvider(this@UserModelTest.repository).commitBySha.getValue("a".repeat(40))

                val differentRepository = Repository(
                    localPath = "different-repository",
                    project = Project(name = "different-project"),
                )

                val user = User(name = "test-user", differentRepository)

                assertAll(
                    { assertThat(user.repository).isNotEqualTo(mockCommit.repository) },
                    {
                        assertThrows<IllegalArgumentException> {
                            user.committedCommits.add(mockCommit)
                        }
                    }
                )
            }

            @Test
            fun `create user, addAll committedCommit with different repository, should fail`() {
                val mockCommit =
                    MockTestDataProvider(this@UserModelTest.repository).commitBySha.getValue("a".repeat(40))

                val differentRepository = Repository(
                    localPath = "different-repository",
                    project = Project(name = "different-project"),
                )

                val user = User(name = "test-user", differentRepository)

                assertAll(
                    { assertThat(user.repository).isNotEqualTo(mockCommit.repository) },
                    {
                        assertThrows<IllegalArgumentException> {
                            user.committedCommits.addAll(listOf(mockCommit))
                        }
                    }
                )
            }

            @Test
            fun `create user, add committedCommit, not added since already done by constructor`() {
                val mockTestDataProvider = MockTestDataProvider(this@UserModelTest.repository)
                val mockCommit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))

                val user = mockTestDataProvider.userByEmail.getValue("a@test.com")

                assertFalse(user.committedCommits.add(mockCommit))
                assertAll(
                    { assertThat(user.authoredCommits).hasSize(0) },
                    { assertThat(user.committedCommits).hasSize(1) },
                    { assertThat(user.committedCommits).containsOnly(mockCommit) },
                    { assertThat(mockCommit.committer).isSameAs(user) }
                )
            }

            @Test
            fun `create user, add committedCommit without committer set, should fail`() {
                val mockCommit =
                    MockTestDataProvider(this@UserModelTest.repository).commitBySha.getValue("a".repeat(40))

                val user = User(name = "test-user", repository)

                assertThrows<IllegalArgumentException> {
                    user.committedCommits.add(mockCommit)
                }
            }

            @Test
            fun `create user, addAll committedCommit without committer set, should fail`() {
                val mockCommit =
                    MockTestDataProvider(this@UserModelTest.repository).commitBySha.getValue("a".repeat(40))

                val user = User(name = "test-user", repository)

                assertThrows<IllegalArgumentException> {
                    user.committedCommits.addAll(listOf(mockCommit))
                }
            }

            @Test
            fun `create user, add committedCommit twice, only added once`() {
                val mockCommit =
                    MockTestDataProvider(this@UserModelTest.repository).commitBySha.getValue("a".repeat(40))

                val user = User(name = "test-user", repository)

                // Set committer before adding to committedCommits
                setField(
                    mockCommit.javaClass.getDeclaredField("committer").apply { isAccessible = true },
                    mockCommit,
                    user
                )

                assertTrue(user.committedCommits.add(mockCommit))
                assertFalse(user.committedCommits.add(mockCommit))
                assertAll(
                    { assertThat(user.authoredCommits).hasSize(0) },
                    { assertThat(user.committedCommits).hasSize(1) },
                    { assertThat(user.committedCommits).containsOnly(mockCommit) },
                    { assertThat(mockCommit.committer).isSameAs(user) }
                )
            }

            @Test
            fun `create user, addAll committedCommit, be added`() {
                val mockCommit =
                    MockTestDataProvider(this@UserModelTest.repository).commitBySha.getValue("b".repeat(40))

                val user = User(name = "test-user", repository)

                // Set committer before adding to committedCommits
                setField(
                    mockCommit.javaClass.getDeclaredField("committer").apply { isAccessible = true },
                    mockCommit,
                    user
                )

                assertTrue(user.committedCommits.addAll(listOf(mockCommit)))
                assertAll(
                    { assertThat(user.authoredCommits).hasSize(0) },
                    { assertThat(user.committedCommits).hasSize(1) },
                    { assertThat(user.committedCommits).containsOnly(mockCommit) },
                    { assertThat(mockCommit.committer).isSameAs(user) }
                )
            }

            @Test
            fun `create user, addAll committedCommit twice, only added once`() {
                val mockCommit =
                    MockTestDataProvider(this@UserModelTest.repository).commitBySha.getValue("b".repeat(40))

                val user = User(name = "test-user", repository)

                // Set committer before adding to committedCommits
                setField(
                    mockCommit.javaClass.getDeclaredField("committer").apply { isAccessible = true },
                    mockCommit,
                    user
                )

                assertTrue(user.committedCommits.addAll(listOf(mockCommit)))
                assertFalse(user.committedCommits.addAll(listOf(mockCommit)))
                assertAll(
                    { assertThat(user.authoredCommits).hasSize(0) },
                    { assertThat(user.committedCommits).hasSize(1) },
                    { assertThat(user.committedCommits).containsOnly(mockCommit) },
                    { assertThat(mockCommit.committer).isSameAs(user) }
                )
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
                val user = User(name = "test-user", repository)
                assertThat(user.authoredCommits).isEmpty()
            }

            @Test
            fun `create user, add authoredCommits with different repository, should fail`() {
                val mockCommit =
                    MockTestDataProvider(this@UserModelTest.repository).commitBySha.getValue("a".repeat(40))

                val differentRepository = Repository(
                    localPath = "different-repository",
                    project = Project(name = "different-project"),
                )

                val user = User(name = "test-user", differentRepository)

                assertAll(
                    { assertThat(user.repository).isNotEqualTo(mockCommit.repository) },
                    {
                        assertThrows<IllegalArgumentException> {
                            user.authoredCommits.add(mockCommit)
                        }
                    }
                )
            }

            @Test
            fun `create user, addAll authoredCommits with different repository, should fail`() {
                val mockCommit =
                    MockTestDataProvider(this@UserModelTest.repository).commitBySha.getValue("a".repeat(40))

                val differentRepository = Repository(
                    localPath = "different-repository",
                    project = Project(name = "different-project"),
                )

                val user = User(name = "test-user", differentRepository)

                assertAll(
                    { assertThat(user.repository).isNotEqualTo(mockCommit.repository) },
                    {
                        assertThrows<IllegalArgumentException> {
                            user.authoredCommits.addAll(listOf(mockCommit))
                        }
                    }
                )
            }

            @Test
            fun `create user, add authoredCommits, be added`() {
                val mockCommit =
                    MockTestDataProvider(this@UserModelTest.repository).commitBySha.getValue("a".repeat(40))

                val user = User(name = "test-user", repository)

                assertTrue(user.authoredCommits.add(mockCommit))
                assertAll(
                    { assertThat(user.committedCommits).hasSize(0) },
                    { assertThat(user.authoredCommits).hasSize(1) },
                    { assertThat(user.authoredCommits).containsOnly(mockCommit) },
                    { assertThat(mockCommit.author).isSameAs(user) },
                )
            }

            @Test
            fun `create user, add authoredCommits twice, only added once`() {
                val mockCommit =
                    MockTestDataProvider(this@UserModelTest.repository).commitBySha.getValue("a".repeat(40))

                val user = User(name = "test-user", repository)

                assertTrue(user.authoredCommits.add(mockCommit))
                assertFalse(user.authoredCommits.add(mockCommit))
                assertAll(
                    { assertThat(user.committedCommits).hasSize(0) },
                    { assertThat(user.authoredCommits).hasSize(1) },
                    { assertThat(user.authoredCommits).containsOnly(mockCommit) }
                )
            }

            @Test
            fun `create user, addAll authoredCommits, be added`() {
                val mockCommit =
                    MockTestDataProvider(this@UserModelTest.repository).commitBySha.getValue("b".repeat(40))

                val user = User(name = "test-user", repository)

                assertTrue(user.authoredCommits.addAll(listOf(mockCommit)))
                assertAll(
                    { assertThat(user.committedCommits).hasSize(0) },
                    { assertThat(user.authoredCommits).hasSize(1) },
                    { assertThat(user.authoredCommits).containsOnly(mockCommit) },
                    { assertThat(mockCommit.author).isSameAs(user) },
                )
            }

            @Test
            fun `create user, addAll authoredCommits twice, only added once`() {
                val mockCommit =
                    MockTestDataProvider(this@UserModelTest.repository).commitBySha.getValue("b".repeat(40))

                val user = User(name = "test-user", repository)

                assertTrue(user.authoredCommits.addAll(listOf(mockCommit)))
                assertFalse(user.authoredCommits.addAll(listOf(mockCommit)))
                assertAll(
                    { assertThat(user.committedCommits).hasSize(0) },
                    { assertThat(user.authoredCommits).hasSize(1) },
                    { assertThat(user.authoredCommits).containsOnly(mockCommit) },
                )
            }

            @Test
            fun `create user, add as committer and author, should be same object`() {
                val mockCommit =
                    MockTestDataProvider(this@UserModelTest.repository).commitBySha.getValue("b".repeat(40))

                val user = User(name = "test-user", repository)
                setField(
                    mockCommit.javaClass.getDeclaredField("committer").apply { isAccessible = true },
                    mockCommit,
                    user
                )

                assertAll(
                    { assertTrue(user.committedCommits.add(mockCommit)) },
                    { assertTrue(user.authoredCommits.add(mockCommit)) }
                )

                assertAll(
                    { assertThat(user.committedCommits).hasSize(1) },
                    { assertThat(user.authoredCommits).hasSize(1) },
                    { assertThat(user.authoredCommits).containsOnly(mockCommit) },
                    { assertThat(user.committedCommits).containsOnly(mockCommit) },
                    { assertThat(user.committedCommits.first()).isSameAs(user.authoredCommits.first()) },
                )

                assertAll(
                    { assertThat(mockCommit.committer).isSameAs(user) },
                    { assertThat(mockCommit.author).isSameAs(user) },
                    { assertThat(mockCommit.author).isSameAs(mockCommit.committer) },
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
            val user = User(name = "test-user", repository)
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
            val user = User(name = "test-user", repository)
            assertThat(user.files).isEmpty()
        }
    }

}
