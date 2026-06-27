package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.infrastructure.arangodb.assembler.ProjectAssembler
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IProjectDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.ProjectRepository
import com.inso_world.binocular.model.Project
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import kotlin.uuid.ExperimentalUuidApi

@Repository
internal class ProjectDao
    @Autowired
    constructor(
        private val projectAssembler: ProjectAssembler,
        private val projectRepository: ProjectRepository,
        projectMapper: ProjectMapper,
    ) : MappedArangoDbDao<Project, ProjectEntity, String>(projectRepository, projectMapper),
        IProjectDao {
        companion object {
            val logger by logger()
        }

        @Autowired
        private lateinit var repositoryDao: RepositoryDao

        @Autowired
        private lateinit var issueDao: IssueDao

        @Autowired
        private lateinit var mergeRequestDao: MergeRequestDao

        override fun findAllEntities(): Iterable<ProjectEntity> = this.projectRepository.findAll()

        @Deprecated("legacy", replaceWith = ReplaceWith("findAllEntities()"))
        override fun findAll(): Iterable<Project> = this.findAllEntities().map(projectAssembler::toDomain)

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

        /**
         * Persists a [ProjectEntity] and cascades its optional [ProjectEntity.repository] child.
         *
         * Save order is load-bearing: the project document is repserted **without** the
         * repository `@Ref` first (the repository's own `project` ref requires the project
         * document to exist), then the repository is cascade-saved, and only then is the
         * `@Ref` written via a second save. Writing the ref before the repository document
         * exists fails for preset keys: Spring Data ArangoDB eagerly resolves refs when
         * deserializing the repsert result and throws on dangling references.
         *
         * @param entity the project entity to persist; [ProjectEntity.repository] may be set.
         * @return the saved entity, with the repository `@Ref` wired when present.
         */
        fun create(entity: ProjectEntity): ProjectEntity {
            logger.debug("Creating new project: {}", entity)

            var savedEntity = projectRepository.save(entity)

            // Save Issues
            entity.issues.forEach { issue ->
                issueDao.create(issue)
            }

            // Save Merge Requests
            entity.mergeRequests.forEach { mr ->
                mergeRequestDao.create(mr)
            }

            savedEntity = entity.repository?.let {
                val savedRepo = repositoryDao.create(it)
                savedEntity.repository = savedRepo
                // update so that @Ref gets updated
                return@let projectRepository.save(savedEntity)
            } ?: savedEntity

            return savedEntity
        }

        @Deprecated("Use create(entity: ProjectEntity) instead", ReplaceWith("create(entity)"))
        override fun create(entity: Project): Project = throw NotImplementedError("create(entity: ProjectEntity)")
    }
