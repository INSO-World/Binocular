package com.inso_world.binocular.cli.persistence.dao.sql.interfaces

import com.inso_world.binocular.cli.entity.Repository
import com.inso_world.binocular.core.persistence.dao.interfaces.IDao
import java.util.*

interface IRepositoryDao : IDao<Repository, Long> {
  fun findByName(gitDir: String): Repository?
}
