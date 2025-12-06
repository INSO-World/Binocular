package com.inso_world.binocular.model

import com.inso_world.binocular.data.MockTestDataProvider
import com.inso_world.binocular.model.utils.ReflectionUtils.Companion.setField
import com.inso_world.binocular.model.vcs.ReferenceCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi

class BranchModelTest {
    private lateinit var repository: Repository
    private lateinit var head: Commit

    @BeforeEach
    fun setup() {
        repository = Repository(localPath = "test repo", project = Project(name = "test project"))
        val developer = Developer(name = "Test Developer", email = "dev@test.com", repository = repository)
        val signature = Signature(developer = developer, timestamp = LocalDateTime.now().minusSeconds(1))
        head = Commit(
            sha = "a".repeat(40),
            message = "msg1",
            authorSignature = signature,
            repository = repository,
        )
    }

    @Test
    fun `add branch to other repository too, should fail`() {
        val mockCommit = MockTestDataProvider(this@BranchModelTest.repository).commitBySha.getValue("a".repeat(40))
        val dummyBranch =
            branch(
                name = "branch",
                // other repo on purpose
                repository = this@BranchModelTest.repository,
                head = mockCommit,
            )
        assertThrows<IllegalArgumentException> {
            Repository(
                localPath = "test repo",
                project = Project(name = "test project")
            ).branches.add(dummyBranch)
        }
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.data.DummyTestData#provideAllowedStrings")
    fun `create branch with allowed names, should succeed`(
        name: String,
    ) {
        assertDoesNotThrow {
            branch(
                name = name,
                fullName = name
            )
        }
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.data.DummyTestData#provideBlankStrings")
    fun `create branch with blank name, should fail`(
        name: String,
    ) {
        assertThrows<IllegalArgumentException> {
            branch(
                name = name,
                fullName = name
            )
        }
    }

    @Test
    fun `create branch, check iid is set automatically`() {
        val branch = branch()

        assertThat(branch.iid).isNotNull()
    }

    @Test
    fun `create branch, stores provided metadata`() {
        val branch = branch(
            name = "main",
            fullName = "refs/heads/main",
            category = ReferenceCategory.LOCAL_BRANCH
        )

        assertThat(branch.fullName).isEqualTo("refs/heads/main")
        assertThat(branch.category).isEqualTo(ReferenceCategory.LOCAL_BRANCH)
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.data.DummyTestData#provideBlankStrings")
    fun `create branch with blank fullName should fail`(
        fullName: String,
    ) {
        assertThrows<IllegalArgumentException> {
            branch(name = "branch", fullName = fullName)
        }
    }

    @Test
    fun `create branch, validate uniqueKey`() {
        val branch = branch()

        @OptIn(ExperimentalUuidApi::class)
        assertAll(
            { assertThat(branch.uniqueKey).isEqualTo(Branch.Key(repository.iid, "branch")) },
            { assertThat(branch.uniqueKey.repositoryId).isEqualTo(repository.iid) },
            // compare .value here
            // Because inline classes may be represented both as the underlying value and as a wrapper, referential equality is pointless for them and is therefore prohibited.
            // https://kotlinlang.org/docs/inline-classes.html#representation
            { assertThat(branch.uniqueKey.repositoryId.value).isSameAs(repository.iid.value) },
            { assertThat(branch.uniqueKey.name).isSameAs(branch.name) },
        )
    }

    @Test
    fun `create branch, check that hashCode is based on iid`() {
        val branch = branch()

        assertThat(branch.hashCode()).isEqualTo(branch.iid.hashCode())
    }

    @Test
    fun `create branch, assert that id is null`() {
        val branch = branch()

        assertThat(branch.id).isNull()
    }

    @Test
    @Disabled
    fun `create branch, then copy, check that they are not equal`() {
//        val mockCommit = MockTestDataProvider(this@BranchModelTest.repository).commitBySha.getValue("a".repeat(40))
//        val branch = Branch(
//            name = "branch",
//            repository = Repository(
//                localPath = "test repo",
//                project = Project(name = "test project")
//            ),
//            head = mockCommit,
//        )
//        val branchCopy = branch.clone()
//
//        assertThat(branch).isNotSameAs(branchCopy)
//        assertThat(branch).isNotEqualTo(branchCopy)
//        assertThat(branch.iid).isNotEqualTo(branchCopy.iid)
    }

    @Test
    @Disabled
    fun `create branch, then copy, edit iid, check that they are equal`() {
//        val branch = Branch(
//            name = "branch",
//            repository,
//        )
//        val originIid = branch.iid
//        val branchCopy = branch.copy()
//        setField(
//            branchCopy.javaClass.superclass.superclass.getDeclaredField("iid"),
//            branchCopy,
//            originIid
//        )
//
//        assertThat(branch).isNotSameAs(branchCopy)
//        assertThat(branch.iid).isEqualTo(originIid)
//        assertThat(branch.iid).isEqualTo(branchCopy.iid)
//        assertThat(branch).isEqualTo(branchCopy)
    }

    @Test
    fun `create branch, check link to repository`() {
        val branch = branch()

        assertThat(branch.repository).isSameAs(repository)
        assertThat(branch.repository.branches).hasSize(1)
        assertThat(branch.repository.branches).containsOnly(branch)
    }

    @Nested
    inner class CommitRelation {
        @BeforeEach
        fun setup() {
            this@BranchModelTest.setup()
        }

        @Test
        fun `create branch, add commit from different repository, should fail`() {
            val head = MockTestDataProvider(this@BranchModelTest.repository).commitBySha.getValue("a".repeat(40))

            val differentRepository = Repository(
                localPath = "different-repository",
                project = Project(name = "different-project"),
            )

            val branch = branch(
                repository = this@BranchModelTest.repository,
                head = head
            )

            setField(
                head.javaClass.getDeclaredField("repository"),
                head,
                differentRepository
            )
            assertAll(
                { assertThat(branch.repository).isNotEqualTo(head.repository) },
                {
                    assertThrows<IllegalArgumentException> {
                        branch.head = head
                    }
                }
            )
        }

        @Test
        fun `create branch, with commit, get head, should succeed`() {
            val mockCommit = MockTestDataProvider(this@BranchModelTest.repository).commitBySha.getValue("a".repeat(40))

            val branch = branch(
                repository = this@BranchModelTest.repository,
                head = mockCommit
            )

            assertThat(branch.head).isSameAs(mockCommit)
        }

        @Test
        fun `create branch, with commit, get commits, should succeed`() {
            val mockCommits = MockTestDataProvider(this@BranchModelTest.repository).commitBySha.getValue("a".repeat(40))

            val branch = branch(
                repository = this@BranchModelTest.repository,
                head = mockCommits
            )

            assertAll(
                { assertThat(branch.commits).hasSize(1) },
                { assertThat(branch.commits).containsOnly(mockCommits) },
                { assertThat(branch.commits.first()).isSameAs(mockCommits) }
            )
        }

        @Test
        fun `create branch, commit history of 2, get commits, should succeed`() {
            var head: Commit
            var mockCommitB: Commit
            with(MockTestDataProvider(this@BranchModelTest.repository)) {
                head = this.commitBySha.getValue("a".repeat(40))
                mockCommitB = this.commitBySha.getValue("b".repeat(40))

                head.parents.add(mockCommitB)
            }

            val branch = branch(
                repository = this@BranchModelTest.repository,
                head = head
            )

            with(branch.commits) {
                assertAll(
                    { assertThat(this).hasSize(2) },
                    { assertThat(this).containsOnly(head, mockCommitB) },
                    { assertThat(this.first()).isSameAs(head) },
                    { assertThat(this.last()).isSameAs(mockCommitB) }
                )
            }
        }

        @Test
        fun `create branch, commit history of 3, get commits, should succeed`() {
            var head: Commit
            var mockCommitB: Commit
            var mockCommitC: Commit
            with(MockTestDataProvider(this@BranchModelTest.repository)) {
                head = this.commitBySha.getValue("a".repeat(40))
                mockCommitB = this.commitBySha.getValue("b".repeat(40))
                mockCommitC = this.commitBySha.getValue("c".repeat(40))

                head.parents.add(mockCommitB)
                mockCommitB.parents.add(mockCommitC)
            }

            val branch = branch(
                repository = this@BranchModelTest.repository,
                head = head
            )

            with(branch.commits) {
                assertAll(
                    { assertThat(this).hasSize(3) },
                    { assertThat(this).containsOnly(head, mockCommitB, mockCommitC) },
                    { assertThat(this.first()).isSameAs(head) },
                    { assertThat(this.last()).isSameAs(mockCommitC) }
                )
            }
        }

    }

    @Nested
    inner class FileRelation {
        @BeforeEach
        fun setup() {
            this@BranchModelTest.setup()
        }

        @Test
        fun `create branch, check that file relation is empty`() {
            val mockCommit = MockTestDataProvider(this@BranchModelTest.repository).commitBySha.getValue("a".repeat(40))
            val branch = branch(
                repository = repository,
                head = mockCommit
            )

            assertThat(branch.files).isEmpty()
        }
    }

    private fun branch(
        name: String = "branch",
        fullName: String = name,
        category: ReferenceCategory = ReferenceCategory.LOCAL_BRANCH,
        repository: Repository = this.repository,
        head: Commit = this.head
    ): Branch =
        Branch(
            name = name,
            fullName = fullName,
            category = category,
            repository = repository,
            head = head
        )
}
