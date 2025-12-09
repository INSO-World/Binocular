package com.inso_world.binocular.ffi.integration.internal

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.integration.base.BaseIntegrationTest
import com.inso_world.binocular.ffi.BinocularConfig
import com.inso_world.binocular.ffi.BinocularFfiTestApplication
import com.inso_world.binocular.ffi.internal.findRepo
import com.inso_world.binocular.ffi.internal.traverseBranch
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.util.concurrent.TimeUnit

@SpringBootTest(
    classes = [BinocularFfiTestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
@ExtendWith(SpringExtension::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class GitComparison : BaseIntegrationTest() {

    @Autowired
    private lateinit var cfg: BinocularConfig

    companion object {
        private val logger by logger()
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "origin/main",
            "origin/develop"
        ]
    )
    fun `Binocular, traverse branch, check commit count`(branchName: String) {
        logger.info(branchName)
        val repo = findRepo("./")

        val (_, branchCommits) = traverseBranch(
            repo, branchName,
            skipMerges = cfg.gix.skipMerges,
            useMailmap = cfg.gix.useMailmap
        )
        // localPath points to .git directory, so get the parent for git commands
        val repoDir = File(repo.gitDir).parentFile

        val gitLogProcess = ProcessBuilder(
            "git", "rev-list", "--count", branchName
        )
            .directory(repoDir)
            .redirectErrorStream(true)
            .start()
        logger.debug("{}", gitLogProcess.info())

        val gitCount = gitLogProcess.inputStream.bufferedReader().readLine().toInt()
        logger.debug("gitCount = {}", gitCount)

        assertThat(branchCommits.size).isEqualTo(gitCount)
    }


    @ParameterizedTest
    @CsvSource(
        value = [
            "origin/main",
            "origin/develop"
        ]
    )
    fun `Binocular, traverse branch, check committer`(branchName: String) {
        logger.info(branchName)
        val repo = findRepo("./")
        assertThat(repo).isNotNull()

        val (_, branchCommits) = traverseBranch(
            repo, branchName,
            skipMerges = cfg.gix.skipMerges,
            useMailmap = cfg.gix.useMailmap
        )
        // localPath points to .git directory, so get the parent for git commands
        val repoDir = File(repo.gitDir).parentFile

        val committerGroups = branchCommits.filter { it.committer != null }.groupBy { it.committer!!.email }
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
                lines.filter { it.contains(committer) }.count()
            }
            gitLogProcess.waitFor(5, TimeUnit.SECONDS)

            logger.info("Committer: ${committer} - Commits: $lineCount")
            assertThat(commits.size).isEqualTo(lineCount)
        }
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "origin/main",
            "origin/develop"
        ]
    )
    fun `Binocular, traverse branch, check author`(branchName: String) {
        logger.info(branchName)
        val repo = findRepo("./")
        assertThat(repo).isNotNull()

        val (_, branchCommits) = traverseBranch(
            repo, branchName,
            skipMerges = cfg.gix.skipMerges,
            useMailmap = cfg.gix.useMailmap
        )
        // localPath points to .git directory, so get the parent for git commands
        val repoDir = File(repo.gitDir).parentFile

        val authorGroups = branchCommits.filter { it.author != null }.groupBy { it.author!!.email }
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
                lines.filter { it.contains(author) }.count()
            }
            gitLogProcess.waitFor(5, TimeUnit.SECONDS)

            logger.info("Committer: ${author} - Commits: $lineCount")
            assertThat(commits.size).isEqualTo(lineCount)
        }
    }
}
