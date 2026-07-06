package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.IDao
import com.inso_world.binocular.model.Module
import com.inso_world.binocular.model.metrics.AuthorCountPerModule
import com.inso_world.binocular.model.metrics.CiRatePerModule

internal interface IModuleDao : IDao<Module, String>{
    fun countAuthorCommitsByModule(neededModules: List<String>): List<AuthorCountPerModule>

    fun findCiErrorRateByModule(since: Long, until: Long, neededModules: List<String>): List<CiRatePerModule>
}
