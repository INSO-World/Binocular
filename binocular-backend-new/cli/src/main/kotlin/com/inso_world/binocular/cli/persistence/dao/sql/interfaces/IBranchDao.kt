package com.inso_world.binocular.cli.persistence.dao.sql.interfaces

import com.inso_world.binocular.cli.entity.Branch
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IDao

interface IBranchDao : IDao<Branch, Long> {
    fun findByNameAndRepositoryId(
        name: String,
        repositoryId: Long,
    ): Branch?
}
