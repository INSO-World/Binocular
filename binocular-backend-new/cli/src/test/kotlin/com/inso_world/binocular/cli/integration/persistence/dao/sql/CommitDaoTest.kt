package com.inso_world.binocular.cli.integration.persistence.dao.sql

import com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest
import com.inso_world.binocular.cli.persistence.dao.sql.interfaces.ICommitDao
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired

internal class CommitDaoTest(
  @Autowired private val commitDao: ICommitDao
) : BasePersistenceTest() {
  @Test
  fun simpleRepo_check_master_branch_leaf_node() {
    val masterLeaf = commitDao.findHeadForBranch(
      this.simpleRepo,
      "master"
    );
    // does not exist yet (branches not yet stored properly)
    assertAll(
      { assertThat(masterLeaf).isNotNull() },
      { assertThat(masterLeaf!!.repository!!.id).isEqualTo(this.simpleRepo.id) },
      { assertThat(masterLeaf!!.id).isNotNull() },
      { assertThat(masterLeaf!!.sha).isEqualTo("b51199ab8b83e31f64b631e42b2ee0b1c7e3259a") }
    )
  }

  @Test
  fun simpleRepo_check_null_branch_leaf_node() {
    val masterLeaf = commitDao.findAllLeafCommits(
      this.simpleRepo,
    ).toList();
//      null // null should mean every leaf node is returned
    assertAll(
      { assertThat(masterLeaf).isNotEmpty() },
      { assertThat(masterLeaf.count()).isEqualTo(1) },
      { assertThat(masterLeaf[0].repository!!.id).isEqualTo(this.simpleRepo.id) },
      { assertThat(masterLeaf[0].id).isNotNull() },
      { assertThat(masterLeaf[0].sha).isEqualTo("b51199ab8b83e31f64b631e42b2ee0b1c7e3259a") }
    )
  }

  @Test
  fun simpleRepo_check_non_existing_branch_leaf_node() {
    val masterLeaf = commitDao.findHeadForBranch(
      this.simpleRepo,
      "notexisting"
    );
    assertThat(masterLeaf).isNull()
  }

  @Test
  fun octoRepo_check_master_branch_leaf_node() {
    val masterLeaf = commitDao.findHeadForBranch(
      this.octoRepo,
      "master"
    );
    // does not exist yet (branches not yet stored properly)
    assertAll(
      { assertThat(masterLeaf).isNotNull() },
      { assertThat(masterLeaf!!.repository!!.id).isEqualTo(this.octoRepo.id) },
      { assertThat(masterLeaf!!.id).isNotNull() },
      { assertThat(masterLeaf!!.sha).isEqualTo("b51199ab8b83e31f64b631e42b2ee0b1c7e3259a") }
    )
  }

  @Test
  fun octoRepo_check_all_leaf_nodes() {
    val leafs = commitDao.findAllLeafCommits(
      this.octoRepo,
    );
    val leafsItems = listOf(
      "9d9f548a1c6a4a620031720e5453eb84dd40adda",
      "4b718511f7e233291f6a3dd522a22290cc246ec7",
      "d49ff1dd82b47fb466c3e97f4aa2db2121fe23ba",
      "ed167f854e871a1566317302c158704f71f8d16c" // imported?
    )
    assertAll(
      { assertThat(leafs).isNotEmpty() },
      { assertThat(leafs.count()).isEqualTo(4) },
      { assertThat(leafs.map { it.sha }).containsAll(leafsItems) },
//      { assertThat(leafs!!.id).isNotNull() },
//      { assertThat(leafs!!.sha).isEqualTo("b51199ab8b83e31f64b631e42b2ee0b1c7e3259a") }
    )
  }
}
