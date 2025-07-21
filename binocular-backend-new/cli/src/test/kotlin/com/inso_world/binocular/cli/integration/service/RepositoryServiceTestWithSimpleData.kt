package com.inso_world.binocular.cli.integration.service

import com.inso_world.binocular.cli.index.vcs.VcsCommit
import com.inso_world.binocular.cli.index.vcs.VcsPerson
import com.inso_world.binocular.cli.index.vcs.toDto
import com.inso_world.binocular.cli.index.vcs.toVcsRepository
import com.inso_world.binocular.cli.integration.service.base.BaseServiceTest
import com.inso_world.binocular.cli.integration.utils.generateCommits
import com.inso_world.binocular.cli.integration.utils.setupRepoConfig
import com.inso_world.binocular.cli.service.RepositoryService
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.ffi.BinocularFfi
import com.inso_world.binocular.ffi.pojos.BinocularRepositoryPojo
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime

internal class RepositoryServiceTestWithSimpleData(
    @Autowired private val repositoryService: RepositoryService,
    @Autowired
    private val commitDao: CommitInfrastructurePort,
    @Autowired @Lazy private val projectPort: ProjectInfrastructurePort,
) : BaseServiceTest() {
    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate
    private val ffi = BinocularFfi()

    @Transactional
    @Test
    fun `find existing simple repo`() {
        val repo = this.repositoryService.findRepo("${FIXTURES_PATH}/${SIMPLE_REPO}")
        assertAll(
            { assertThat(repo).isNotNull() },
            { assertThat(repo?.id).isNotNull() },
            { assertThat(this.simpleRepo).usingRecursiveComparison().isEqualTo(repo) },
            { assertThat(repo?.commits).hasSize(14) },
            { assertThat(repo?.user).hasSize(3) },
            { assertThat(repo?.branches).hasSize(1) },
            { assertThat(repo?.projectId).isNotNull() },
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
        var localRepo =
//            {
//                val r = simpleRepoConfig.repo.toVcsRepository().toDomain()
//                r.project = simpleRepoConfig.project
//                simpleRepoConfig.project.repo = r
//                projectPort.save(simpleRepoConfig.project).repo ?: throw IllegalStateException("project not found")
//            }()
            {
                val r = simpleRepoConfig.repo.toVcsRepository().toDomain()
                r.projectId = simpleRepoConfig.project.id
                simpleRepoConfig.project.repo = r
                r
            }()

        generateCommits(simpleRepoConfig, localRepo)
        localRepo = projectPort.create(simpleRepoConfig.project).repo ?: throw IllegalStateException("project not found")
        // = localRepo
//        localRepo = this.repositoryRepository.save(localRepo)

        val head = this.repositoryService.getHeadCommits(localRepo, branch)
        assertAll(
            { assertThat(head).isNotNull() },
            { assertThat(head?.sha).isEqualTo(headCommit) },
            { assertThat(head?.branches).hasSize(1) },
            { assertThat(head?.branches?.map { it.name }).contains(branch) },
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
    @Transactional
    fun `update simple repo, add another commit, same branch`() {
        val repo = this.repositoryService.findRepo("${FIXTURES_PATH}/${SIMPLE_REPO}")

        assertAll(
            { assertThat(repo?.commits).hasSize(14) },
            { assertThat(repo?.branches).hasSize(1) },
            { assertThat(repo?.branches?.toList()[0]?.commitShas).hasSize(14) },
            { assertThat(repo?.user).hasSize(3) },
        )

        val newVcsCommit =
            VcsCommit(
                "1234567890123456789012345678901234567890",
                "msg1",
                "master",
                VcsPerson("User A", "a@test.com"),
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                listOf("b51199ab8b83e31f64b631e42b2ee0b1c7e3259a"),
            )
        val vcsRepo =
            BinocularRepositoryPojo(
                gitDir = this.simpleRepo.name,
                workTree = null,
            ) // workTree & commonDir not relevant here
        this.repositoryService.addCommits(vcsRepo, listOf(newVcsCommit), simpleProject)

        val repo2 = this.repositoryService.findRepo("${FIXTURES_PATH}/${SIMPLE_REPO}")

        assertAll(
            { assertThat(repo2?.commits).hasSize(15) },
            { assertThat(repo2?.branches).hasSize(1) },
            { assertThat(repo2?.branches?.toList()[0]?.commitShas).hasSize(15) },
            { assertThat(repo2?.user).hasSize(4) },
        )
    }

    @Test
    @Transactional
    fun `update simple repo, add another commit, new branch`() {
        val repo = this.repositoryService.findRepo("${FIXTURES_PATH}/${SIMPLE_REPO}")

        assertAll(
            { assertThat(repo?.commits).hasSize(14) },
            { assertThat(repo?.branches).hasSize(1) },
            { assertThat(repo?.branches?.toList()[0]?.commitShas).hasSize(14) },
            { assertThat(repo?.user).hasSize(3) },
        )

        // required to manipulate history
        var hashes =
            ffi.traverseBranch(ffi.findRepo("${FIXTURES_PATH}/${SIMPLE_REPO}"), "master").map {
                it.branch = "develop"
                it.toDto()
            }
        hashes = hashes.toMutableList()
        hashes.add(
            VcsCommit(
                "1234567890123456789012345678901234567890",
                "msg1",
                "develop",
                VcsPerson("User C", "committer@test.com"),
                VcsPerson("User A", "author@test.com"),
                LocalDateTime.now(),
                LocalDateTime.now(),
                listOf("b51199ab8b83e31f64b631e42b2ee0b1c7e3259a"),
            ),
        )
        val vcsRepo =
            BinocularRepositoryPojo(
                gitDir = this.simpleRepo.name,
                workTree = null,
            ) // workTree & commonDir not relevant here
        this.repositoryService.addCommits(vcsRepo, hashes, simpleProject)

        val repo2 = this.repositoryService.findRepo("${FIXTURES_PATH}/${SIMPLE_REPO}")

        assertAll(
            "repo2",
            { assertThat(repo2?.commits).hasSize(15) },
            { assertThat(repo2?.branches).hasSize(2) },
            { assertThat(repo2?.user).hasSize(5) },
            { assertThat(repo2?.branches?.map { it.commitShas.count() }).containsAll(listOf(14, 1)) },
            { assertThat(repo2?.branches?.map { it.name }).containsAll(listOf("master", "develop")) },
        )
    }
}
