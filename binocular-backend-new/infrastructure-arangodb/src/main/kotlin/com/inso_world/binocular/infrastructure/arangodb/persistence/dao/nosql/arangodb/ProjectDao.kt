package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IProjectDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.IssueRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.MergeRequestRepository
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.ProjectRepository
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.MergeRequest
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

    @Autowired
    private lateinit var issueDao: IssueDao

    @Autowired
    private lateinit var mergeRequestDao: MergeRequestDao

    @MappingSession
    override fun findByName(name: String): Project? {
        return this.projectRepository.findByName(name)?.let {
            this.mapper.toDomain(it)
        }
    }

    @MappingSession
    override fun create(entity: Project): Project {
        logger.debug("Creating new project: {}", entity)

        val mappedEntity = mapper.toEntity(entity)
        var savedEntity = projectRepository.save(mappedEntity)

        // Save Issues
        entity.issues.forEach { issue ->
            issueDao.save(issue)
        }

        // Save Merge Requests
        entity.mergeRequests.forEach { mr ->
            mergeRequestDao.save(mr)
        }

        savedEntity = mappedEntity.repository?.let { repository ->
            val savedRepo = repositoryDao.create(repository)
            savedEntity.repository = savedRepo
            // update so that @Ref gets updated
            return@let projectRepository.save(savedEntity)
        } ?: savedEntity

        return mapper.toDomain(savedEntity)
    }
}
