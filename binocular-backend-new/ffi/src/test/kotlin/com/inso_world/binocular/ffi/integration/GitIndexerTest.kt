package com.inso_world.binocular.ffi.integration

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.index.GitIndexer
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest
import com.inso_world.binocular.ffi.BinocularFfiTestApplication
import com.inso_world.binocular.ffi.internal.UniffiException
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.ClassOrderer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestClassOrder
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
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
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import java.util.stream.Stream
import kotlin.io.path.Path

/**
 * Comprehensive integration tests for [com.inso_world.binocular.core.index.GitIndexer].
 *
 * Tests the GitIndexer component which wraps the FFI layer for Git operations, verifying:
 * - Repository discovery and initialization
 * - Branch traversal and retrieval
 * - Commit finding and traversal
 * - Exception handling and error scenarios
 *
 * ### Test Organization
 * Tests are organized into nested classes by functionality:
 * - [RepositoryOperations]: Repository discovery and validation
 * - [BranchOperations]: Branch finding and traversal
 * - [CommitOperations]: Commit finding and history traversal
 * - [ErrorHandling]: Exception handling and error scenarios
 * - [Integration]: Full workflow integration scenarios
 */
@TestClassOrder(ClassOrderer.OrderAnnotation::class)
@SpringBootTest(
    classes = [BinocularFfiTestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
@ExtendWith(SpringExtension::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
internal open class GitIndexerTest : BaseFixturesIntegrationTest() {

    @Autowired
    private lateinit var indexer: GitIndexer

    private lateinit var project: Project

    @BeforeEach
    fun setUp() {
        project = Project(name = "test project")
    }

    @Nested
    @DisplayName("Repository operations")
    inner class RepositoryOperations {

        @ParameterizedTest
        @ValueSource(strings = [SIMPLE_REPO, OCTO_REPO, ADVANCED_REPO])
        fun `findRepo should locate existing repositories`(repoName: String) {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/$repoName"), project)

            assertAll(
                "Repository $repoName should be found and configured correctly",
                {
                    assertThat(Paths.get(repo.localPath).toString()).isEqualTo(
                        Paths.get("${FIXTURES_PATH}/$repoName/.git").toString(),
                    )
                },
                { assertThat(repo.project).isSameAs(project) },
                { assertThat(repo.commits).isEmpty() },
                { assertThat(repo.branches).isEmpty() }
            )
        }

        @Test
        fun `findRepo with non-git directory should throw exception`() {
            val nonGitPath = Files.createTempDirectory(LocalDateTime.now().toString())

            val e = assertThrows<UniffiException.GixDiscoverException> {
                indexer.findRepo(nonGitPath, project)
            }
            assertThat(e.message).contains(nonGitPath.toString())
        }

        @Test
        fun `findRepo should create separate Repository instances for different projects`() {
            val project1 = Project(name = "project1")
            val project2 = Project(name = "project2")

            val repo1 = indexer.findRepo(Path("${FIXTURES_PATH}/${SIMPLE_REPO}"), project1)
            val repo2 = indexer.findRepo(Path("${FIXTURES_PATH}/${SIMPLE_REPO}"), project2)

            assertAll(
                { assertThat(repo1).isNotSameAs(repo2) },
                { assertThat(repo1.project).isSameAs(project1) },
                { assertThat(repo2.project).isSameAs(project2) },
                { assertThat(repo1.localPath).isEqualTo(repo2.localPath) }
            )
        }
    }

    @Nested
    @DisplayName("Branch operations")
    @Order(Int.MAX_VALUE)
    @TestMethodOrder(MethodOrderer.OrderAnnotation::class)
    inner class BranchOperations {

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.ffi.integration.GitIndexerTest#findAllBranchesData")
        fun `findAllBranches should return all branches for repository`(
            repoName: String,
            localBranches: Collection<String>,
            remoteBranches: Collection<String>,
            expectedBranchCount: Int
        ) {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/$repoName"), project)
            val branches = indexer.findAllBranches(repo)
            val actualBranchNames = branches.map { it.name }
            val expectedLocalNames = localBranches.map(::normalizeBranchName)
            val expectedRemoteNames = remoteBranches.map(::normalizeBranchName)

            assertAll(
                "Repository $repoName should have correct branches",
                { assertThat(branches).isNotEmpty() },
                { assertThat(branches).hasSize(expectedBranchCount) },
                { assertThat(actualBranchNames).containsAll(expectedLocalNames) },
                { assertThat(actualBranchNames).containsAll(expectedRemoteNames) },
            )
        }

        @ParameterizedTest
        @CsvSource(
            "${SIMPLE_REPO},refs/heads/master,14",
            "${SIMPLE_REPO},refs/remotes/origin/master,13",
            // OCTO
            "${OCTO_REPO},refs/heads/master,19",
            "${OCTO_REPO},refs/heads/octo1,16",
            "${OCTO_REPO},refs/heads/octo2,16",
            "${OCTO_REPO},refs/heads/octo3,16",
            "${OCTO_REPO},refs/heads/bugfix,17",
            "${OCTO_REPO},refs/heads/feature,17",
            "${OCTO_REPO},refs/heads/imported,1",
            // ADVANCED
            "${ADVANCED_REPO},refs/heads/master,35",
            "${ADVANCED_REPO},refs/heads/imported,4"
        )
        fun `traverseBranch should return branch with correct commit count`(
            repoName: String,
            branchName: String,
            expectedCommitCount: Int
        ) {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/$repoName"), project)
            val (branch, commits) = indexer.traverseBranch(repo, branchName)

            assertAll(
                "Branch $branchName in $repoName should have correct structure",
                { assertThat(branch.name).isEqualTo(normalizeBranchName(branchName)) },
                { assertThat(branch.repository).isSameAs(repo) },
                { assertThat(commits).hasSize(expectedCommitCount) },
                { assertThat(commits.map { it.repository }).doesNotContainNull() },
                { assertThat(commits).allSatisfy { assertThat(it.repository).isSameAs(repo) } },
                { assertThat(branch.head).isIn(commits) },
                { assertThat(branch.commits).containsOnly(*commits.toTypedArray()) },
                { assertThat(repo.commits).containsOnly(*commits.toTypedArray()) },
                { assertThat(repo.branches).containsOnly(branch) }
            )
        }

        @Test
        fun `traverseBranch with non-existent branch should throw exception`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${SIMPLE_REPO}"), project)

            assertThrows<UniffiException.ReferenceException> {
                indexer.traverseBranch(repo, "non-existent-branch")
            }
        }

        @Test
        fun `traverseBranch same branch twice should not duplicate in repository`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${SIMPLE_REPO}"), project)

            indexer.traverseBranch(repo, "refs/remotes/origin/master")

            assertAll(
                "First traversal",
                { assertThat(repo.branches).hasSize(1) },
                { assertThat(repo.commits).hasSize(13) },
            )

            indexer.traverseBranch(repo, "origin/master")

            assertAll(
                "Second traversal should not create duplicates",
                { assertThat(repo.branches).hasSize(1) },
                { assertThat(repo.commits).hasSize(13) },
            )
        }

        @Test
        fun `traverseBranch different branches should add commits correctly`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${SIMPLE_REPO}"), project)

            val (originMaster, originCommits) = indexer.traverseBranch(repo, "refs/remotes/origin/master")

            assertAll(
                "First branch traversal",
                { assertThat(repo.branches).hasSize(1) },
                { assertThat(repo.commits).hasSize(13) },
                { assertThat(originCommits).hasSize(13) },
                { assertThat(originMaster.commits).hasSize(13) },
                { assertThat(repo.commits).containsExactlyInAnyOrder(*originMaster.commits.toTypedArray()) }
            )

            val (master, masterCommits) = indexer.traverseBranch(repo, "refs/heads/master")

            assertAll(
                "Second branch with additional commit",
                { assertThat(repo.branches).hasSize(2) },
                { assertThat(repo.commits).hasSize(14) },
                { assertThat(masterCommits).hasSize(14) },
                { assertThat(master.commits).hasSize(14) },
                { assertThat(repo.commits).containsExactlyInAnyOrder(*master.commits.toTypedArray()) }
            )
        }

        @Test
        @Order(Int.MAX_VALUE)
        fun `traverseBranch after adding commit should update branch head`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${SIMPLE_REPO}"), project)

            run {
                val result = indexer.traverseBranch(repo, "refs/heads/master")

                assertAll(
                    "Initial state",
                    { assertThat(repo.branches).hasSize(1) },
                    { assertThat(repo.commits).hasSize(14) },
                    { assertThat(result.first.commits).hasSize(14) },
                    { assertThat(result.first.head.sha).isEqualTo("b51199ab8b83e31f64b631e42b2ee0b1c7e3259a") },
                    { assertThat(repo.branches.first().head).isSameAs(result.first.head) },
                )
            }

            addCommit()

            run {
                val result = indexer.traverseBranch(repo, "refs/heads/master")

                assertAll(
                    "After adding commit",
                    { assertThat(repo.branches).hasSize(1) },
                    { assertThat(repo.commits).hasSize(15) },
                    { assertThat(result.first.commits).hasSize(15) },
                    { assertThat(result.first.head.sha).isEqualTo("ab32b11aed9f1760aa7a40391719201e5e76b443") },
                    { assertThat(repo.branches.first().head).isSameAs(result.first.head) },
                )
            }
        }
    }

    @Nested
    @DisplayName("Commit operations")
    inner class CommitOperations {

        @ParameterizedTest
        @CsvSource(
            "${SIMPLE_REPO},b51199ab8b83e31f64b631e42b2ee0b1c7e3259a",
            "${OCTO_REPO},4dedc3c738eee6b69c43cde7d89f146912532cff",
            "${ADVANCED_REPO},379dc91fb055ba385b5e5446428ffbe38804fa99"
        )
        fun `findCommit with HEAD should return correct commit`(repoName: String, expectedSha: String) {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/$repoName"), project)
            val commit = indexer.findCommit(repo, "HEAD")

            assertAll(
                "HEAD commit for $repoName",
                { assertThat(commit).isNotNull() },
                { assertThat(commit.sha).isEqualTo(expectedSha) },
                { assertThat(commit.repository).isSameAs(repo) }
            )
        }

        @ParameterizedTest
        @CsvSource(
            "${SIMPLE_REPO},48a384a6a9188f376835005cd10fd97542e69bf7",
            "${OCTO_REPO},d16fb2d78e3d867377c078a03aadc5aa34bdb408",
            "${ADVANCED_REPO},5c81ebfb36467b8d1f70295adf2f9ae5a93a2c33"
        )
        fun `findCommit with specific SHA should return correct commit`(repoName: String, sha: String) {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/$repoName"), project)
            val commit = indexer.findCommit(repo, sha)

            assertAll(
                { assertThat(commit).isNotNull() },
                { assertThat(commit.sha).isEqualTo(sha) },
                { assertThat(commit.repository).isSameAs(repo) }
            )
        }

        @Test
        fun `findCommit with invalid SHA should throw exception`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${SIMPLE_REPO}"), project)

            assertThrows<Exception> {
                indexer.findCommit(repo, "invalid-sha-that-does-not-exist")
            }
        }

        @Test
        fun `findCommit should return same instance for same SHA in same repository`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${SIMPLE_REPO}"), project)
            val sha = "48a384a6a9188f376835005cd10fd97542e69bf7"

            val commit1 = indexer.findCommit(repo, sha)
            val commit2 = indexer.findCommit(repo, sha)

            assertAll(
                { assertThat(commit1.sha).isEqualTo(commit2.sha) },
                { assertThat(commit1.repository).isSameAs(commit2.repository) }
            )
        }

        @ParameterizedTest
        @CsvSource(
            "${SIMPLE_REPO},HEAD,14",
            "${SIMPLE_REPO},b51199ab8b83e31f64b631e42b2ee0b1c7e3259a,14",
            "${SIMPLE_REPO},3d28b65c324cc8ee0bb7229fb6ac5d7f64129e90,13",
            "${SIMPLE_REPO},2403472fd3b2c4487f66961929f1e5895c5013e1,9",
            "${SIMPLE_REPO},48a384a6a9188f376835005cd10fd97542e69bf7,1",
            // OCTO
            "${OCTO_REPO},HEAD,19",
            "${OCTO_REPO},4dedc3c738eee6b69c43cde7d89f146912532cff,19",
            "${OCTO_REPO},f556329d268afeb5e5298e37fd8bfb5ef2058a9d,15",
            "${OCTO_REPO},bf51258d6da9aaca9b75e2580251539026b6246a,16",
            "${OCTO_REPO},d5d38cc858bd78498efbe0005052f5cb1fd38cb9,16",
            "${OCTO_REPO},42fbbe93509ed894cbbd61e4dbc07a440720c491,16",
            "${OCTO_REPO},d16fb2d78e3d867377c078a03aadc5aa34bdb408,17",
            "${OCTO_REPO},3e15df55908eefdb720a7bc78065bcadb6b9e9cc,17",
            // ADVANCED
            "${ADVANCED_REPO},HEAD,35",
            "${ADVANCED_REPO},379dc91fb055ba385b5e5446428ffbe38804fa99,35",
            "${ADVANCED_REPO},3c47b3a6ba6811bcefd21203809d79b2aa1b4b4b,34",
            "${ADVANCED_REPO},82df82770ef416d66c52b383281d21e03376fde0,29",
            "${ADVANCED_REPO},09aa9cb6a6322b4ba4506f168b944f0045b11cbe,4",
            "${ADVANCED_REPO},ed167f854e871a1566317302c158704f71f8d16c,1",
            "${ADVANCED_REPO},5c81ebfb36467b8d1f70295adf2f9ae5a93a2c33,1"
        )
        fun `traverse from commit should return correct number of commits`(
            repoName: String,
            startSha: String,
            expectedCount: Int
        ) {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/$repoName"), project)
            val startCommit = indexer.findCommit(repo, startSha)
            val commits = indexer.traverse(repo, startCommit, null)

            assertAll(
                "Traversing from $startSha in $repoName",
                { assertThat(commits).isNotEmpty() },
                { assertThat(commits).hasSize(expectedCount) },
                { assertThat(commits).allSatisfy { assertThat(it.repository).isSameAs(repo) } },
                { assertThat(commits.first()).isSameAs(startCommit) }
            )
        }

        @Test
        fun `traverse from initial commit should return single commit`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${SIMPLE_REPO}"), project)
            val initialCommit = indexer.findCommit(repo, "48a384a6a9188f376835005cd10fd97542e69bf7")
            val commits = indexer.traverse(repo, initialCommit, null)

            assertAll(
                { assertThat(commits).hasSize(1) },
                { assertThat(commits.first()).isSameAs(initialCommit) },
                { assertThat(commits.first().parents).isEmpty() }
            )
        }

        @Test
        fun `traverse with source and target should return commits between them`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${SIMPLE_REPO}"), project)
            val head = indexer.findCommit(repo, "HEAD")
            val target = indexer.findCommit(repo, "48a384a6a9188f376835005cd10fd97542e69bf7")

            val commits = indexer.traverse(repo, head, target)

            assertAll(
                { assertThat(commits).isNotEmpty() },
                { assertThat(commits.first()).isSameAs(head) },
                { assertThat(commits).doesNotContain(target) }
            )
        }

        @Test
        fun `traverse should populate commit relationships`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${SIMPLE_REPO}"), project)
            val head = indexer.findCommit(repo, "HEAD")
            val commits = indexer.traverse(repo, head, null)

            val commitsWithParents = commits.filter { it.parents.isNotEmpty() }
            assertAll(
                { assertThat(commitsWithParents).isNotEmpty() },
                {
                    assertThat(commitsWithParents).allSatisfy { commit ->
                        commit.parents.forEach { parent ->
                            assertThat(parent.repository).isSameAs(repo)
                            assertThat(commits).contains(parent)
                        }
                    }
                }
            )
        }
    }

    @Nested
    @DisplayName("Error handling")
    inner class ErrorHandling {

        @Test
        fun `operations on invalid repository should fail gracefully`() {
            val invalidRepo = Repository(
                localPath = "/invalid/path",
                project = project
            )

            assertAll(
                {
                    assertThrows<UniffiException.GixDiscoverException> {
                        indexer.traverseBranch(invalidRepo, "refs/heads/master")
                    }
                },
                {
                    assertThrows<UniffiException.GixDiscoverException> {
                        indexer.findCommit(invalidRepo, "HEAD")
                    }
                },
                {
                    assertThrows<UniffiException.GixDiscoverException> {
                        indexer.findAllBranches(invalidRepo)
                    }
                }
            )
        }
    }

    @Nested
    @DisplayName("Integration scenarios")
    inner class Integration {

        @Test
        fun `complete workflow - Binocular`() {
            val repo = indexer.findRepo(Path("./"), project)
            assertThat(repo).isNotNull()

            val (branch, branchCommits) = indexer.traverseBranch(repo, "origin/main")
            assertAll(
                { assertThat(branchCommits).hasSize(1881) }
            )
        }

        @Test
        fun `complete workflow - find repo, traverse branch, find commits`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${SIMPLE_REPO}"), project)
            assertThat(repo).isNotNull()

            val (branch, branchCommits) = indexer.traverseBranch(repo, "refs/heads/master")
            assertAll(
                { assertThat(branch).isNotNull() },
                { assertThat(branchCommits).hasSize(14) },
                { assertThat(repo.branches).containsOnly(branch) },
                { assertThat(repo.commits).hasSize(14) }
            )

            val specificCommit = indexer.findCommit(repo, "48a384a6a9188f376835005cd10fd97542e69bf7")
            assertAll(
                { assertThat(specificCommit).isNotNull() },
                { assertThat(branchCommits).contains(specificCommit) }
            )

            val commitsFromSpecific = indexer.traverse(repo, specificCommit, null)
            assertAll(
                { assertThat(commitsFromSpecific).hasSize(1) },
                { assertThat(commitsFromSpecific.first()).isSameAs(specificCommit) }
            )

            val allBranches = indexer.findAllBranches(repo)
            assertAll(
                { assertThat(allBranches).hasSize(2) },
                {
                    assertThat(allBranches.map { it.name }).containsExactlyInAnyOrder(
                        normalizeBranchName("refs/heads/master"),
                        normalizeBranchName("refs/remotes/origin/master")
                    )
                }
            )
        }

        @Test
        fun `multiple repositories should be independent`() {
            val project1 = Project(name = "project1")
            val project2 = Project(name = "project2")

            val repo1 = indexer.findRepo(Path("${FIXTURES_PATH}/${SIMPLE_REPO}"), project1)
            val repo2 = indexer.findRepo(Path("${FIXTURES_PATH}/${OCTO_REPO}"), project2)

            val (branch1, commits1) = indexer.traverseBranch(repo1, "refs/heads/master")
            val (branch2, commits2) = indexer.traverseBranch(repo2, "refs/heads/master")

            assertAll(
                "Repositories should be independent",
                { assertThat(repo1).isNotSameAs(repo2) },
                { assertThat(repo1.project).isNotSameAs(repo2.project) },
                { assertThat(branch1.repository).isSameAs(repo1) },
                { assertThat(branch2.repository).isSameAs(repo2) },
                { assertThat(commits1).hasSize(14) },
                { assertThat(commits2).hasSize(19) },
                { assertThat(commits1).allSatisfy { assertThat(it.repository).isSameAs(repo1) } },
                { assertThat(commits2).allSatisfy { assertThat(it.repository).isSameAs(repo2) } }
            )
        }

        @Test
        fun `traversing multiple branches in same repository should build complete commit graph`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${OCTO_REPO}"), project)

            val branchNames = listOf("master", "octo1", "octo2", "octo3", "bugfix", "feature").map { "refs/heads/$it" }
            val branches = branchNames.map { indexer.traverseBranch(repo, it).first }

            assertAll(
                "All branches should be in repository",
                { assertThat(repo.branches).hasSizeGreaterThanOrEqualTo(branchNames.size) },
                { assertThat(repo.branches).containsAll(branches) },
                { assertThat(repo.commits).isNotEmpty() },
                {
                    branches.forEach { branch ->
                        assertThat(repo.commits).containsAll(branch.commits)
                    }
                }
            )
        }

        @Test
        fun `commit parent relationships should be correctly established across branches`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${OCTO_REPO}"), project)
            val (masterBranch, _) = indexer.traverseBranch(repo, "refs/heads/master")

            val mergeCommits = masterBranch.commits.filter { it.parents.size > 2 }

            assertAll(
                { assertThat(mergeCommits).isNotEmpty() },
                {
                    mergeCommits.forEach { merge ->
                        assertAll(
                            "Merge commit ${merge.sha} parents should be valid",
                            { assertThat(merge.parents).hasSizeGreaterThan(2) },
                            {
                                merge.parents.forEach { parent ->
                                    assertThat(parent.repository).isSameAs(repo)
                                    assertThat(masterBranch.commits).contains(parent)
                                }
                            }
                        )
                    }
                }
            )
        }
    }

    @Nested
    inner class CompareToGit {
        @ParameterizedTest
        @CsvSource(
            value = [
                "origin/main",
                "origin/develop"
            ]
        )
        fun `Binocular, traverse branch, check committer`(branchName: String) {
            logger.info(branchName)
            val repo = indexer.findRepo(Path("./"), project)
            assertThat(repo).isNotNull()

            val (_, branchCommits) = indexer.traverseBranch(repo, branchName)
            // localPath points to .git directory, so get the parent for git commands
            val repoDir = File(repo.localPath).parentFile

            val committerGroups = branchCommits.groupBy { it.committer }
            for ((committer, commits) in committerGroups) {
                val gitLogProcess = ProcessBuilder(
                    "git", "log", "--use-mailmap", "--pretty=format:'%cN <%cE>'",
                    branchName
                )
                    .directory(repoDir)
                    .redirectErrorStream(true)
                    .start()
                logger.debug("{}", gitLogProcess.info())

                val lineCount = gitLogProcess.inputStream.bufferedReader().useLines { lines ->
                    lines.count { it.contains(committer.email.orEmpty()) }
                }
                gitLogProcess.waitFor(5, TimeUnit.SECONDS)

                logger.info("Committer: ${committer.email} - Commits: $lineCount")
                assertThat(committer.committedCommits.size).isEqualTo(lineCount)
            }
        }

        @ParameterizedTest
        @CsvSource(
            value = [
                "origin/main",
                "origin/develop"
            ]
        )
        fun `Binocular, traverse branch, check authors`(branchName: String) {
            logger.info(branchName)
            val repo = indexer.findRepo(Path("./"), project)
            assertThat(repo).isNotNull()

            val (_, branchCommits) = indexer.traverseBranch(repo, branchName)
            // localPath points to .git directory, so get the parent for git commands
            val repoDir = File(repo.localPath).parentFile

            val authorGroups = branchCommits.filter { it.author != null }.groupBy { requireNotNull(it.author) }
            for ((author, commits) in authorGroups) {
                val gitLogProcess = ProcessBuilder(
                    "git", "log", "--use-mailmap", "--pretty=format:'%aN <%aE>'",
                    branchName
                )
                    .directory(repoDir)
                    .redirectErrorStream(true)
                    .start()
                logger.debug("{}", gitLogProcess.info())

                val lineCount = gitLogProcess.inputStream.bufferedReader().useLines { lines ->
                    lines.count { it.contains(author.email.orEmpty()) }
                }
                gitLogProcess.waitFor(5, TimeUnit.SECONDS)

                logger.info("Author: ${author.email} - Commits: $lineCount")
                assertAll(
                    { assertThat(author.authoredCommits).hasSameSizeAs(commits) },
                    { assertThat(author.authoredCommits).containsExactlyInAnyOrder(*commits.toTypedArray()) },
                    { assertThat(author.authoredCommits.size).isEqualTo(lineCount) }
                )
            }
        }
    }

    companion object {
        private val logger by logger()

        private fun normalizeBranchName(refName: String): String =
            refName
                .removePrefix("refs/heads/")
                .removePrefix("refs/remotes/")
                .removePrefix("refs/tags/")

        @JvmStatic
        fun findAllBranchesData(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    SIMPLE_REPO,
                    listOf("refs/heads/master"),
                    listOf("refs/remotes/origin/master"),
                    2
                ),
                Arguments.of(
                    OCTO_REPO,
                    listOf(
                        "bugfix",
                        "feature",
                        "imported",
                        "master",
                        "octo1",
                        "octo2",
                        "octo3"
                    ).map { "refs/heads/$it" },
                    emptyList<String>(),
                    7
                ),
                Arguments.of(
                    ADVANCED_REPO,
                    listOf(
                        "bugfix",
                        "extra",
                        "feature",
                        "imported",
                        "master",
                        "octo1",
                        "octo2",
                        "octo3"
                    ).map { "refs/heads/$it" },
                    emptyList<String>(),
                    8
                )
            )
    }
}
