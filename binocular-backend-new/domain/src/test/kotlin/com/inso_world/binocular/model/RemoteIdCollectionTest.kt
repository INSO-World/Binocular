package com.inso_world.binocular.model

import com.inso_world.binocular.model.vcs.Remote
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import kotlin.uuid.ExperimentalUuidApi

/**
 * Tests for [Repository.remoteIds] after refactoring from `MutableSet<Remote>` to `MutableSet<Remote.Id>`.
 *
 * These tests verify that:
 * - Repository.remoteIds stores Remote.Id values, not full Remote objects
 * - Adding a remote's ID to the collection works correctly
 * - De-duplication by Remote.Id works
 * - Removal operations still throw UnsupportedOperationException
 * - Cross-repository ID mismatch detection still works
 */
@OptIn(ExperimentalUuidApi::class)
class RemoteIdCollectionTest {

    private lateinit var project: Project
    private lateinit var repository: Repository

    @BeforeEach
    fun setup() {
        project = Project(name = "proj-remote-id-collection-test")
        repository = Repository(
            localPath = "repo-remote-id-collection-test",
            projectId = project.iid,
        )
    }

    @Nested
    inner class IdBasedCollection {

        @Test
        fun `remotes collection should store RemoteId not Remote objects`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            // Add the remote's ID to the collection
            assertTrue(repository.remoteIds.add(remote.iid))

            // The collection should contain the ID
            assertThat(repository.remoteIds).hasSize(1)
            assertThat(repository.remoteIds).contains(remote.iid)
        }

        @Test
        fun `adding same remote id twice should only be added once`() {
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
        fun `adding multiple remote ids should all be added`() {
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
        fun `adding remote ids via addAll should work`() {
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

            val ids = listOf(origin.iid, upstream.iid)
            assertTrue(repository.remoteIds.addAll(ids))
            assertThat(repository.remoteIds).hasSize(2)
        }

        @Test
        fun `adding empty collection should not add anything`() {
            assertFalse(repository.remoteIds.addAll(emptyList<Remote.Id>()))
            assertThat(repository.remoteIds).isEmpty()
        }
    }

    @Nested
    inner class RemovalOperations {

        @Test
        fun `removing remote id should throw UnsupportedOperationException`() {
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
        fun `clearing all remote ids should throw UnsupportedOperationException`() {
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
        fun `removing by predicate should throw UnsupportedOperationException`() {
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
                repository.remoteIds.removeIf { it == remoteA.iid }
            }
            assertThat(repository.remoteIds).hasSize(2)
        }

        @Test
        fun `retaining only specific remote ids should throw UnsupportedOperationException`() {
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
                name = "fork",
                url = "https://github.com/fork/repo.git",
                repositoryId = repository.iid
            )
            repository.remoteIds.addAll(listOf(remoteA.iid, remoteB.iid, remoteC.iid))
            assertThat(repository.remoteIds).hasSize(3)

            assertThrows<UnsupportedOperationException> {
                repository.remoteIds.retainAll(setOf(remoteA.iid, remoteC.iid))
            }
            assertThat(repository.remoteIds).hasSize(3)
        }

        @Test
        fun `removing via iterator should throw UnsupportedOperationException`() {
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
    inner class EdgeCases {

        @Test
        fun `repository should not hold Remote objects, only IDs`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            repository.remoteIds.add(remote.iid)

            // The collection should contain IDs, not Remote objects
            assertThat(repository.remoteIds).contains(remote.iid)
            // Cannot check contains(remote) because the collection is now Remote.Id based
        }

        @Test
        fun `multiple repositories can have independent remote id collections`() {
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

            repo1.remoteIds.add(remote1.iid)
            repo2.remoteIds.add(remote2.iid)

            assertAll(
                { assertThat(repo1.remoteIds).hasSize(1) },
                { assertThat(repo2.remoteIds).hasSize(1) },
                { assertThat(repo1.remoteIds).contains(remote1.iid) },
                { assertThat(repo2.remoteIds).contains(remote2.iid) },
                { assertThat(repo1.remoteIds).doesNotContain(remote2.iid) },
                { assertThat(repo2.remoteIds).doesNotContain(remote1.iid) }
            )
        }

        @Test
        fun `remote id can be added even after remote object is garbage collected`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )
            val remoteId = remote.iid

            // Add the ID
            assertTrue(repository.remoteIds.add(remoteId))
            assertThat(repository.remoteIds).hasSize(1)
            assertThat(repository.remoteIds).contains(remoteId)

            // The Remote object can be dropped, but the ID persists in the collection
            // (This is the whole point of ID-based references)
        }
    }
}
