package com.inso_world.binocular.cli.integration.commands

import com.inso_world.binocular.cli.BinocularCommandLineApplication
import com.inso_world.binocular.cli.commands.Index
import com.inso_world.binocular.cli.service.RepositoryService
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest
import com.inso_world.binocular.core.integration.base.InfrastructureDataSetup
import com.inso_world.binocular.infrastructure.sql.SqlTestConfig
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

@SpringBootTest(
    classes = [BinocularCommandLineApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
@ContextConfiguration(
    initializers = [
        SqlTestConfig.Initializer::class,
    ]
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
internal class VcsIndexCommandsTest() : BaseFixturesIntegrationTest() {

    @all:Autowired
    private lateinit var infrastructureDataSetup: InfrastructureDataSetup

    @all:Autowired
    private lateinit var repoService: RepositoryService

    @all:Autowired
    private lateinit var idxClient: Index


    companion object {
        val BINOCULAR_REPO_PATH = Path("../../.git")
        @JvmStatic
        @BeforeAll
        fun checkPath() {
            assertThat(BINOCULAR_REPO_PATH).exists()
        }
    }

    @BeforeEach
    fun setup() {
        cleanup()
    }

    @AfterEach
    fun cleanup() {
        this.infrastructureDataSetup.teardown()
    }

    /**
     * Helper to create a test commit with the new Signature-based constructor.
     */
    private fun createTestCommit(
        sha: String,
        message: String?,
        repository: Repository,
        developerName: String = "Test User",
        developerEmail: String = "test@example.com",
        timestamp: LocalDateTime = LocalDateTime.now().minusHours(1)
    ): Commit {
        val developer = Developer(
            name = developerName,
            email = developerEmail,
            repository = repository
        )
        val signature = Signature(developer = developer, timestamp = timestamp)
        return Commit(
            sha = sha,
            message = message,
            authorSignature = signature,
            repository = repository,
        )
    }

    @Nested
    inner class BinocularRepo {

        @BeforeEach
        fun `cleanup inner`() {
            cleanup()
        }

        @Test
        fun `index branch origin-feature-5`() {
            idxClient.commits(
                repoPath = BINOCULAR_REPO_PATH.absolutePathString(),
                branchName = "origin/feature/5",
                projectName = "Binocular",
            )
        }

        @Test
        fun `index branch origin-main`() {
            idxClient.commits(
                repoPath = BINOCULAR_REPO_PATH.absolutePathString(),
                branchName = "origin/main",
                projectName = "Binocular",
            )

            val repo = repoService.findRepo(BINOCULAR_REPO_PATH.absolutePathString())
            assertNotNull(repo)
            assertThat(repo.commits).hasSize(1881)
        }

        @Test
        fun `index branch origin-develop`() {
            idxClient.commits(
                repoPath = BINOCULAR_REPO_PATH.absolutePathString(),
                branchName = "origin/develop",
                projectName = "Binocular",
            )

            val repo = repoService.findRepo(BINOCULAR_REPO_PATH.absolutePathString())
            assertNotNull(repo)
            assertThat(repo.commits).hasSize(2310)
        }

        @Test
        fun `index branch origin-feature-6`() {
            idxClient.commits(
                repoPath = BINOCULAR_REPO_PATH.absolutePathString(),
                branchName = "origin/feature/6",
                projectName = "Binocular",
            )
        }

        @Test
        @Disabled
        fun `index branch origin-feature-6, calculate diffs`() {
            val projectName = "Binocular"
            idxClient.commits(
                repoPath = BINOCULAR_REPO_PATH.absolutePathString(),
                branchName = "origin/feature/6",
                projectName = projectName,
            )
            idxClient.diffs(projectName)
        }

        @Test
        fun `index branch origin-feature-6 and then origin-feature-5`() {
            idxClient.commits(
                repoPath = BINOCULAR_REPO_PATH.absolutePathString(),
                branchName = "origin/feature/6",
                projectName = "Binocular",
            )
//            assertThat(repoService.f?.commits).hasSize(207)
            assertDoesNotThrow {
                idxClient.commits(
                    repoPath = BINOCULAR_REPO_PATH.absolutePathString(),
                    branchName = "origin/feature/5",
                    projectName = "Binocular",
                )
            }
//            assertThat(repoService.findRepo(path)?.commits).hasSize(219)
        }

        @Test
        fun `index branch origin-feature-6 and then again`() {
            idxClient.commits(
                repoPath = BINOCULAR_REPO_PATH.absolutePathString(),
                branchName = "origin/feature/6",
                projectName = "Binocular",
            )
//            assertThat(repoService.f?.commits).hasSize(207)
            assertDoesNotThrow {
                idxClient.commits(
                    repoPath = BINOCULAR_REPO_PATH.absolutePathString(),
                    branchName = "origin/feature/6",
                    projectName = "Binocular",
                )
            }
//            assertThat(repoService.findRepo(path)?.commits).hasSize(219)
        }

        @Test
        fun `index branch origin-feature-6 and then origin-feature-9`() {
            idxClient.commits(
                repoPath = BINOCULAR_REPO_PATH.absolutePathString(),
                branchName = "origin/feature/6",
                projectName = "Binocular",
            )
//            assertThat(repoService.findRepo(path)?.commits).hasSize(207)
            assertDoesNotThrow {
                idxClient.commits(
                    repoPath = BINOCULAR_REPO_PATH.absolutePathString(),
                    branchName = "origin/feature/9",
                    projectName = "Binocular",
                )
            }
//            assertThat(repoService.findRepo(path)?.commits).hasSize(219)
        }
    }

    @Nested
    inner class OctoRepo {
        @BeforeEach
        fun `cleanup inner`() {
            cleanup()
        }

        @Test
        fun `index branch master`() {
            assertDoesNotThrow {
                idxClient.commits(
                    repoPath = "$FIXTURES_PATH/$OCTO_REPO",
                    branchName = "master",
                    projectName = OCTO_PROJECT_NAME,
                )
            }
            val repo = repoService.findRepo("$FIXTURES_PATH/$OCTO_REPO")
                ?: throw IllegalStateException("repo cannot be null here")
            assertAll(
                "check numbers",
                { assertThat(repo).isNotNull() },
                { assertThat(repo.branches).isNotEmpty() },
                { assertThat(repo.branches).hasSize(1) },
                { assertThat(repo.branches.map { it.name }).containsOnly("master") },
                { assertThat(repo.commits).isNotEmpty() },
                { assertThat(repo.commits).hasSize(19) },
                { assertThat(repo.developers).hasSize(3) },
            )
        }
    }

    @Nested
    inner class SimpleRepo {

        @BeforeEach
        fun setup() {
            cleanup()
        }

        @ParameterizedTest
        @CsvSource(
            "master,14",
            "origin/master,13",
        )
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        fun `index commits -b master`(
            branchName: String,
            noOfCommits: Int,
        ) {
            idxClient.commits(
                repoPath = "$FIXTURES_PATH/$SIMPLE_REPO",
                branchName = branchName,
                projectName = SIMPLE_PROJECT_NAME,
            )

            val repo = this@VcsIndexCommandsTest.repoService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")
            assertAll(
                { assertThat(repo).isNotNull() },
                { assertThat(repo?.branches).isNotEmpty() },
                { assertThat(repo?.branches).hasSize(1) },
                { assertThat(repo?.branches?.map { it.name }).contains(branchName) },
                { assertThat(repo?.commits).isNotEmpty() },
                { assertThat(repo?.commits).hasSize(noOfCommits) },
                { assertThat(repo?.developers).hasSize(3) },
            )
        }

        @Test
        fun `repeated commit indexing with different branches`() {
            idxClient.commits(
                repoPath = "$FIXTURES_PATH/$SIMPLE_REPO",
                branchName = "origin/master",
                projectName = SIMPLE_PROJECT_NAME,
            )

            val repo1 = this@VcsIndexCommandsTest.repoService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")

            assertAll(
                { assertThat(repo1).isNotNull() },
                { assertThat(repo1?.branches).isNotEmpty() },
                { assertThat(repo1?.branches).hasSize(1) },
                { assertThat(repo1?.branches?.map { it.name }).contains("origin/master") },
                { assertThat(repo1?.commits).isNotEmpty() },
                { assertThat(repo1?.commits).hasSize(13) },
                { assertThat(repo1?.developers).hasSize(3) },
            )

            idxClient.commits(
                repoPath = "$FIXTURES_PATH/$SIMPLE_REPO",
                branchName = "master",
                projectName = SIMPLE_PROJECT_NAME,
            )

            val repo2 = this@VcsIndexCommandsTest.repoService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")

            assertAll(
                { assertThat(repo2).isNotNull() },
                { assertThat(repo2?.branches).isNotEmpty() },
                { assertThat(repo2?.branches).hasSize(2) },
                { assertThat(repo2?.branches?.map { it.name }).containsAll(listOf("origin/master", "master")) },
                { assertThat(repo2?.commits).isNotEmpty() },
                { assertThat(repo2?.commits).hasSize(14) },
                { assertThat(repo2?.developers).hasSize(3) },
            )
        }

        @ParameterizedTest
        @CsvSource(
            "master,14",
            "origin/master,13",
        )
        fun `repeated commit indexing, should not change anything`(
            branchName: String,
            numberOfCommits: Int,
        ) {
            idxClient.commits(
                repoPath = "$FIXTURES_PATH/$SIMPLE_REPO",
                branchName = branchName,
                projectName = SIMPLE_PROJECT_NAME,
            )

            val repo1 =
                run {
                    val repo = requireNotNull(
                        this@VcsIndexCommandsTest.repoService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")
                    )
                    assertAll(
                        { assertThat(repo).isNotNull() },
                        { assertThat(repo.branches).isNotEmpty() },
                        { assertThat(repo.branches).hasSize(1) },
                        { assertThat(repo.branches.map { it.name }).contains(branchName) },
                        { assertThat(repo.commits).isNotEmpty() },
                        { assertThat(repo.commits).hasSize(numberOfCommits) },
                        { assertThat(repo.developers).hasSize(3) },
                    )
                    repo
                }

            idxClient.commits(
                repoPath = "$FIXTURES_PATH/$SIMPLE_REPO",
                branchName = branchName,
                projectName = SIMPLE_PROJECT_NAME,
            )

            val repo2 =
                run {
                    val repo = this@VcsIndexCommandsTest.repoService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")

                    assertAll(
                        { assertThat(repo).isNotNull() },
                        { assertThat(repo?.branches).isNotEmpty() },
                        { assertThat(repo?.branches).hasSize(1) },
                        { assertThat(repo?.branches?.map { it.name }).contains(branchName) },
                        { assertThat(repo?.commits).isNotEmpty() },
                        { assertThat(repo?.commits).hasSize(numberOfCommits) },
                        { assertThat(repo?.developers).hasSize(3) },
                    )
                    repo
                }

            run {
                assertAll(
                    { assertThat(repo1).isNotNull() },
                    { assertThat(repo2).isNotNull() },
                    { assertThat(repo1).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(repo2) },
                )
            }
        }

        @Test
        fun `discover new commit on branch with new developer for committer`() {
            idxClient.commits(
                repoPath = "$FIXTURES_PATH/$SIMPLE_REPO",
                branchName = "master",
                projectName = SIMPLE_PROJECT_NAME,
            )
            val repo1: Repository =
                run {
                    val repo = requireNotNull(this@VcsIndexCommandsTest.repoService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO"))
                    assertThat(repo).isNotNull()
                    assertAll(
                        "branches",
                        { assertThat(repo.branches).isNotEmpty() },
                        { assertThat(repo.branches).hasSize(1) },
                        { assertThat(repo.branches.map { it.name }).contains("master") },
                        { assertThat(repo.branches.flatMap { it.commits }).hasSize(14) },
                    )
                    assertAll(
                        "commits",
                        { assertThat(repo.commits).isNotEmpty() },
                        { assertThat(repo.commits).hasSize(14) },
                    )
                    assertThat(repo.developers).hasSize(3)
                    return@run repo
                }

            val newVcsCommit =
                run {
                    // Find existing parent commit
                    val parent = repo1.commits.find { it.sha == "b51199ab8b83e31f64b631e42b2ee0b1c7e3259a" }
                        ?: createTestCommit(
                            sha = "b51199ab8b83e31f64b631e42b2ee0b1c7e3259a",
                            message = "parent",
                            repository = repo1,
                            developerName = "User B",
                            developerEmail = "b@test.com"
                        )

                    // Create child commit with new developer
                    val child = createTestCommit(
                        sha = "123456789a123456789b123456789c123456789d",
                        message = "msg1",
                        repository = repo1,
                        developerName = "User A",
                        developerEmail = "a@test.com"
                    )

                    child.parents.add(parent)
                    return@run child
                }
            repo1.branches.first().head = newVcsCommit

            assertThat(
                repo1.commits.find { it.sha == "b51199ab8b83e31f64b631e42b2ee0b1c7e3259a" }
            ).isNotNull()

            val repo2 = assertDoesNotThrow {
                this@VcsIndexCommandsTest.repoService.addCommits(repo1, listOf(newVcsCommit))
            }

            run {
                assertThat(repo2).isNotNull()
                assertThat(repo2.id).isNotNull()
                assertAll(
                    "branches",
                    { assertThat(repo2.branches).hasSize(1) },
                    { assertThat(repo2.branches.map { it.name }).contains("master") },
                    { assertThat(repo2.branches.flatMap { it.commits }).hasSize(15) },
                )
                assertAll(
                    "commits",
                    { assertThat(repo2.commits).isNotEmpty() },
                    { assertThat(repo2.commits).hasSize(15) }, // new commit (new head)
                )
                assertAll(
                    "developers",
                    { assertThat(repo2.developers).hasSize(4) },
                    { assertThat(repo2.developers.map { it.email }).contains("a@test.com") },
                ) // new developer a@test.com
            }
        }
    }
}
