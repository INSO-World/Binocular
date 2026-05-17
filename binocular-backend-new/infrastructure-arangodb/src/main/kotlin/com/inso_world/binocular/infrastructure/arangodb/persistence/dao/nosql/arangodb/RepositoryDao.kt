package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IRepositoryDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.*
import com.inso_world.binocular.model.Repository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository as SpringRepository

@SpringRepository
internal class RepositoryDao @Autowired constructor(
    private val repositoryRepository: RepositoryRepository,
    private val repositoryMapper: RepositoryMapper,
)
    :
    MappedArangoDbDao<Repository, RepositoryEntity, String>(repositoryRepository, repositoryMapper),
    IRepositoryDao
{

    companion object {
        val logger by logger()
    }

    @Autowired @Lazy
    private lateinit var projectDao: ProjectDao

    @Autowired
    private lateinit var commitRepository: CommitRepository

    @Autowired
    private lateinit var branchRepository: BranchRepository

    @Autowired
    private lateinit var developerRepository: DeveloperRepository

    @Autowired
    private lateinit var fileRepository: FileRepository

    @MappingSession
    override fun findByName(name: String): RepositoryEntity? {
        return this.repositoryRepository.findByLocalPath(name)
    }

    @MappingSession
    fun create(entity: RepositoryEntity): RepositoryEntity {
        val savedEntity = repositoryRepository.save(entity)

        entity.commits.forEach { commitRepository.save(it) }

        entity.files.forEach { fileRepository.save(it) }

        return savedEntity
    }
}