package com.inso_world.binocular.core.service

import com.inso_world.binocular.model.Repository
import org.springframework.transaction.annotation.Transactional

interface RepositoryInfrastructurePort : BinocularInfrastructurePort<Repository> {
    @Transactional(readOnly = true)
    fun findByName(name: String): Repository?

//    @Transactional(readOnly = true)
//    fun findAllBranches(repository: Repository): Iterable<Branch>
//
//    @Transactional(readOnly = true)
//    fun findAllCommits(repository: Repository): Iterable<Commit>
}
