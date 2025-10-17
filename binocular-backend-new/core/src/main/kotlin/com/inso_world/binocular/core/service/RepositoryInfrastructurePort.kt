package com.inso_world.binocular.core.service

import com.inso_world.binocular.model.Repository

interface RepositoryInfrastructurePort : BinocularInfrastructurePort<Repository> {
    fun findByName(name: String): Repository?

//    fun findAllBranches(repository: Repository): Iterable<Branch>
//
//    fun findAllCommits(repository: Repository): Iterable<Commit>
}
