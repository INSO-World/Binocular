package com.inso_world.binocular.model

import com.inso_world.binocular.domain.data.MockTestDataProvider
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
            projectId = project.iid,
        )
        mockTestDataProvider = MockTestDataProvider(repository)
    }

    @Nested
    inner class Construction {
        @Test
        fun `create remote with valid name and url, should succeed`() {
            assertDoesNotThrow {
                Remote(
                    name = "origin",
                    url = "https://github.com/user/repo.git",
                    repositoryId = repository.iid
                )
            }
        }

        @Test
        fun `create remote, check iid is set automatically`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            assertThat(remote.iid).isNotNull()
        }

        @Test
        fun `create remote, check it must be explicitly added to repository`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            // Remote is NOT auto-added anymore; must be explicitly added
            assertAll(
                { assertThat(repository.remoteIds).isEmpty() },
                { assertThat(remote.repositoryId).isEqualTo(repository.iid) }
            )

            // Explicit add works
            assertTrue(repository.remoteIds.add(remote.iid))
            assertAll(
                { assertThat(repository.remoteIds).hasSize(1) },
                { assertThat(repository.remoteIds).contains(remote.iid) }
            )
        }

        @Test
        fun `create remote, check id is null by default`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
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
                    repositoryId = repository.iid
                )
            }
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.domain.data.DummyTestData#provideBlankStrings")
        fun `create remote with blank name, should fail`(name: String) {
            assertThrows<IllegalArgumentException> {
                Remote(
                    name = name,
                    url = "https://github.com/user/repo.git",
                    repositoryId = repository.iid
                )
            }
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.domain.data.DummyTestData#provideBlankStrings")
        fun `create remote with blank url, should fail`(url: String) {
            assertThrows<IllegalArgumentException> {
                Remote(
                    name = "origin",
                    url = url,
                    repositoryId = repository.iid
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
                    repositoryId = repository.iid
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
                repositoryId = repository.iid
            )

            @OptIn(ExperimentalUuidApi::class)
            assertAll(
                { assertThat(remote.uniqueKey).isEqualTo(Remote.Key(repository.iid, "origin")) },
                { assertThat(remote.uniqueKey.repositoryId).isEqualTo(repository.iid) },
                { assertThat(remote.uniqueKey.repositoryId.value).isSameAs(repository.iid.value) },
                { assertThat(remote.uniqueKey.name).isEqualTo("origin") }
            )
        }

        @Test
        fun `create remote with name containing whitespace, uniqueKey should trim`() {
            val remote = Remote(
                name = "  origin  ",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            assertThat(remote.uniqueKey.name).isEqualTo("origin")
        }

        @Test
        fun `create remote, validate hashCode is based on iid`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            assertThat(remote.hashCode()).isEqualTo(remote.iid.hashCode())
        }

        @Test
        fun `create two remotes, check they are not equal`() {
            val remoteA = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )
            val remoteB = Remote(
                name = "upstream",
                url = "https://github.com/other/repo.git",
                repositoryId = repository.iid
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
                repositoryId = repository.iid
            )
            val originIid = remoteA.iid
            val originUniqueKey = remoteA.uniqueKey

            val remoteB = remoteA.copy()

            setField(
                remoteB.javaClass.superclass.getDeclaredField("iid"),
                remoteB,
                originIid
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
                repositoryId = repository.iid
            )
            val originIid = remoteA.iid
            val originUniqueKey = remoteA.uniqueKey

            val remoteB = remoteA.copy(name = "upstream")

            setField(
                remoteB.javaClass.superclass.getDeclaredField("iid"),
                remoteB,
                originIid
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
                repositoryId = repository.iid
            )

            assertTrue(repository.remoteIds.add(remote.iid))
            assertAll(
                { assertThat(repository.remoteIds).hasSize(1) },
                { assertThat(repository.remoteIds).contains(remote.iid) },
                { assertThat(remote.repositoryId).isEqualTo(repository.iid) }
            )
        }

        @Test
        fun `add same remote to repository twice, should only be added once`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            assertAll(
                { assertTrue(repository.remoteIds.add(remote.iid)) },
                { assertFalse(repository.remoteIds.add(remote.iid)) },
                { assertThat(repository.remoteIds).hasSize(1) }
            )
        }

        @Test
        fun `add multiple remotes with different names, expect all to be added`() {
            val origin = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )
            val upstream = Remote(
                name = "upstream",
                url = "https://github.com/upstream/repo.git",
                repositoryId = repository.iid
            )
            val fork = Remote(
                name = "fork",
                url = "https://github.com/fork/repo.git",
                repositoryId = repository.iid
            )

            assertTrue(repository.remoteIds.add(origin.iid))
            assertTrue(repository.remoteIds.add(upstream.iid))
            assertTrue(repository.remoteIds.add(fork.iid))

            assertAll(
                { assertThat(repository.remoteIds).hasSize(3) },
                { assertThat(repository.remoteIds).contains(origin.iid, upstream.iid, fork.iid) }
            )
        }

        @Test
        fun `add remote with same name twice, expect both IDs to be added`() {
            val remoteA = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )
            val remoteB = Remote(
                name = "origin",
                url = "https://github.com/other/repo.git",
                repositoryId = repository.iid
            )

            assertTrue(repository.remoteIds.add(remoteA.iid))
            assertTrue(repository.remoteIds.add(remoteB.iid))

            assertAll(
                { assertThat(repository.remoteIds).hasSize(2) },
                { assertThat(repository.remoteIds).contains(remoteA.iid) },
                { assertThat(remoteA.url).isEqualTo("https://github.com/user/repo.git") }
            )
        }

        @Test
        fun `add remote via addAll, expect to be added`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            assertTrue(repository.remoteIds.addAll(listOf(remote.iid)))
            assertThat(repository.remoteIds).hasSize(1)
        }

        @Test
        fun `add multiple remotes via addAll, expect all to be added`() {
            val origin = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )
            val upstream = Remote(
                name = "upstream",
                url = "https://github.com/upstream/repo.git",
                repositoryId = repository.iid
            )

            assertTrue(repository.remoteIds.addAll(listOf(origin.iid, upstream.iid)))
            assertThat(repository.remoteIds).hasSize(2)
        }

        @Test
        fun `add same remote twice via addAll, expect only one to be added`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            assertAll(
                { assertTrue(repository.remoteIds.addAll(listOf(remote.iid))) },
                { assertFalse(repository.remoteIds.addAll(listOf(remote.iid))) },
                { assertThat(repository.remoteIds).hasSize(1) }
            )
        }

        @Test
        fun `add duplicate remotes via addAll, expect unique to be added`() {
            val remoteA = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )
            val remoteB = Remote(
                name = "upstream",
                url = "https://github.com/upstream/repo.git",
                repositoryId = repository.iid
            )
            val remoteC = Remote(
                name = "origin",
                url = "https://github.com/other/repo.git",
                repositoryId = repository.iid
            )

            assertTrue(repository.remoteIds.addAll(listOf(remoteA.iid, remoteB.iid, remoteC.iid)))

            assertAll(
                { assertThat(repository.remoteIds).hasSize(3) },
                { assertThat(repository.remoteIds).contains(remoteA.iid, remoteB.iid, remoteC.iid) }
            )
        }

        @Test
        fun `add remote from different repository, should work with ID-based collection`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )
            val otherRepo = mockTestDataProvider.repositoriesByPath.getValue("repo-pg-1")

            assertThat(remote.repositoryId).isNotEqualTo(otherRepo.iid)

            assertTrue(otherRepo.remoteIds.add(remote.iid))
        }

        @Test
        fun `add empty collection of remotes, expect no remotes added`() {
            val emptyList = emptyList<Remote.Id>()

            assertFalse(repository.remoteIds.addAll(emptyList))
            assertThat(repository.remoteIds).hasSize(0)
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
                repositoryId = repository.iid
            )
            repository.remoteIds.add(remote.iid)
            assertThat(repository.remoteIds).hasSize(1)

            assertThrows<UnsupportedOperationException> {
                repository.remoteIds.remove(remote.iid)
            }
            assertThat(repository.remoteIds).hasSize(1)
        }

        @Test
        fun `clear all remotes should throw UnsupportedOperationException`() {
            val remoteA = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )
            val remoteB = Remote(
                name = "upstream",
                url = "https://github.com/upstream/repo.git",
                repositoryId = repository.iid
            )
            repository.remoteIds.addAll(listOf(remoteA.iid, remoteB.iid))
            assertThat(repository.remoteIds).hasSize(2)

            assertThrows<UnsupportedOperationException> {
                repository.remoteIds.clear()
            }
            assertThat(repository.remoteIds).hasSize(2)
        }

        @Test
        fun `remove remote by predicate should throw UnsupportedOperationException`() {
            val remoteA = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )
            val remoteB = Remote(
                name = "upstream",
                url = "https://github.com/upstream/repo.git",
                repositoryId = repository.iid
            )
            repository.remoteIds.addAll(listOf(remoteA.iid, remoteB.iid))
            assertThat(repository.remoteIds).hasSize(2)

            assertThrows<UnsupportedOperationException> {
                repository.remoteIds.removeIf { true }
            }
            assertThat(repository.remoteIds).hasSize(2)
        }

        @Test
        fun `retain only specific remotes should throw UnsupportedOperationException`() {
            val origin = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )
            val upstream = Remote(
                name = "upstream",
                url = "https://github.com/upstream/repo.git",
                repositoryId = repository.iid
            )
            val fork = Remote(
                name = "fork",
                url = "https://github.com/fork/repo.git",
                repositoryId = repository.iid
            )
            repository.remoteIds.addAll(listOf(origin.iid, upstream.iid, fork.iid))
            assertThat(repository.remoteIds).hasSize(3)

            assertThrows<UnsupportedOperationException> {
                repository.remoteIds.retainAll(setOf(origin.iid, fork.iid))
            }
            assertThat(repository.remoteIds).hasSize(3)
        }

        @Test
        fun `remove via iterator should throw UnsupportedOperationException`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )
            repository.remoteIds.add(remote.iid)
            assertThat(repository.remoteIds).hasSize(1)

            val iterator = repository.remoteIds.iterator()
            assertTrue(iterator.hasNext())
            iterator.next()

            assertThrows<UnsupportedOperationException> {
                iterator.remove()
            }
            assertThat(repository.remoteIds).hasSize(1)
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
                repositoryId = repository.iid
            )
            repository.remoteIds.add(remote.iid)
            assertThat(repository.remoteIds).hasSize(1)

            remote.url = "git@github.com:user/repo.git"

            assertAll(
                { assertThat(repository.remoteIds).hasSize(1) },
                { assertThat(remote.url).isEqualTo("git@github.com:user/repo.git") }
            )
        }

        @Test
        fun `create remote then modify database id, expect changes to persist`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )
            assertThat(remote.id).isNull()

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
                repositoryId = repository.iid
            )

            assertAll(
                { assertThat(remote.name).isEqualTo(longName) },
                { assertTrue(repository.remoteIds.add(remote.iid)) }
            )
        }

        @Test
        fun `create remote with very long url, should be added`() {
            val longUrl = "https://github.com/" + "a".repeat(1000) + "/repo.git"
            val remote = Remote(
                name = "origin",
                url = longUrl,
                repositoryId = repository.iid
            )

            assertAll(
                { assertTrue(repository.remoteIds.add(remote.iid)) },
                { assertThat(remote.url).isEqualTo(longUrl) }
            )
        }

        @Test
        fun `create remote with special characters in URL, should succeed`() {
            val specialUrl = "https://user:password@github.com:8080/path/to/repo.git?param=value#fragment"
            val remote = Remote(
                name = "origin",
                url = specialUrl,
                repositoryId = repository.iid
            )

            assertAll(
                { assertTrue(repository.remoteIds.add(remote.iid)) },
                { assertThat(remote.url).isEqualTo(specialUrl) }
            )
        }

        @Test
        fun `create remotes with same url but different names, should all be added`() {
            val sameUrl = "https://github.com/user/repo.git"
            val origin = Remote(
                name = "origin",
                url = sameUrl,
                repositoryId = repository.iid
            )
            val backup = Remote(
                name = "backup",
                url = sameUrl,
                repositoryId = repository.iid
            )

            assertTrue(repository.remoteIds.add(origin.iid))
            assertTrue(repository.remoteIds.add(backup.iid))

            assertAll(
                { assertThat(repository.remoteIds).hasSize(2) },
                { assertThat(repository.remoteIds).contains(origin.iid, backup.iid) }
            )
        }

        @Test
        fun `create remote with name trimming, check uniqueKey uses trimmed value`() {
            val remote = Remote(
                name = "  origin  ",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
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
                repositoryId = repo1.iid
            )
            val remote2 = Remote(
                name = "origin",
                url = "https://github.com/user/repo2.git",
                repositoryId = repo2.iid
            )

            assertAll(
                { assertThat(remote1.repositoryId).isEqualTo(repo1.iid) },
                { assertThat(remote2.repositoryId).isEqualTo(repo2.iid) },
                { assertThat(remote1.name).isEqualTo("origin") },
                { assertThat(remote2.name).isEqualTo("origin") },
                { assertThat(remote1.uniqueKey).isNotEqualTo(remote2.uniqueKey) }
            )
        }
    }
}
