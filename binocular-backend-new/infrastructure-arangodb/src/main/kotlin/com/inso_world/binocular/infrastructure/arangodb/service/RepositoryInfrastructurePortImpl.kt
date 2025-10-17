package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.RepositoryDao
import com.inso_world.binocular.model.Repository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class RepositoryInfrastructurePortImpl : RepositoryInfrastructurePort {
    companion object {
        val logger by logger()
    }


    @Autowired
    private lateinit var repositoryDao: RepositoryDao

    override fun findAll(): Iterable<Repository> {
        return this.repositoryDao.findAll()
    }

    override fun findAll(pageable: Pageable): Page<Repository> {
        return this.repositoryDao.findAll(pageable)
    }

    override fun findById(id: String): Repository? {
        return this.repositoryDao.findById(id)
    }

    override fun create(value: Repository): Repository {
        return this.repositoryDao.create(value)
    }

    override fun saveAll(values: Collection<Repository>): Iterable<Repository> {
        return this.repositoryDao.saveAll(values)
    }

    override fun delete(value: Repository) {
        return this.repositoryDao.delete(value)
    }

    override fun update(value: Repository): Repository {
        TODO("Not yet implemented")
    }

    override fun updateAndFlush(value: Repository): Repository {
        TODO("Not yet implemented")
    }

    override fun findByName(name: String): Repository? {
        return this.repositoryDao.findByName(name)
    }

    override fun deleteById(id: String) {
        this.repositoryDao.deleteById(id)
    }

    override fun deleteAll() {
        this.repositoryDao.deleteAll()
    }
}
