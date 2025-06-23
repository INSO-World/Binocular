package com.inso_world.binocular.cli.integration.persistence.dao.sql

import com.inso_world.binocular.cli.entity.Repository
import com.inso_world.binocular.cli.index.vcs.toVcsRepository
import com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest
import com.inso_world.binocular.cli.integration.utils.generateCommits
import com.inso_world.binocular.cli.integration.utils.setupRepoConfig
import com.inso_world.binocular.cli.persistence.dao.sql.interfaces.ICommitDao
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired

internal class CommitDaoTest(
    @Autowired private val commitDao: ICommitDao,
) : BasePersistenceTest() {
    @Test
    fun simpleRepo_check_master_branch_leaf_node() {
        val masterLeaf =
            commitDao.findHeadForBranch(
                this.simpleRepo,
                "master",
            )
        assertAll(
            { assertThat(masterLeaf).isNotNull() },
            { assertThat(masterLeaf!!.repository!!.id).isEqualTo(this.simpleRepo.id) },
            { assertThat(masterLeaf!!.id).isNotNull() },
            { assertThat(masterLeaf!!.sha).isEqualTo("b51199ab8b83e31f64b631e42b2ee0b1c7e3259a") },
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
            { assertThat(masterLeaf[0].repository!!.id).isEqualTo(this.simpleRepo.id) },
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
            { assertThat(masterLeaf!!.repository!!.id).isEqualTo(this.octoRepo.id) },
            { assertThat(masterLeaf!!.id).isNotNull() },
            { assertThat(masterLeaf!!.sha).isEqualTo("4dedc3c738eee6b69c43cde7d89f146912532cff") },
            { assertThat(masterLeaf!!.parents).hasSize(4) },
            {
                assertThat(masterLeaf!!.parents.map { it.sha }).containsAll(
                    listOf(
                        "f556329d268afeb5e5298e37fd8bfb5ef2058a9d",
                        "42fbbe93509ed894cbbd61e4dbc07a440720c491",
                        "d5d38cc858bd78498efbe0005052f5cb1fd38cb9",
                        "bf51258d6da9aaca9b75e2580251539026b6246a",
                    ),
                )
            },
            { assertThat(masterLeaf!!.branches).hasSize(1) },
            { assertThat(masterLeaf!!.branches.toList()[0].name).isEqualTo("master") },
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
                )
            var tmpRepo = octoRepoConfig.repo.toVcsRepository().toEntity()
            generateCommits(octoRepoConfig, localRepo ?: tmpRepo)
            tmpRepo = this.repositoryRepository.save(localRepo ?: tmpRepo)
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
