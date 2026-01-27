package com.inso_world.binocular.indexer.vcs

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.index.GitIndexer
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest
import com.inso_world.binocular.model.Project
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.io.path.Path

/**
 * Baseline tests to verify that old email addresses are present when mailmap is disabled.
 *
 * These tests complement [MailmapTest] by verifying that without mailmap,
 * the original author/committer identities are preserved.
 *
 * Only runs with the gix profile to ensure consistent behavior testing.
 */
@SpringBootTest(
    classes = [VcsIndexerTestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
@ExtendWith(SpringExtension::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestPropertySource(properties = ["binocular.gix.use-mailmap=false"])
internal class MailmapDisabledTest : BaseFixturesIntegrationTest() {

    @Autowired
    private lateinit var indexer: GitIndexer

    private lateinit var project: Project

    companion object Companion {
        private val logger by logger()

        /**
         * Old email addresses that should be present when mailmap is disabled.
         */
        val OLD_EMAILS = setOf(
            "alice@example.com",
            "alice@home.local",
            "bob@example.com",
            "bobby@personal.net",
            "carol@example.com",
            "ccoder@oldcompany.org",
            "dave@example.com"
        )

        /**
         * Old names that should be present when mailmap is disabled.
         */
        val OLD_NAMES = setOf(
            "Alice",
            "alice",
            "Bob",
            "Bobby",
            "Carol",
            "C. Coder",
            "Dave"
        )
    }

    @BeforeEach
    fun setUp() {
        project = Project(name = "mailmap-disabled-test")
    }

    @Nested
    @DisplayName("Original identities preserved when mailmap disabled")
    inner class OriginalIdentitiesPreserved {

        @Test
        fun `traverseBranch without mailmap should preserve original author emails`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${MAILMAP_REPO}"), project)
            val (_, commits) = indexer.traverseBranch(repo, "refs/heads/master")

            val authorEmails = commits.map { it.author.email }.toSet()

            logger.info("Author emails without mailmap: {}", authorEmails)

            // Should contain at least some of the old emails
            assertThat(authorEmails)
                .describedAs("Without mailmap, original old emails should be present")
                .containsAnyElementsOf(OLD_EMAILS)
        }

        @Test
        fun `traverseBranch without mailmap should preserve original committer emails`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${MAILMAP_REPO}"), project)
            val (_, commits) = indexer.traverseBranch(repo, "refs/heads/master")

            val committerEmails = commits.map { it.committer.email }.toSet()

            logger.info("Committer emails without mailmap: {}", committerEmails)

            // Should contain at least some of the old emails
            assertThat(committerEmails)
                .describedAs("Without mailmap, original old emails should be present")
                .containsAnyElementsOf(OLD_EMAILS)
        }

        @Test
        fun `traverseBranch without mailmap should preserve original author names`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${MAILMAP_REPO}"), project)
            val (_, commits) = indexer.traverseBranch(repo, "refs/heads/master")

            val authorNames = commits.map { it.author.name }.toSet()

            logger.info("Author names without mailmap: {}", authorNames)

            // Should contain at least some of the old names
            assertThat(authorNames)
                .describedAs("Without mailmap, original old names should be present")
                .containsAnyElementsOf(OLD_NAMES)
        }

        @Test
        fun `traverseBranch without mailmap should preserve original committer names`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${MAILMAP_REPO}"), project)
            val (_, commits) = indexer.traverseBranch(repo, "refs/heads/master")

            val committerNames = commits.map { it.committer.name }.toSet()

            logger.info("Committer names without mailmap: {}", committerNames)

            // Should contain at least some of the old names
            assertThat(committerNames)
                .describedAs("Without mailmap, original old names should be present")
                .containsAnyElementsOf(OLD_NAMES)
        }
    }

    @Nested
    @DisplayName("Multiple developer identities when mailmap disabled")
    inner class MultipleDeveloperIdentities {

        @Test
        fun `repository should have multiple developer identities for same person when mailmap disabled`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${MAILMAP_REPO}"), project)
            indexer.traverseBranch(repo, "refs/heads/master")

            val developerEmails = repo.developers.map { it.email }.toSet()

            logger.info("Repository developers without mailmap: {}", developerEmails)

            // Without mailmap, Alice should have multiple entries
            val aliceEmails = developerEmails.filter { it.contains("alice", ignoreCase = true) }
            assertThat(aliceEmails.size)
                .describedAs("Without mailmap, Alice should have multiple email identities")
                .isGreaterThan(1)
        }

        @Test
        fun `commits should be scattered across multiple identities when mailmap disabled`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${MAILMAP_REPO}"), project)
            val (_, commits) = indexer.traverseBranch(repo, "refs/heads/master")

            val commitsByAuthorEmail = commits.groupBy { it.author.email }

            logger.info("Commits grouped by author email without mailmap:")
            commitsByAuthorEmail.forEach { (email, emailCommits) ->
                logger.info("  $email: ${emailCommits.size} commits")
            }

            // Without mailmap, we should have more email keys than developers
            // because same person has multiple emails
            assertThat(commitsByAuthorEmail.keys.size)
                .describedAs("Without mailmap, there should be multiple email identities")
                .isGreaterThan(5) // More than the 5 canonical emails
        }
    }
}
