package com.inso_world.binocular.cli.integration.persistence.dao.sql

import com.inso_world.binocular.cli.index.vcs.toVcsRepository
import com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceNoDataTest
import com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest
import com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceWithDataTest
import com.inso_world.binocular.cli.integration.utils.generateCommits
import com.inso_world.binocular.cli.integration.utils.setupRepoConfig
import com.inso_world.binocular.cli.service.addCommit
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
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
import java.time.LocalDateTime
import java.util.stream.Stream

internal class CommitDaoTest(
    @Autowired private val commitDao: CommitInfrastructurePort,
) : BasePersistenceTest() {
    @Autowired
    private lateinit var projectDao: ProjectInfrastructurePort

    @Autowired
    private lateinit var repositoryDao: RepositoryInfrastructurePort

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
                projectDao.save(
                    Project(
                        name = "test",
                    ),
                )
            project.repo =
                repositoryDao.save(
                    Repository(
                        id = null,
                        name = "testRepository",
                        projectId = project.id,
                    ),
                )
            project = projectDao.update(project)
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
                    commitDao.save(
                        Commit(
                            sha = invalidSha,
                            message = "msg",
                            commitDateTime = LocalDateTime.of(2024, 1, 1, 1, 1),
                            authorDateTime = LocalDateTime.of(2024, 1, 1, 1, 1),
                        ),
                    )
                }
            assertThat(exception.message).contains("save.domain.sha")
            entityManager.clear()
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest#provideBlankStrings")
        fun `commit with invalid sha value should fail`(invalidSha: String) {
            val exception =
                assertThrows<jakarta.validation.ConstraintViolationException> {
                    commitDao.save(
                        Commit(
                            sha = invalidSha,
                            message = "msg",
                            commitDateTime = LocalDateTime.of(2024, 1, 1, 1, 1),
                            authorDateTime = LocalDateTime.of(2024, 1, 1, 1, 1),
                            repositoryId = "asdf",
                        ),
                    )
                }
            assertThat(exception.message).contains("save.domain.sha")
            entityManager.clear()
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest#provideBlankStrings")
        fun `commit with invalid message should fail`(invalidMessage: String) {
            // Then - This should fail due to validation constraint
            val exception =
                assertThrows<jakarta.validation.ConstraintViolationException> {
                    commitDao.save(
                        Commit(
                            sha = "091618c311d7c539c0ec316d0a86a6dbee6a3943",
                            message = invalidMessage,
                        ),
                    )
                }
            assertThat(exception.message).contains("save.domain.message")
            entityManager.clear()
        }

        @ParameterizedTest
        @MethodSource(
            "com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest#provideAllowedPastOrPresentDateTime",
        )
        fun `commit with valid commitDateTime should not fail`(validCommitTime: LocalDateTime) {
            val cmt =
                Commit(
                    sha = "091618c311d7c539c0ec316d0a86a6dbee6a3943",
                    message = "msg",
                    commitDateTime = validCommitTime,
                    authorDateTime = null,
                    repositoryId = project.repo?.id,
                )
            project.repo?.addCommit(cmt)
            assertDoesNotThrow {
                commitDao.save(cmt)
            }
        }

        @ParameterizedTest
        @MethodSource(
            "com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest#provideAllowedPastOrPresentDateTime",
        )
        fun `commit with valid authorDateTime should not fail`(validAuthorTime: LocalDateTime) {
            val cmt =
                Commit(
                    sha = "091618c311d7c539c0ec316d0a86a6dbee6a3943",
                    message = "msg",
                    commitDateTime = LocalDateTime.of(2024, 1, 1, 1, 1),
                    authorDateTime = validAuthorTime,
                    repositoryId = project.repo?.id,
                )
            project.repo?.addCommit(cmt)
            assertDoesNotThrow {
                commitDao.save(cmt)
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
                    commitDao.save(cmt)
                }
            assertThat(exception.message).contains("save.domain.commitDateTime")
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
                    commitDao.save(cmt)
                }
            assertThat(exception.message).contains("save.domain.authorDateTime")
            entityManager.clear()
        }
    }

    @Nested
    inner class FilledDatabase : BasePersistenceWithDataTest() {
        @Test
        fun simpleRepo_check_master_branch_leaf_node() {
            val masterLeaf =
                commitDao.findHeadForBranch(
                    this.simpleRepo,
                    "master",
                )
            assertAll(
                { assertThat(masterLeaf).isNotNull() },
                { assertThat(masterLeaf?.repository?.id).isEqualTo(this.simpleRepo.id) },
                { assertThat(masterLeaf?.id).isNotNull() },
                { assertThat(masterLeaf?.sha).isEqualTo("b51199ab8b83e31f64b631e42b2ee0b1c7e3259a") },
            )
        }

        @Test
        fun simpleRepo_check_null_branch_leaf_node() {
            val masterLeaf =
                commitDao
                    .findAllLeafCommits(
                        this.simpleRepo,
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
        fun simpleRepo_check_non_existing_branch_leaf_node() {
            val masterLeaf =
                commitDao.findHeadForBranch(
                    this.simpleRepo,
                    "notexisting",
                )
            assertThat(masterLeaf).isNull()
        }

        @Test
        fun octoRepo_check_master_branch_leaf_node() {
            val masterLeaf =
                commitDao.findHeadForBranch(
                    this.octoRepo,
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
        fun octoRepo_check_all_leaf_nodes() {
            this.cleanup()

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
                    localRepo ?: {
                        val r = octoRepoConfig.repo.toVcsRepository().toDomain()
                        r.project = octoRepoConfig.project
                        octoRepoConfig.project.repo = r
                        projectDao.save(octoRepoConfig.project).repo ?: throw IllegalStateException("project not found")
                    }()
                generateCommits(octoRepoConfig, tmpRepo)
                tmpRepo = this.repositoryRepository.update(tmpRepo)
                return tmpRepo
            }

            var localRepo = genBranchCommits(null, "master")
            localRepo = genBranchCommits(localRepo, "bugfix")
            localRepo = genBranchCommits(localRepo, "feature")
            localRepo = genBranchCommits(localRepo, "imported")

            val leafs =
                commitDao.findAllLeafCommits(
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
