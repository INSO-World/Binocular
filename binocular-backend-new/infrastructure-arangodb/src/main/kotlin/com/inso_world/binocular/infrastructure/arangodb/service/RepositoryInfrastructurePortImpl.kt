package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.model.Repository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class RepositoryInfrastructurePortImpl : RepositoryInfrastructurePort {
    var logger: Logger = LoggerFactory.getLogger(RepositoryInfrastructurePortImpl::class.java)

    override fun findAll(): Iterable<Repository> {
        TODO("Not yet implemented")
    }

    override fun findAll(pageable: Pageable): Page<Repository> {
        TODO("Not yet implemented")
    }

    override fun findById(id: String): Repository? {
        TODO("Not yet implemented")
    }

    override fun save(entity: Repository): Repository {
        TODO("Not yet implemented")
    }

    override fun saveAll(entities: Collection<Repository>): Iterable<Repository> {
        TODO("Not yet implemented")
    }

    override fun delete(entity: Repository) {
        TODO("Not yet implemented")
    }

    override fun update(entity: Repository): Repository {
        TODO("Not yet implemented")
    }

    override fun updateAndFlush(entity: Repository): Repository {
        TODO("Not yet implemented")
    }

    override fun findByName(gitDir: String): Repository? {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: String) {
        TODO("Not yet implemented")
    }
}
