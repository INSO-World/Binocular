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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Repository
internal class ProjectDao
    @Autowired
    constructor(
        private val projectRepository: ProjectRepository,
        projectMapper: ProjectMapper,
    ) : MappedArangoDbDao<Project, ProjectEntity, String>(projectRepository, projectMapper),
        IProjectDao {
        companion object {
            val logger by logger()
        }

        @Autowired
        private lateinit var repositoryDao: RepositoryDao

        override fun findByName(name: String): Project? =
            this.projectRepository.findByName(name)?.let {
                this.mapper.toDomain(it)
            }

        @OptIn(ExperimentalUuidApi::class)
        override fun findByIid(iid: Project.Id): Project? =
            this.projectRepository.findByIid(iid.value)?.let {
                this.mapper.toDomain(it)
            }

        @OptIn(ExperimentalUuidApi::class)
        override fun findEntityByIid(iid: Project.Id): ProjectEntity? = this.projectRepository.findByIid(iid.value)

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

        @Deprecated("Use create(entity: ProjectEntity) instead", ReplaceWith("create(entity)"))
        override fun create(entity: Project): Project = throw NotImplementedError("create(entity: ProjectEntity)")
    }
