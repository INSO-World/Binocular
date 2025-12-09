package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IRepositoryDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.RepositoryRepository
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

//    fun findAll(): Iterable<Repository> {
//        return this.repositoryRepository.findAll()
//    }

//    @Autowired
//    private lateinit var projectMapper: ProjectMapper

    override fun findByName(name: String): RepositoryEntity? {
        return this.repositoryRepository.findByLocalPath(name)
    }

    fun create(entity: RepositoryEntity): RepositoryEntity {
        val savedEntity = repositoryRepository.save(entity)

        val existingProject = this.projectDao.findByName(entity.project.name)
        if(existingProject == null) {
            this.projectDao.create(entity.project)
        }

        return savedEntity
    }
}

