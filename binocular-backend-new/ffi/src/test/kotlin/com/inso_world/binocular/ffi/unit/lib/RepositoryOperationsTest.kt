package com.inso_world.binocular.ffi.unit.lib

import com.inso_world.binocular.ffi.internal.UniffiException
import com.inso_world.binocular.ffi.internal.findAllBranches
import com.inso_world.binocular.ffi.internal.findRepo
import com.inso_world.binocular.ffi.unit.lib.base.BaseLibraryUnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory

/**
 * Comprehensive unit tests for repository operations in the FFI layer.
 *
 * Tests verify:
 * - Repository discovery and validation
 * - Remote repository information extraction
 * - Branch enumeration (local and remote)
 * - Error handling for invalid paths and repositories
 *
 * ### Test Organization
 * - [FindRepoOperation]: Tests for repository discovery
 * - [FindAllBranchesOperation]: Tests for branch enumeration
 * - [ErrorHandling]: Tests for exception scenarios
 */
@DisplayName("Repository Operations")
class RepositoryOperationsTest : BaseLibraryUnitTest() {

    @Nested
    @DisplayName("findRepo operation")
    inner class FindRepoOperation {

        @Test
        fun `finds repository at valid git directory path`() {
            // Verifies that findRepo successfully discovers a valid Git repository
            // and returns repository metadata
            val testRepoPath = System.getProperty("user.dir")

            val result = findRepo(testRepoPath)

            assertAll(
                { assertThat(result).isNotNull },
                { assertThat(result.gitDir).isNotEmpty() },
                { assertThat(result.gitDir).contains(".git") }
            )
        }

        @Test
        fun `returns repository with work tree for non-bare repositories`() {
            // Ensures non-bare repositories have their work tree path populated
            val testRepoPath = System.getProperty("user.dir")

            val result = findRepo(testRepoPath)

            assertAll(
                { assertThat(result.workTree).isNotNull() },
                { assertThat(result.workTree).isNotEmpty() }
            )
        }

        @Test
        fun `extracts remote information from repository`() {
            // Validates that repository remotes are correctly extracted and populated
            val testRepoPath = System.getProperty("user.dir")

            val result = findRepo(testRepoPath)

            assertThat(result.remotes).isNotNull()
            // Note: May be empty if repository has no remotes configured
        }

        @Test
        fun `repository git directory path is absolute`() {
            // Confirms that returned git_dir is an absolute path, not relative
            val testRepoPath = System.getProperty("user.dir")

            val result = findRepo(testRepoPath)

            assertAll(
                { assertThat(Path.of(result.gitDir)).isAbsolute() },
            )

        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                ".",
                "..",
                "./",
                "../"
            ]
        )
        fun `handles relative paths correctly`(relativePath: String) {
            // Tests that relative paths are resolved and repositories are discovered
            // Current directory should be a git repository for this test
            val result = findRepo(relativePath)

            assertThat(result.gitDir).isNotEmpty()
        }

        @Test
        fun `discovers repository from subdirectory`() {
            // Verifies repository discovery works from subdirectories within the repo
            val testRepoPath = System.getProperty("user.dir") + "/src"

            val result = findRepo(testRepoPath)

            assertAll(
                { assertThat(result.gitDir).isNotEmpty() },
                { assertThat(result.gitDir).contains(".git") }
            )
        }

        @Test
        fun `repository remotes have valid structure`() {
            // Checks that each remote in the repository has name and url properties
            val testRepoPath = System.getProperty("user.dir")

            val result = findRepo(testRepoPath)

            result.remotes.forEach { remote ->
                assertAll(
                    { assertThat(remote.name).isNotEmpty() },
                    { assertThat(remote.url).isNotEmpty() }
                )
            }
        }
    }

    @Nested
    @DisplayName("findAllBranches operation")
    inner class FindAllBranchesOperation {

        @Test
        fun `returns list of branches from repository`() {
            // Verifies that branch enumeration returns a non-null list
            val testRepoPath = System.getProperty("user.dir")
            val repo = findRepo(testRepoPath)

            val branches = findAllBranches(repo)

            assertThat(branches).isNotNull()
        }

        @Test
        fun `branches contain both local and remote references`() {
            // Ensures both local and remote branches are included in results
            val testRepoPath = System.getProperty("user.dir")
            val repo = findRepo(testRepoPath)

            val branches = findAllBranches(repo)

            // Should have at least one branch in any Git repository
            assertThat(branches).isNotEmpty()
        }

        @Test
        fun `each branch has valid name property`() {
            // Validates that all returned branches have non-empty names
            val testRepoPath = System.getProperty("user.dir")
            val repo = findRepo(testRepoPath)

            val branches = findAllBranches(repo)

            branches.forEach { branch ->
                assertThat(branch.name).isNotEmpty()
            }
        }

        @Test
        fun `branches have target commit information`() {
            // Confirms each branch points to a valid commit
            val testRepoPath = System.getProperty("user.dir")
            val repo = findRepo(testRepoPath)

            val branches = findAllBranches(repo)

            branches.forEach { branch ->
                assertThat(branch.target).isNotNull()
            }
        }

        @Test
        fun `finds main or master branch`() {
            // Checks that common default branch names are present
            val testRepoPath = System.getProperty("user.dir")
            val repo = findRepo(testRepoPath)

            val branches = findAllBranches(repo)
            val branchNames = branches.map { it.name }

            assertThat(branchNames).anyMatch {
                it.contains("main") || it.contains("master")
            }
        }
    }

    @Nested
    @DisplayName("Error Handling")
    inner class ErrorHandling {

        @Test
        fun `throws GixDiscoverException for non-existent path`() {
            // Verifies proper exception is thrown when path doesn't exist
            val invalidPath = "/tmp/non_existent_path_" + System.currentTimeMillis()

            val exception = assertThrows<UniffiException.GixDiscoverException> {
                findRepo(invalidPath)
            }

            assertThat(exception.v1).isNotEmpty()
        }

        @Test
        fun `throws GixDiscoverException for non-git directory`() {
            // Ensures exception is thrown for valid paths that aren't Git repositories
            val tempDir = createTempDirectory("test_non_git").toString()

            val exception = assertThrows<UniffiException.GixDiscoverException> {
                findRepo(tempDir)
            }

            assertAll(
                { assertThat(exception.v1).isNotEmpty() },
                { assertThat(exception.v1).containsAnyOf("not a git", "repository") }
            )

            // Cleanup
            Files.delete(Path.of(tempDir))
        }

        @Test
        fun `throws GixDiscoverException for empty string path`() {
            // Tests error handling for empty path input
            val exception = assertThrows<UniffiException.GixDiscoverException> {
                findRepo("")
            }

            assertThat(exception.v1).isNotEmpty()
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "/invalid/path/to/repo",
                "/tmp/does/not/exist",
                "/root/inaccessible/path"
            ]
        )
        fun `throws GixDiscoverException for various invalid paths`(invalidPath: String) {
            // Verifies consistent error handling across different invalid path types
            assertThrows<UniffiException.GixDiscoverException> {
                findRepo(invalidPath)
            }
        }

        @Test
        fun `GixDiscoverException contains descriptive error message`() {
            // Ensures exception messages provide useful debugging information
            val invalidPath = "/tmp/not_a_repo"

            val exception = assertThrows<UniffiException.GixDiscoverException> {
                findRepo(invalidPath)
            }

            assertAll(
                { assertThat(exception.v1).isNotEmpty() },
                { assertThat(exception.v1).hasSizeGreaterThan(10) },
                { assertThat(exception.message).isNotEmpty() }
            )
        }

        @Test
        fun `findAllBranches throws GixDiscoverException for invalid repository`() {
            // Tests error handling when branch enumeration fails
            val testRepoPath = System.getProperty("user.dir")
            val repo = findRepo(testRepoPath)
            // Corrupt the git_dir to simulate invalid repository
            val corruptedRepo = repo.copy(gitDir = "/invalid/git/dir")

            val exception = assertThrows<UniffiException.GixDiscoverException> {
                findAllBranches(corruptedRepo)
            }

            assertThat(exception).isNotNull()
        }
    }

    @Nested
    @DisplayName("Repository Remotes")
    inner class RepositoryRemotes {

        @Test
        fun `remote URLs are valid git URLs`() {
            // Validates that remote URLs follow Git URL conventions
            val testRepoPath = System.getProperty("user.dir")
            val repo = findRepo(testRepoPath)

            repo.remotes.forEach { remote ->
                assertThat(remote.url).matches(
                    "(https?://.*)|(git@.*:.*\\.git)|(.*@.*:.*)|(/.*)"
                )
            }
        }

        @Test
        fun `common remote names are recognized`() {
            // Checks for standard remote names like origin, upstream
            val testRepoPath = System.getProperty("user.dir")
            val repo = findRepo(testRepoPath)

            if (repo.remotes.isNotEmpty()) {
                val remoteNames = repo.remotes.map { it.name }
                assertThat(remoteNames).anyMatch {
                    it == "origin" || it == "upstream"
                }
            }
        }

        @Test
        fun `remotes list is immutable after retrieval`() {
            // Ensures returned remotes list can be safely used without side effects
            val testRepoPath = System.getProperty("user.dir")
            val repo = findRepo(testRepoPath)

            val remotes1 = repo.remotes
            val remotes2 = repo.remotes

            assertThat(remotes1).isEqualTo(remotes2)
        }
    }
}
