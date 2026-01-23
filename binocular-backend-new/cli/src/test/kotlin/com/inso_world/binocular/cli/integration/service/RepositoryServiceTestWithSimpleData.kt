package com.inso_world.binocular.cli.integration.service

import com.inso_world.binocular.cli.integration.service.base.BaseServiceTest
import com.inso_world.binocular.cli.integration.utils.setupRepoConfig
import com.inso_world.binocular.cli.service.RepositoryService
import com.inso_world.binocular.core.data.MockTestDataProvider
import com.inso_world.binocular.core.index.GitIndexer
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import java.time.LocalDateTime
import kotlin.io.path.Path

internal class RepositoryServiceTestWithSimpleData @Autowired constructor(
    private val repositoryService: RepositoryService,
    @Lazy private val projectPort: ProjectInfrastructurePort,
    @Autowired private val userInfrastructurePort: UserInfrastructurePort,
) : BaseServiceTest() {

    @Autowired
    private lateinit var indexer: GitIndexer

    private lateinit var testData: MockTestDataProvider

    @BeforeEach
    fun setup() {
        this.testData = MockTestDataProvider()
    }

    /**
     * Creates a test commit using the new Signature-based constructor.
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

    @Test
    fun `find existing simple repo`() {
        val repo = this.repositoryService.findRepo("${FIXTURES_PATH}/${SIMPLE_REPO}")
        assertThat(repo).isNotNull()
        assertAll(
            { assertThat(repo?.id).isNotNull() },
            { assertThat(this.simpleRepo).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(repo) },
            { assertThat(repo?.commits).hasSize(14) },
            { assertThat(repo?.developers).hasSize(3) },
            { assertThat(repo?.branches).hasSize(1) },
            { assertThat(repo?.project).isNotNull() },
            { assertThat(repo?.project?.id).isNotNull() },
        )
    }

    @ParameterizedTest
    @CsvSource(
        "master,b51199ab8b83e31f64b631e42b2ee0b1c7e3259a",
        "origin/master,3d28b65c324cc8ee0bb7229fb6ac5d7f64129e90",
    )
    fun `get head commit of main branch`(
        branch: String,
        headCommit: String,
    ) {
        this.cleanup()

        val simpleRepoConfig =
            setupRepoConfig(
                indexer,
                "${FIXTURES_PATH}/${SIMPLE_REPO}",
                "HEAD",
                branch,
                SIMPLE_PROJECT_NAME,
            )
        val localRepo =
            run {
                val r = simpleRepoConfig.repo
                simpleRepoConfig.project.repo = r
                requireNotNull(
                    projectPort.create(simpleRepoConfig.project).repo
                ) {
                    "project not found"
                }
            }

        val head = this.repositoryService.getHeadCommits(localRepo, branch)
        assertAll(
            { assertThat(head).isNotNull() },
            { assertThat(head?.sha).isEqualTo(headCommit) },
        )
    }

    @Test
    fun `get head commit non existing branch`() {
        assertAll(
            {
                assertDoesNotThrow {
                    this.repositoryService.getHeadCommits(this.simpleRepo, "non-existing-branch-12345657890")
                }
            },
        )
    }

    @Test
    fun `update simple repo, add another commit, same branch`() {
        val repo = requireNotNull(this.repositoryService.findRepo("${FIXTURES_PATH}/${SIMPLE_REPO}")) {
            "repository '${FIXTURES_PATH}/${SIMPLE_REPO}' not found (1)"
        }

        assertAll(
            { assertThat(repo.commits).hasSize(14) },
            { assertThat(repo.branches).hasSize(1) },
            { assertThat(repo.branches.toList()[0].commits).hasSize(14) },
            { assertThat(repo.developers).hasSize(3) },
        )

        val newVcsCommit =
            run {
                // Find existing parent commit from the repo
                val parent = repo.commits.find { it.sha == "b51199ab8b83e31f64b631e42b2ee0b1c7e3259a" }
                    ?: createTestCommit(
                        sha = "b51199ab8b83e31f64b631e42b2ee0b1c7e3259a",
                        message = null,
                        repository = repo,
                        developerName = "Parent Committer",
                        developerEmail = "parent@test.com"
                    )

                // Get a test commit from MockTestDataProvider and create proper version
                val testCommit = this.testData.commits[1]
                val child = createTestCommit(
                    sha = testCommit.sha,
                    message = testCommit.message,
                    repository = repo,
                    developerName = testCommit.author.name,
                    developerEmail = testCommit.author.email
                )
                child.parents.add(parent)
                repo.branches.first().head = child

                return@run child
            }

        val repo2 = this.repositoryService.addCommits(repo, listOf(newVcsCommit))
        assertThat(repo2).isSameAs(repo)

        assertAll(
            { assertThat(repo2.commits).hasSize(15) },
            { assertThat(repo2.branches).hasSize(1) },
            { assertThat(repo2.branches.first().commits).hasSize(15) },
            { assertThat(repo2.developers).hasSize(4) },
        )
        val repoList = repositoryPort.findAll()
        assertThat(repoList).hasSize(1)
        with(repoList.first()) {
            assertAll(
                "check database numbers",
                { assertThat(this).isNotSameAs(repo2) },
                { assertThat(this.commits).hasSize(15) },
                { assertThat(this.branches).hasSize(1) },
                { assertThat(this.branches.first().commits).hasSize(15) },
                { assertThat(this.developers).hasSize(4) },
            )
        }
    }

    @Test
    @Disabled("needs update for new domain model - branch manipulation changed")
    fun `update simple repo, add another commit, new branch`() {
        run {
            val repo = requireNotNull(this.repositoryService.findRepo("${FIXTURES_PATH}/${SIMPLE_REPO}")) {
                "repository '${FIXTURES_PATH}/${SIMPLE_REPO}' not found (1)"
            }

            assertAll(
                { assertThat(repo.commits).hasSize(14) },
                { assertThat(repo.branches).hasSize(1) },
                { assertThat(repo.branches.toList()[0].commits).hasSize(14) },
                { assertThat(repo.developers).hasSize(3) },
            )
        }

        val project = Project(name = SIMPLE_PROJECT_NAME)

        // required to manipulate history
        var hashes =
            run {
                val path = Path("${FIXTURES_PATH}/${SIMPLE_REPO}")
                val repo = indexer.findRepo(path, project)
                val hashes = indexer.traverseBranch(repo, "master")
                // Note: branch field manipulation no longer works the same way with new model
                hashes.second
            }
        hashes = hashes.toMutableList()
        val parent = requireNotNull(hashes.find { it.sha == "8f34ebee8f593193048f8bcbf848501bf2465865" }) {
            "must find parent here"
        }
        hashes.add(
            run {
                val testCommit = this.testData.commits[0]
                val cmt = createTestCommit(
                    sha = testCommit.sha,
                    message = testCommit.message,
                    repository = project.repo ?: throw IllegalStateException("repo must exist"),
                    developerName = this.testData.developers[1].name,
                    developerEmail = this.testData.developers[1].email
                )
                cmt.parents.add(parent)
                cmt
            }
        )
        run {
            val repo = requireNotNull(this.repositoryService.findRepo("${FIXTURES_PATH}/${SIMPLE_REPO}")) {
                "repository '${FIXTURES_PATH}/${SIMPLE_REPO}' not found (1)"
            }
            this.repositoryService.addCommits(repo, hashes)
        }

        run {
            val repo = requireNotNull(this.repositoryService.findRepo("${FIXTURES_PATH}/${SIMPLE_REPO}")) {
                "repository '${FIXTURES_PATH}/${SIMPLE_REPO}' not found (1)"
            }

            assertAll(
                "repo2",
                { assertThat(repo.commits).hasSize(15) },
                { assertThat(repo.branches).hasSize(2) },
                { assertThat(repo.developers).hasSize(5) },
                { assertThat(repo.branches.map { it.commits.count() }).containsAll(listOf(14, 3)) },
                { assertThat(repo.branches.map { it.name }).containsAll(listOf("master", "new one")) },
            )
        }
    }
}
