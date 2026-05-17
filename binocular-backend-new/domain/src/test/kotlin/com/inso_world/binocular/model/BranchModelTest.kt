package com.inso_world.binocular.model

import com.inso_world.binocular.domain.data.MockTestDataProvider
import com.inso_world.binocular.model.vcs.ReferenceCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class BranchModelTest {
    private lateinit var repository: Repository
    private var headCommitId: Commit.Id = Commit.Id(kotlin.uuid.Uuid.random())

    @BeforeEach
    fun setup() {
        val project = Project(name = "test project")
        repository = Repository(localPath = "test repo", projectId = project.iid)
        val developer = Developer(name = "Test Developer", email = "dev@test.com")
        val signature = Signature(
            developerId = developer.iid,
            gitSignature = developer.gitSignature,
            timestamp = LocalDateTime.now().minusSeconds(1),
        )
        val head = Commit(
            sha = "a".repeat(40),
            message = "msg1",
            authorSignature = signature,
            repositoryId = repository.iid,
        )
        headCommitId = head.iid
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.domain.data.DummyTestData#provideAllowedStrings")
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
    @MethodSource("com.inso_world.binocular.domain.data.DummyTestData#provideBlankStrings")
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
    @MethodSource("com.inso_world.binocular.domain.data.DummyTestData#provideBlankStrings")
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
    fun `create branch, check link to repositoryId`() {
        val branch = branch()

        assertThat(branch.repositoryId).isEqualTo(repository.iid)
    }

    @Test
    fun `create branch, check headCommitId is set`() {
        val branch = branch()

        assertThat(branch.headCommitId).isEqualTo(headCommitId)
    }

    @Test
    fun `create branch, can change headCommitId`() {
        val branch = branch()
        val newHead = Commit(
            sha = "b".repeat(40),
            message = "new head",
            authorSignature = Signature(
                developerId = Developer(name = "Dev", email = "dev@test.com").iid,
                gitSignature = "Dev ",
                timestamp = LocalDateTime.now().minusSeconds(1),
            ),
            repositoryId = repository.iid,
        )

        branch.headCommitId = newHead.iid
        assertThat(branch.headCommitId).isEqualTo(newHead.iid)
    }

    private fun branch(
        name: String = "branch",
        fullName: String = name,
        category: ReferenceCategory = ReferenceCategory.LOCAL_BRANCH,
    ): Branch =
        Branch(
            name = name,
            fullName = fullName,
            category = category,
            repositoryId = repository.iid,
            headCommitId = headCommitId,
        )
}
