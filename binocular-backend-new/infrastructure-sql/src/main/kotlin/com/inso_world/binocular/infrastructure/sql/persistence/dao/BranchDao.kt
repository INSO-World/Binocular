package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IBranchDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.repository.BranchRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.springframework.validation.annotation.Validated

/**
 * SQL implementation of IBranchDao.
 */
@Repository
@Validated
internal class BranchDao(
    @Autowired private val repo: BranchRepository,
) : SqlDao<BranchEntity, Long>(),
    IBranchDao {
    init {
        this.setClazz(BranchEntity::class.java)
        this.setRepository(repo)
    }
}
