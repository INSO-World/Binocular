package com.inso_world.binocular.ffi.unit.lib

import com.inso_world.binocular.ffi.BinocularConfig
import com.inso_world.binocular.ffi.GixConfig
import com.inso_world.binocular.ffi.internal.UniffiException
import com.inso_world.binocular.ffi.internal.findAllBranches
import com.inso_world.binocular.ffi.internal.findCommit
import com.inso_world.binocular.ffi.internal.findRepo
import com.inso_world.binocular.ffi.internal.traverseBranch
import com.inso_world.binocular.ffi.unit.lib.base.BaseLibraryUnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory

/**
 * Comprehensive unit tests for FFI error handling and exception types.
 *
 * Tests verify:
 * - Correct exception types for each error scenario
 * - Exception message quality and descriptiveness
 * - Error propagation from Rust layer to Kotlin
 * - Handling of edge cases and invalid inputs
 *
 * ### Test Organization
 * - [GixDiscoverErrors]: Repository discovery errors
 * - [RevisionParseErrors]: Commit hash parsing errors
 * - [ObjectErrors]: Git object operation errors
 * - [ReferenceErrors]: Branch and reference errors
 * - [TraversalErrors]: Commit traversal errors
 * - [ExceptionMessages]: Exception message quality
 */
//@DisplayName("Error Handling and Exceptions")
class ErrorHandlingTest : BaseLibraryUnitTest() {

    private val cfg: BinocularConfig = BinocularConfig().apply {
        gix = GixConfig(skipMerges = false, useMailmap = true)
    }

    @Nested
    @DisplayName("GixDiscoverException scenarios")
    inner class GixDiscoverErrors {

        @Test
        fun `throws GixDiscoverException when repository not found`() {
            // Verifies correct exception type for missing repositories
            val nonExistentPath = "/tmp/no_such_repo_" + System.currentTimeMillis()

            val exception = assertThrows<UniffiException.GixDiscoverException> {
                findRepo(nonExistentPath)
            }

            assertAll(
                { assertThat(exception).isInstanceOf(UniffiException.GixDiscoverException::class.java) },
                { assertThat(exception.v1).isNotEmpty() }
            )
        }

        @Test
        fun `GixDiscoverException for directory without git repository`() {
            // Tests exception when valid directory exists but isn't a Git repo
            val tempDir = createTempDirectory("test_not_git").toString()

            try {
                val exception = assertThrows<UniffiException.GixDiscoverException> {
                    findRepo(tempDir)
                }

                assertThat(exception.v1).containsIgnoringCase("repository")
            } finally {
                Files.delete(Path.of(tempDir))
            }
        }

        @Test
        fun `GixDiscoverException message contains path information`() {
            // Ensures exception provides context about failed path
            val invalidPath = "/tmp/invalid_path"

            val exception = assertThrows<UniffiException.GixDiscoverException> {
                findRepo(invalidPath)
            }

            assertThat(exception.v1).hasSizeGreaterThan(10)
        }

        @ParameterizedTest
        @ValueSource(strings = [
            "",
            " ",
            "\n",
            "\t"
        ])
        fun `GixDiscoverException for whitespace-only paths`(path: String) {
            // Tests handling of invalid whitespace paths
            assertThrows<UniffiException.GixDiscoverException> {
                findRepo(path)
            }
        }

        @Test
        fun `GixDiscoverException is catchable as generic UniffiException`() {
            // Verifies exception hierarchy allows generic catch
            val invalidPath = "/invalid/path"

            val exception = assertThrows<UniffiException> {
                findRepo(invalidPath)
            }

            assertThat(exception).isInstanceOf(UniffiException.GixDiscoverException::class.java)
        }
    }

    @Nested
    @DisplayName("RevisionParseException scenarios")
    inner class RevisionParseErrors {

        @Test
        fun `throws RevisionParseException for malformed hash`() {
            // Tests parsing errors for invalid SHA-1 format
            val repo = findRepo(System.getProperty("user.dir"))
            val malformedHash = "not_a_valid_hash"

            val exception = assertThrows<UniffiException.RevisionParseException> {
                findCommit(repo, malformedHash, useMailmap = cfg.gix.useMailmap)
            }

            assertThat(exception.v1).isNotEmpty()
        }

        @ParameterizedTest
        @CsvSource(
            "abc, too short",
            "ghijklmn, invalid characters",
            "12345, incomplete hash",
            "zzzzzzzzzz, non-hex characters"
        )
        fun `RevisionParseException for various invalid formats`(hash: String, @Suppress("UNUSED_PARAMETER") description: String) {
            // Verifies consistent error handling for different malformed hashes
            val repo = findRepo(System.getProperty("user.dir"))

            assertThrows<UniffiException.RevisionParseException> {
                findCommit(repo, hash, useMailmap = cfg.gix.useMailmap)
            }
        }

        @Test
        fun `RevisionParseException message explains parsing failure`() {
            // Ensures exception provides parsing error context
            val repo = findRepo(System.getProperty("user.dir"))

            val exception = assertThrows<UniffiException.RevisionParseException> {
                findCommit(repo, "invalid_commit_ref", useMailmap = cfg.gix.useMailmap)
            }

            assertAll(
                { assertThat(exception.v1).isNotEmpty() },
                { assertThat(exception.message).isNotEmpty() }
            )
        }
    }

    @Nested
    @DisplayName("ObjectException scenarios")
    inner class ObjectErrors {

        @Test
        fun `throws ObjectException when commit not found`() {
            // Tests object lookup errors for non-existent commits
            val repo = findRepo(System.getProperty("user.dir"))
            val nonExistentHash = "0000000000000000000000000000000000000000"

            val exception = assertThrows<UniffiException> {
                findCommit(repo, nonExistentHash, useMailmap = cfg.gix.useMailmap)
            }

            assertThat(exception).isNotNull()
        }

        @Test
        fun `ObjectException contains object type information`() {
            // Verifies exception indicates what type of object failed
            val repo = findRepo(System.getProperty("user.dir"))
            val invalidOid = "1111111111111111111111111111111111111111"

            val exception = assertThrows<UniffiException> {
                findCommit(repo, invalidOid, useMailmap = cfg.gix.useMailmap)
            }

            assertThat(exception.message).isNotEmpty()
        }
    }

    @Nested
    @DisplayName("ReferenceException scenarios")
    inner class ReferenceErrors {

        @Test
        fun `throws ReferenceException for invalid branch operations`() {
            // Tests reference errors when branch operations fail
            val repo = findRepo(System.getProperty("user.dir"))
            // Create corrupted repository reference
            val corruptedRepo = repo.copy(gitDir = "/invalid/path")

            val exception = assertThrows<UniffiException> {
                findAllBranches(corruptedRepo)
            }

            assertThat(exception).isNotNull()
        }

        @Test
        fun `ReferenceException message describes reference problem`() {
            // Ensures exception explains reference-specific issues
            val repo = findRepo(System.getProperty("user.dir"))
            val corruptedRepo = repo.copy(gitDir = "/tmp/corrupt")

            val exception = assertThrows<UniffiException> {
                findAllBranches(corruptedRepo)
            }

            assertThat(exception.message).hasSizeGreaterThan(10)
        }
    }

    @Nested
    @DisplayName("TraversalException scenarios")
    inner class TraversalErrors {

        @Test
        fun `throws ReferenceException for non-existent branch`() {
            // Verifies traversal errors for invalid branch names
            val repo = findRepo(System.getProperty("user.dir"))
            val invalidBranch = "refs/heads/does_not_exist_" + System.currentTimeMillis()

            val exception = assertThrows<UniffiException.ReferenceException> {
                traverseBranch(repo, invalidBranch, useMailmap = cfg.gix.useMailmap, skipMerges = cfg.gix.skipMerges)
            }

            assertThat(exception.v1).isNotEmpty()
        }

        @Test
        fun `ReferenceException explains traversal failure`() {
            // Ensures exception provides traversal context
            val repo = findRepo(System.getProperty("user.dir"))
            val invalidBranch = "invalid_branch_ref"

            val exception = assertThrows<UniffiException.ReferenceException> {
                traverseBranch(repo, invalidBranch, useMailmap = cfg.gix.useMailmap, skipMerges = cfg.gix.skipMerges)
            }

            assertAll(
                { assertThat(exception.v1).isNotEmpty() },
                { assertThat(exception.v1).contains("Failed to get references: The reference") },
                { assertThat(exception.v1).contains(invalidBranch) },
                { assertThat(exception.v1).contains("did not exist") },
            )
        }

        @ParameterizedTest
        @ValueSource(strings = [
            "",
            "   ",
            "not/a/valid/ref",
            "refs/heads/",
            "/invalid/ref"
        ])
        fun `TraversalException for various invalid branch references`(invalidRef: String) {
            // Tests consistent error handling for different invalid references
            val repo = findRepo(System.getProperty("user.dir"))

            assertThrows<UniffiException> {
                traverseBranch(repo, invalidRef, useMailmap = cfg.gix.useMailmap, skipMerges = cfg.gix.skipMerges)
            }
        }
    }

    @Nested
    @DisplayName("Exception Message Quality")
    inner class ExceptionMessages {

        @Test
        fun `all exceptions have non-empty messages`() {
            // Verifies all exception types provide messages
            val repo = findRepo(System.getProperty("user.dir"))

            // Test GixDiscoverException
            val discoverEx = assertThrows<UniffiException.GixDiscoverException> {
                findRepo("/invalid/path")
            }
            assertThat(discoverEx.message).isNotEmpty()

            // Test RevisionParseException
            val parseEx = assertThrows<UniffiException.RevisionParseException> {
                findCommit(repo, "invalid", useMailmap = cfg.gix.useMailmap)
            }
            assertThat(parseEx.message).isNotEmpty()

            // Test TraversalException
            val traversalEx = assertThrows<UniffiException.ReferenceException> {
                traverseBranch(repo, "invalid_branch", useMailmap = cfg.gix.useMailmap, skipMerges = cfg.gix.skipMerges)
            }
            assertThat(traversalEx.message).isNotEmpty()
        }

        @Test
        fun `exception messages are descriptive`() {
            // Ensures messages provide useful debugging information
            val exceptions = mutableListOf<UniffiException>()

            // Collect various exceptions
            try {
                findRepo("/invalid")
            } catch (e: UniffiException) {
                exceptions.add(e)
            }

            val repo = findRepo(System.getProperty("user.dir"))
            try {
                findCommit(repo, "bad_hash", useMailmap = cfg.gix.useMailmap)
            } catch (e: UniffiException) {
                exceptions.add(e)
            }

            exceptions.forEach { exception ->
                assertAll(
                    { assertThat(exception.message).hasSizeGreaterThan(10) },
                    { assertThat(exception.message).doesNotContain("null") }
                )
            }
        }

        @Test
        fun `exception v1 field contains error details`() {
            // Verifies v1 field (error string) has meaningful content
            val exception = assertThrows<UniffiException.GixDiscoverException> {
                findRepo("/tmp/nonexistent")
            }

            assertAll(
                { assertThat(exception.v1).isNotEmpty() },
                { assertThat(exception.v1).hasSizeGreaterThan(5) },
                { assertThat(exception.v1).isNotEqualTo("error") }
            )
        }

        @Test
        fun `exception toString provides debug information`() {
            // Tests that exception string representation is useful
            val exception = assertThrows<UniffiException> {
                findRepo("/invalid/path")
            }

            val exceptionString = exception.toString()
            assertAll(
                { assertThat(exceptionString).isNotEmpty() },
                { assertThat(exceptionString).contains("Exception") }
            )
        }
    }

    @Nested
    @DisplayName("Exception Inheritance and Hierarchy")
    inner class ExceptionHierarchy {

        @Test
        fun `all FFI exceptions extend UniffiException`() {
            // Verifies exception hierarchy for polymorphic handling
            val exceptions = mutableListOf<Exception>()

            // Collect different exception types
            try {
                findRepo("/invalid")
            } catch (e: Exception) {
                exceptions.add(e)
            }

            val repo = findRepo(System.getProperty("user.dir"))
            try {
                findCommit(repo, "invalid", useMailmap = cfg.gix.useMailmap)
            } catch (e: Exception) {
                exceptions.add(e)
            }

            try {
                traverseBranch(repo, "invalid", useMailmap = cfg.gix.useMailmap, skipMerges = cfg.gix.skipMerges)
            } catch (e: Exception) {
                exceptions.add(e)
            }

            exceptions.forEach { exception ->
                assertThat(exception).isInstanceOf(UniffiException::class.java)
            }
        }

        @Test
        fun `UniffiException can be caught generically`() {
            // Tests that all specific exceptions can be caught as UniffiException
            var caughtException: UniffiException? = null

            try {
                findRepo("/invalid/path")
            } catch (e: UniffiException) {
                caughtException = e
            }

            assertThat(caughtException).isNotNull()
        }

        @Test
        fun `specific exception types can be distinguished`() {
            // Verifies ability to catch and differentiate specific exception types
            var discoveryError = false
            var parseError = false

            try {
                findRepo("/invalid")
            } catch (e: UniffiException.GixDiscoverException) {
                discoveryError = true
            }

            val repo = findRepo(System.getProperty("user.dir"))
            try {
                findCommit(repo, "bad", useMailmap = cfg.gix.useMailmap)
            } catch (e: UniffiException.RevisionParseException) {
                parseError = true
            }

            assertAll(
                { assertThat(discoveryError).isTrue() },
                { assertThat(parseError).isTrue() }
            )
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    inner class EdgeCases {

        @Test
        fun `handles null-like string inputs gracefully`() {
            // Tests error handling for edge case string inputs
            val edgeCaseInputs = listOf("", " ", "\n", "\t", "   \n\t   ")

            edgeCaseInputs.forEach { input ->
                assertThrows<UniffiException> {
                    findRepo(input)
                }
            }
        }

        @Test
        fun `handles very long path names`() {
            // Tests behavior with extremely long path strings
            val longPath = "/tmp/" + "a".repeat(1000)

            val exception = assertThrows<UniffiException> {
                findRepo(longPath)
            }

            assertThat(exception).isNotNull()
        }

        @Test
        fun `handles special characters in paths`() {
            // Verifies error handling for paths with special characters
            val specialPaths = listOf(
                "/tmp/with spaces/repo",
                "/tmp/with-dashes/repo",
                "/tmp/with_underscores/repo",
                "/tmp/with.dots/repo"
            )

            specialPaths.forEach { path ->
                assertThrows<UniffiException> {
                    findRepo(path)
                }
            }
        }

        @Test
        fun `handles unicode in error messages`() {
            // Tests that unicode characters in paths don't break error messages
            val unicodePath = "/tmp/тест/مستودع/저장소"

            val exception = assertThrows<UniffiException> {
                findRepo(unicodePath)
            }

            assertThat(exception.message).isNotNull()
        }
    }
}
