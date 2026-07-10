package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.IDao
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Reference

internal interface IBranchDao : IDao<Branch, String> {
    fun findByName(name: String): Branch?

    fun findByIid(iid: Reference.Id): Branch?

    fun findByRepositoryAndName(
        repoPath: String,
        name: String
    ): Branch?
}
