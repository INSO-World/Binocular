package com.inso_world.binocular.cli.integration.commands

import com.inso_world.binocular.cli.commands.Index
import com.inso_world.binocular.cli.entity.Repository
import com.inso_world.binocular.cli.index.vcs.VcsCommit
import com.inso_world.binocular.cli.index.vcs.VcsPerson
import com.inso_world.binocular.cli.service.RepositoryService
import com.inso_world.binocular.internal.BinocularRepository
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Transactional
internal class VcsIndexCommandsTest(
  @Autowired val idxClient: Index,
//  @Autowired val client: ShellTestClient,
  @Autowired val repoService: RepositoryService,
  @Autowired val transactionTemplate: TransactionTemplate,
) : BaseShellTest() {
  @ParameterizedTest
  @CsvSource(
    "master,14",
    "origin/master,13",
  )
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  fun `index commits -b master - simple repo`(
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
    )

//     you can then assert that the session isComplete() or simply proceed with your DB checks
//    await().atMost(5, TimeUnit.SECONDS).untilAsserted {
    val repo = transactionTemplate.execute { this.repoService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO") }
    assertAll(
      { assertThat(repo).isNotNull() },
      { assertThat(repo!!.id).isNotNull() },
      { assertThat(repo!!.branches).isNotEmpty() },
      { assertThat(repo!!.branches).hasSize(1) },
      { assertThat(repo!!.branches.map { it.name }).contains(branchName) },
      { assertThat(repo!!.commits).isNotEmpty() },
      { assertThat(repo!!.commits).hasSize(noOfCommits) },
      { assertThat(repo!!.user).hasSize(3) },
    )
//      }
//    }
  }

  @Test
  fun `repeated commit indexing with different branches`() {
    idxClient.commits(
      repoPath = "$FIXTURES_PATH/$SIMPLE_REPO",
      branchName = "origin/master",
    )
//    val session = client.interactive().run()
//
//    session.write(
//      session.writeSequence().text("index").space().text("commits").space().text("-b").space().text("origin/master")
//        .space().text("--repo_path").space().text("$FIXTURES_PATH/$SIMPLE_REPO").space().carriageReturn().build()
//    )

    // Check no 1.
//    await().atMost(5, TimeUnit.SECONDS).untilAsserted {
    transactionTemplate.execute {
      val repo1 = this.repoService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")

      assertAll(
        { assertThat(repo1).isNotNull() },
        { assertThat(repo1!!.id).isNotNull() },
        { assertThat(repo1!!.branches).isNotEmpty() },
        { assertThat(repo1!!.branches).hasSize(1) },
        { assertThat(repo1!!.branches.map { it.name }).contains("origin/master") },
        { assertThat(repo1!!.commits).isNotEmpty() },
        { assertThat(repo1!!.commits).hasSize(13) },
        { assertThat(repo1!!.user).hasSize(3) },
      )
    }
//    }

//    session.write(
//      session.writeSequence().text("index").space().text("commits").space().text("-b").space().text("master").space()
//        .text("--repo_path").space().text("$FIXTURES_PATH/$SIMPLE_REPO").space().carriageReturn().build()
//    )
    idxClient.commits(
      repoPath = "$FIXTURES_PATH/$SIMPLE_REPO",
      branchName = "master",
    )
//    await().atLeast(5, TimeUnit.SECONDS).atMost(20, TimeUnit.SECONDS)
//      .untilAsserted {
    transactionTemplate.execute {
      val repo2 = this.repoService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")

      assertAll(
        { assertThat(repo2).isNotNull() },
        { assertThat(repo2!!.id).isNotNull() },
        { assertThat(repo2!!.branches).isNotEmpty() },
        { assertThat(repo2!!.branches).hasSize(2) },
        { assertThat(repo2!!.branches.map { it.name }).containsAll(listOf("origin/master", "master")) },
        { assertThat(repo2!!.commits).isNotEmpty() },
        { assertThat(repo2!!.commits).hasSize(14) },
        { assertThat(repo2!!.user).hasSize(3) },
      )
    }
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
//    val session = client.interactive().run()
//    session.write(
//      session.writeSequence().text("index").space().text("commits").space().text("-b").space().text(branchName).space()
//        .text("--repo_path").space().text("$FIXTURES_PATH/$SIMPLE_REPO").space().carriageReturn().build()
//    )
    idxClient.commits(
      repoPath = "$FIXTURES_PATH/$SIMPLE_REPO",
      branchName = branchName,
    )

//    var repo1: Repository? = null
//    await().atMost(5, TimeUnit.MINUTES).untilAsserted {
    val repo1 =
      transactionTemplate.execute {
        val repo = this.repoService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")
        assertAll(
          { assertThat(repo).isNotNull() },
          { assertThat(repo!!.id).isNotNull() },
          { assertThat(repo!!.branches).isNotEmpty() },
          { assertThat(repo!!.branches).hasSize(1) },
          { assertThat(repo!!.branches.map { it.name }).contains(branchName) },
          { assertThat(repo!!.commits).isNotEmpty() },
          { assertThat(repo!!.commits).hasSize(numberOfCommits) },
          { assertThat(repo!!.user).hasSize(3) },
        )
//      repo1 = repo
        repo
      }
//    }

//    session.write(
//      session.writeSequence().text("index").space().text("commits").space().text("-b").space().text(branchName).space()
//        .text("--repo_path").space().text("$FIXTURES_PATH/$SIMPLE_REPO").space().carriageReturn().build()
//    )
    idxClient.commits(
      repoPath = "$FIXTURES_PATH/$SIMPLE_REPO",
      branchName = branchName,
    )

//    var repo2: Repository? = null
//    await().atMost(2, TimeUnit.SECONDS).untilAsserted {
    val repo2 =
      transactionTemplate.execute {
        val repo = this.repoService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")

        assertAll(
          { assertThat(repo).isNotNull() },
          { assertThat(repo!!.id).isNotNull() },
          { assertThat(repo!!.branches).isNotEmpty() },
          { assertThat(repo!!.branches).hasSize(1) },
          { assertThat(repo!!.branches.map { it.name }).contains(branchName) },
          { assertThat(repo!!.commits).isNotEmpty() },
          { assertThat(repo!!.commits).hasSize(numberOfCommits) },
          { assertThat(repo!!.user).hasSize(3) },
        )
        repo
      }
//    }
    transactionTemplate.execute {
      assertAll(
        { assertThat(repo1).isNotNull() },
        { assertThat(repo2).isNotNull() },
        { assertThat(repo1).usingRecursiveAssertion().isEqualTo(repo2) },
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
    )
    var repo1: Repository? = null
//    await().atMost(20, TimeUnit.SECONDS).untilAsserted {
    transactionTemplate.execute {
      val repo = this.repoService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")
      assertAll(
        { assertThat(repo).isNotNull() },
        { assertThat(repo!!.id).isNotNull() },
        { assertThat(repo!!.branches).isNotEmpty() },
        { assertThat(repo!!.branches).hasSize(1) },
        { assertThat(repo!!.branches.map { it.name }).contains("master") },
        { assertThat(repo!!.commits).isNotEmpty() },
        { assertThat(repo!!.commits).hasSize(14) },
        { assertThat(repo!!.user).hasSize(3) },
      )
      repo1 = repo
//      }
    }

    val newVcsCommit =
      VcsCommit(
        "123456789_123456789_123456789_123456789_",
        "msg1",
        "master",
        VcsPerson("User A", "a@test.com"),
        null,
        LocalDateTime.now(),
        LocalDateTime.now(),
        listOf("b51199ab8b83e31f64b631e42b2ee0b1c7e3259a"),
      ) // .toEntity()
//    // TODO change to this.commitDao.findHeadForBranch(this.simpleRepo, "master")
    repo1!!.commits.find { it.sha == "b51199ab8b83e31f64b631e42b2ee0b1c7e3259a" }

    transactionTemplate.execute {
      val vcsRepo =
        BinocularRepository(
          gitDir = "$FIXTURES_PATH/$SIMPLE_REPO",
          workTree = null,
          commonDir = null,
        ) // workTree & commonDir not relevant here
      this.repoService.addCommits(vcsRepo, listOf(newVcsCommit), "")
//      newCommit.parents = listOf(head!!)
//      newCommit.repository = repo1
//      assertThat(newCommit.committer).isNotNull()
//      assertThat(newCommit.author).isNull()
//      newCommit.committer!!.repository = repo1
//      repo1!!.user.add(newCommit.committer!!)
//
//      repo1!!.commits.add(newCommit)
      this.repoService.save(repo1!!)
    }

    transactionTemplate.execute {
      val repo2 = this.repoService.findRepo("$FIXTURES_PATH/$SIMPLE_REPO")
      assertAll(
        { assertThat(repo2).isNotNull() },
        { assertThat(repo2!!.id).isNotNull() },
        { assertThat(repo2!!.branches).isNotEmpty() },
        { assertThat(repo2!!.branches).hasSize(1) },
        { assertThat(repo2!!.branches.map { it.name }).contains("master") },
        { assertThat(repo2!!.commits).isNotEmpty() },
        { assertThat(repo2!!.commits).hasSize(15) }, // new commit (new head)
        { assertThat(repo2!!.user).hasSize(4) }, // new user a@test.com
      )
    }
  }
}
