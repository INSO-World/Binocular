package com.inso_world.binocular.core.service

import com.inso_world.binocular.model.Project

interface ProjectInfrastructurePort : BinocularInfrastructurePort<Project> {
    fun findByIid(iid: Project.Id): Project?
    fun findByName(name: String): Project?
}
