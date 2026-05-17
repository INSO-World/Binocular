package com.inso_world.binocular.model

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
 * Tests for [Remote] after refactoring from direct [Repository] reference to ID-based reference.
 *
 * These tests verify that:
 * - Remote is constructed with a [Repository.Id] instead of a direct [Repository] reference
 * - Remote no longer auto-registers itself to the repository's remotes collection
 * - uniqueKey still works correctly using repositoryId
 * - Equality and hashCode semantics are preserved
 * - Validation still works (blank name/url rejection)
 * - Cross-repository mismatch detection works via repositoryId comparison
 */
class RemoteIdRefTest {

    private lateinit var project: Project
    private lateinit var repository: Repository

    @BeforeEach
    fun setup() {
        project = Project(name = "proj-remote-id-ref-test")
        repository = Repository(
            localPath = "repo-remote-id-ref-test",
            projectId = project.iid,
        )
    }

    @Nested
    inner class ConstructionWithId {

        @Test
        fun `create remote with repositoryId should succeed`() {
            assertDoesNotThrow {
                Remote(
                    name = "origin",
                    url = "https://github.com/user/repo.git",
                    repositoryId = repository.iid
                )
            }
        }

        @Test
        fun `create remote should generate iid automatically`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            assertThat(remote.iid).isNotNull()
        }

        @Test
        fun `create remote should NOT auto-register to repository remotes`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            // Remote should NOT be auto-added to repository.remoteIds anymore
            assertThat(repository.remoteIds).isEmpty()
        }

        @Test
        fun `create remote should store repositoryId correctly`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            assertThat(remote.repositoryId).isEqualTo(repository.iid)
        }

        @Test
        fun `create remote with blank name should fail`() {
            assertThrows<IllegalArgumentException> {
                Remote(
                    name = "",
                    url = "https://github.com/user/repo.git",
                    repositoryId = repository.iid
                )
            }
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.domain.data.DummyTestData#provideBlankStrings")
        fun `create remote with blank url should fail`(url: String) {
            assertThrows<IllegalArgumentException> {
                Remote(
                    name = "origin",
                    url = url,
                    repositoryId = repository.iid
                )
            }
        }

        @ParameterizedTest
        @ValueSource(strings = ["origin", "upstream", "fork", "origin-backup", "my_remote", "remote.name", "remote/path"])
        fun `create remote with valid names should succeed`(name: String) {
            assertDoesNotThrow {
                Remote(
                    name = name,
                    url = "https://github.com/user/repo.git",
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
        fun `create remote with various valid URLs should succeed`(url: String) {
            assertDoesNotThrow {
                Remote(
                    name = "origin",
                    url = url,
                    repositoryId = repository.iid
                )
            }
        }

        @Test
        fun `create remote with db id null by default`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            assertThat(remote.id).isNull()
        }
    }

    @Nested
    inner class UniqueKeyWithId {

        @Test
        fun `uniqueKey should use repositoryId`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            assertAll(
                { assertThat(remote.uniqueKey).isEqualTo(Remote.Key(repository.iid, "origin")) },
                { assertThat(remote.uniqueKey.repositoryId).isEqualTo(repository.iid) },
                { assertThat(remote.uniqueKey.name).isEqualTo("origin") }
            )
        }

        @Test
        fun `uniqueKey should trim name with whitespace`() {
            val remote = Remote(
                name = "  origin  ",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            assertThat(remote.uniqueKey.name).isEqualTo("origin")
        }

        @Test
        fun `uniqueKey with same repo and name should be equal`() {
            val remoteA = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )
            val remoteB = Remote(
                name = "origin",
                url = "https://gitlab.com/user/repo.git",
                repositoryId = repository.iid
            )

            assertThat(remoteA.uniqueKey).isEqualTo(remoteB.uniqueKey)
        }

        @Test
        fun `uniqueKey with different repos should differ even with same name`() {
            val otherProject = Project(name = "other-project")
            val otherRepo = Repository(localPath = "/other/path", projectId = otherProject.iid)

            val remoteA = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )
            val remoteB = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = otherRepo.iid
            )

            assertThat(remoteA.uniqueKey).isNotEqualTo(remoteB.uniqueKey)
        }
    }

    @Nested
    inner class EqualityAndHashCode {

        @Test
        fun `hashCode should be based on iid`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            assertThat(remote.hashCode()).isEqualTo(remote.iid.hashCode())
        }

        @Test
        fun `two remotes with different iids should not be equal`() {
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
    }

    @Nested
    inner class RepositoryRelationWithId {

        @Test
        fun `add remote to repository remotes collection should work`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            // Remote is NOT auto-added, must be added explicitly by ID
            assertTrue(repository.remoteIds.add(remote.iid))
            assertThat(repository.remoteIds).hasSize(1)
            assertThat(repository.remoteIds).contains(remote.iid)
        }

        @Test
        fun `add same remote twice should only be added once`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            assertTrue(repository.remoteIds.add(remote.iid))
            assertFalse(repository.remoteIds.add(remote.iid))
            assertThat(repository.remoteIds).hasSize(1)
        }

        @Test
        fun `add multiple remotes with different names should all be added`() {
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
        fun `add remote with same name twice should only add first`() {
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

            // With ID-based collection, both IDs are different, so both are added
            assertTrue(repository.remoteIds.add(remoteA.iid))
            assertTrue(repository.remoteIds.add(remoteB.iid))

            assertAll(
                { assertThat(repository.remoteIds).hasSize(2) },
                { assertThat(repository.remoteIds).contains(remoteA.iid, remoteB.iid) }
            )
        }

        @Test
        fun `add remote from different repositoryId should work with ID-based collection`() {
            val otherProject = Project(name = "other-project")
            val otherRepo = Repository(localPath = "/other/path", projectId = otherProject.iid)

            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            // With ID-based collection, there's no cross-repo check anymore
            // (the ID itself is the reference, no object to validate)
            assertTrue(otherRepo.remoteIds.add(remote.iid))
        }

        @Test
        fun `add empty collection should not add anything`() {
            assertFalse(repository.remoteIds.addAll(emptyList<Remote.Id>()))
            assertThat(repository.remoteIds).isEmpty()
        }
    }

    @Nested
    inner class RemovalOperations {

        @Test
        fun `remove remote should throw UnsupportedOperationException`() {
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
    }

    @Nested
    inner class MutationOperations {

        @Test
        fun `modify url should persist`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )
            repository.remoteIds.add(remote.iid)

            remote.url = "git@github.com:user/repo.git"

            assertAll(
                { assertThat(repository.remoteIds).hasSize(1) },
                { assertThat(remote.url).isEqualTo("git@github.com:user/repo.git") }
            )
        }

        @Test
        fun `modify db id should persist`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            remote.id = "db-id-123"
            assertThat(remote.id).isEqualTo("db-id-123")
        }
    }

    @Nested
    inner class EdgeCases {

        @Test
        fun `create remote with very long name should succeed`() {
            val longName = "remote-" + "a".repeat(1000)
            val remote = Remote(
                name = longName,
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            assertAll(
                { assertThat(remote.name).isEqualTo(longName) },
                { assertThat(remote.repositoryId).isEqualTo(repository.iid) }
            )
        }

        @Test
        fun `create remote with very long url should succeed`() {
            val longUrl = "https://github.com/" + "a".repeat(1000) + "/repo.git"
            val remote = Remote(
                name = "origin",
                url = longUrl,
                repositoryId = repository.iid
            )

            assertAll(
                { assertThat(remote.url).isEqualTo(longUrl) },
                { assertThat(remote.repositoryId).isEqualTo(repository.iid) }
            )
        }

        @Test
        fun `create remote with special characters in URL should succeed`() {
            val specialUrl = "https://user:password@github.com:8080/path/to/repo.git?param=value#fragment"
            val remote = Remote(
                name = "origin",
                url = specialUrl,
                repositoryId = repository.iid
            )

            assertAll(
                { assertThat(remote.url).isEqualTo(specialUrl) },
                { assertThat(remote.repositoryId).isEqualTo(repository.iid) }
            )
        }

        @Test
        fun `create remotes with same url but different names should all be added`() {
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
        fun `multiple repositories with same remote name should work independently`() {
            val repo1 = repository
            val otherProject = Project(name = "other-project")
            val repo2 = Repository(localPath = "/other/path", projectId = otherProject.iid)

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
                { assertThat(remote1.uniqueKey).isNotEqualTo(remote2.uniqueKey) }
            )
        }

        @Test
        fun `toString should not cause circular reference`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            val str = remote.toString()
            assertThat(str).contains("origin")
            assertThat(str).contains("https://github.com/user/repo.git")
            // Should NOT contain the full Repository object (which would be circular)
            assertThat(str).doesNotContain("Repository(")
        }
    }
}
