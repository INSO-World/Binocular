package com.inso_world.binocular.core.service

import com.inso_world.binocular.model.Project

interface ProjectInfrastructurePort : BinocularInfrastructurePort<Project, Project.Id> {
    fun findByName(name: String): Project?
}
