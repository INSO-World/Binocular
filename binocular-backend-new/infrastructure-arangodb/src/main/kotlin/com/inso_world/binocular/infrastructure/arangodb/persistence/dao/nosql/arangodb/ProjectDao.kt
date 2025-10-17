package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IProjectDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.ProjectRepository
import com.inso_world.binocular.model.Project
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class ProjectDao @Autowired constructor(
    private val projectRepository: ProjectRepository,
    projectMapper: ProjectMapper,
) : MappedArangoDbDao<Project, ProjectEntity, String>(projectRepository, projectMapper), IProjectDao {

    @Autowired
    private lateinit var repositoryDao: RepositoryDao

    override fun findByName(name: String): Project? {
        return this.projectRepository.findByName(name)?.let {
            this.mapper.toDomain(it)
        }
    }

    override fun create(entity: Project): Project {
        val mappedEntity = mapper.toEntity(entity)
        val savedEntity = projectRepository.save(mappedEntity)
        val mappedDomain = mapper.toDomain(savedEntity)

        return mappedDomain
    }
}
