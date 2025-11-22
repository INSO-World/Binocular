package com.inso_world.binocular.cli.integration.service

import com.inso_world.binocular.cli.integration.service.base.BaseServiceTest
import com.inso_world.binocular.cli.integration.utils.setupRepoConfig
import com.inso_world.binocular.cli.service.RepositoryService
import com.inso_world.binocular.core.data.MockTestDataProvider
import com.inso_world.binocular.core.index.GitIndexer
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
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
) : BaseServiceTest() {

    @Autowired
    private lateinit var indexer: GitIndexer

    private lateinit var testData: MockTestDataProvider

    @BeforeEach
    fun setup() {
        this.testData = MockTestDataProvider()
    }

    @Test
    fun `find existing simple repo`() {
        val repo = this.repositoryService.findRepo("${FIXTURES_PATH}/${SIMPLE_REPO}")
        assertThat(repo).isNotNull()
        assertAll(
            { assertThat(repo?.id).isNotNull() },
            { assertThat(this.simpleRepo).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(repo) },
            { assertThat(repo?.commits).hasSize(14) },
            { assertThat(repo?.user).hasSize(3) },
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
                "${FIXTURES_PATH}/${SIMPLE_REPO}",
                "HEAD",
                branch,
                SIMPLE_PROJECT_NAME,
            )
        val localRepo =
            run {
                val r = simpleRepoConfig.repo
//                r.project = simpleRepoConfig.project
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
//            { assertThat(head?.branches).hasSize(1) },
//            { assertThat(head?.branches?.map { it.name }).contains(branch) },
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
            { assertThat(repo.user).hasSize(3) },
        )


        val newVcsCommit =
            run {
                // Find existing parent commit from the repo, or create a minimal one
                val parent = repo.commits.find { it.sha == "b51199ab8b83e31f64b631e42b2ee0b1c7e3259a" }
                    ?: run {
                        val committer = repo.user.firstOrNull() ?: User(
                            name = "Parent Committer",
                            repository = repo
                        ).apply { email = "parent@test.com" }
                        Commit(
                            sha = "b51199ab8b83e31f64b631e42b2ee0b1c7e3259a",
                            commitDateTime = LocalDateTime.now(),
                            authorDateTime = null,
                            message = null,
                            repository = repo,
                            committer = committer,
                        )
                    }

                val child = this.testData.commits[1]
                requireNotNull(child.committer) {
                    "Child commit ${child.sha} must have a committer"
                }
                child.parents.add(parent)
//                requireNotNull(repo.branches.find { it.name == "master" }) {
//                    "Branch master must exist here"
//                }.commits.add(child)

                return@run child
            }

        val repo2 =
            requireNotNull(this.repositoryService.findRepo("${FIXTURES_PATH}/${SIMPLE_REPO}")) {
                "repository '${FIXTURES_PATH}/${SIMPLE_REPO}' not found (2)"
            }.let {
                it.commits.add(newVcsCommit)
                this.repositoryService.addCommits(it, listOf(newVcsCommit))
            }
        assertThat(repo2).isNotSameAs(repo)

        assertAll(
            { assertThat(repo2.commits).hasSize(15) },
            { assertThat(repo2.branches).hasSize(1) },
            { assertThat(repo2.branches.toList()[0].commits).hasSize(15) },
            { assertThat(repo2.user).hasSize(4) },
        )
    }

    @Test
    fun `update simple repo, add another commit, new branch`() {
        run {
            val repo = requireNotNull(this.repositoryService.findRepo("${FIXTURES_PATH}/${SIMPLE_REPO}")) {
                "repository '${FIXTURES_PATH}/${SIMPLE_REPO}' not found (1)"
            }

            assertAll(
                { assertThat(repo.commits).hasSize(14) },
                { assertThat(repo.branches).hasSize(1) },
                { assertThat(repo.branches.toList()[0].commits).hasSize(14) },
                { assertThat(repo.user).hasSize(3) },
            )
        }

        val project = Project(name = SIMPLE_PROJECT_NAME)

        // required to manipulate history
        var hashes =
            run {
                val path = Path("${FIXTURES_PATH}/${SIMPLE_REPO}")
                val repo = indexer.findRepo(path, project)
                val hashes = indexer.traverseBranch(repo, "master")
                hashes.second.map {
                    val branchField = Commit::class.java.getDeclaredField("branch")
                    branchField.isAccessible = true
                    branchField.set(it, "develop")
                }
                hashes.second
            }
        hashes = hashes.toMutableList()
        val parent = requireNotNull(hashes.find { it.sha == "8f34ebee8f593193048f8bcbf848501bf2465865" }) {
            "must find parent here"
        }
        hashes.add(
            run {
                val cmt = this.testData.commits[0]
                // Committer is already set in testData, just set author
                cmt.author = this.testData.users[1]
                cmt.parents.add(parent)

                // Note: Branch is added to repository separately if needed

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

//            TODO check behaviour of Git in case of adding a new commit
            assertAll(
                "repo2",
                { assertThat(repo.commits).hasSize(15) },
                { assertThat(repo.branches).hasSize(2) },
                { assertThat(repo.user).hasSize(5) },
                { assertThat(repo.branches.map { it.commits.count() }).containsAll(listOf(14, 3)) },
                { assertThat(repo.branches.map { it.name }).containsAll(listOf("master", "new one")) },
            )
        }
    }
}
