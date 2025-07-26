package com.inso_world.binocular.cli.integration.persistence.dao.sql

import com.inso_world.binocular.cli.index.vcs.toDtos
import com.inso_world.binocular.cli.index.vcs.toVcsRepository
import com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceNoDataTest
import com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest
import com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceWithDataTest
import com.inso_world.binocular.cli.integration.utils.generateCommits
import com.inso_world.binocular.cli.integration.utils.setupRepoConfig
import com.inso_world.binocular.cli.service.RepositoryService
import com.inso_world.binocular.cli.service.addCommit
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest.Companion.FIXTURES_PATH
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest.Companion.OCTO_PROJECT_NAME
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest.Companion.OCTO_REPO
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest.Companion.SIMPLE_PROJECT_NAME
import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest.Companion.SIMPLE_REPO
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import java.time.LocalDateTime
import java.util.stream.Stream

internal class CommitDaoTest(
    @Autowired private val commitPort: CommitInfrastructurePort,
) : BasePersistenceTest() {
    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    @Lazy
    private lateinit var repoService: RepositoryService

    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePort

    companion object {
        @JvmStatic
        fun invalidCommitTime(): Stream<Arguments> =
            Stream.concat(
                provideInvalidPastOrPresentDateTime(),
                Stream.of(null),
            )
    }

    @Nested
    inner class CleanDatabase : BasePersistenceNoDataTest() {
        lateinit var project: Project

        @BeforeEach
        fun setUp() {
            project =
                projectPort.create(
                    Project(
                        name = "test",
                    ),
                )
            project.repo =
                repositoryPort.create(
                    Repository(
                        id = null,
                        name = "testRepository",
                        project = project,
                    ),
                )
            project = projectPort.update(project)
        }

        @ParameterizedTest
        @CsvSource(
            value = [
                "d9db3f4c2975616834504e9191a80fcd0f94ef", // 38 chars
                "75c7762c536f491d3b7b4be1cfa8f22c808bdd2", // 39 chars
                "cd1150dc719edcff6b660cf5ef25976445fb09b75", // 41 chars
                "89e2d9a1f6c6dba6ede92619cd5935bb5b07420bef", // 42 chars
            ],
        )
        fun `commit with invalid sha length should fail`(invalidSha: String) {
            val exception =
                assertThrows<jakarta.validation.ConstraintViolationException> {
                    commitPort.create(
                        Commit(
                            sha = invalidSha,
                            message = "msg",
                            commitDateTime = LocalDateTime.of(2024, 1, 1, 1, 1),
                            authorDateTime = LocalDateTime.of(2024, 1, 1, 1, 1),
                            repositoryId = this.project.repo?.id,
                        ),
                    )
                }
            assertThat(exception.message).contains(".value.sha")
            entityManager.clear()
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest#provideBlankStrings")
        fun `commit with invalid sha value should fail`(invalidSha: String) {
            val exception =
                assertThrows<jakarta.validation.ConstraintViolationException> {
                    commitPort.create(
                        Commit(
                            sha = invalidSha,
                            message = "msg",
                            commitDateTime = LocalDateTime.of(2024, 1, 1, 1, 1),
                            authorDateTime = LocalDateTime.of(2024, 1, 1, 1, 1),
                            repositoryId = this.project.repo?.id,
                            branches =
                                mutableSetOf(
                                    Branch(
                                        name = "test",
                                    ),
                                ),
                        ),
                    )
                }
            assertThat(exception.message).contains(".value.sha")
            entityManager.clear()
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest#provideBlankStrings")
        fun `commit with invalid message should fail`(invalidMessage: String) {
            // Then - This should fail due to validation constraint
            val exception =
                assertThrows<jakarta.validation.ConstraintViolationException> {
                    commitPort.create(
                        Commit(
                            sha = "091618c311d7c539c0ec316d0a86a6dbee6a3943",
                            message = invalidMessage,
                        ),
                    )
                }
            assertThat(exception.message).contains(".value.message")
            entityManager.clear()
        }

        @ParameterizedTest
        @MethodSource(
            "com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest#provideAllowedPastOrPresentDateTime",
        )
        fun `commit with valid commitDateTime should not fail`(validCommitTime: LocalDateTime) {
            val user =
                User(
                    name = "test",
                    email = "test@example.com",
                )
            val cmt =
                Commit(
                    sha = "091618c311d7c539c0ec316d0a86a6dbee6a3943",
                    message = "msg",
                    commitDateTime = validCommitTime,
                    authorDateTime = null,
                    repositoryId = project.repo?.id,
                    branches =
                        mutableSetOf(
                            Branch(
                                name = "test",
                            ),
                        ),
                )
            user.addCommittedCommit(cmt)
            project.repo?.addCommit(cmt)
            project.repo?.user?.add(user)
            assertDoesNotThrow {
                commitPort.create(cmt)
            }
        }

        @ParameterizedTest
        @MethodSource(
            "com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest#provideAllowedPastOrPresentDateTime",
        )
        fun `commit with valid authorDateTime should not fail`(validAuthorTime: LocalDateTime) {
            val user =
                User(
                    name = "test",
                    email = "test@example.com",
                )
            val cmt =
                Commit(
                    sha = "091618c311d7c539c0ec316d0a86a6dbee6a3943",
                    message = "msg",
                    commitDateTime = LocalDateTime.of(2024, 1, 1, 1, 1),
                    authorDateTime = validAuthorTime,
                    repositoryId = project.repo?.id,
                    branches =
                        mutableSetOf(
                            Branch(
                                name = "test",
                            ),
                        ),
                )
            user.addCommittedCommit(cmt)
            project.repo?.addCommit(cmt)
            project.repo?.user?.add(user)
            assertDoesNotThrow {
                commitPort.create(cmt)
            }
        }

        @ParameterizedTest
        @MethodSource(
            "com.inso_world.binocular.cli.integration.persistence.dao.sql.CommitDaoTest#invalidCommitTime",
        )
        fun `commit with invalid commitDateTime should fail`(invalidCommitTime: LocalDateTime?) {
            val cmt =
                Commit(
                    sha = "091618c311d7c539c0ec316d0a86a6dbee6a3943",
                    message = "msg",
                    commitDateTime = invalidCommitTime,
                    authorDateTime = null,
                    repositoryId = project.repo?.id,
                )
            project.repo?.addCommit(cmt)
            val exception =
                assertThrows<jakarta.validation.ConstraintViolationException> {
                    commitPort.create(cmt)
                }
            assertThat(exception.message).contains(".value.commitDateTime")
            entityManager.clear()
        }

        @ParameterizedTest
        @MethodSource(
            "com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest#provideInvalidPastOrPresentDateTime",
        )
        fun `commit with invalid authorDateTime should fail`(invalidAuthorTime: LocalDateTime?) {
            val cmt =
                Commit(
                    sha = "091618c311d7c539c0ec316d0a86a6dbee6a3943",
                    message = "msg",
                    commitDateTime = LocalDateTime.of(2024, 1, 1, 1, 1),
                    authorDateTime = invalidAuthorTime,
                    repositoryId = project.repo?.id,
                )
            project.repo?.addCommit(cmt)
            val exception =
                assertThrows<jakarta.validation.ConstraintViolationException> {
                    commitPort.create(cmt)
                }
            assertThat(exception.message).contains(".value.authorDateTime")
            entityManager.clear()
        }

        @Nested
        inner class SimpleRepo {
            @BeforeEach
            fun setup() {
//                cleanup the stuff from upper class
                cleanup()
            }

            @Test
            fun `index repo, expect all commits in database`() {
                val project =
                    projectPort.create(
                        Project(
                            name = "test",
                        ),
                    )


                val cfg = setupRepoConfig(
                    "${FIXTURES_PATH}/${SIMPLE_REPO}",
                    "HEAD",
                    projectName = SIMPLE_PROJECT_NAME,
                )

                repoService.addCommits(cfg.repo, cfg.hashes.toDtos(), project)

                val allCommits = commitPort.findAll()
                assertThat(allCommits).hasSize(14)
                run {
//                    check relationship for HEAD
                    val cmt = allCommits.find { it.sha == "b51199ab8b83e31f64b631e42b2ee0b1c7e3259a" }
                        ?: throw IllegalStateException("must find commit here")
                    val child = allCommits.find { it.sha == "3d28b65c324cc8ee0bb7229fb6ac5d7f64129e90" }
                        ?: throw IllegalStateException("must find commit here")
                    assertThat(cmt.children).isEmpty()
                    assertThat(cmt.parents).hasSize(1)
                    assertThat(cmt.parents.toList()[0]).isSameAs(child)
                    assertThat(child.children).hasSize(1)
                    assertThat(child.children.toList()[0]).isSameAs(cmt)
                }
                run {
//                    check relationship for somewhere in the middle
                    val suspect = allCommits.find { it.sha == "97babe02ece29439d6f71201067b2c71d3352a81" }
                        ?: throw IllegalStateException("must find commit here")
                    val child = allCommits.find { it.sha == "2403472fd3b2c4487f66961929f1e5895c5013e1" }
                        ?: throw IllegalStateException("must find commit here")
                    val parent = allCommits.find { it.sha == "f56209474698e776112b442b5426d37f36b6c41d" }
                        ?: throw IllegalStateException("must find commit here")
                    assertAll(
                        "check suspect",
                        { assertThat(suspect.children).hasSize(1) },
                        { assertThat(suspect.parents).hasSize(1) },
                        { assertThat(suspect.parents.toList()[0]).isSameAs(parent) },
                        { assertThat(suspect.children.toList()[0]).isSameAs(child) }
                    )
                    assertAll(
                        "check child",
                        { assertThat(child.parents).hasSize(1) },
                        { assertThat(child.parents.toList()[0]).isSameAs(suspect) }
                    )
                    assertAll(
                        "check parent",
                        { assertThat(parent.children).hasSize(1) },
                        { assertThat(parent.children.toList()[0]).isSameAs(suspect) }
                    )
                }
                run {
                    val suspect = allCommits.find { it.sha == "48a384a6a9188f376835005cd10fd97542e69bf7" }
                        ?: throw IllegalStateException("must find commit here")
                    val child = allCommits.find { it.sha == "8f34ebee8f593193048f8bcbf848501bf2465865" }
                        ?: throw IllegalStateException("must find commit here")
                    assertAll(
                        "check suspect",
                        { assertThat(suspect.children).hasSize(1) },
                        { assertThat(suspect.parents).isEmpty() },
                        { assertThat(suspect.children.toList()[0]).isSameAs(child) }
                    )
                    assertAll(
                        "check child",
                        { assertThat(child.parents).hasSize(1) },
                        { assertThat(child.parents.toList()[0]).isSameAs(suspect) }
                    )
                }
            }
        }

        @Nested
        inner class OctoRepo {
            @BeforeEach
            fun setup() {
                //                cleanup the stuff from upper class
                cleanup()
            }

            @Test
            fun `index repo, expect all commits in database`() {
                val project =
                    projectPort.create(
                        Project(
                            name = "test",
                        ),
                    )

                val cfg = setupRepoConfig(
                    "${FIXTURES_PATH}/${OCTO_REPO}",
                    "HEAD",
                    projectName = OCTO_PROJECT_NAME,
                )

                repoService.addCommits(cfg.repo, cfg.hashes.toDtos(), project)

                val allCommits = commitPort.findAll()
                assertThat(allCommits).hasSize(19)
                run {
//                    check relationship for HEAD
                    val suspect = allCommits.find { it.sha == "4dedc3c738eee6b69c43cde7d89f146912532cff" }
                        ?: throw IllegalStateException("must find suspect commit here")
                    val parents = run {
                        val mapped = allCommits.associateBy { it.sha }

                        val cmtBeforeBranching = mapped["f556329d268afeb5e5298e37fd8bfb5ef2058a9d"]
                            ?: throw IllegalStateException("must find master commit here")
                        val octo3 = mapped["bf51258d6da9aaca9b75e2580251539026b6246a"]
                            ?: throw IllegalStateException("must find octo3 commit here")
                        val octo2 = mapped["d5d38cc858bd78498efbe0005052f5cb1fd38cb9"]
                            ?: throw IllegalStateException("must find octo2 commit here")
                        val octo1 = mapped["42fbbe93509ed894cbbd61e4dbc07a440720c491"]
                            ?: throw IllegalStateException("must find octo1 commit here")

                        assertAll(
                            "check parent cmtBeforeBranching",
                            { assertThat(cmtBeforeBranching.children).hasSize(4) },
                            { assertThat(cmtBeforeBranching.children).contains(suspect) }
                        )
                        assertAll(
                            "check parent octo1",
                            { assertThat(octo1.children).hasSize(1) },
                            { assertThat(octo1.children.toList()[0]).isSameAs(suspect) }
                        )
                        assertAll(
                            "check parent octo2",
                            { assertThat(octo2.children).hasSize(1) },
                            { assertThat(octo2.children.toList()[0]).isSameAs(suspect) }
                        )
                        assertAll(
                            "check parent octo3",
                            { assertThat(octo3.children).hasSize(1) },
                            { assertThat(octo3.children.toList()[0]).isSameAs(suspect) }
                        )

                        setOf(cmtBeforeBranching, octo3, octo2, octo1)
                    }

                    assertAll("check suspect",
                        { assertThat(suspect.children).isEmpty() },
                        { assertThat(suspect.parents).hasSize(4) },
                        { assertThat(suspect.parents).containsAll(parents) }
                    )
                }
                run {
//                    check relationship for somewhere in the middle
                    val suspect = allCommits.find { it.sha == "e236fdb066254a9a6acfbc5517b3865c09586831" }
                        ?: throw IllegalStateException("must find commit here")
                    val child = allCommits.find { it.sha == "abe9605d4e1fe269089f615aee4736103b5318ca" }
                        ?: throw IllegalStateException("must find commit here")
                    val parent = allCommits.find { it.sha == "69f39f7e4a8d201333e5125a5e27381bc2b874d4" }
                        ?: throw IllegalStateException("must find commit here")
                    assertAll(
                        "check suspect",
                        { assertThat(suspect.children).hasSize(1) },
                        { assertThat(suspect.parents).hasSize(1) },
                        { assertThat(suspect.parents.toList()[0]).isSameAs(parent) },
                        { assertThat(suspect.children.toList()[0]).isSameAs(child) }
                    )
                    assertAll(
                        "check child",
                        { assertThat(child.parents).hasSize(1) },
                        { assertThat(child.parents.toList()[0]).isSameAs(suspect) }
                    )
                    assertAll(
                        "check parent",
                        { assertThat(parent.children).hasSize(1) },
                        { assertThat(parent.children.toList()[0]).isSameAs(suspect) }
                    )
                }
                run {
                    val suspect = allCommits.find { it.sha == "8bf17bb514dd0fb3d763d19e2e0d67ba3129a61e" }
                        ?: throw IllegalStateException("must find commit here")
                    val child = allCommits.find { it.sha == "6b1155bb139e8c984f9b5343bf4595d1c98e516d" }
                        ?: throw IllegalStateException("must find commit here")
                    assertAll(
                        "check suspect",
                        { assertThat(suspect.children).hasSize(1) },
                        { assertThat(suspect.parents).isEmpty() },
                        { assertThat(suspect.children.toList()[0]).isSameAs(child) }
                    )
                    assertAll(
                        "check child",
                        { assertThat(child.parents).hasSize(1) },
                        { assertThat(child.parents.toList()[0]).isSameAs(suspect) }
                    )
                }
            }
        }
    }

    @Nested
    inner class FilledDatabase : BasePersistenceWithDataTest() {

        @Nested
        inner class SimpleRepo {

            @Test
            fun `simpleRepo, check master branch leaf node`() {
                val masterLeaf =
                    commitPort.findHeadForBranch(
                        simpleRepo,
                        "master",
                    )
                assertAll(
                    { assertThat(masterLeaf).isNotNull() },
                    { assertThat(masterLeaf?.repositoryId).isEqualTo(simpleRepo.id) },
                    { assertThat(masterLeaf?.id).isNotNull() },
                    { assertThat(masterLeaf?.sha).isEqualTo("b51199ab8b83e31f64b631e42b2ee0b1c7e3259a") },
                )
            }

            @Test
            fun `simpleRepo, check null branch leaf node`() {
                val masterLeaf =
                    commitPort
                        .findAllLeafCommits(
                            simpleRepo,
                        ).toList()
                assertAll(
                    { assertThat(masterLeaf).isNotEmpty() },
                    { assertThat(masterLeaf).hasSize(1) },
//                { assertThat(masterLeaf[0].repository!!.id).isEqualTo(this.simpleRepo.id) },
                    { assertThat(masterLeaf[0].id).isNotNull() },
                    { assertThat(masterLeaf[0].sha).isEqualTo("b51199ab8b83e31f64b631e42b2ee0b1c7e3259a") },
                )
            }

            @Test
            fun `simpleRepo, check nonexisting branch leaf node`() {
                val masterLeaf =
                    commitPort.findHeadForBranch(
                        simpleRepo,
                        "notexisting",
                    )
                assertThat(masterLeaf).isNull()
            }
        }

        @Nested
        inner class OctoRepo {
            @Test
            fun `octoRepo, check master branch leaf node`() {
                val masterLeaf =
                    commitPort.findHeadForBranch(
                        octoRepo,
                        "master",
                    )
                assertAll(
                    { assertThat(masterLeaf).isNotNull() },
//                { assertThat(masterLeaf!!.repository!!.id).isEqualTo(this.octoRepo.id) },
                    { assertThat(masterLeaf?.id).isNotNull() },
                    { assertThat(masterLeaf?.sha).isEqualTo("4dedc3c738eee6b69c43cde7d89f146912532cff") },
                    { assertThat(masterLeaf?.parents).hasSize(4) },
                    {
                        assertThat(masterLeaf?.parents?.map { it.sha }).containsAll(
                            listOf(
                                "f556329d268afeb5e5298e37fd8bfb5ef2058a9d",
                                "42fbbe93509ed894cbbd61e4dbc07a440720c491",
                                "d5d38cc858bd78498efbe0005052f5cb1fd38cb9",
                                "bf51258d6da9aaca9b75e2580251539026b6246a",
                            ),
                        )
                    },
                    { assertThat(masterLeaf?.branches).hasSize(1) },
                    { assertThat(masterLeaf?.branches?.toList()[0]?.name).isEqualTo("master") },
                )
            }

            @Test
            fun `octoRepo, check all leaf nodes`() {
                cleanup()

                fun genBranchCommits(
                    localRepo: Repository?,
                    branch: String,
                ): Repository {
                    val octoRepoConfig =
                        setupRepoConfig(
                            "${FIXTURES_PATH}/${OCTO_REPO}",
                            "HEAD",
                            branch,
                            projectName = OCTO_PROJECT_NAME,
                        )
                    var tmpRepo =
                        localRepo ?: run {
                            val r = octoRepoConfig.repo.toVcsRepository().toDomain()
                            r.project = octoRepoConfig.project
                            octoRepoConfig.project.repo = r
                            projectPort.create(octoRepoConfig.project).repo
                                ?: throw IllegalStateException("project not found")
                        }
                    generateCommits(repoService, octoRepoConfig, tmpRepo)
                    tmpRepo = repositoryPort.update(tmpRepo)
                    return tmpRepo
                }

                var localRepo = genBranchCommits(null, "master")
                localRepo = genBranchCommits(localRepo, "bugfix")
                localRepo = genBranchCommits(localRepo, "feature")
                localRepo = genBranchCommits(localRepo, "imported")

                val leafs =
                    commitPort.findAllLeafCommits(
                        localRepo,
                    )
                val leafsItems =
                    listOf(
                        "4dedc3c738eee6b69c43cde7d89f146912532cff", // master
                        "3e15df55908eefdb720a7bc78065bcadb6b9e9cc", // bugfix
                        "d16fb2d78e3d867377c078a03aadc5aa34bdb408", // feature
                        "ed167f854e871a1566317302c158704f71f8d16c", // imported
                    )
                assertAll(
                    { assertThat(leafs).isNotEmpty() },
                    { assertThat(leafs).hasSize(4) },
                    { assertThat(leafs.map { it.sha }).containsAll(leafsItems) },
                    {
                        assertThat(leafs.flatMap { it.branches.map { b -> b.name } }).containsAll(
                            listOf(
                                "master",
                                "bugfix",
                                "feature",
                                "imported",
                            ),
                        )
                    },
                )
            }
        }
    }
}

