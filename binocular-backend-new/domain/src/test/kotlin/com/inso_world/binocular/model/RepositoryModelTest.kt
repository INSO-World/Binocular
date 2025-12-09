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
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.ExperimentalUuidApi

class RepositoryModelTest {

    private lateinit var mockTestDataProvider: MockTestDataProvider

    private lateinit var repository: Repository

    @BeforeEach
    fun setup() {
        val project = Project(name = "proj-repository-model-test")
        repository = Repository(
            localPath = "repo-repository-model-test",
            project = project,
        )
        mockTestDataProvider = MockTestDataProvider(repository)

        // clear field via reflection
        for (fieldName in listOf("_legacyUsers", "developers", "branches", "commits", "remotes")) {
            val base = NonRemovingMutableSet::class.java

            val field = repository.javaClass.getDeclaredField(fieldName)
                .apply { this.isAccessible = true }
            val obj = field.get(repository) ?: return

            val backingField = base.getDeclaredField("backing").apply { isAccessible = true }
            val backing = (backingField.get(obj) as ConcurrentHashMap<*, *>)
            backing.clear()
        }
    }

    @Test
    fun `create empty repository, checks that iid is created automatically`() {
        val project = Project(name = "test-project")
        val repo = Repository(
            localPath = "test",
            project = project,
        )

        assertThat(repo.iid).isNotNull()
        // check reference
        assertThat(repo.project).isSameAs(project)
        assertThat(repo.project.repo).isSameAs(repo)
    }

    @Test
    fun `create repository, validate uniqueKey`() {
        val project = Project(name = "test-project")
        val repo = Repository(
            localPath = "test",
            project = project,
        )

        @OptIn(ExperimentalUuidApi::class)
        assertAll(
            { assertThat(repo.uniqueKey).isEqualTo(Repository.Key(project.iid, "test")) },
            { assertThat(repo.uniqueKey.projectId).isEqualTo(project.iid) },
            // compare .value here
            // Because inline classes may be represented both as the underlying value and as a wrapper, referential equality is pointless for them and is therefore prohibited.
            // https://kotlinlang.org/docs/inline-classes.html#representation
            { assertThat(repo.uniqueKey.projectId.value).isSameAs(project.iid.value) },
            { assertThat(repo.uniqueKey.localPath).isSameAs(repo.localPath) },
        )
    }

    @Test
    fun `create repository, validate hashCode is same based on iid`() {
        val repo = Repository(
            localPath = "test",
            project = Project(name = "test-project"),
        )

        assertThat(repo.hashCode()).isEqualTo(repo.iid.hashCode())
    }

    @Test
    fun `create repository, copy, check that equals uses iid only`() {
        val repoA = Repository(
            localPath = "test a",
            project = Project(name = "test-project"),
        )
        val repoB = repoA.copy(project = Project(name = "test-project-2"))

        assertThat(repoA).isNotSameAs(repoB)
        assertThat(repoA).isNotEqualTo(repoB)
        assertThat(repoA.iid).isNotEqualTo(repoB.iid)
    }

    @Test
    fun `create repository, edit iid, check that both are equal`() {
        val repoA = Repository(
            localPath = "test a",
            project = Project(name = "test-project"),
        )
        val originIid = repoA.iid
        val originUniqueKey = repoA.uniqueKey
        val repoB = repoA.copy(project = Project(name = "test-project-2"))

        setField(
            repoB.javaClass.superclass.getDeclaredField("iid"),
            repoB,
            originIid
        )
        // edit project as required for equals
        setField(
            repoB.javaClass.getDeclaredField("project"),
            repoB,
            repoA.project
        )

        assertThat(repoA).isNotSameAs(repoB)
        assertThat(repoA).isEqualTo(repoB)
        assertThat(repoA.iid).isEqualTo(originIid)
        assertThat(repoA.uniqueKey).isEqualTo(originUniqueKey)
        assertThat(repoA.iid).isEqualTo(repoB.iid)
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.data.DummyTestData#provideBlankStrings")
    fun `create repository with blank paths, should fail`(
        path: String,
    ) {
        assertThrows<IllegalArgumentException> {
            Repository(
                localPath = path,
                project = Project(name = "test-project"),
            )
        }
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.data.DummyTestData#provideAllowedStrings")
    fun `create repository with allowed paths, should not fail`(
        path: String,
    ) {
        assertDoesNotThrow {
            Repository(
                localPath = path,
                project = Project(name = "test-project"),
            )
        }
    }


    @Nested
    inner class CommitsRelation {
        @BeforeEach
        fun setup() {
            this@RepositoryModelTest.setup()
        }

        @Test
        fun `add commit without parent, expect to be added`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))

            assertTrue(repository.commits.add(commit))
            assertThat(repository.commits).hasSize(1)
            assertThat(repository.commits.toList()[0].repository).isSameAs(repository)
        }

        @Test
        fun `add same commit without parent twice, expect to be added once`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))

            assertAll(
                { assertTrue(repository.commits.add(commit)) },
                { assertFalse(repository.commits.add(commit)) }
            )
            assertAll(
                { assertThat(repository.commits).hasSize(1) },
                { assertThat(repository.commits.toList()[0].repository).isSameAs(repository) }
            )
        }

        @Test
        fun `add commit without parent twice, expect only once be added`() {
            val commitA = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val commitB = mockTestDataProvider.commitBySha.getValue("a".repeat(40)) // same on purpose

            assertAll(
                { assertTrue(repository.commits.add(commitA)) },
                { assertFalse(repository.commits.add(commitB)) }
            )
            assertThat(repository.commits).hasSize(1)
        }

        @Test
        fun `add two commits without parent via add(), expect both to be added`() {
            val commitA = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val commitB = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            assertTrue(repository.commits.add(commitA))
            assertTrue(repository.commits.add(commitB))
            assertThat(repository.commits).hasSize(2)
        }

        @Test
        fun `add two commits without parent via addAll(), expect both to be added`() {
            val commitA = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val commitB = mockTestDataProvider.commitBySha.getValue("b".repeat(40))

            val list = listOf(commitA, commitB)
            assertThat(list).hasSize(2)

            assertTrue(repository.commits.addAll(list))
            assertThat(repository.commits).hasSize(2)
        }

        @Test
        fun `add same commit twice via addAll(), expect to be added once`() {
            val commitA = mockTestDataProvider.commitBySha.getValue("a".repeat(40))

            assertTrue(repository.commits.addAll(listOf(commitA)))
            assertFalse(repository.commits.addAll(listOf(commitA)))
            assertThat(repository.commits).hasSize(1)
        }

        @Test
        fun `add one commits with parent, expect only child to be added`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40)).apply {
                this.parents.add(mockTestDataProvider.commitBySha.getValue("b".repeat(40)))
            }

            assertTrue(repository.commits.add(commit))
            assertThat(repository.commits).hasSize(1)
        }

        @Test
        fun `add one commits with children, expect only parent to be added`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40)).apply {
                this.children.add(mockTestDataProvider.commitBySha.getValue("b".repeat(40)))
            }

            assertTrue(repository.commits.add(commit))
            assertThat(repository.commits).hasSize(1)
        }

        @Test
        fun `add duplicate commits without parent via addAll(), expect only one to be added`() {
            val commitA = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val commitB = mockTestDataProvider.commitBySha.getValue("b".repeat(40))
            val commitC = mockTestDataProvider.commitBySha.getValue("b".repeat(40)) // same on purpose as commitB

            val list = listOf(commitA, commitB, commitC)
            assertThat(list).hasSize(3)

            assertTrue(repository.commits.addAll(list))
            assertThat(repository.commits).hasSize(2)
        }

        @Test
        fun `add commit which belongs to a different repository`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val probeB = mockTestDataProvider.repositoriesByPath.getValue("repo-pg-1")

            assertThat(commit.repository).isNotSameAs(probeB)

            assertThrows<IllegalArgumentException> { probeB.commits.add(commit) }
        }
    }

    @Nested
    inner class BranchesRelation {
        @BeforeEach
        fun setup() {
            this@RepositoryModelTest.setup()
        }

        @Test
        fun `add branch to repository once, should be added once`() {
            val branch = mockTestDataProvider.branchByName.getValue("origin/feature/test")

            assertTrue(repository.branches.add(branch))
            assertThat(repository.branches).hasSize(1)
            // check if reference is set correctly
            assertThat(repository).isSameAs(branch.repository)
        }

        @Test
        fun `add same branch to repository twice, should only be added once`() {
            val branch = mockTestDataProvider.branchByName.getValue("origin/feature/test")

            assertAll(
                { assertTrue(repository.branches.add(branch)) },
                { assertFalse(repository.branches.add(branch)) }
            )
            assertThat(repository.branches).hasSize(1)
        }

        @Test
        fun `add same branch to repository twice via addAll, should only be added once`() {
            val branch = mockTestDataProvider.branchByName.getValue("origin/feature/test")

            assertAll(
                { assertTrue(repository.branches.addAll(listOf(branch))) },
                { assertFalse(repository.branches.addAll(listOf(branch))) }
            )
            assertThat(repository.branches).hasSize(1)
        }

        @Test
        fun `add multiple branches at once, expect all to be added`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branchA = branch(name = "feature/branch-a", head = commit)
            val branchB = branch(name = "feature/branch-b", head = commit)

            assertFalse(repository.branches.addAll(listOf(branchA, branchB))) // already added via constructor
            assertThat(repository.branches).hasSize(2)
            // check if references are set correctly
            assertAll(
                { assertThat(repository).isSameAs(branchA.repository) },
                { assertThat(repository).isSameAs(branchB.repository) }
            )
        }

        @Test
        fun `add multiple branches at once with duplicates, expect unique to be added`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branchA = branch(name = "feature/branch-a", head = commit)
            val branchB = branch(name = "feature/branch-b", head = commit)
            val branchC =
                branch(name = "feature/branch-a", head = commit) // same name as branchA

            val list = listOf(branchA, branchB, branchC)
            assertThat(list).hasSize(3)

            assertThat(repository.branches).hasSize(2)
        }

        @Test
        fun `add branch with different properties, expect to be added`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branch = branch(
                name = "feature/test-branch",
                head = commit
            ).apply {
                active = true
                tracksFileRenames = true
            }

            // assertFalse since branch is already added via constructor
            assertFalse(repository.branches.add(branch))
            assertThat(repository.branches).hasSize(1)
            assertThat(repository).isSameAs(branch.repository)
        }

        @Test
        fun `add branch with commits, expect only branch to be added`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branch = branch(name = "feature/with-commits", head = commit)

            assertFalse(repository.branches.add(branch))
            assertThat(repository.branches).hasSize(1)
            assertThat(repository).isSameAs(branch.repository)
        }

        @Test
        fun `add empty collection of branches, expect no branches added`() {
            val emptyList = emptyList<Branch>()

            assertFalse(repository.branches.addAll(emptyList))
            assertThat(repository.branches).hasSize(0)
        }

        @Test
        fun `add null branch should throw exception`() {
            assertThrows(NullPointerException::class.java) {
                repository.branches.add(null as Branch)
            }
        }

        @Test
        fun `add branch with empty name should throw IllegalArgumentException`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            assertThrows<IllegalArgumentException> {
                branch(name = "", fullName = "", head = commit)
            }
        }

        @Test
        fun `add branch with very long name should be added`() {
            val longName = "feature/" + "a".repeat(1000)
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branch = branch(name = longName, head = commit)

            assertFalse(repository.branches.add(branch))
            assertThat(repository.branches).hasSize(1)
            assertThat(repository).isSameAs(branch.repository)
        }

        @Test
        fun `add branch with special characters in name should be added`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branch = branch(name = "feature/test-branch@#$%^&*()", head = commit)

            assertFalse(repository.branches.add(branch))
            assertThat(repository.branches).hasSize(1)
            assertThat(repository).isSameAs(branch.repository)
        }

        @Test
        fun `add branch that already exists in different repository should be added`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val otherRepo = mockTestDataProvider.repositoriesByPath.getValue("repo-pg-1")
            val branch = branch(name = "feature/shared-branch", head = commit)

            // Add to first repository
            assertFalse(repository.branches.add(branch))
            assertThat(repository.branches).hasSize(1)

            // Add same branch to different repository
            assertThrows<IllegalArgumentException> {
                otherRepo.branches.add(branch)
            }
            assertThat(otherRepo.branches).hasSize(0)

            // Branch should be unchanged
            assertThat(repository).isSameAs(branch.repository)
        }

        @Test
        fun `remove branch from repository should throw UnsupportedOperationException`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branch = branch(name = "feature/to-remove", head = commit)
            assertThat(repository.branches).hasSize(1)

            assertThrows<UnsupportedOperationException> {
                repository.branches.remove(branch)
            }
            assertThat(repository.branches).hasSize(1) // Should still be there
        }

        @Test
        fun `clear all branches should throw UnsupportedOperationException`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branchA = branch(name = "feature/branch-a", head = commit)
            val branchB = branch(name = "feature/branch-b", head = commit)
            assertThat(repository.branches).hasSize(2)

            assertThrows<UnsupportedOperationException> {
                repository.branches.clear()
            }
            assertThat(repository.branches).hasSize(2) // Should still be there
            assertThat(branchA.repository).isSameAs(repository)
            assertThat(branchB.repository).isSameAs(repository)
        }

        @Test
        fun `remove branch by predicate should throw UnsupportedOperationException`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branchA = branch(name = "feature/branch-a", head = commit)
            val branchB = branch(name = "feature/branch-b", head = commit)
            assertThat(repository.branches).hasSize(2)

            assertThrows<UnsupportedOperationException> {
                repository.branches.removeIf { it.name == "feature/branch-a" }
            }
            assertThat(repository.branches).hasSize(2) // Should still be there
        }

        @Test
        fun `retain only specific branches should throw UnsupportedOperationException`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branchA = branch(name = "feature/branch-a", head = commit)
            val branchB = branch(name = "feature/branch-b", head = commit)
            val branchC = branch(name = "feature/branch-c", head = commit)
            assertThat(repository.branches).hasSize(3)

            assertThrows<UnsupportedOperationException> {
                repository.branches.retainAll(setOf(branchA, branchC))
            }
            assertThat(repository.branches).hasSize(3) // Should still be there
        }

        @Test
        fun `add branch then modify its properties, expect changes to persist`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branch = branch(name = "feature/mutable-branch", head = commit)
            assertThat(repository.branches).hasSize(1)

            // Modify branch properties
            branch.head = mockTestDataProvider.commitBySha.getValue("a".repeat(40))

            // Verify the branch still exists and changes are preserved
            assertThat(repository.branches).hasSize(1)
            assertThat(repository.branches.first().commits).hasSize(1)
        }

        @Test
        fun `add branch with same name but different properties, expect only one added`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branchA = branch(name = "feature/same-name", head = commit).apply {
                active = true
            }
            val branchB = branch(name = "feature/same-name", head = commit).apply {
                active = false
            }

            assertThat(repository.branches).hasSize(1)
            assertThat(repository.branches.first().active).isTrue()
        }

        @Test
        fun `add branch then try to remove and add again, expect removal to fail`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branch = branch(name = "feature/re-add", head = commit)

            // Add branch
            assertFalse(repository.branches.add(branch))
            assertThat(repository.branches).hasSize(1)

            // Try to remove branch - should throw exception
            assertThrows<UnsupportedOperationException> {
                repository.branches.remove(branch)
            }
            assertThat(repository.branches).hasSize(1) // Should still be there

            // Try to add same branch again - should return false (already exists)
            assertFalse(repository.branches.add(branch))
            assertThat(repository.branches).hasSize(1)
            assertThat(repository).isSameAs(branch.repository)
        }

        @Test
        fun `add branch with commits then try to remove, expect removal to fail`() {
            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))
            val branch = branch(name = "feature/with-commits", head = commit)

            repository.branches.add(branch)
            assertThat(repository.branches).hasSize(1)
            assertThat(branch.commits).hasSize(1)

            // Try to remove branch - should throw exception
            assertThrows<UnsupportedOperationException> {
                repository.branches.remove(branch)
            }
            assertThat(repository.branches).hasSize(1) // Should still be there
            // Commits should still exist in the branch
            assertThat(branch.commits).hasSize(1)
        }

        @Test
        fun `add branch to multiple repositories, expect only first repository to work`() {
            val otherRepo = mockTestDataProvider.repositoriesByPath.getValue("repo-pg-1")
            assertThat(otherRepo).isNotSameAs(repository)

            val commit = mockTestDataProvider.commitBySha.getValue("a".repeat(40))

            // Create branch without repository first
            val branch = branch(name = "feature/multi-repo", head = commit)

            // Add to first repository
            assertThat(branch.repository).isSameAs(repository)
            assertThat(repository.branches).hasSize(1)

            // Try to add to second repository - should not work since it's a different repository
            assertThrows<IllegalArgumentException> {
                otherRepo.branches.add(branch)
            }
            assertThat(branch.repository).isSameAs(repository) // Should still reference the original repository
            assertThat(repository.branches).hasSize(1) // Should be removed from first repo
            assertThat(otherRepo.branches).hasSize(0)
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

            assertTrue(repository.user.add(user))
            assertThat(repository.user).hasSize(1)
            // check if reference is set correctly
            assertThat(repository).isSameAs(user.repository)
        }

        @Test
        fun `add same user to repository twice, should only be added once`() {
            val user = mockTestDataProvider.userByEmail.getValue("a@test.com")

            assertAll(
                { assertTrue(repository.user.add(user)) },
                { assertFalse(repository.user.add(user)) }
            )
            assertThat(repository.user).hasSize(1)
        }

        @Test
        fun `add multiple users at once, expect all to be added`() {
            val userA = mockTestDataProvider.userByEmail.getValue("a@test.com")
            val userB = mockTestDataProvider.userByEmail.getValue("b@test.com")

            assertTrue(repository.user.addAll(listOf(userA, userB)))
            assertThat(repository.user).hasSize(2)
        }

        @Test
        fun `add same user twice via addAll, expect only one to be added`() {
            val userA = mockTestDataProvider.userByEmail.getValue("a@test.com")

            assertAll(
                { assertTrue(repository.user.addAll(listOf(userA))) },
                { assertFalse(repository.user.addAll(listOf(userA))) },
            )
            assertThat(repository.user).hasSize(1)
        }

        @Test
        fun `add multiple users at once with duplicates, expect unique to be added`() {
            val userA = mockTestDataProvider.userByEmail.getValue("a@test.com")
            val userB = mockTestDataProvider.userByEmail.getValue("b@test.com")
            val userC = mockTestDataProvider.userByEmail.getValue("a@test.com") // same as userA

            val list = listOf(userA, userB, userC)
            assertThat(list).hasSize(3)

            assertTrue(repository.user.addAll(list))
            assertThat(repository.user).hasSize(2)
        }

        @Test
        fun `add user from another repository, should fail`() {
            val userA = mockTestDataProvider.userByEmail.getValue("a@test.com")
            val probeB = mockTestDataProvider.repositoriesByPath.getValue("repo-pg-1")

            assertThat(userA.repository).isNotSameAs(probeB)

            assertThrows<IllegalArgumentException> { probeB.user.add(userA) }
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
                repository = repository
            )

            assertFalse(repository.remotes.add(remote)) // already added via constructor
            assertThat(repository.remotes).hasSize(1)
            // check if reference is set correctly
            assertThat(repository).isSameAs(remote.repository)
        }

        @Test
        fun `add same remote to repository twice, should only be added once`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )

            assertAll(
                { assertFalse(repository.remotes.add(remote)) }, // already added via constructor
                { assertFalse(repository.remotes.add(remote)) }
            )
            assertThat(repository.remotes).hasSize(1)
        }

        @Test
        fun `add same remote to repository twice via addAll, should only be added once`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )

            assertAll(
                { assertFalse(repository.remotes.addAll(listOf(remote))) }, // already added via constructor
                { assertFalse(repository.remotes.addAll(listOf(remote))) }
            )
            assertThat(repository.remotes).hasSize(1)
        }

        @Test
        fun `add multiple remotes at once, expect all to be added`() {
            val remoteA = Remote(name = "origin", url = "https://github.com/user/repo.git", repository = repository)
            val remoteB = Remote(name = "upstream", url = "https://github.com/upstream/repo.git", repository = repository)

            assertFalse(repository.remotes.addAll(listOf(remoteA, remoteB))) // already added via constructor
            assertThat(repository.remotes).hasSize(2)
            // check if references are set correctly
            assertAll(
                { assertThat(repository).isSameAs(remoteA.repository) },
                { assertThat(repository).isSameAs(remoteB.repository) }
            )
        }

        @Test
        fun `add multiple remotes at once with duplicates, expect unique to be added`() {
            val remoteA = Remote(name = "origin", url = "https://github.com/user/repo.git", repository = repository)
            val remoteB = Remote(name = "upstream", url = "https://github.com/upstream/repo.git", repository = repository)
            val remoteC = Remote(name = "origin", url = "https://different.com/repo.git", repository = repository) // same name as remoteA

            val list = listOf(remoteA, remoteB, remoteC)
            assertThat(list).hasSize(3)

            assertThat(repository.remotes).hasSize(2) // remoteC not added due to same business key as remoteA
        }

        @Test
        fun `add remote with different URL, expect to be added`() {
            val remote = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )

            // assertFalse since remote is already added via constructor
            assertFalse(repository.remotes.add(remote))
            assertThat(repository.remotes).hasSize(1)
            assertThat(repository).isSameAs(remote.repository)
        }

        @Test
        fun `add empty collection of remotes, expect no remotes added`() {
            val emptyList = emptyList<Remote>()

            assertFalse(repository.remotes.addAll(emptyList))
            assertThat(repository.remotes).hasSize(0)
        }

        @Test
        fun `add null remote should throw exception`() {
            assertThrows(NullPointerException::class.java) {
                repository.remotes.add(null as Remote)
            }
        }

        @Test
        fun `add remote with empty name should throw IllegalArgumentException`() {
            assertThrows<IllegalArgumentException> {
                Remote(name = "", url = "https://github.com/user/repo.git", repository = repository)
            }
        }

        @Test
        fun `add remote with blank name should throw IllegalArgumentException`() {
            assertThrows<IllegalArgumentException> {
                Remote(name = "   ", url = "https://github.com/user/repo.git", repository = repository)
            }
        }

        @Test
        fun `add remote with empty url should throw IllegalArgumentException`() {
            assertThrows<IllegalArgumentException> {
                Remote(name = "origin", url = "", repository = repository)
            }
        }

        @Test
        fun `add remote with blank url should throw IllegalArgumentException`() {
            assertThrows<IllegalArgumentException> {
                Remote(name = "origin", url = "   ", repository = repository)
            }
        }

        @Test
        fun `add remote with very long name should be added`() {
            val longName = "remote-" + "a".repeat(1000)
            val remote = Remote(name = longName, url = "https://github.com/user/repo.git", repository = repository)

            assertFalse(repository.remotes.add(remote))
            assertThat(repository.remotes).hasSize(1)
            assertThat(repository).isSameAs(remote.repository)
        }

        @Test
        fun `add remote with very long url should be added`() {
            val longPath = "path/".repeat(100)
            val longUrl = "https://github.com/user/$longPath/repo.git"
            val remote = Remote(name = "origin", url = longUrl, repository = repository)

            assertFalse(repository.remotes.add(remote))
            assertThat(repository.remotes).hasSize(1)
            assertThat(repository).isSameAs(remote.repository)
        }

        @Test
        fun `add remote with special characters in url should be added`() {
            val remote = Remote(
                name = "origin",
                url = "https://user:password@github.com:443/user/repo-name_123.git?query=value#fragment",
                repository = repository
            )

            assertFalse(repository.remotes.add(remote))
            assertThat(repository.remotes).hasSize(1)
            assertThat(repository).isSameAs(remote.repository)
        }

        @Test
        fun `add remote with different protocols, expect all to be added`() {
            val httpsRemote = Remote(name = "https", url = "https://github.com/user/repo.git", repository = repository)
            val sshRemote = Remote(name = "ssh", url = "ssh://git@github.com/user/repo.git", repository = repository)
            val gitRemote = Remote(name = "git", url = "git://github.com/user/repo.git", repository = repository)
            val fileRemote = Remote(name = "file", url = "file:///path/to/repo.git", repository = repository)

            assertThat(repository.remotes).hasSize(4)
            assertAll(
                { assertThat(repository).isSameAs(httpsRemote.repository) },
                { assertThat(repository).isSameAs(sshRemote.repository) },
                { assertThat(repository).isSameAs(gitRemote.repository) },
                { assertThat(repository).isSameAs(fileRemote.repository) }
            )
        }

        @Test
        fun `add remote that already exists in different repository should fail`() {
            val otherRepo = mockTestDataProvider.repositoriesByPath.getValue("repo-pg-1")
            val remote = Remote(name = "origin", url = "https://github.com/shared/repo.git", repository = repository)

            // Add to first repository
            assertFalse(repository.remotes.add(remote))
            assertThat(repository.remotes).hasSize(1)

            // Try to add same remote to different repository
            assertThrows<IllegalArgumentException> {
                otherRepo.remotes.add(remote)
            }
            assertThat(otherRepo.remotes).hasSize(0)

            // Remote should be unchanged
            assertThat(repository).isSameAs(remote.repository)
        }

        @Test
        fun `remove remote from repository should throw UnsupportedOperationException`() {
            val remote = Remote(name = "origin", url = "https://github.com/user/repo.git", repository = repository)
            assertThat(repository.remotes).hasSize(1)

            assertThrows<UnsupportedOperationException> {
                repository.remotes.remove(remote)
            }
            assertThat(repository.remotes).hasSize(1) // Should still be there
        }

        @Test
        fun `clear all remotes should throw UnsupportedOperationException`() {
            val remoteA = Remote(name = "origin", url = "https://github.com/user/repo.git", repository = repository)
            val remoteB = Remote(name = "upstream", url = "https://github.com/upstream/repo.git", repository = repository)
            assertThat(repository.remotes).hasSize(2)

            assertThrows<UnsupportedOperationException> {
                repository.remotes.clear()
            }
            assertThat(repository.remotes).hasSize(2) // Should still be there
            assertThat(remoteA.repository).isSameAs(repository)
            assertThat(remoteB.repository).isSameAs(repository)
        }

        @Test
        fun `remove remote by predicate should throw UnsupportedOperationException`() {
            val remoteA = Remote(name = "origin", url = "https://github.com/user/repo.git", repository = repository)
            val remoteB = Remote(name = "upstream", url = "https://github.com/upstream/repo.git", repository = repository)
            assertThat(repository.remotes).hasSize(2)

            assertThrows<UnsupportedOperationException> {
                repository.remotes.removeIf { it.name == "origin" }
            }
            assertThat(repository.remotes).hasSize(2) // Should still be there
        }

        @Test
        fun `retain only specific remotes should throw UnsupportedOperationException`() {
            val remoteA = Remote(name = "origin", url = "https://github.com/user/repo.git", repository = repository)
            val remoteB = Remote(name = "upstream", url = "https://github.com/upstream/repo.git", repository = repository)
            val remoteC = Remote(name = "fork", url = "https://github.com/fork/repo.git", repository = repository)
            assertThat(repository.remotes).hasSize(3)

            assertThrows<UnsupportedOperationException> {
                repository.remotes.retainAll(setOf(remoteA, remoteC))
            }
            assertThat(repository.remotes).hasSize(3) // Should still be there
        }

        @Test
        fun `add remote then modify its url, expect changes to persist`() {
            val remote = Remote(name = "origin", url = "https://github.com/user/repo.git", repository = repository)
            assertThat(repository.remotes).hasSize(1)

            // Modify remote URL
            remote.url = "https://gitlab.com/user/repo.git"

            // Verify the remote still exists and changes are preserved
            assertThat(repository.remotes).hasSize(1)
            assertThat(repository.remotes.first().url).isEqualTo("https://gitlab.com/user/repo.git")
        }

        @Test
        fun `add remote with same name but different url, expect only one added`() {
            val remoteA = Remote(
                name = "origin",
                url = "https://github.com/user/repo.git",
                repository = repository
            )
            val remoteB = Remote(
                name = "origin",
                url = "https://gitlab.com/user/repo.git",
                repository = repository
            )

            assertThat(repository.remotes).hasSize(1)
            assertThat(repository.remotes.first().url).isEqualTo("https://github.com/user/repo.git")
        }

        @Test
        fun `add remote then try to remove and add again, expect removal to fail`() {
            val remote = Remote(name = "origin", url = "https://github.com/user/repo.git", repository = repository)

            // Add remote
            assertFalse(repository.remotes.add(remote))
            assertThat(repository.remotes).hasSize(1)

            // Try to remove remote - should throw exception
            assertThrows<UnsupportedOperationException> {
                repository.remotes.remove(remote)
            }
            assertThat(repository.remotes).hasSize(1) // Should still be there

            // Try to add same remote again - should return false (already exists)
            assertFalse(repository.remotes.add(remote))
            assertThat(repository.remotes).hasSize(1)
            assertThat(repository).isSameAs(remote.repository)
        }

        @Test
        fun `add remote to multiple repositories, expect only first repository to work`() {
            val otherRepo = mockTestDataProvider.repositoriesByPath.getValue("repo-pg-1")
            assertThat(otherRepo).isNotSameAs(repository)

            // Create remote with repository
            val remote = Remote(name = "origin", url = "https://github.com/multi/repo.git", repository = repository)

            // Add to first repository
            assertThat(remote.repository).isSameAs(repository)
            assertThat(repository.remotes).hasSize(1)

            // Try to add to second repository - should not work since it's a different repository
            assertThrows<IllegalArgumentException> {
                otherRepo.remotes.add(remote)
            }
            assertThat(remote.repository).isSameAs(repository) // Should still reference the original repository
            assertThat(repository.remotes).hasSize(1)
            assertThat(otherRepo.remotes).hasSize(0)
        }

        @Test
        fun `add multiple standard Git remotes, expect all to be added`() {
            val origin = Remote(name = "origin", url = "https://github.com/user/repo.git", repository = repository)
            val upstream = Remote(name = "upstream", url = "https://github.com/upstream/repo.git", repository = repository)
            val fork = Remote(name = "fork", url = "https://github.com/fork/repo.git", repository = repository)

            assertThat(repository.remotes).hasSize(3)
            assertThat(repository.remotes).containsExactlyInAnyOrder(origin, upstream, fork)
        }

        @Test
        fun `add remote with hyphen and underscore in name, should be added`() {
            val remoteA = Remote(name = "origin-https", url = "https://github.com/user/repo.git", repository = repository)
            val remoteB = Remote(name = "origin_ssh", url = "ssh://git@github.com/user/repo.git", repository = repository)

            assertThat(repository.remotes).hasSize(2)
            assertAll(
                { assertThat(repository).isSameAs(remoteA.repository) },
                { assertThat(repository).isSameAs(remoteB.repository) }
            )
        }

        @Test
        fun `contains check for existing remote should return true`() {
            val remote = Remote(name = "origin", url = "https://github.com/user/repo.git", repository = repository)

            assertThat(repository.remotes.contains(remote)).isTrue()
        }

        @Test
        fun `contains check for non-existing remote should return false`() {
            val otherRepo = mockTestDataProvider.repositoriesByPath.getValue("repo-pg-1")
            val remoteInOtherRepo = Remote(name = "origin", url = "https://github.com/user/repo.git", repository = otherRepo)

            assertThat(repository.remotes.contains(remoteInOtherRepo)).isFalse()
        }

        @Test
        fun `iterate over remotes collection, expect all remotes returned`() {
            val origin = Remote(name = "origin", url = "https://github.com/user/repo.git", repository = repository)
            val upstream = Remote(name = "upstream", url = "https://github.com/upstream/repo.git", repository = repository)
            val fork = Remote(name = "fork", url = "https://github.com/fork/repo.git", repository = repository)

            val remoteNames = repository.remotes.map { it.name }.toSet()

            assertThat(remoteNames).containsExactlyInAnyOrder("origin", "upstream", "fork")
        }
    }

    private fun branch(
        name: String,
        head: Commit,
        repository: Repository = this.repository,
        fullName: String = name,
        category: ReferenceCategory = ReferenceCategory.LOCAL_BRANCH
    ): Branch =
        Branch(
            name = name,
            fullName = fullName,
            category = category,
            repository = repository,
            head = head
        )
}
