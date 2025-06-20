package com.inso_world.binocular.cli.integration.persistence.dao.sql

import com.inso_world.binocular.cli.entity.Commit
import com.inso_world.binocular.cli.index.vcs.toDto
import com.inso_world.binocular.cli.index.vcs.toVcsRepository
import com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest
import com.inso_world.binocular.cli.persistence.dao.sql.interfaces.ICommitDao
import com.inso_world.binocular.cli.persistence.dao.sql.interfaces.IRepositoryDao
import com.inso_world.binocular.cli.persistence.dao.sql.interfaces.IUserDao
import com.inso_world.binocular.cli.uniffi.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired

internal class RepositoryDaoTest(
  @Autowired val repositoryDao: IRepositoryDao
) : BasePersistenceTest() {

  @Test
  fun non_saved_should_return_no_repositories() {
    val repos = repositoryDao.findAll();
    assertThat(repos).isEmpty()
  }

}
