package com.inso_world.binocular.cli.integration.persistence.dao.sql

import com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest
import com.inso_world.binocular.cli.persistence.dao.sql.interfaces.IRepositoryDao
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class RepositoryDaoTest(
    @Autowired val repositoryDao: IRepositoryDao,
) : BasePersistenceTest() {
    @BeforeEach
    fun setup() {
        this.cleanup()
    }

    @Test
    fun non_saved_should_return_no_repositories() {
        val repos = repositoryDao.findAll()
        assertThat(repos).isEmpty()
    }
}
