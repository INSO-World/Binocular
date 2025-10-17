package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IRepositoryDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.RepositoryRepository
import com.inso_world.binocular.model.Repository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository as SpringRepository

@SpringRepository
class RepositoryDao @Autowired constructor(
    private val repositoryRepository: RepositoryRepository,
    repositoryMapper: RepositoryMapper,
) : MappedArangoDbDao<Repository, RepositoryEntity, String>(repositoryRepository, repositoryMapper), IRepositoryDao {

    @Autowired
    private lateinit var projectMapper: ProjectMapper

    override fun findByName(name: String): Repository? {
        return this.repositoryRepository.findByName(name)?.let {
            this.mapper.toDomain(it)
        }
    }

    override fun create(entity: Repository): Repository {
        val mappedEntity = mapper.toEntity(entity)
        return repository.save(mappedEntity).let {
            mapper.toDomain(it)
        }.apply {
            project = entity.project
            requireNotNull(entity.project).repo = this
        }
    }
}

