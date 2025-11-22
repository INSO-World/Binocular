package com.inso_world.binocular.model

import com.inso_world.binocular.data.MockTestDataProvider
import com.inso_world.binocular.model.utils.ReflectionUtils.Companion.setField
import com.inso_world.binocular.model.vcs.Remote
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
import org.junit.jupiter.params.provider.ValueSource
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.ExperimentalUuidApi

/**
 * Comprehensive test suite for the [Remote] domain model.
 *
 * Tests cover:
 * - Construction and validation
 * - Identity and equality semantics
 * - Repository relationships
 * - Business key uniqueness
 * - Add-only collection semantics
 * - Edge cases and error conditions
 */
class RemoteModelTest {

    private lateinit var mockTestDataProvider: MockTestDataProvider
    private lateinit var repository: Repository

    @BeforeEach
    fun setup() {
        val project = Project(name = "proj-remote-model-test")
        repository = Repository(
            localPath = "repo-remote-model-test",
            project = project,
        )
        mockTestDataProvider = MockTestDataProvider(repository)

        // Clear remotes collection via reflection
        val base = NonRemovingMutableSet::class.java
        val field = repository.javaClass.getDeclaredField("remotes")
            .apply { this.isAccessible = true }
        val obj = field.get(repository) ?: return

        val backingField = base.getDeclaredField("backing").apply { isAccessible = true }
        val backing = (backingField.get(obj) as ConcurrentHashMap<*, *>)
        backing.clear()
    }

    @Nested
    inner class Construction {
        @Test
        fun `create remote with valid name and url, should succeed`() {
            assertDoesNotThrow {
                Remote(
                    name = "origin",
                    url = "https://github.com/user/repo.git",
                    repository = repository
                )
            }
        }

        @Test
        fun `create remote, check iid is set automatically`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )

            assertThat(remote.iid).isNotNull()
        }

        @Test
        fun `create remote, check it is automatically added to repository`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )

            assertAll(
                { assertThat(repository.remotes).hasSize(1) },
                { assertThat(repository.remotes).contains(remote) },
                { assertThat(remote.repository).isSameAs(repository) }
            )
        }

        @Test
        fun `create remote, check id is null by default`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )

            assertThat(remote.id).isNull()
        }

        @ParameterizedTest
        @ValueSource(strings = ["origin", "upstream", "fork", "origin-backup", "my_remote", "remote.name", "remote/path"])
        fun `create remote with valid names, should succeed`(name: String) {
            assertDoesNotThrow {
                Remote(
                    name = name,
                    url = "https://github.com/user/repo.git",
                    repository = repository
                )
            }
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.data.DummyTestData#provideBlankStrings")
        fun `create remote with blank name, should fail`(name: String) {
            assertThrows<IllegalArgumentException> {
                Remote(
                    name = name,
                    url = "https://github.com/user/repo.git",
                    repository = repository
                )
            }
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.data.DummyTestData#provideBlankStrings")
        fun `create remote with blank url, should fail`(url: String) {
            assertThrows<IllegalArgumentException> {
                Remote(
                    name = "origin",
                    url = url,
                    repository = repository
                )
            }
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "https://github.com/user/repo.git",
                "git@github.com:user/repo.git",
                "ssh://git@github.com/user/repo.git",
                "git://github.com/user/repo.git",
                "https://gitlab.com/group/subgroup/project.git",
                "file:///path/to/repo.git",
                "/absolute/path/to/repo",
                "../relative/path/to/repo"
            ]
        )
        fun `create remote with various valid URLs, should succeed`(url: String) {
            assertDoesNotThrow {
                Remote(
                    name = "origin",
                    url = url,
                    repository = repository
                )
            }
        }
    }

    @Nested
    inner class IdentityAndEquality {
        @Test
        fun `create remote, validate uniqueKey`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )

            @OptIn(ExperimentalUuidApi::class)
            assertAll(
                { assertThat(remote.uniqueKey).isEqualTo(Remote.Key(repository.iid, "origin")) },
                { assertThat(remote.uniqueKey.repositoryId).isEqualTo(repository.iid) },
                // Compare .value here because inline classes may be represented both as the underlying value and as a wrapper
                // https://kotlinlang.org/docs/inline-classes.html#representation
                { assertThat(remote.uniqueKey.repositoryId.value).isSameAs(repository.iid.value) },
                { assertThat(remote.uniqueKey.name).isEqualTo("origin") }
            )
        }

        @Test
        fun `create remote with name containing whitespace, uniqueKey should trim`() {
            val remote = Remote(
                name = "  origin  ",
                url = "https://github.com/user/repo.git",
                repository = repository
            )

            assertThat(remote.uniqueKey.name).isEqualTo("origin")
        }

        @Test
        fun `create remote, validate hashCode is based on iid`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )

            assertThat(remote.hashCode()).isEqualTo(remote.iid.hashCode())
        }

        @Test
        fun `create two remotes, check they are not equal`() {
            val remoteA = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )
            val remoteB = Remote(
                name = "upstream",
                url = "https://github.com/other/repo.git",
                repository = repository
            )

            assertAll(
                { assertThat(remoteA).isNotSameAs(remoteB) },
                { assertThat(remoteA).isNotEqualTo(remoteB) },
                { assertThat(remoteA.iid).isNotEqualTo(remoteB.iid) }
            )
        }

        @Test
        fun `create remote, copy and edit iid via reflection, should be equal`() {
            val remoteA = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )
            val originIid = remoteA.iid
            val originUniqueKey = remoteA.uniqueKey

            val remoteB = remoteA.copy()

            // Edit iid and repository to match remoteA
            setField(
                remoteB.javaClass.superclass.getDeclaredField("iid"),
                remoteB,
                originIid
            )
            setField(
                remoteB.javaClass.getDeclaredField("repository"),
                remoteB,
                remoteA.repository
            )

            assertAll(
                { assertThat(remoteA).isNotSameAs(remoteB) },
                { assertThat(remoteA).isEqualTo(remoteB) },
                { assertThat(remoteA.iid).isEqualTo(originIid) },
                { assertThat(remoteA.uniqueKey).isEqualTo(originUniqueKey) },
                { assertThat(remoteA.iid).isEqualTo(remoteB.iid) }
            )
        }

        @Test
        fun `create remote, copy and edit iid via reflection, change name, should not be equal`() {
            val remoteA = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )
            val originIid = remoteA.iid
            val originUniqueKey = remoteA.uniqueKey

            val remoteB = remoteA.copy(name = "upstream")

            // Edit iid and repository to match remoteA
            setField(
                remoteB.javaClass.superclass.getDeclaredField("iid"),
                remoteB,
                originIid
            )
            setField(
                remoteB.javaClass.getDeclaredField("repository"),
                remoteB,
                remoteA.repository
            )

            assertAll(
                { assertThat(remoteA).isNotSameAs(remoteB) },
                { assertThat(remoteA).isNotEqualTo(remoteB) },
                { assertThat(remoteA.iid).isEqualTo(originIid) },
                { assertThat(remoteA.uniqueKey).isEqualTo(originUniqueKey) },
                { assertThat(remoteA.iid).isEqualTo(remoteB.iid) }
            )
        }
    }

    @Nested
    inner class RepositoryRelation {
        @BeforeEach
        fun setup() {
            this@RemoteModelTest.setup()
        }

        @Test
        fun `add remote to repository once, should be added`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )

            // Remote is already added via constructor
            assertAll(
                { assertThat(repository.remotes).hasSize(1) },
                { assertThat(repository.remotes).contains(remote) },
                { assertThat(remote.repository).isSameAs(repository) }
            )
        }

        @Test
        fun `add same remote to repository twice, should only be added once`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )

            assertAll(
                // Already added via constructor
                { assertFalse(repository.remotes.add(remote)) },
                { assertThat(repository.remotes).hasSize(1) }
            )
        }

        @Test
        fun `add multiple remotes with different names, expect all to be added`() {
            val origin = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )
            val upstream = Remote(
                name = "upstream",
                url = "https://github.com/upstream/repo.git",
                repository = repository
            )
            val fork = Remote(
                name = "fork",
                url = "https://github.com/fork/repo.git",
                repository = repository
            )

            assertAll(
                { assertThat(repository.remotes).hasSize(3) },
                { assertThat(repository.remotes).contains(origin, upstream, fork) }
            )
        }

        @Test
        fun `add remote with same name twice, expect only first to be added`() {
            val remoteA = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )
            val remoteB = Remote(
                name = "origin",
                url = "https://github.com/other/repo.git",
                repository = repository
            )

            assertAll(
                // First one added via constructor
                { assertThat(repository.remotes).hasSize(1) },
                { assertThat(repository.remotes.first()).isSameAs(remoteA) },
                { assertThat(repository.remotes.first().url).isEqualTo("https://github.com/user/repo.git") }
            )
        }

        @Test
        fun `add remote via addAll, expect to be added`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )

            // Already added via constructor
            assertFalse(repository.remotes.addAll(listOf(remote)))
            assertThat(repository.remotes).hasSize(1)
        }

        @Test
        fun `add multiple remotes via addAll, expect all to be added`() {
            val origin = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )
            val upstream = Remote(
                name = "upstream",
                url = "https://github.com/upstream/repo.git",
                repository = repository
            )

            // Already added via constructor
            assertFalse(repository.remotes.addAll(listOf(origin, upstream)))
            assertThat(repository.remotes).hasSize(2)
        }

        @Test
        fun `add same remote twice via addAll, expect only one to be added`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )

            assertAll(
                // Already added via constructor
                { assertFalse(repository.remotes.addAll(listOf(remote))) },
                { assertFalse(repository.remotes.addAll(listOf(remote))) },
                { assertThat(repository.remotes).hasSize(1) }
            )
        }

        @Test
        fun `add duplicate remotes via addAll, expect unique to be added`() {
            val remoteA = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )
            val remoteB = Remote(
                name = "upstream",
                url = "https://github.com/upstream/repo.git",
                repository = repository
            )
            val remoteC = Remote(
                name = "origin", // Same name as remoteA
                url = "https://github.com/other/repo.git",
                repository = repository
            )

            assertAll(
                // All added via constructor, but remoteC has duplicate name
                { assertThat(repository.remotes).hasSize(2) },
                { assertThat(repository.remotes).contains(remoteA, remoteB) },
                { assertThat(repository.remotes).doesNotContain(remoteC) }
            )
        }

        @Test
        fun `add remote from different repository, should fail`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )
            val otherRepo = mockTestDataProvider.repositoriesByPath.getValue("repo-pg-1")

            assertThat(remote.repository).isNotSameAs(otherRepo)

            assertThrows<IllegalArgumentException> {
                otherRepo.remotes.add(remote)
            }
        }

        @Test
        fun `add empty collection of remotes, expect no remotes added`() {
            val emptyList = emptyList<Remote>()

            assertFalse(repository.remotes.addAll(emptyList))
            assertThat(repository.remotes).hasSize(0)
        }
    }

    @Nested
    inner class RemovalOperations {
        @BeforeEach
        fun setup() {
            this@RemoteModelTest.setup()
        }

        @Test
        fun `remove remote from repository should throw UnsupportedOperationException`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )
            assertThat(repository.remotes).hasSize(1)

            assertThrows<UnsupportedOperationException> {
                repository.remotes.remove(remote)
            }
            assertThat(repository.remotes).hasSize(1) // Should still be there
        }

        @Test
        fun `clear all remotes should throw UnsupportedOperationException`() {
            Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )
            Remote(
                name = "upstream",
                url = "https://github.com/upstream/repo.git",
                repository = repository
            )
            assertThat(repository.remotes).hasSize(2)

            assertThrows<UnsupportedOperationException> {
                repository.remotes.clear()
            }
            assertThat(repository.remotes).hasSize(2) // Should still be there
        }

        @Test
        fun `remove remote by predicate should throw UnsupportedOperationException`() {
            Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )
            Remote(
                name = "upstream",
                url = "https://github.com/upstream/repo.git",
                repository = repository
            )
            assertThat(repository.remotes).hasSize(2)

            assertThrows<UnsupportedOperationException> {
                repository.remotes.removeIf { it.name == "origin" }
            }
            assertThat(repository.remotes).hasSize(2) // Should still be there
        }

        @Test
        fun `retain only specific remotes should throw UnsupportedOperationException`() {
            val origin = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )
            val upstream = Remote(
                name = "upstream",
                url = "https://github.com/upstream/repo.git",
                repository = repository
            )
            val fork = Remote(
                name = "fork",
                url = "https://github.com/fork/repo.git",
                repository = repository
            )
            assertThat(repository.remotes).hasSize(3)

            assertThrows<UnsupportedOperationException> {
                repository.remotes.retainAll(setOf(origin, fork))
            }
            assertThat(repository.remotes).hasSize(3) // Should still be there
        }

        @Test
        fun `remove via iterator should throw UnsupportedOperationException`() {
            Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )
            assertThat(repository.remotes).hasSize(1)

            val iterator = repository.remotes.iterator()
            assertTrue(iterator.hasNext())
            iterator.next()

            assertThrows<UnsupportedOperationException> {
                iterator.remove()
            }
            assertThat(repository.remotes).hasSize(1) // Should still be there
        }
    }

    @Nested
    inner class MutationOperations {
        @BeforeEach
        fun setup() {
            this@RemoteModelTest.setup()
        }

        @Test
        fun `create remote then modify url, expect changes to persist`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )
            assertThat(repository.remotes).hasSize(1)

            // Modify URL
            remote.url = "git@github.com:user/repo.git"

            assertAll(
                { assertThat(repository.remotes).hasSize(1) },
                { assertThat(repository.remotes.first().url).isEqualTo("git@github.com:user/repo.git") }
            )
        }

        @Test
        fun `create remote then modify database id, expect changes to persist`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )
            assertThat(remote.id).isNull()

            // Modify database ID (as infrastructure layers would do)
            remote.id = "db-id-123"

            assertThat(remote.id).isEqualTo("db-id-123")
        }
    }

    @Nested
    inner class EdgeCases {
        @BeforeEach
        fun setup() {
            this@RemoteModelTest.setup()
        }

        @Test
        fun `create remote with very long name, should be added`() {
            val longName = "remote-" + "a".repeat(1000)
            val remote = Remote(
                name = longName,
                url = "https://github.com/user/repo.git",
                repository = repository
            )

            assertAll(
                { assertThat(repository.remotes).hasSize(1) },
                { assertThat(remote.name).isEqualTo(longName) }
            )
        }

        @Test
        fun `create remote with very long url, should be added`() {
            val longUrl = "https://github.com/" + "a".repeat(1000) + "/repo.git"
            val remote = Remote(
                name = "origin",
                url = longUrl,
                repository = repository
            )

            assertAll(
                { assertThat(repository.remotes).hasSize(1) },
                { assertThat(remote.url).isEqualTo(longUrl) }
            )
        }

        @Test
        fun `create remote with special characters in URL, should succeed`() {
            val specialUrl = "https://user:password@github.com:8080/path/to/repo.git?param=value#fragment"
            val remote = Remote(
                name = "origin",
                url = specialUrl,
                repository = repository
            )

            assertAll(
                { assertThat(repository.remotes).hasSize(1) },
                { assertThat(remote.url).isEqualTo(specialUrl) }
            )
        }

        @Test
        fun `create remotes with same url but different names, should all be added`() {
            val sameUrl = "https://github.com/user/repo.git"
            val origin = Remote(
                name = "origin",
                url = sameUrl,
                repository = repository
            )
            val backup = Remote(
                name = "backup",
                url = sameUrl,
                repository = repository
            )

            assertAll(
                { assertThat(repository.remotes).hasSize(2) },
                { assertThat(repository.remotes).contains(origin, backup) }
            )
        }

        @Test
        fun `create remote with name trimming, check uniqueKey uses trimmed value`() {
            val remote = Remote(
                name = "  origin  ",
                url = "https://github.com/user/repo.git",
                repository = repository
            )

            assertAll(
                { assertThat(remote.name).isEqualTo("  origin  ") },
                { assertThat(remote.uniqueKey.name).isEqualTo("origin") }
            )
        }

        @Test
        fun `create multiple repositories with same remote name, should all work independently`() {
            val repo1 = repository
            val repo2 = mockTestDataProvider.repositoriesByPath.getValue("repo-pg-1")

            val remote1 = Remote(
                name = "origin",
                url = "https://github.com/user/repo1.git",
                repository = repo1
            )
            val remote2 = Remote(
                name = "origin",
                url = "https://github.com/user/repo2.git",
                repository = repo2
            )

            assertAll(
                { assertThat(repo1.remotes).hasSize(1) },
                { assertThat(repo2.remotes).hasSize(1) },
                { assertThat(remote1.name).isEqualTo("origin") },
                { assertThat(remote2.name).isEqualTo("origin") },
                { assertThat(remote1.uniqueKey).isNotEqualTo(remote2.uniqueKey) }
            )
        }
    }
}
