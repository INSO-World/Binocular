package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.IDao
import com.inso_world.binocular.model.Project

internal interface IProjectDao : IDao<Project, String> {
    fun findByName(name: String): Project?
}
