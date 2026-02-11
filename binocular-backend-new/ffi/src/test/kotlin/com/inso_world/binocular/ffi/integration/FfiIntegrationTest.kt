package com.inso_world.binocular.ffi.integration

import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest
import com.inso_world.binocular.ffi.FfiConfig
import com.inso_world.binocular.ffi.BinocularFfiTestApplication
import com.inso_world.binocular.ffi.internal.*
import com.inso_world.binocular.ffi.util.Utils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.nio.file.Files
import java.time.LocalDateTime
import java.util.stream.Stream

/**
 * Integration tests for Rust FFI bindings exposed via UniFFI.
 *
 * Tests all FFI functions defined in the Rust crate to ensure correct:
 * - Data marshalling between Kotlin and Rust
 * - Error handling across the FFI boundary
 * - Git operations via the gix library
 * - Memory management and resource cleanup
 *
 * ### Test Organization
 * Tests are organized into nested classes by FFI module:
 * - [BasicOperations]: Simple connectivity and repository discovery
 * - [RepositoryOperations]: Repository discovery and metadata retrieval
 * - [BranchOperations]: Branch enumeration and traversal
 * - [CommitOperations]: Commit lookup and history traversal
 * - [DiffOperations]: Diff calculation for commit pairs
 * - [BlameOperations]: Blame calculation for files
 * - [ErrorHandling]: Exception handling across FFI boundary
 * - [DataMarshalling]: Type conversion and data integrity
 */
@TestClassOrder(ClassOrderer.OrderAnnotation::class)
@SpringBootTest(
    classes = [BinocularFfiTestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
@ExtendWith(SpringExtension::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
internal class FfiIntegrationTest : BaseFixturesIntegrationTest() {

    @Autowired
    private lateinit var cfg: FfiConfig

    companion object {
        private lateinit var simpleRepo: GixRepository
        private lateinit var octoRepo: GixRepository
        private lateinit var advancedRepo: GixRepository

        @BeforeAll
        @JvmStatic
        fun loadLibrary() {
            // Load the native library
            Utils.loadPlatformLibrary("gix_binocular")
        }

        @JvmStatic
        fun branchTestData(): Stream<Arguments> = Stream.of(
            Arguments.of(SIMPLE_REPO, "refs/heads/master", 14),
            Arguments.of(SIMPLE_REPO, "refs/remotes/origin/master", 13),
            Arguments.of(OCTO_REPO, "refs/heads/master", 19),
            Arguments.of(OCTO_REPO, "refs/heads/octo1", 16),
            Arguments.of(OCTO_REPO, "refs/heads/octo2", 16),
            Arguments.of(OCTO_REPO, "refs/heads/octo3", 16),
            Arguments.of(ADVANCED_REPO, "refs/heads/master", 35),
            Arguments.of(ADVANCED_REPO, "refs/heads/imported", 4)
        )

        @JvmStatic
        fun commitHistoryData(): Stream<Arguments> = Stream.of(
            Arguments.of(SIMPLE_REPO, "HEAD", null, 14),
            Arguments.of(SIMPLE_REPO, "b51199ab8b83e31f64b631e42b2ee0b1c7e3259a", null, 14),
            Arguments.of(SIMPLE_REPO, "48a384a6a9188f376835005cd10fd97542e69bf7", null, 1),
            Arguments.of(OCTO_REPO, "HEAD", null, 19),
            Arguments.of(OCTO_REPO, "4dedc3c738eee6b69c43cde7d89f146912532cff", null, 19),
            Arguments.of(ADVANCED_REPO, "HEAD", null, 35),
            Arguments.of(ADVANCED_REPO, "379dc91fb055ba385b5e5446428ffbe38804fa99", null, 35)
        )

        private fun normalizeBranchName(refName: String): String =
            refName
                .removePrefix("refs/heads/")
                .removePrefix("refs/remotes/")
                .removePrefix("refs/tags/")
    }

    @BeforeEach
    fun setUpRepositories() {
//        if (!::simpleRepo.isInitialized) {
        simpleRepo = findRepo("${FIXTURES_PATH}/${SIMPLE_REPO}")
//        }
//        if (!::octoRepo.isInitialized) {
        octoRepo = findRepo("${FIXTURES_PATH}/${OCTO_REPO}")
//        }
//        if (!::advancedRepo.isInitialized) {
        advancedRepo = findRepo("${FIXTURES_PATH}/${ADVANCED_REPO}")
//        }
    }

    @Nested
    @Order(1)
    @DisplayName("Basic FFI operations")
    inner class BasicOperations {

        @Test
        fun `hello should execute without errors`() {
            assertDoesNotThrow {
                hello()
            }
        }

        @Test
        fun `hello can be called multiple times`() {
            assertDoesNotThrow {
                repeat(5) { hello() }
            }
        }
    }

    @Nested
    @Order(2)
    @DisplayName("Repository operations via FFI")
    inner class RepositoryOperations {

        @ParameterizedTest
        @ValueSource(strings = [SIMPLE_REPO, OCTO_REPO, ADVANCED_REPO])
        fun `findRepo should discover repository and return metadata`(repoName: String) {
            val repo = findRepo("${FIXTURES_PATH}/$repoName")

            assertAll(
                "Repository $repoName should be discovered with correct metadata",
                { assertThat(repo.gitDir).contains(".git") },
                { assertThat(repo.gitDir).contains(repoName) },
                { assertThat(repo.workTree).isNotNull() },
                { assertThat(repo.remotes).isNotNull() }
            )
        }

        @Test
        fun `findRepo with simple-repo should have origin remote`() {
            val repo = findRepo("${FIXTURES_PATH}/${SIMPLE_REPO}")

            assertAll(
                { assertThat(repo.remotes).isNotEmpty() },
                { assertThat(repo.remotes.map { it.name }).contains("origin") }
            )
        }

        @Test
        fun `findRepo with non-git directory should throw GixDiscoverException`() {
            val nonGitPath = Files.createTempDirectory(LocalDateTime.now().toString())

            val exception = assertThrows<UniffiException.GixDiscoverException> {
                findRepo(nonGitPath.toString())
            }

            assertThat(exception.message).contains(nonGitPath.toString())
        }

        @Test
        fun `findRepo with invalid path should throw GixDiscoverException`() {
            assertThrows<UniffiException.GixDiscoverException> {
                findRepo("/invalid/nonexistent/path")
            }
        }

        @Test
        fun `findRepo returns consistent data for multiple calls`() {
            val repo1 = findRepo("${FIXTURES_PATH}/${SIMPLE_REPO}")
            val repo2 = findRepo("${FIXTURES_PATH}/${SIMPLE_REPO}")

            assertAll(
                { assertThat(repo1.gitDir).isEqualTo(repo2.gitDir) },
                { assertThat(repo1.workTree).isEqualTo(repo2.workTree) },
                { assertThat(repo1.remotes.size).isEqualTo(repo2.remotes.size) }
            )
        }

        @Test
        fun `findRepo with work tree should populate workTree field`() {
            val repo = findRepo("${FIXTURES_PATH}/${SIMPLE_REPO}")

            assertAll(
                { assertThat(repo.workTree).isNotNull() },
                { assertThat(repo.workTree).contains(SIMPLE_REPO) },
                { assertThat(repo.workTree).doesNotContain(".git") }
            )
        }
    }

    @Nested
    @Order(3)
    @DisplayName("Branch operations via FFI")
    inner class BranchOperations {

        @ParameterizedTest
        @CsvSource(
            "${SIMPLE_REPO},2",
            "${OCTO_REPO},7",
            "${ADVANCED_REPO},8"
        )
        fun `findAllBranches should return all branches in repository`(repoName: String, expectedCount: Int) {
            val repo = findRepo("${FIXTURES_PATH}/$repoName")
            val branches = findAllBranches(repo)

            assertAll(
                "Repository $repoName should have $expectedCount branches",
                { assertThat(branches).hasSize(expectedCount) },
                { assertThat(branches).allMatch { it.name.isNotEmpty() } },
                { assertThat(branches).allMatch { it.fullName.isNotEmpty() } },
                { assertThat(branches).allMatch { it.target.isNotEmpty() } }
            )
        }

        @Test
        fun `findAllBranches should return both local and remote branches for simple-repo`() {
            val branches = findAllBranches(simpleRepo)
            val branchNames = branches.map(GixBranch::name)

            assertAll(
                { assertThat(branches).hasSize(2) },
                { assertThat(branchNames).contains("master") },
                { assertThat(branchNames).contains("origin/master") }
            )
        }

        @Test
        fun `findAllBranches should distinguish between local and remote branches`() {
            val branches = findAllBranches(simpleRepo)

            val localBranches = branches.filter { it.category == GixReferenceCategory.LOCAL_BRANCH }
            val remoteBranches = branches.filter { it.category == GixReferenceCategory.REMOTE_BRANCH }

            assertAll(
                { assertThat(localBranches).isNotEmpty() },
                { assertThat(remoteBranches).isNotEmpty() },
                { assertThat(localBranches.size + remoteBranches.size).isEqualTo(branches.size) }
            )
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.ffi.integration.FfiIntegrationTest#branchTestData")
        fun `traverseBranch should return branch with commits`(
            repoName: String,
            branchName: String,
            expectedCommitCount: Int
        ) {
            val repo = findRepo("${FIXTURES_PATH}/$repoName")
            val result = traverseBranch(
                repo, branchName,
                skipMerges = cfg.gix.skipMerges,
                useMailmap = cfg.gix.useMailmap
            )

            assertAll(
                "Branch $branchName in $repoName",
                { assertThat(result.branch.name).isEqualTo(normalizeBranchName(branchName)) },
                { assertThat(result.commits).hasSize(expectedCommitCount) },
                { assertThat(result.commits).allMatch { it.oid.isNotEmpty() } },
                { assertThat(result.commits).allMatch { it.message.isNotEmpty() } }
            )
        }

        @Test
        fun `traverseBranch should populate commit metadata correctly`() {
            val result = traverseBranch(
                simpleRepo, "refs/heads/master",
                skipMerges = cfg.gix.skipMerges,
                useMailmap = cfg.gix.useMailmap
            )

            assertAll(
                { assertThat(result.commits).allMatch { it.oid.length == 40 } },
                { assertThat(result.commits).allMatch { it.message.isNotBlank() } },
                { assertThat(result.commits).allMatch { it.author != null } },
                { assertThat(result.commits).allMatch { it.committer != null } }
            )
        }

        @Test
        fun `traverseBranch with non-existent branch should throw ReferenceException`() {
            assertThrows<UniffiException.ReferenceException> {
                traverseBranch(
                    simpleRepo, "refs/heads/nonexistent-branch",
                    skipMerges = cfg.gix.skipMerges,
                    useMailmap = cfg.gix.useMailmap
                )
            }
        }

        @Test
        fun `traverseBranch should populate parent relationships`() {
            val result = traverseBranch(
                simpleRepo, "refs/heads/master",
                skipMerges = cfg.gix.skipMerges,
                useMailmap = cfg.gix.useMailmap
            )

            val commitsWithParents = result.commits.filter { it.parents.isNotEmpty() }
            assertAll(
                { assertThat(commitsWithParents).isNotEmpty() },
                {
                    commitsWithParents.forEach { commit ->
                        assertThat(commit.parents).allMatch { it.length == 40 }
                    }
                }
            )
        }

        @Test
        fun `traverseBranch should handle merge commits with multiple parents`() {
            val result = traverseBranch(
                octoRepo, "refs/heads/master",
                skipMerges = cfg.gix.skipMerges,
                useMailmap = cfg.gix.useMailmap
            )

            val mergeCommits = result.commits.filter { it.parents.size > 1 }
            assertAll(
                { assertThat(mergeCommits).isNotEmpty() },
                {
                    mergeCommits.forEach { commit ->
                        assertThat(commit.parents.size).isGreaterThan(1)
                    }
                }
            )
        }

        @Test
        fun `traverseBranch should include file tree information`() {
            val result = traverseBranch(
                simpleRepo, "refs/heads/master",
                skipMerges = cfg.gix.skipMerges,
                useMailmap = cfg.gix.useMailmap
            )

            val commitsWithFiles = result.commits.filter { it.fileTree.isNotEmpty() }
            assertAll(
                { assertThat(commitsWithFiles).isNotEmpty() },
                {
                    commitsWithFiles.forEach { commit ->
                        assertThat(commit.fileTree).allMatch { it.isNotEmpty() }
                    }
                }
            )
        }
    }

    @Nested
    @Order(4)
    @DisplayName("Commit operations via FFI")
    inner class CommitOperations {

        @ParameterizedTest
        @CsvSource(
            "${SIMPLE_REPO},HEAD,b51199ab8b83e31f64b631e42b2ee0b1c7e3259a",
            "${SIMPLE_REPO},b51199ab8b83e31f64b631e42b2ee0b1c7e3259a,b51199ab8b83e31f64b631e42b2ee0b1c7e3259a",
            "${OCTO_REPO},HEAD,4dedc3c738eee6b69c43cde7d89f146912532cff",
            "${ADVANCED_REPO},HEAD,379dc91fb055ba385b5e5446428ffbe38804fa99"
        )
        fun `findCommit should return commit with correct SHA`(
            repoName: String,
            commitRef: String,
            expectedSha: String
        ) {
            val repo = findRepo("${FIXTURES_PATH}/$repoName")
            val commit = findCommit(
                repo, commitRef,
                useMailmap = cfg.gix.useMailmap
            )

            assertAll(
                { assertThat(commit.oid).isEqualTo(expectedSha) },
                { assertThat(commit.message).isNotBlank() },
                { assertThat(commit.author).isNotNull() },
                { assertThat(commit.committer).isNotNull() }
            )
        }

        @Test
        fun `findCommit should populate signature fields correctly`() {
            val commit = findCommit(
                simpleRepo, "HEAD",
                useMailmap = cfg.gix.useMailmap
            )

            assertAll(
                "Commit signatures",
                { assertThat(commit.author).isNotNull() },
                { assertThat(commit.author?.name).isNotBlank() },
                { assertThat(commit.author?.email).isNotBlank() },
                { assertThat(commit.committer).isNotNull() },
                { assertThat(commit.committer?.name).isNotBlank() },
                { assertThat(commit.committer?.email).isNotBlank() }
            )
        }

        @Test
        fun `findCommit with invalid SHA should throw RevisionParseException`() {
            assertThrows<UniffiException.RevisionParseException> {
                findCommit(
                    simpleRepo, "invalid-sha-format",
                    useMailmap = cfg.gix.useMailmap
                )
            }
        }

        @Test
        fun `findCommit with non-existent SHA should throw RevisionParseException`() {
            assertThrows<UniffiException.RevisionParseException> {
                findCommit(
                    simpleRepo, "0000000000000000000000000000000000000000",
                    useMailmap = cfg.gix.useMailmap
                )
            }
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.ffi.integration.FfiIntegrationTest#commitHistoryData")
        fun `traverseHistory should return correct number of commits`(
            repoName: String,
            startRef: String,
            targetSha: String?,
            expectedCount: Int
        ) {
            val repo = findRepo("${FIXTURES_PATH}/$repoName")
            val startCommit = findCommit(
                repo, startRef,
                useMailmap = cfg.gix.useMailmap
            )
            val commits = traverseHistory(
                repo, startCommit.oid, targetSha,
                useMailmap = cfg.gix.useMailmap
            )

            assertAll(
                "Traversing from $startRef in $repoName",
                { assertThat(commits).hasSize(expectedCount) },
                { assertThat(commits.first().oid).isEqualTo(startCommit.oid) },
                { assertThat(commits).allMatch { it.oid.length == 40 } }
            )
        }

        @Test
        fun `traverseHistory from initial commit should return single commit`() {
            val initialSha = "48a384a6a9188f376835005cd10fd97542e69bf7"
            val commits = traverseHistory(
                simpleRepo, initialSha, null,
                useMailmap = cfg.gix.useMailmap
            )

            assertAll(
                { assertThat(commits).hasSize(1) },
                { assertThat(commits.first().oid).isEqualTo(initialSha) },
                { assertThat(commits.first().parents).isEmpty() }
            )
        }

        @Test
        fun `traverseHistory with target should stop at target commit`() {
            val headCommit = findCommit(
                simpleRepo, "HEAD",
                useMailmap = cfg.gix.useMailmap
            )
            val targetSha = "48a384a6a9188f376835005cd10fd97542e69bf7"

            val commits = traverseHistory(
                simpleRepo, headCommit.oid, targetSha,
                useMailmap = cfg.gix.useMailmap
            )

            assertAll(
                { assertThat(commits).isNotEmpty() },
                { assertThat(commits.first().oid).isEqualTo(headCommit.oid) },
                { assertThat(commits.map { it.oid }).doesNotContain(targetSha) }
            )
        }

        @Test
        fun `traverseHistory should preserve commit order (newest first)`() {
            val commits = traverseHistory(
                simpleRepo, findCommit(
                    simpleRepo, "HEAD",
                    useMailmap = cfg.gix.useMailmap
                ).oid, null,
                useMailmap = cfg.gix.useMailmap
            )

            // Verify chronological order by checking that each commit's timestamp
            // is older than or equal to the previous commit
            val timestamps = commits.mapNotNull { it.committer?.time?.seconds }
            assertThat(timestamps.zipWithNext()).allMatch { (newer, older) ->
                newer >= older
            }
        }

        @Test
        fun `traverseHistory should handle merge commits correctly`() {
            val commits = traverseHistory(
                octoRepo, findCommit(
                    octoRepo, "HEAD",
                    useMailmap = cfg.gix.useMailmap
                ).oid, null,
                useMailmap = cfg.gix.useMailmap
            )

            val mergeCommits = commits.filter { it.parents.size > 1 }
            assertAll(
                { assertThat(mergeCommits).isNotEmpty() },
                { assertThat(commits).contains(*mergeCommits.toTypedArray()) }
            )
        }
    }

    @Nested
    @Order(5)
    @DisplayName("Diff operations via FFI")
    inner class DiffOperations {

        @Test
        fun `diffs should calculate diff for single commit pair`() {
            val headCommit = findCommit(
                simpleRepo, "HEAD",
                useMailmap = cfg.gix.useMailmap
            )
            val parentSha = headCommit.parents.firstOrNull()

            val diffInput = GixDiffInput(
                suspect = headCommit.oid,
                target = parentSha
            )

            val diffs = diffs(simpleRepo, listOf(diffInput), 1u, GixDiffAlgorithm.HISTOGRAM)

            assertAll(
                { assertThat(diffs).hasSize(1) },
                { assertThat(diffs.first().commit.oid).isEqualTo(headCommit.oid) },
                { assertThat(diffs.first().parent?.oid).isEqualTo(parentSha) },
                { assertThat(diffs.first().files).isNotEmpty() }
            )
        }

        @Test
        fun `diffs should handle multiple commit pairs`() {
            val commits = traverseHistory(
                simpleRepo, findCommit(
                    simpleRepo, "HEAD",
                    useMailmap = cfg.gix.useMailmap
                ).oid, null,
                useMailmap = cfg.gix.useMailmap
            )
                .take(3)

            val diffInputs = commits.mapNotNull { commit ->
                val parent = commit.parents.firstOrNull()
                if (parent != null) {
                    GixDiffInput(commit.oid, parent)
                } else null
            }

            val diffs = diffs(simpleRepo, diffInputs, 2u, GixDiffAlgorithm.HISTOGRAM)

            assertAll(
                { assertThat(diffs).hasSizeGreaterThanOrEqualTo(diffInputs.size) },
                { assertThat(diffs).allMatch { it.files.isNotEmpty() } }
            )
        }

        @Test
        fun `diffs should populate file change statistics`() {
            val headCommit = findCommit(
                simpleRepo, "HEAD",
                useMailmap = cfg.gix.useMailmap
            )
            val diffInput = GixDiffInput(headCommit.oid, headCommit.parents.firstOrNull())

            val diffs = diffs(simpleRepo, listOf(diffInput), 1u, GixDiffAlgorithm.HISTOGRAM)
            val fileDiffs = diffs.first().files

            assertAll(
                { assertThat(fileDiffs).isNotEmpty() },
                {
                    fileDiffs.forEach { file ->
                        assertThat(file.insertions).isGreaterThanOrEqualTo(0u)
                        assertThat(file.deletions).isGreaterThanOrEqualTo(0u)
                    }
                }
            )
        }

        @Test
        fun `diffs should identify change types correctly`() {
            val commits = traverseHistory(
                simpleRepo, findCommit(
                    simpleRepo, "HEAD",
                    useMailmap = cfg.gix.useMailmap
                ).oid, null,
                useMailmap = cfg.gix.useMailmap
            )
            val diffInputs = commits.take(5).mapNotNull { commit ->
                val parent = commit.parents.firstOrNull()
                if (parent != null) GixDiffInput(commit.oid, parent) else null
            }

            val diffs = diffs(simpleRepo, diffInputs, 2u, GixDiffAlgorithm.HISTOGRAM)

            val allChanges = diffs.flatMap { it.files }
            assertAll(
                { assertThat(allChanges).isNotEmpty() },
                {
                    allChanges.forEach { change ->
                        assertThat(change.change).isNotNull()
                    }
                }
            )
        }

        @Test
        fun `diffs with different algorithms should produce results`() {
            val headCommit = findCommit(
                simpleRepo, "HEAD",
                useMailmap = cfg.gix.useMailmap
            )
            val diffInput = GixDiffInput(headCommit.oid, headCommit.parents.firstOrNull())

            val algorithms = listOf(
                GixDiffAlgorithm.HISTOGRAM,
                GixDiffAlgorithm.MYERS,
                GixDiffAlgorithm.MYERS_MINIMAL
            )

            algorithms.forEach { algorithm ->
                val result = diffs(simpleRepo, listOf(diffInput), 1u, algorithm)
                assertThat(result).isNotEmpty()
            }
        }

        @Test
        fun `diffs should handle initial commit without parent`() {
            val initialSha = "48a384a6a9188f376835005cd10fd97542e69bf7"
            val diffInput = GixDiffInput(initialSha, null)

            val diffs = diffs(simpleRepo, listOf(diffInput), 1u, GixDiffAlgorithm.HISTOGRAM)

            assertAll(
                { assertThat(diffs).hasSize(1) },
                { assertThat(diffs.first().commit.oid).isEqualTo(initialSha) },
                { assertThat(diffs.first().parent).isNull() },
                { assertThat(diffs.first().files).isNotEmpty() }
            )
        }

        @Test
        fun `diffs with multiple threads should complete successfully`() {
            val commits = traverseHistory(
                simpleRepo, findCommit(
                    simpleRepo, "HEAD",
                    useMailmap = cfg.gix.useMailmap
                ).oid, null,
                useMailmap = cfg.gix.useMailmap
            )
            val diffInputs = commits.take(10).mapNotNull { commit ->
                val parent = commit.parents.firstOrNull()
                if (parent != null) GixDiffInput(commit.oid, parent) else null
            }

            val singleThreaded = diffs(simpleRepo, diffInputs, 1u, GixDiffAlgorithm.HISTOGRAM)
            val multiThreaded = diffs(simpleRepo, diffInputs, 4u, GixDiffAlgorithm.HISTOGRAM)

            assertAll(
                { assertThat(singleThreaded.size).isEqualTo(multiThreaded.size) },
                { assertThat(singleThreaded.map { it.commit }).containsAll(multiThreaded.map { it.commit }) }
            )
        }
    }

    @Nested
    @Order(6)
    @DisplayName("Blame operations via FFI")
    inner class BlameOperations {

        @Test
        fun `blames should calculate blame for single file in commit`() {
            val headCommit = findCommit(
                simpleRepo, "HEAD",
                useMailmap = cfg.gix.useMailmap
            )
            val filePath = "file2.txt"

            val defines = mapOf(headCommit.oid to listOf(filePath))

            val blames = blames(simpleRepo, defines, GixDiffAlgorithm.HISTOGRAM, 1u)

            assertAll(
                { assertThat(blames).hasSize(1) },
                { assertThat(blames.first().commit).isEqualTo(headCommit.oid) },
                { assertThat(blames.first().blames).isNotEmpty() }
            )
        }

        @Test
        fun `blames should populate blame entries correctly`() {
            val commit = findCommit(
                simpleRepo, "HEAD",
                useMailmap = cfg.gix.useMailmap
            )
            val filePath = "file2.txt"
            val defines = mapOf(commit.oid to listOf(filePath))

            val blames = blames(simpleRepo, defines, GixDiffAlgorithm.HISTOGRAM, 1u)
            val blameOutcome = blames.first().blames.first()

            assertAll(
                { assertThat(blameOutcome.filePath).isEqualTo(filePath) },
                { assertThat(blameOutcome.entries).isNotEmpty() },
                {
                    blameOutcome.entries.forEach { entry ->
                        assertThat(entry.commitId).isNotEmpty()
                        assertThat(entry.len).isGreaterThan(0u)
                    }
                }
            )
        }

        @Test
        fun `blames should handle multiple files in same commit`() {
            val commit = findCommit(
                simpleRepo, "HEAD",
                useMailmap = cfg.gix.useMailmap
            )
            val files = listOf("file2.txt", ".gitignore")

            val defines = mapOf(commit.oid to files)

            val blames = blames(simpleRepo, defines, GixDiffAlgorithm.HISTOGRAM, 1u)

            assertAll(
                { assertThat(blames).hasSize(1) },
                { assertThat(blames.first().blames).hasSizeGreaterThanOrEqualTo(1) }
            )
        }

        @Test
        fun `blames should handle multiple commits`() {
            val commits = traverseHistory(
                simpleRepo, findCommit(
                    simpleRepo, "HEAD",
                    useMailmap = cfg.gix.useMailmap
                ).oid, null,
                useMailmap = cfg.gix.useMailmap
            )
                .take(3)

            val defines = commits.associate { it.oid to listOf("file2.txt") }

            val blames = blames(simpleRepo, defines, GixDiffAlgorithm.HISTOGRAM, 2u)

            assertAll(
                { assertThat(blames).hasSizeGreaterThanOrEqualTo(1) },
                { assertThat(blames.map { it.commit }).containsAnyOf(*commits.map { it.oid }.toTypedArray()) }
            )
        }

        @Test
        fun `blames with different algorithms should produce results`() {
            val commit = findCommit(
                simpleRepo, "HEAD",
                useMailmap = cfg.gix.useMailmap
            )
            val defines = mapOf(commit.oid to listOf("file2.txt"))

            val algorithms = listOf(
                GixDiffAlgorithm.HISTOGRAM,
                GixDiffAlgorithm.MYERS,
                GixDiffAlgorithm.MYERS_MINIMAL
            )

            algorithms.forEach { algorithm ->
                val result = blames(simpleRepo, defines, algorithm, 1u)
                assertThat(result).isNotEmpty()
            }
        }

        @Test
        fun `blames should verify line ranges are valid`() {
            val commit = findCommit(
                simpleRepo, "HEAD",
                useMailmap = cfg.gix.useMailmap
            )
            val defines = mapOf(commit.oid to listOf("file2.txt"))

            val blames = blames(simpleRepo, defines, GixDiffAlgorithm.HISTOGRAM, 1u)
            val entries = blames.first().blames.first().entries

            assertAll(
                { assertThat(entries).isNotEmpty() },
                {
                    entries.forEach { entry ->
                        assertThat(entry.startInBlamedFile).isGreaterThanOrEqualTo(0u)
                        assertThat(entry.startInSourceFile).isGreaterThanOrEqualTo(0u)
                        assertThat(entry.len).isGreaterThan(0u)
                    }
                }
            )
        }
    }

    @Nested
    @Order(7)
    @DisplayName("Error handling across FFI boundary")
    inner class ErrorHandling {

        @Test
        fun `invalid repository path should throw appropriate exception`() {
            assertThrows<UniffiException.GixDiscoverException> {
                findRepo("/completely/invalid/path")
            }
        }

        @Test
        fun `invalid commit reference should throw RevisionParseException`() {
            assertThrows<UniffiException.RevisionParseException> {
                findCommit(
                    simpleRepo, "not-a-valid-ref",
                    useMailmap = cfg.gix.useMailmap
                )
            }
        }

        @Test
        fun `invalid branch reference should throw ReferenceException`() {
            assertThrows<UniffiException.ReferenceException> {
                traverseBranch(
                    simpleRepo, "refs/heads/does-not-exist",
                    skipMerges = cfg.gix.skipMerges,
                    useMailmap = cfg.gix.useMailmap
                )
            }
        }

        @Test
        fun `exception messages should be descriptive`() {
            val exception = assertThrows<UniffiException.GixDiscoverException> {
                findRepo("/invalid/path")
            }

            assertThat(exception.message)
                .isNotBlank()
                .contains("path")
        }

        @Test
        fun `operations on corrupted repository should fail gracefully`() {
            val tempDir = Files.createTempDirectory("corrupted-repo")
            Files.createDirectory(tempDir.resolve(".git"))

            assertThrows<UniffiException> {
                findRepo(tempDir.toString())
            }
        }
    }

    @Nested
    @Order(8)
    @DisplayName("Data marshalling and type conversion")
    inner class DataMarshalling {

        @Test
        fun `ObjectId should be correctly marshalled as SHA string`() {
            val commit = findCommit(
                simpleRepo, "HEAD",
                useMailmap = cfg.gix.useMailmap
            )

            assertAll(
                { assertThat(commit.oid).hasSize(40) },
                { assertThat(commit.oid).matches("[0-9a-f]{40}") }
            )
        }

        @Test
        fun `GixSignature should preserve all fields`() {
            val commit = findCommit(
                simpleRepo, "HEAD",
                useMailmap = cfg.gix.useMailmap
            )

            assertAll(
                "Author signature",
                { assertThat(commit.author).isNotNull() },
                { assertThat(commit.author?.name).isNotBlank() },
                { assertThat(commit.author?.email).isNotBlank() },
                { assertThat(commit.author?.time).isNotNull() },
                { assertThat(commit.author?.time?.seconds).isGreaterThan(0L) }
            )
        }

        @Test
        fun `GixBranch should preserve reference category`() {
            val branches = findAllBranches(simpleRepo)

            val categories = branches.map { it.category }.toSet()
            assertAll(
                { assertThat(categories).isNotEmpty() },
                { assertThat(categories).contains(GixReferenceCategory.LOCAL_BRANCH) }
            )
        }

        @Test
        fun `GixCommit parents should be marshalled as ObjectId list`() {
            val commits = traverseHistory(
                simpleRepo, findCommit(
                    simpleRepo, "HEAD",
                    useMailmap = cfg.gix.useMailmap
                ).oid, null,
                useMailmap = cfg.gix.useMailmap
            )
            val commitsWithParents = commits.filter { it.parents.isNotEmpty() }

            assertAll(
                { assertThat(commitsWithParents).isNotEmpty() },
                {
                    commitsWithParents.forEach { commit ->
                        assertThat(commit.parents).allMatch { it.length == 40 }
                    }
                }
            )
        }

        @Test
        fun `GixDiff should preserve all file change metadata`() {
            val headCommit = findCommit(
                simpleRepo, "HEAD",
                useMailmap = cfg.gix.useMailmap
            )
            val diffInput = GixDiffInput(headCommit.oid, headCommit.parents.firstOrNull())

            val diffs = diffs(simpleRepo, listOf(diffInput), 1u, GixDiffAlgorithm.HISTOGRAM)
            val fileDiffs = diffs.first().files

            assertAll(
                { assertThat(fileDiffs).isNotEmpty() },
                {
                    fileDiffs.forEach { file ->
                        assertThat(file.change).isNotNull()
                        when (file.change) {
                            is GixChangeType.Addition -> {
                                assertThat((file.change as GixChangeType.Addition).location).isNotEmpty()
                            }

                            is GixChangeType.Deletion -> {
                                assertThat((file.change as GixChangeType.Deletion).location).isNotEmpty()
                            }

                            is GixChangeType.Modification -> {
                                assertThat((file.change as GixChangeType.Modification).location).isNotEmpty()
                            }

                            is GixChangeType.Rewrite -> {
                                val rewrite = file.change as GixChangeType.Rewrite
                                assertThat(rewrite.location).isNotEmpty()
                                assertThat(rewrite.sourceLocation).isNotEmpty()
                            }
                        }
                    }
                }
            )
        }

        @Test
        fun `Optional fields should be correctly marshalled`() {
            val commits = traverseHistory(
                simpleRepo, findCommit(
                    simpleRepo, "HEAD",
                    useMailmap = cfg.gix.useMailmap
                ).oid, null,
                useMailmap = cfg.gix.useMailmap
            )

            val initialCommit = commits.find { it.parents.isEmpty() }
            assertThat(initialCommit).isNotNull()

            val diffInput = GixDiffInput(initialCommit!!.oid, null)
            val diffs = diffs(simpleRepo, listOf(diffInput), 1u, GixDiffAlgorithm.HISTOGRAM)

            assertAll(
                { assertThat(diffs.first().parent).isNull() },
                { assertThat(diffs.first().commit).isNotNull() }
            )
        }

        @Test
        fun `BString should be correctly marshalled to Kotlin String`() {
            val result = traverseBranch(
                simpleRepo, "refs/heads/master",
                skipMerges = cfg.gix.skipMerges,
                useMailmap = cfg.gix.useMailmap
            )
            val fileTree = result.commits.flatMap { it.fileTree }

            assertAll(
                { assertThat(fileTree).isNotEmpty() },
                { assertThat(fileTree).allMatch { it.isNotEmpty() } }
            )
        }

        @Test
        fun `GixRemote should marshal name and url correctly`() {
            val remotes = simpleRepo.remotes

            assertAll(
                { assertThat(remotes).isNotEmpty() },
                {
                    remotes.forEach { remote ->
                        assertThat(remote.name).isNotBlank()
                        // URL might be null for some remotes
                        if (remote.url != null) {
                            assertThat(remote.url).isNotBlank()
                        }
                    }
                }
            )
        }
    }
}
