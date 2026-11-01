package com.inso_world.binocular.ffi.unit.extensions

import com.inso_world.binocular.core.unit.base.BaseUnitTest
import com.inso_world.binocular.ffi.extensions.toDomain
import com.inso_world.binocular.ffi.internal.GixBranch
import com.inso_world.binocular.ffi.internal.GixReferenceCategory
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import com.inso_world.binocular.model.vcs.ReferenceCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDateTime

/**
 * Unit tests for [toDomain] extension function.
 *
 * Covers branch name normalization, category mapping, head SHA conversion, and edge cases.
 */
class BranchTest : BaseUnitTest() {
    private lateinit var project: Project
    private lateinit var repository: Repository
    private lateinit var headCommit: Commit

    @BeforeEach
    fun setUp() {
        project = Project(name = "test-project")
        repository =
            Repository(
                localPath = "/path/to/repo",
                projectId = project.iid,
            ).apply { this.project = project }
        val developer =
            Developer(name = "Test Committer", email = "committer@test.com", repositoryId = repository.iid).apply {
                this.repository =
                    repository
            }
        val signatureA = Signature(developerId = developer.iid, developer = developer, timestamp = LocalDateTime.of(2024, 1, 1, 0, 0))
        headCommit =
            Commit(
                sha = "a".repeat(40),
                authorSignature = signatureA,
                repositoryId = repository.iid,
                repository = repository,
            )
    }

    // ========== Creating New Branches ==========

    @Test
    fun `toDomain creates new branch when no existing branch matches`() {
        val ffiBranch = gixBranch(name = "main")

        val result = ffiBranch.toDomain(repository.iid, headCommit)

        assertThat(result).isNotNull
        assertThat(result.name).isEqualTo("main")
        assertThat(result.fullName).isEqualTo("main")
        assertThat(result.category).isEqualTo(ReferenceCategory.LOCAL_BRANCH)
        assertThat(result.headSha).isEqualTo(headCommit.sha)
    }

    @Test
    fun `toDomain creates branch with specified head commit`() {
        val ffiBranch = gixBranch(name = "develop")

        val result = ffiBranch.toDomain(repository.iid, headCommit)

        assertThat(result.headSha).isEqualTo(headCommit.sha)
        assertThat(result.repositoryId).isEqualTo(repository.iid)
    }

    @Test
    fun `toDomain copies full name and category metadata`() {
        val ffiBranch =
            gixBranch(
                name = "feature",
                fullName = "refs/heads/feature",
                category = GixReferenceCategory.LOCAL_BRANCH,
            )

        val result = ffiBranch.toDomain(repository.iid, headCommit)

        assertThat(result.fullName).isEqualTo("refs/heads/feature")
        assertThat(result.category).isEqualTo(ReferenceCategory.LOCAL_BRANCH)
    }

    // ========== Edge Cases ==========

    @Nested
    inner class EdgeCases {
        @Test
        fun `toDomain handles minimal valid branch name`() {
            val ffiBranch = gixBranch(name = "x")

            val result = ffiBranch.toDomain(repository.iid, headCommit)

            assertThat(result.name).isEqualTo("x")
        }

        @Test
        fun `toDomain handles very long branch name`() {
            val longName = "feature/" + "very-long-name-".repeat(50)
            val ffiBranch = gixBranch(name = longName)

            val result = ffiBranch.toDomain(repository.iid, headCommit)

            assertThat(result.name).isEqualTo(longName)
        }

        @Test
        fun `toDomain handles branch name with special characters`() {
            val ffiBranch = gixBranch(name = "feature/issue-#123")

            val result = ffiBranch.toDomain(repository.iid, headCommit)

            assertThat(result.name).isEqualTo("feature/issue-#123")
        }

        @Test
        fun `toDomain handles branch name with Unicode characters`() {
            val ffiBranch = gixBranch(name = "功能/新特性")

            val result = ffiBranch.toDomain(repository.iid, headCommit)

            assertThat(result.name).isEqualTo("功能/新特性")
        }

        @Test
        fun `toDomain handles branch name with dots`() {
            val ffiBranch = gixBranch(name = "release/1.0.0")

            val result = ffiBranch.toDomain(repository.iid, headCommit)

            assertThat(result.name).isEqualTo("release/1.0.0")
        }

        @Test
        fun `toDomain handles branch name with underscores and hyphens`() {
            val ffiBranch = gixBranch(name = "feature_branch-new-ui")

            val result = ffiBranch.toDomain(repository.iid, headCommit)

            assertThat(result.name).isEqualTo("feature_branch-new-ui")
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "main",
                "develop",
                "feature/new-ui",
                "hotfix/urgent-fix",
                "release/v1.0.0",
                "bugfix/fix-bug-123",
                "experiment/try-something",
            ],
        )
        fun `toDomain handles common Git branch naming patterns`(branchName: String) {
            val ffiBranch = gixBranch(name = branchName)

            val result = ffiBranch.toDomain(repository.iid, headCommit)

            assertThat(result.name).isEqualTo(branchName)
        }
    }

    @Test
    fun `toDomain always returns non-null Branch`() {
        // Explicit null check to ensure contract is satisfied
        val ffiBranch = gixBranch(name = "refs/heads/main")

        val result = ffiBranch.toDomain(repository.iid, headCommit)

        assertThat(result).isNotNull
    }

    companion object {
        private const val DEFAULT_TARGET = "0000000000000000000000000000000000000000"

        private fun gixBranch(
            name: String,
            fullName: String = name,
            target: String = DEFAULT_TARGET,
            category: GixReferenceCategory = GixReferenceCategory.LOCAL_BRANCH,
        ) = GixBranch(
            fullName = fullName,
            name = name,
            target = target,
            category = category,
        )
    }
}
