package com.inso_world.binocular.core.service

import com.inso_world.binocular.model.Repository

interface RepositoryInfrastructurePort : BinocularInfrastructurePort<Repository> {
    fun findByName(gitDir: String): Repository?
}
