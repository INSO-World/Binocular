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

class BranchModelTest {
    private lateinit var repository: Repository

    @BeforeEach
    fun setup() {
        repository = Repository(localPath = "test repo", project = Project(name = "test project"))
    }

    @Test
    fun `add branch to other repository too, should fail`() {
        val dummyBranch =
            Branch(
                name = "branch",
                repository = Repository(
                    localPath = "test repo",
                    project = Project(name = "test project")
                )
            )
        assertThrows<IllegalArgumentException> { repository.branches.add(dummyBranch) }
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.data.DummyTestData#provideAllowedStrings")
    fun `create branch with allowed names, should succeed`(
        name: String,
    ) {
        assertDoesNotThrow {
            Branch(
                name = name,
                repository = Repository(
                    localPath = "test repo",
                    project = Project(name = "test project")
                )
            )
        }
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.data.DummyTestData#provideBlankStrings")
    fun `create branch with blank name, should fail`(
        name: String,
    ) {
        assertThrows<IllegalArgumentException> {
            Branch(
                name = name,
                repository = Repository(
                    localPath = "test repo",
                    project = Project(name = "test project")
                )
            )
        }
    }

    @Test
    fun `create branch, check iid is set automatically`() {
        val branch = Branch(
            name = "branch",
            repository = Repository(
                localPath = "test repo",
                project = Project(name = "test project")
            )
        )

        assertThat(branch.iid).isNotNull()
    }

    @Test
    fun `create branch, validate uniqueKey`() {
        val repository = Repository(
            localPath = "test repo",
            project = Project(name = "test project")
        )
        val branch = Branch(
            name = "branch",
            repository = repository
        )

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
        val repository = Repository(
            localPath = "test repo",
            project = Project(name = "test project")
        )
        val branch = Branch(
            name = "branch",
            repository = repository
        )

        assertThat(branch.hashCode()).isEqualTo(branch.iid.hashCode())
    }

    @Test
    fun `create branch, assert that id is null`() {
        val repository = Repository(
            localPath = "test repo",
            project = Project(name = "test project")
        )
        val branch = Branch(
            name = "branch",
            repository = repository
        )

        assertThat(branch.id).isNull()
    }

    @Test
    fun `create branch, then copy, check that they are not equal`() {
        val branch = Branch(
            name = "branch",
            repository = Repository(
                localPath = "test repo",
                project = Project(name = "test project")
            )
        )
        val branchCopy = branch.copy()

        assertThat(branch).isNotSameAs(branchCopy)
        assertThat(branch).isNotEqualTo(branchCopy)
        assertThat(branch.iid).isNotEqualTo(branchCopy.iid)
    }

    @Test
    fun `create branch, then copy, edit iid, check that they are equal`() {
        val branch = Branch(
            name = "branch",
            repository = Repository(
                localPath = "test repo",
                project = Project(name = "test project")
            )
        )
        val originIid = branch.iid
        val branchCopy = branch.copy()
        setField(
            branchCopy.javaClass.superclass.getDeclaredField("iid"),
            branchCopy,
            originIid
        )

        assertThat(branch).isNotSameAs(branchCopy)
        assertThat(branch.iid).isEqualTo(originIid)
        assertThat(branch.iid).isEqualTo(branchCopy.iid)
        assertThat(branch).isEqualTo(branchCopy)
    }

    @Test
    fun `create branch, check link to repository`() {
        val branch = Branch(
            name = "branch",
            repository = repository
        )

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
            val mockCommits = MockTestDataProvider(this@BranchModelTest.repository).commitBySha.getValue("a".repeat(40))

            val differentRepository = Repository(
                localPath = "different-repository",
                project = Project(name = "different-project"),
            )

            val branch = Branch(
                name = "branch",
                repository = differentRepository
            )

            assertAll(
                { assertThat(branch.repository).isNotEqualTo(mockCommits.repository) },
                {
                    assertThrows<IllegalArgumentException> {
                        branch.commits.add(mockCommits)
                    }
                }
            )
        }

        @Test
        fun `create branch, addAll commit from different repository, should fail`() {
            val mockCommits = MockTestDataProvider(this@BranchModelTest.repository).commitBySha.getValue("a".repeat(40))


            val differentRepository = Repository(
                localPath = "different-repository",
                project = Project(name = "different-project"),
            )

            val branch = Branch(
                name = "branch",
                repository = differentRepository
            )

            assertAll(
                { assertThat(branch.repository).isNotEqualTo(mockCommits.repository) },
                {
                    assertThrows<IllegalArgumentException> {
                        branch.commits.addAll(listOf(mockCommits))
                    }
                }
            )
        }

        @Test
        fun `create branch, add commits via addAll`() {
            val mockCommits = MockTestDataProvider(this@BranchModelTest.repository).commits.take(2)

            val branch = Branch(
                name = "branch",
                repository = repository
            )

            assertTrue(branch.commits.addAll(mockCommits))

            assertThat(branch.commits).hasSize(2)
            assertThat(branch.commits.flatMap { it.branches }).containsOnly(branch)
            branch.commits.flatMap { it.branches }.forEach {
                assertThat(it).isSameAs(branch)
            }

        }

        @Test
        fun `create branch, add commit twice via addAll, should be added once`() {
            val mockCommit = MockTestDataProvider(this@BranchModelTest.repository).commitBySha.getValue("a".repeat(40))

            val branch = Branch(
                name = "branch",
                repository = repository
            )

            assertAll(
                { assertTrue(branch.commits.addAll(listOf(mockCommit))) },
                { assertFalse(branch.commits.addAll(listOf(mockCommit))) },
            )

            assertAll(
                { assertThat(branch.commits).hasSize(1) },
                { assertThat(branch.commits).containsOnly(mockCommit) },
            )

            assertAll(
                { assertThat(mockCommit.branches).hasSize(1) },
                { assertThat(mockCommit.branches).containsOnly(branch) },
            )
        }

        @Test
        fun `create branch, add commit twice via add, should be added once`() {
            val mockCommit = MockTestDataProvider(this@BranchModelTest.repository).commitBySha.getValue("a".repeat(40))

            val branch = Branch(
                name = "branch",
                repository = repository
            )

            assertAll(
                { assertTrue(branch.commits.add(mockCommit)) },
                { assertFalse(branch.commits.add(mockCommit)) },
            )

            assertAll(
                { assertThat(branch.commits).hasSize(1) },
                { assertThat(branch.commits).containsOnly(mockCommit) },
            )

            assertAll(
                { assertThat(mockCommit.branches).hasSize(1) },
                { assertThat(mockCommit.branches).containsOnly(branch) },
            )
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
            val branch = Branch(
                name = "branch",
                repository = repository
            )

            assertThat(branch.files).isEmpty()
        }
    }
}
