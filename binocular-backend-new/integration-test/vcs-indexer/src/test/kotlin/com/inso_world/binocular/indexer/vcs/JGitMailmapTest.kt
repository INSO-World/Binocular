package com.inso_world.binocular.indexer.vcs

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.index.GitIndexer
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest
import com.inso_world.binocular.model.Project
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.io.path.Path

/**
 * Integration tests for mailmap support in JGit GitIndexer.
 *
 * These tests verify that the `binocular.jgit.use-mailmap` configuration option
 * correctly applies .mailmap transformations to author and committer identities.
 *
 * The mailmap fixture contains commits from developers with multiple email addresses
 * that should be normalized to canonical addresses via the .mailmap file.
 */
@SpringBootTest(
    classes = [VcsIndexerTestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
@ExtendWith(SpringExtension::class)
@ActiveProfiles("jgit")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestPropertySource(properties = ["binocular.jgit.use-mailmap=true"])
internal class JGitMailmapTest : BaseFixturesIntegrationTest() {

    @Autowired
    private lateinit var indexer: GitIndexer

    private lateinit var project: Project

    companion object Companion {
        private val logger by logger()

        /**
         * Canonical email addresses after mailmap transformation.
         * These are the expected email addresses for all commits after applying .mailmap.
         */
        val CANONICAL_EMAILS = setOf(
            "alice@company.com",
            "bob@company.com",
            "carol@company.com",
            "dave@company.com",
            "admin@company.com"
        )

        /**
         * Old email addresses that should be transformed by mailmap.
         * These should NOT appear after mailmap is applied.
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
         * Canonical names after mailmap transformation.
         */
        val CANONICAL_NAMES = setOf(
            "Alice Developer",
            "Bob Builder",
            "Carol Coder",
            "Dave DevOps",
            "Admin"
        )

        /**
         * Old names that should be transformed by mailmap.
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
        project = Project(name = "jgit-mailmap-test")
    }

    @Nested
    @DisplayName("Mailmap email normalization")
    inner class MailmapEmailNormalization {

        @Test
        fun `traverseBranch with mailmap should normalize author emails to canonical addresses`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${MAILMAP_REPO}"), project)
            val (_, commits) = indexer.traverseBranch(repo, "refs/heads/master")

            val authorEmails = commits.map { it.author.email }.toSet()

            logger.info("Author emails after mailmap: {}", authorEmails)

            assertAll(
                "All author emails should be canonical",
                { assertThat(authorEmails).isSubsetOf(CANONICAL_EMAILS) },
                { assertThat(authorEmails).doesNotContainAnyElementsOf(OLD_EMAILS) }
            )
        }

        @Test
        fun `traverseBranch with mailmap should normalize committer emails to canonical addresses`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${MAILMAP_REPO}"), project)
            val (_, commits) = indexer.traverseBranch(repo, "refs/heads/master")

            val committerEmails = commits.map { it.committer.email }.toSet()

            logger.info("Committer emails after mailmap: {}", committerEmails)

            assertAll(
                "All committer emails should be canonical",
                { assertThat(committerEmails).isSubsetOf(CANONICAL_EMAILS) },
                { assertThat(committerEmails).doesNotContainAnyElementsOf(OLD_EMAILS) }
            )
        }

        @Test
        fun `findCommit with mailmap should return normalized email addresses`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${MAILMAP_REPO}"), project)
            val headCommit = indexer.findCommit(repo, "HEAD")

            logger.info("HEAD commit author: {} <{}>",
                headCommit.author.name, headCommit.author.email)
            logger.info("HEAD commit committer: {} <{}>",
                headCommit.committer.name, headCommit.committer.email)

            assertAll(
                "HEAD commit should have canonical email addresses",
                { assertThat(headCommit.committer.email).isIn(CANONICAL_EMAILS) },
                { assertThat(headCommit.author.email).isIn(CANONICAL_EMAILS) }
            )
        }
    }

    @Nested
    @DisplayName("Mailmap name normalization")
    inner class MailmapNameNormalization {

        @Test
        fun `traverseBranch with mailmap should normalize author names to canonical names`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${MAILMAP_REPO}"), project)
            val (_, commits) = indexer.traverseBranch(repo, "refs/heads/master")

            val authorNames = commits.map { it.author.name }.toSet()

            logger.info("Author names after mailmap: {}", authorNames)

            assertAll(
                "All author names should be canonical",
                { assertThat(authorNames).isSubsetOf(CANONICAL_NAMES) },
                { assertThat(authorNames).doesNotContainAnyElementsOf(OLD_NAMES) }
            )
        }

        @Test
        fun `traverseBranch with mailmap should normalize committer names to canonical names`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${MAILMAP_REPO}"), project)
            val (_, commits) = indexer.traverseBranch(repo, "refs/heads/master")

            val committerNames = commits.map { it.committer.name }.toSet()

            logger.info("Committer names after mailmap: {}", committerNames)

            assertAll(
                "All committer names should be canonical",
                { assertThat(committerNames).isSubsetOf(CANONICAL_NAMES) },
                { assertThat(committerNames).doesNotContainAnyElementsOf(OLD_NAMES) }
            )
        }
    }

    @Nested
    @DisplayName("Developer identity consolidation")
    inner class DeveloperIdentityConsolidation {

        @Test
        fun `mailmap should consolidate multiple identities into single developer per canonical email`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${MAILMAP_REPO}"), project)
            val (_, commits) = indexer.traverseBranch(repo, "refs/heads/master")

            // Group commits by author email
            val commitsByAuthorEmail = commits.groupBy { it.author.email }

            logger.info("Commits grouped by canonical author email:")
            commitsByAuthorEmail.forEach { (email, emailCommits) ->
                logger.info("  $email: ${emailCommits.size} commits")
            }

            // All commits from alice@example.com, alice@home.local, and alice@company.com
            // should now be grouped under alice@company.com
            val aliceCommits = commitsByAuthorEmail["alice@company.com"] ?: emptyList()
            assertThat(aliceCommits.size)
                .describedAs("Alice should have all her commits consolidated")
                .isGreaterThanOrEqualTo(3) // At least 3 commits with old emails + canonical

            // Verify no old emails exist
            assertThat(commitsByAuthorEmail.keys)
                .doesNotContainAnyElementsOf(OLD_EMAILS)
        }

        @Test
        fun `repository developers should only contain canonical identities`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${MAILMAP_REPO}"), project)
            indexer.traverseBranch(repo, "refs/heads/master")

            val developerEmails = repo.developers.map { it.email }.toSet()

            logger.info("Repository developers after mailmap: {}", developerEmails)

            assertAll(
                "Repository should only contain canonical developer emails",
                { assertThat(developerEmails).isSubsetOf(CANONICAL_EMAILS) },
                { assertThat(developerEmails).doesNotContainAnyElementsOf(OLD_EMAILS) }
            )
        }
    }

    @Nested
    @DisplayName("Mailmap with traverse history")
    inner class MailmapWithTraverseHistory {

        @Test
        fun `traverse history with mailmap should apply mailmap transformations`() {
            val repo = indexer.findRepo(Path("${FIXTURES_PATH}/${MAILMAP_REPO}"), project)
            val headCommit = indexer.findCommit(repo, "HEAD")
            val commits = indexer.traverse(repo, headCommit, null)

            val allEmails = commits.flatMap {
                listOf(it.author.email, it.committer.email)
            }.toSet()

            logger.info("All emails in history: {}", allEmails)

            assertAll(
                "All emails in history should be canonical after mailmap",
                { assertThat(allEmails).isSubsetOf(CANONICAL_EMAILS) },
                { assertThat(allEmails).doesNotContainAnyElementsOf(OLD_EMAILS) }
            )
        }
    }
}
