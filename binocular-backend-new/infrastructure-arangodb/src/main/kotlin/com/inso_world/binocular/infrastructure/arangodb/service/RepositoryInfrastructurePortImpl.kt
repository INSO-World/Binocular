package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.CommitDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.RepositoryDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.RepositoryMapper
import com.inso_world.binocular.model.Repository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
internal class RepositoryInfrastructurePortImpl : RepositoryInfrastructurePort {
    companion object {
        val logger by logger()
    }


    @Autowired
    private lateinit var commitDao: CommitDao

    @Autowired
    private lateinit var repositoryDao: RepositoryDao

    @Autowired
    private lateinit var repositoryMapper: RepositoryMapper

    override fun findByIid(iid: Repository.Id): Repository? {
        TODO("Not yet implemented")
    }

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
        val mappedEntity = repositoryMapper.toEntity(value)
        val savedEntity = this.repositoryDao.create(mappedEntity)
        return repositoryMapper.toDomain(savedEntity)
    }

    override fun saveAll(values: Collection<Repository>): Iterable<Repository> {
        return this.repositoryDao.saveAll(values)
    }

    override fun update(value: Repository): Repository {
        TODO("Not yet implemented")
    }

    override fun findByName(name: String): Repository? {
        return this.repositoryDao.findByName(name)?.let { this.repositoryMapper.toDomain(it) }
    }

}
