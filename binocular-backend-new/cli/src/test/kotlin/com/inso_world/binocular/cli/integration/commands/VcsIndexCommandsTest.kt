package com.inso_world.binocular.cli.integration.commands

import com.inso_world.binocular.cli.BinocularCommandLineApplication
import com.inso_world.binocular.cli.commands.Index
import com.inso_world.binocular.cli.service.RepositoryService
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest
import com.inso_world.binocular.core.integration.base.InfrastructureDataSetup
import com.inso_world.binocular.infrastructure.sql.SqlTestConfig
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@SpringBootTest(
    classes = [BinocularCommandLineApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
@ContextConfiguration(
    initializers = [
        SqlTestConfig.Initializer::class,
    ]
)
internal class VcsIndexCommandsTest() : BaseFixturesIntegrationTest() {

    @all:Autowired
    private lateinit var infrastructureDataSetup: InfrastructureDataSetup

    @all:Autowired
    private lateinit var repoService: RepositoryService

    @all:Autowired
    private lateinit var idxClient: Index

    @AfterEach
    fun cleanup() {
        this.infrastructureDataSetup.teardown()
    }

    @Nested
    inner class BinocularRepo {
        @AfterEach
        fun `cleanup inner`() {
            cleanup()
        }

        @Test
        fun `index branch origin-feature-5`() {
            idxClient.commits(
                repoPath = "../../",
                branchName = "origin/feature/5",
                "Binocular",
            )
        }

        @Test
        fun `index branch origin-feature-6`() {
            idxClient.commits(
                repoPath = "../../",
                branchName = "origin/feature/6",
                "Binocular",
            )
        }


        @Test
        fun `index branch origin-feature-6 and then origin-feature-5`() {
            val path = "../../"
            idxClient.commits(
                repoPath = path,
                branchName = "origin/feature/6",
                "Binocular",
            )
//            assertThat(repoService.f?.commits).hasSize(207)
            assertDoesNotThrow {
                idxClient.commits(
                    repoPath = path,
                    branchName = "origin/feature/5",
                    "Binocular",
                )
            }
//            assertThat(repoService.findRepo(path)?.commits).hasSize(219)
        }

        @Test
        fun `index branch origin-feature-6 and then again`() {
            val path = "../../.git"
            idxClient.commits(
                repoPath = path,
                branchName = "origin/feature/6",
                "Binocular",
            )
//            assertThat(repoService.f?.commits).hasSize(207)
            assertDoesNotThrow {
                idxClient.commits(
                    repoPath = path,
                    branchName = "origin/feature/6",
                    "Binocular",
                )
            }
//            assertThat(repoService.findRepo(path)?.commits).hasSize(219)
        }

        @Test
        fun `index branch origin-feature-6 and then origin-feature-9`() {
            val path = "../../.git"
            idxClient.commits(
                repoPath = path,
                branchName = "origin/feature/6",
                "Binocular",
            )
//            assertThat(repoService.findRepo(path)?.commits).hasSize(207)
            assertDoesNotThrow {
                idxClient.commits(
                    repoPath = path,
                    branchName = "origin/feature/9",
                    "Binocular",
                )
            }
//            assertThat(repoService.findRepo(path)?.commits).hasSize(219)
        }
    }

    @Nested
    inner class OctoRepo {
        @AfterEach
        fun `cleanup inner`() {
            cleanup()
        }

        @Test
        fun `index branch master`() {
            assertDoesNotThrow {
                idxClient.commits(
                    repoPath = "$FIXTURES_PATH/$OCTO_REPO",
                    branchName = "master",
                    OCTO_PROJECT_NAME,
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
                { assertThat(repo.user).hasSize(3) },
            )
        }
    }

    @ParameterizedTest
    @CsvSource(
        "master,14",
        "origin/master,13",
    )
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun `SIMPLE_REPO, index commits -b master`(
        branchName: String,
        noOfCommits: Int,
    ) {
//    val session = client.interactive().run()
//
//    session.write(
//      session.writeSequence().text("index").space().text("commits").space().text("-b").space().text(branchName).space()
//        .text("--repo_path").space().text("$FIXTURES_PATH/$SIMPLE_REPO").carriageReturn().build()
//    )
        idxClient.commits(
            repoPath = "$FIXTURES_PATH/$SIMPLE_REPO",
            branchName = branchName,
            SIMPLE_PROJECT_NAME,
        )

//     you can then assert that the session isComplete() or simply proceed with your DB checks
//    await().atMost(5, TimeUnit.SECONDS).untilAsserted {
        val repo = this.repoService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")
        assertAll(
            { assertThat(repo).isNotNull() },
//            { assertThat(repo?.id).isNotNull() },
            { assertThat(repo?.branches).isNotEmpty() },
            { assertThat(repo?.branches).hasSize(1) },
            { assertThat(repo?.branches?.map { it.name }).contains(branchName) },
            { assertThat(repo?.commits).isNotEmpty() },
            { assertThat(repo?.commits).hasSize(noOfCommits) },
            { assertThat(repo?.user).hasSize(3) },
        )
//      }
//    }
    }

    @Test
    fun `repeated commit indexing with different branches`() {
        idxClient.commits(
            repoPath = "$FIXTURES_PATH/$SIMPLE_REPO",
            branchName = "origin/master",
            SIMPLE_PROJECT_NAME,
        )
//    val session = client.interactive().run()
//
//    session.write(
//      session.writeSequence().text("index").space().text("commits").space().text("-b").space().text("origin/master")
//        .space().text("--repo_path").space().text("$FIXTURES_PATH/$SIMPLE_REPO").space().carriageReturn().build()
//    )

        // Check no 1.
//    await().atMost(5, TimeUnit.SECONDS).untilAsserted {
        val repo1 = this.repoService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")

        assertAll(
            { assertThat(repo1).isNotNull() },
//                { assertThat(repo1?.id).isNotNull() },
            { assertThat(repo1?.branches).isNotEmpty() },
            { assertThat(repo1?.branches).hasSize(1) },
            { assertThat(repo1?.branches?.map { it.name }).contains("origin/master") },
            { assertThat(repo1?.commits).isNotEmpty() },
            { assertThat(repo1?.commits).hasSize(13) },
            { assertThat(repo1?.user).hasSize(3) },
        )
//    }

//    session.write(
//      session.writeSequence().text("index").space().text("commits").space().text("-b").space().text("master").space()
//        .text("--repo_path").space().text("$FIXTURES_PATH/$SIMPLE_REPO").space().carriageReturn().build()
//    )
        idxClient.commits(
            repoPath = "$FIXTURES_PATH/$SIMPLE_REPO",
            branchName = "master",
            SIMPLE_PROJECT_NAME,
        )
//    await().atLeast(5, TimeUnit.SECONDS).atMost(20, TimeUnit.SECONDS)
//      .untilAsserted {
        val repo2 = this.repoService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")

        assertAll(
            { assertThat(repo2).isNotNull() },
//                { assertThat(repo2?.id).isNotNull() },
            { assertThat(repo2?.branches).isNotEmpty() },
            { assertThat(repo2?.branches).hasSize(2) },
            { assertThat(repo2?.branches?.map { it.name }).containsAll(listOf("origin/master", "master")) },
            { assertThat(repo2?.commits).isNotEmpty() },
            { assertThat(repo2?.commits).hasSize(14) },
            { assertThat(repo2?.user).hasSize(3) },
        )
//      }
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
            SIMPLE_PROJECT_NAME,
        )

        val repo1 =
            run {
                val repo = requireNotNull(
                    this.repoService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")
                )
                assertAll(
                    { assertThat(repo).isNotNull() },
                    { assertThat(repo.branches).isNotEmpty() },
                    { assertThat(repo.branches).hasSize(1) },
                    { assertThat(repo.branches.map { it.name }).contains(branchName) },
                    { assertThat(repo.commits).isNotEmpty() },
                    { assertThat(repo.commits).hasSize(numberOfCommits) },
                    { assertThat(repo.user).hasSize(3) },
                )
                repo
            }

        idxClient.commits(
            repoPath = "$FIXTURES_PATH/$SIMPLE_REPO",
            branchName = branchName,
            SIMPLE_PROJECT_NAME,
        )

        val repo2 =
            run {
                val repo = this.repoService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")

                assertAll(
                    { assertThat(repo).isNotNull() },
//                    { assertThat(repo?.id).isNotNull() },
                    { assertThat(repo?.branches).isNotEmpty() },
                    { assertThat(repo?.branches).hasSize(1) },
                    { assertThat(repo?.branches?.map { it.name }).contains(branchName) },
                    { assertThat(repo?.commits).isNotEmpty() },
                    { assertThat(repo?.commits).hasSize(numberOfCommits) },
                    { assertThat(repo?.user).hasSize(3) },
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
    fun `discover new commit on branch with new user for committer`() {
//    val session = client.interactive().run()
//    session.write(
//      session.writeSequence().text("index").space().text("commits").space().text("-b").space().text("master").space()
//        .text("--repo_path").space().text("$FIXTURES_PATH/$SIMPLE_REPO").space().carriageReturn().build()
//    )
        idxClient.commits(
            repoPath = "$FIXTURES_PATH/$SIMPLE_REPO",
            branchName = "master",
            SIMPLE_PROJECT_NAME,
        )
        val repo1: Repository =
//    await().atMost(20, TimeUnit.SECONDS).untilAsserted {
            run {
                val repo = requireNotNull(this.repoService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO"))
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
                assertThat(repo.user).hasSize(3)
                return@run repo
//      }
            }

        val newVcsCommit =
            run {
                val branch = Branch(name = "master")
                val parent = Commit(
                    id = null,
                    "b51199ab8b83e31f64b631e42b2ee0b1c7e3259a",
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    "parent",
                    null,
                    null,
                    "master",
                )
                parent.committer = User(name = "User B", email = "b@test.com")
                val child = Commit(
                    id = null,
                    "123456789_123456789_123456789_123456789_",
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    "msg1",
                    null,
                    null,
                    null
                )
                child.committer = User(name = "User A", email = "a@test.com")
                child.parents.add(parent)
                branch.commits.add(child)
                return@run child
            }
        // TODO change to this.commitDao.findHeadForBranch(this.simpleRepo, "master")
        assertThat(
            repo1.commits.find { it.sha == "b51199ab8b83e31f64b631e42b2ee0b1c7e3259a" }
        ).isNotNull()

        val repo2 = assertDoesNotThrow {
            this.repoService.addCommits(repo1, listOf(newVcsCommit))
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
                "user",
                { assertThat(repo2.user).hasSize(4) },
                { assertThat(repo2.user.map { it.email }).contains("a@test.com") },
            ) // new user a@test.com
        }
    }
}
