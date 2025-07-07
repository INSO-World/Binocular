package com.inso_world.binocular.cli.persistence.dao.sql.interfaces

import com.inso_world.binocular.cli.entity.Project
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IDao

interface IProjectDao : IDao<Project, Long> {
    fun findByName(name: String): Project?
}
