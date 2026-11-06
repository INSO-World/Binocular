package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IProjectDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.ProjectRepository
import com.inso_world.binocular.model.Project
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
internal class ProjectDao @Autowired constructor(
    private val projectRepository: ProjectRepository,
    projectMapper: ProjectMapper,
) : MappedArangoDbDao<Project, ProjectEntity, String>(projectRepository, projectMapper), IProjectDao {

    companion object {
        val logger by logger()
    }

    @Autowired
    private lateinit var repositoryDao: RepositoryDao

    override fun findByName(name: String): Project? {
        return this.projectRepository.findByName(name)?.let {
            this.mapper.toDomain(it)
        }
    }

    fun create(entity: ProjectEntity): ProjectEntity {
        logger.debug("Creating new project: {}", entity)

        var savedEntity = projectRepository.save(entity)

        savedEntity = entity.repository?.let { repository ->
            val savedRepo = repositoryDao.create(repository)
            savedEntity.repository = savedRepo
            // update so that @Ref gets updated
            return@let projectRepository.save(savedEntity)
        } ?: savedEntity

        return savedEntity
    }

    @MappingSession
    override fun create(entity: Project): Project {
        logger.debug("Creating new project: {}", entity)

        val mappedEntity = mapper.toEntity(entity)
        var savedEntity = projectRepository.save(mappedEntity)

        savedEntity = mappedEntity.repository?.let { repository ->
            val savedRepo = repositoryDao.create(repository)
            savedEntity.repository = savedRepo
            // update so that @Ref gets updated
            return@let projectRepository.save(savedEntity)
        } ?: savedEntity

        return mapper.toDomain(savedEntity)
    }
}
