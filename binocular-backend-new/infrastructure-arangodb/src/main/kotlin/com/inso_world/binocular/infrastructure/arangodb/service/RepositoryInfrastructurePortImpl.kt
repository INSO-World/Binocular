package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.CommitDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.RepositoryDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.RepositoryMapper
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
internal class RepositoryInfrastructurePortImpl : RepositoryInfrastructurePort,
    AbstractInfrastructurePort<Repository, String>() {

    @PostConstruct
    fun init() {
        super.dao = repositoryDao
    }
    companion object {
        val logger by logger()
    }

    @Autowired
    private lateinit var commitDao: CommitDao

    @Autowired
    private lateinit var repositoryDao: RepositoryDao

    @Autowired
    private lateinit var repositoryMapper: RepositoryMapper

    @MappingSession
    override fun findByIid(iid: Repository.Id): Repository? {
        TODO("Not yet implemented")
    }

    @MappingSession
    override fun findAll(): Iterable<Repository> = this.repositoryDao.findAll()

    @MappingSession
    override fun findAll(pageable: Pageable): Page<Repository> = this.repositoryDao.findAll(pageable)

    @MappingSession
    override fun findById(id: String): Repository? = this.repositoryDao.findById(id)

    override fun create(value: Repository): Repository {
        val mappedEntity = repositoryMapper.toEntity(value)
        val savedEntity = this.repositoryDao.create(mappedEntity)
        return repositoryMapper.toDomain(savedEntity)
    }

    override fun saveAll(values: Collection<Repository>): Iterable<Repository> = this.repositoryDao.saveAll(values)

    override fun update(value: Repository): Repository {
        TODO("Not yet implemented")
    }

    @MappingSession
    override fun findByName(name: String): Repository? = this.repositoryDao.findByName(name)?.let { this.repositoryMapper.toDomain(it) }

    @MappingSession
    override fun findExistingCommits(
        repo: Repository,
        shas: Set<String>,
    ): Sequence<Commit> {
        TODO("Not yet implemented")
    }

    @MappingSession
    override fun findBranch(
        repository: Repository,
        name: String,
    ): Branch? {
        TODO("Not yet implemented")
    }
}
