package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IRepositoryDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.RepositoryRepository
import com.inso_world.binocular.model.Repository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository as SpringRepository

@SpringRepository
internal class RepositoryDao
    @Autowired
    constructor(
        private val repositoryRepository: RepositoryRepository,
        private val repositoryMapper: RepositoryMapper,
        private val projectMapper: ProjectMapper,
    ) : MappedArangoDbDao<Repository, RepositoryEntity, String>(repositoryRepository, repositoryMapper),
        IRepositoryDao {
        companion object {
            val logger by logger()
        }

        @Autowired
        @Lazy
        private lateinit var projectDao: ProjectDao

        override fun findAll(): Iterable<Repository> = this.findAll(PageRequest.of(0, Int.MAX_VALUE))

        override fun findAll(pageable: Pageable): Page<Repository> {
            val result = repository.findAll(pageable)

            result.content.forEach {
                // fill mapping context with project
                projectMapper.toDomain(
                    requireNotNull(it.project, { "Repository needs Project set" })
                )
            }

            val content = toDomainList(result.content)
            val totalElements = result.totalElements

            return Page(content, totalElements, pageable)
        }

        override fun findByName(name: String): RepositoryEntity? = this.repositoryRepository.findByLocalPath(name)

        fun create(entity: RepositoryEntity): RepositoryEntity {
            val savedEntity = repositoryRepository.save(entity)

            val project =
                entity.project
                    ?: throw IllegalStateException("RepositoryEntity.project not loaded from ArangoDB — @Ref field was null.")
            val existingProject = this.projectDao.findByName(project.name)
            if (existingProject == null) {
                this.projectDao.create(project)
            }

            return savedEntity
        }
    }
