package com.inso_world.binocular.model

import com.inso_world.binocular.domain.data.MockTestDataProvider
import com.inso_world.binocular.model.utils.ReflectionUtils.Companion.setField
import com.inso_world.binocular.model.vcs.ReferenceCategory
import com.inso_world.binocular.model.vcs.Remote
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
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

@OptIn(ExperimentalUuidApi::class)
class RepositoryModelTest {

    private lateinit var mockTestDataProvider: MockTestDataProvider

    private lateinit var repository: Repository

    @BeforeEach
    fun setup() {
        val project = Project(name = "proj-repository-model-test")
        repository = Repository(
            localPath = "repo-repository-model-test",
            projectId = project.iid,
        )
        mockTestDataProvider = MockTestDataProvider(repository)
    }

    @Test
    fun `create empty repository, checks that iid is created automatically`() {
        val project = Project(name = "test-project")
        val repo = Repository(
            localPath = "test",
            projectId = project.iid,
        )

        assertThat(repo.iid).isNotNull()
        assertThat(repo.projectId).isEqualTo(project.iid)
    }

    @Test
    fun `create repository, validate uniqueKey`() {
        val project = Project(name = "test-project")
        val repo = Repository(
            localPath = "test",
            projectId = project.iid,
        )

        @OptIn(ExperimentalUuidApi::class)
        assertAll(
            { assertThat(repo.uniqueKey).isEqualTo(Repository.Key(project.iid, "test")) },
            { assertThat(repo.uniqueKey.projectId).isEqualTo(project.iid) },
            { assertThat(repo.uniqueKey.projectId.value).isSameAs(project.iid.value) },
            { assertThat(repo.uniqueKey.localPath).isSameAs(repo.localPath) },
        )
    }

    @Test
    fun `create repository, validate hashCode is same based on iid`() {
        val repo = Repository(
            localPath = "test",
            projectId = Project(name = "test-project").iid,
        )

        assertThat(repo.hashCode()).isEqualTo(repo.iid.hashCode())
    }

    @Test
    fun `create repository, copy, check that equals uses iid only`() {
        val project1 = Project(name = "test-project")
        val repoA = Repository(
            localPath = "test a",
            projectId = project1.iid,
        )
        val project2 = Project(name = "test-project-2")
        val repoB = repoA.copy(projectId = project2.iid)

        assertThat(repoA).isNotSameAs(repoB)
        assertThat(repoA).isNotEqualTo(repoB)
        assertThat(repoA.iid).isNotEqualTo(repoB.iid)
    }

    @Test
    fun `create repository, edit iid, check that both are equal`() {
        val project1 = Project(name = "test-project")
        val repoA = Repository(
            localPath = "test a",
            projectId = project1.iid,
        )
        val originIid = repoA.iid
        val originUniqueKey = repoA.uniqueKey
        val project2 = Project(name = "test-project-2")
        val repoB = repoA.copy(projectId = project2.iid)

        setField(
            repoB.javaClass.superclass.getDeclaredField("iid"),
            repoB,
            originIid
        )

        assertThat(repoA).isNotSameAs(repoB)
        assertThat(repoA.iid).isEqualTo(originIid)
        assertThat(repoA.uniqueKey).isEqualTo(originUniqueKey)
        assertThat(repoA.iid).isEqualTo(repoB.iid)
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.domain.data.DummyTestData#provideBlankStrings")
    fun `create repository with blank paths, should fail`(
        path: String,
    ) {
        assertThrows<IllegalArgumentException> {
            Repository(
                localPath = path,
                projectId = Project(name = "test-project").iid,
            )
        }
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.domain.data.DummyTestData#provideAllowedStrings")
    fun `create repository with allowed paths, should not fail`(
        path: String,
    ) {
        assertDoesNotThrow {
            Repository(
                localPath = path,
                projectId = Project(name = "test-project").iid,
            )
        }
    }

    @Nested
    inner class CommitIdsRelation {
        @BeforeEach
        fun setup() {
            this@RepositoryModelTest.setup()
        }

        @Test
        fun `add commit id without parent, expect to be added`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))

            assertTrue(repository.commitIds.add(commit.iid))
            assertThat(repository.commitIds).hasSize(1)
        }

        @Test
        fun `add same commit id twice, expect to be added once`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))

            assertAll(
                { assertTrue(repository.commitIds.add(commit.iid)) },
                { assertFalse(repository.commitIds.add(commit.iid)) }
            )
            assertThat(repository.commitIds).hasSize(1)
        }

        @Test
        fun `add two commits without parent via add(), expect both to be added`() {
            val commitA = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val commitB = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            assertTrue(repository.commitIds.add(commitA.iid))
            assertTrue(repository.commitIds.add(commitB.iid))
            assertThat(repository.commitIds).hasSize(2)
        }

        @Test
        fun `add two commits without parent via addAll(), expect both to be added`() {
            val commitA = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val commitB = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            assertTrue(repository.commitIds.addAll(listOf(commitA.iid, commitB.iid)))
            assertThat(repository.commitIds).hasSize(2)
        }

        @Test
        fun `add same commit id twice via addAll(), expect to be added once`() {
            val commitA = mockTestDataProvider.commitBySha.getValue("a".repeat(40))

            assertTrue(repository.commitIds.addAll(listOf(commitA.iid)))
            assertFalse(repository.commitIds.addAll(listOf(commitA.iid)))
            assertThat(repository.commitIds).hasSize(1)
        }
    }

    @Nested
    inner class BranchIdsRelation {
        @BeforeEach
        fun setup() {
            this@RepositoryModelTest.setup()
        }

        @Test
        fun `add branch id to repository once, should be added once`() {
            val branch = mockTestDataProvider.branchByName.getValue("origin/feature/test")

            assertTrue(repository.branchIds.add(Branch.Id(branch.iid.value)))
            assertThat(repository.branchIds).hasSize(1)
            assertThat(branch.repositoryId).isEqualTo(repository.iid)
        }

        @Test
        fun `add same branch id to repository twice, should only be added once`() {
            val branch = mockTestDataProvider.branchByName.getValue("origin/feature/test")

            assertAll(
                { assertTrue(repository.branchIds.add(Branch.Id(branch.iid.value))) },
                { assertFalse(repository.branchIds.add(Branch.Id(branch.iid.value))) }
            )
            assertThat(repository.branchIds).hasSize(1)
        }

        @Test
        fun `add same branch id to repository twice via addAll, should only be added once`() {
            val branch = mockTestDataProvider.branchByName.getValue("origin/feature/test")

            assertAll(
                { assertTrue(repository.branchIds.addAll(listOf(Branch.Id(branch.iid.value)))) },
                { assertFalse(repository.branchIds.addAll(listOf(Branch.Id(branch.iid.value)))) }
            )
            assertThat(repository.branchIds).hasSize(1)
        }

        @Test
        fun `add multiple branch ids at once, expect all to be added`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branchA = branch(name = "feature/branch-a", head = commit)
            val branchB = branch(name = "feature/branch-b", head = commit)

            assertTrue(repository.branchIds.addAll(listOf(Branch.Id(branchA.iid.value), Branch.Id(branchB.iid.value))))
            assertThat(repository.branchIds).hasSize(2)
            assertAll(
                { assertThat(branchA.repositoryId).isEqualTo(repository.iid) },
                { assertThat(branchB.repositoryId).isEqualTo(repository.iid) }
            )
        }

        @Test
        fun `add empty collection of branch ids, expect no branches added`() {
            val emptyList = emptyList<Branch.Id>()

            assertFalse(repository.branchIds.addAll(emptyList))
            assertThat(repository.branchIds).hasSize(0)
        }

        @Test
        fun `add branch with empty name should throw IllegalArgumentException`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            assertThrows<IllegalArgumentException> {
                branch(name = "", fullName = "", head = commit)
            }
        }
    }

    @Nested
    inner class UserRelation {
        @BeforeEach
        fun setup() {
            this@RepositoryModelTest.setup()
        }

        @Test
        fun `add user to repository once, should be added once`() {
            val user = mockTestDataProvider.userByEmail.getValue("a@test.com")

            // User is auto-added to repository.user on construction
            // So the repository already has users from MockTestDataProvider
            assertThat(repository.user).contains(user)
            assertThat(repository).isSameAs(user.repository)
        }

        @Test
        fun `add same user to repository twice, should only be added once`() {
            val user = mockTestDataProvider.userByEmail.getValue("a@test.com")

            // User is already in repository.user from construction (User.init auto-registers)
            // So adding again should return false
            val beforeAdd = repository.user.size
            val addedAgain = repository.user.add(user)
            assertThat(repository.user).hasSize(beforeAdd) // size unchanged
            // Note: NonRemovingMutableSet deduplicates by uniqueKey, so same user won't be added twice
        }

        @Test
        fun `add multiple users at once, expect all to be added`() {
            val userA = mockTestDataProvider.userByEmail.getValue("a@test.com")
            val userB = mockTestDataProvider.userByEmail.getValue("b@test.com")

            // Both users are already in repository.user from construction
            assertThat(repository.user).contains(userA, userB)
        }

        @Test
        fun `add same user twice via addAll, expect only one to be added`() {
            val userA = mockTestDataProvider.userByEmail.getValue("a@test.com")

            // User is already in repository.user from construction
            val beforeAdd = repository.user.size
            repository.user.addAll(listOf(userA))
            assertThat(repository.user).hasSize(beforeAdd) // size unchanged
        }
    }

    @Nested
    inner class RemotesRelation {
        @BeforeEach
        fun setup() {
            this@RepositoryModelTest.setup()
        }

        @Test
        fun `add remote to repository once, should be added once`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            assertTrue(repository.remoteIds.add(remote.iid))
            assertThat(repository.remoteIds).hasSize(1)
            assertThat(remote.repositoryId).isEqualTo(repository.iid)
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
                { assertFalse(repository.remoteIds.add(remote.iid)) }
            )
            assertThat(repository.remoteIds).hasSize(1)
        }

        @Test
        fun `add same remote to repository twice via addAll, should only be added once`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            assertAll(
                { assertTrue(repository.remoteIds.addAll(listOf(remote.iid))) },
                { assertFalse(repository.remoteIds.addAll(listOf(remote.iid))) }
            )
            assertThat(repository.remoteIds).hasSize(1)
        }

        @Test
        fun `add multiple remotes at once, expect all to be added`() {
            val remoteA = Remote(name = "origin", url = "https://github.com/user/repo.git", repositoryId = repository.iid)
            val remoteB = Remote(name = "upstream", url = "https://github.com/upstream/repo.git", repositoryId = repository.iid)

            assertTrue(repository.remoteIds.addAll(listOf(remoteA.iid, remoteB.iid)))
            assertThat(repository.remoteIds).hasSize(2)
            assertAll(
                { assertThat(remoteA.repositoryId).isEqualTo(repository.iid) },
                { assertThat(remoteB.repositoryId).isEqualTo(repository.iid) }
            )
        }

        @Test
        fun `add remote with different URL, expect to be added`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repositoryId = repository.iid
            )

            assertTrue(repository.remoteIds.add(remote.iid))
            assertThat(repository.remoteIds).hasSize(1)
            assertThat(remote.repositoryId).isEqualTo(repository.iid)
        }

        @Test
        fun `add empty collection of remotes, expect no remotes added`() {
            val emptyList = emptyList<Remote.Id>()

            assertFalse(repository.remoteIds.addAll(emptyList))
            assertThat(repository.remoteIds).hasSize(0)
        }

        @Test
        fun `add null remote should throw exception`() {
            assertThrows(NullPointerException::class.java) {
                repository.remoteIds.add(null as Remote.Id)
            }
        }

        @Test
        fun `add remote with empty name should throw IllegalArgumentException`() {
            assertThrows<IllegalArgumentException> {
                Remote(name = "", url = "https://github.com/user/repo.git", repositoryId = repository.iid)
            }
        }

        @Test
        fun `add remote with blank name should throw IllegalArgumentException`() {
            assertThrows<IllegalArgumentException> {
                Remote(name = "   ", url = "https://github.com/user/repo.git", repositoryId = repository.iid)
            }
        }

        @Test
        fun `add remote with empty url should throw IllegalArgumentException`() {
            assertThrows<IllegalArgumentException> {
                Remote(name = "origin", url = "", repositoryId = repository.iid)
            }
        }

        @Test
        fun `add remote with blank url should throw IllegalArgumentException`() {
            assertThrows<IllegalArgumentException> {
                Remote(name = "origin", url = "   ", repositoryId = repository.iid)
            }
        }

        @Test
        fun `add remote with very long name should be added`() {
            val longName = "remote-" + "a".repeat(1000)
            val remote = Remote(name = longName, url = "https://github.com/user/repo.git", repositoryId = repository.iid)

            assertTrue(repository.remoteIds.add(remote.iid))
            assertThat(repository.remoteIds).hasSize(1)
            assertThat(remote.repositoryId).isEqualTo(repository.iid)
        }

        @Test
        fun `add remote with different protocols, expect all to be added`() {
            val httpsRemote = Remote(name = "https", url = "https://github.com/user/repo.git", repositoryId = repository.iid)
            val sshRemote = Remote(name = "ssh", url = "ssh://git@github.com/user/repo.git", repositoryId = repository.iid)
            val gitRemote = Remote(name = "git", url = "git://github.com/user/repo.git", repositoryId = repository.iid)
            val fileRemote = Remote(name = "file", url = "file:///path/to/repo.git", repositoryId = repository.iid)

            assertTrue(repository.remoteIds.addAll(listOf(httpsRemote.iid, sshRemote.iid, gitRemote.iid, fileRemote.iid)))
            assertThat(repository.remoteIds).hasSize(4)
        }

        @Test
        fun `add remote that already exists in different repository should work with ID-based collection`() {
            val otherRepo = mockTestDataProvider.repositoriesByPath.getValue("repo-pg-1")
            val remote = Remote(name = "origin", url = "https://github.com/shared/repo.git", repositoryId = repository.iid)

            assertTrue(repository.remoteIds.add(remote.iid))
            assertThat(repository.remoteIds).hasSize(1)

            // With ID-based collection, adding same ID to different repo works
            assertTrue(otherRepo.remoteIds.add(remote.iid))
            assertThat(otherRepo.remoteIds).hasSize(1)

            assertThat(remote.repositoryId).isEqualTo(repository.iid)
        }

        @Test
        fun `remove remote from repository should throw UnsupportedOperationException`() {
            val remote = Remote(name = "origin", url = "https://github.com/user/repo.git", repositoryId = repository.iid)
            repository.remoteIds.add(remote.iid)
            assertThat(repository.remoteIds).hasSize(1)

            assertThrows<UnsupportedOperationException> {
                repository.remoteIds.remove(remote.iid)
            }
            assertThat(repository.remoteIds).hasSize(1)
        }

        @Test
        fun `clear all remotes should throw UnsupportedOperationException`() {
            val remoteA = Remote(name = "origin", url = "https://github.com/user/repo.git", repositoryId = repository.iid)
            val remoteB = Remote(name = "upstream", url = "https://github.com/upstream/repo.git", repositoryId = repository.iid)
            repository.remoteIds.addAll(listOf(remoteA.iid, remoteB.iid))
            assertThat(repository.remoteIds).hasSize(2)

            assertThrows<UnsupportedOperationException> {
                repository.remoteIds.clear()
            }
            assertThat(repository.remoteIds).hasSize(2)
        }

        @Test
        fun `remove remote by predicate should throw UnsupportedOperationException`() {
            val remoteA = Remote(name = "origin", url = "https://github.com/user/repo.git", repositoryId = repository.iid)
            val remoteB = Remote(name = "upstream", url = "https://github.com/upstream/repo.git", repositoryId = repository.iid)
            repository.remoteIds.addAll(listOf(remoteA.iid, remoteB.iid))
            assertThat(repository.remoteIds).hasSize(2)

            assertThrows<UnsupportedOperationException> {
                repository.remoteIds.removeIf { true }
            }
            assertThat(repository.remoteIds).hasSize(2)
        }

        @Test
        fun `retain only specific remotes should throw UnsupportedOperationException`() {
            val remoteA = Remote(name = "origin", url = "https://github.com/user/repo.git", repositoryId = repository.iid)
            val remoteB = Remote(name = "upstream", url = "https://github.com/upstream/repo.git", repositoryId = repository.iid)
            val remoteC = Remote(name = "fork", url = "https://github.com/fork/repo.git", repositoryId = repository.iid)
            repository.remoteIds.addAll(listOf(remoteA.iid, remoteB.iid, remoteC.iid))
            assertThat(repository.remoteIds).hasSize(3)

            assertThrows<UnsupportedOperationException> {
                repository.remoteIds.retainAll(setOf(remoteA.iid, remoteC.iid))
            }
            assertThat(repository.remoteIds).hasSize(3)
        }

        @Test
        fun `add remote then modify its url, expect changes to persist`() {
            val remote = Remote(name = "origin", url = "https://github.com/user/repo.git", repositoryId = repository.iid)
            repository.remoteIds.add(remote.iid)
            assertThat(repository.remoteIds).hasSize(1)

            remote.url = "https://gitlab.com/user/repo.git"

            assertThat(repository.remoteIds).hasSize(1)
            assertThat(remote.url).isEqualTo("https://gitlab.com/user/repo.git")
        }

        @Test
        fun `add remote then try to remove and add again, expect removal to fail`() {
            val remote = Remote(name = "origin", url = "https://github.com/user/repo.git", repositoryId = repository.iid)

            assertTrue(repository.remoteIds.add(remote.iid))
            assertThat(repository.remoteIds).hasSize(1)

            assertThrows<UnsupportedOperationException> {
                repository.remoteIds.remove(remote.iid)
            }
            assertThat(repository.remoteIds).hasSize(1)

            assertFalse(repository.remoteIds.add(remote.iid))
            assertThat(repository.remoteIds).hasSize(1)
            assertThat(remote.repositoryId).isEqualTo(repository.iid)
        }

        @Test
        fun `add multiple standard Git remotes, expect all to be added`() {
            val origin = Remote(name = "origin", url = "https://github.com/user/repo.git", repositoryId = repository.iid)
            val upstream = Remote(name = "upstream", url = "https://github.com/upstream/repo.git", repositoryId = repository.iid)
            val fork = Remote(name = "fork", url = "https://github.com/fork/repo.git", repositoryId = repository.iid)

            assertTrue(repository.remoteIds.addAll(listOf(origin.iid, upstream.iid, fork.iid)))
            assertThat(repository.remoteIds).hasSize(3)
            assertThat(repository.remoteIds).containsExactlyInAnyOrder(origin.iid, upstream.iid, fork.iid)
        }

        @Test
        fun `contains check for existing remote should return true`() {
            val remote = Remote(name = "origin", url = "https://github.com/user/repo.git", repositoryId = repository.iid)
            repository.remoteIds.add(remote.iid)

            assertThat(repository.remoteIds.contains(remote.iid)).isTrue()
        }

        @Test
        fun `contains check for non-existing remote should return false`() {
            val otherRepo = mockTestDataProvider.repositoriesByPath.getValue("repo-pg-1")
            val remoteInOtherRepo = Remote(name = "origin", url = "https://github.com/user/repo.git", repositoryId = otherRepo.iid)

            assertThat(repository.remoteIds.contains(remoteInOtherRepo.iid)).isFalse()
        }

        @Test
        fun `iterate over remotes collection, expect all remotes returned`() {
            val origin = Remote(name = "origin", url = "https://github.com/user/repo.git", repositoryId = repository.iid)
            val upstream = Remote(name = "upstream", url = "https://github.com/upstream/repo.git", repositoryId = repository.iid)
            val fork = Remote(name = "fork", url = "https://github.com/fork/repo.git", repositoryId = repository.iid)

            repository.remoteIds.addAll(listOf(origin.iid, upstream.iid, fork.iid))

            val remoteIds = repository.remoteIds.map { it.toString() }.toSet()

            assertThat(remoteIds).hasSize(3)
        }
    }

    private fun branch(
        name: String,
        head: Commit,
        fullName: String = name,
        category: ReferenceCategory = ReferenceCategory.LOCAL_BRANCH
    ): Branch =
        Branch(
            name = name,
            fullName = fullName,
            category = category,
            repositoryId = repository.iid,
            headCommitId = head.iid
        )
}
