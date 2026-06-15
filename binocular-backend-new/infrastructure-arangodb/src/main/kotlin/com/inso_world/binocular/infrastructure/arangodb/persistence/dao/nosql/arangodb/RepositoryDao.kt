package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IRepositoryDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.DeveloperMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.*
import com.inso_world.binocular.model.Repository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.springframework.stereotype.Repository as SpringRepository

/**
 * ArangoDB implementation of [IRepositoryDao].
 *
 * Reads repository entities and hydrates their related domain objects (project, developers)
 * by interleaving mapper calls so that the [com.inso_world.binocular.core.persistence.mapper.context.MappingContext]
 * is populated in the correct dependency order:
 * 1. Project (required by [RepositoryMapper.toDomain])
 * 2. Repository domain object
 * 3. Developers (auto-added to `repository.developers` via [com.inso_world.binocular.model.Developer.init])
 */
@SpringRepository
internal class RepositoryDao
    @Autowired
    constructor(
        private val repositoryRepository: RepositoryRepository,
        private val repositoryMapper: RepositoryMapper,
        private val projectMapper: ProjectMapper,
        private val developerRepository: DeveloperRepository,
        private val developerMapper: DeveloperMapper,
    ) : MappedArangoDbDao<Repository, RepositoryEntity, String>(repositoryRepository, repositoryMapper),
        IRepositoryDao {
        companion object {
            val logger by logger()
        }

        @Autowired
        @Lazy
        private lateinit var projectDao: ProjectDao

        @Autowired
        private lateinit var commitRepository: CommitRepository

        @Autowired
        private lateinit var branchRepository: BranchRepository

        @Autowired
        private lateinit var remoteRepository: RemoteRepository

        @Autowired
        private lateinit var fileRepository: FileRepository

        @Autowired
        private lateinit var revisionRepository: RevisionRepository

        override fun findAll(): Iterable<Repository> = this.findAll(PageRequest.of(0, Int.MAX_VALUE))

        /**
         * Returns a page of repositories, with each repository's developers fully populated.
         *
         * Mapping order per entity:
         * 1. [projectMapper.toDomain] — seeds project into context (required by [RepositoryMapper]).
         * 2. [repositoryMapper.toDomain] — maps the repository and registers it in context.
         * 3. [developerRepository.findByRepositoryKey] — fetches developer entities for this repo.
         * 4. For each developer entity: injects the owning [RepositoryEntity] (lazy ref is null from AQL)
         *    then calls [developerMapper.toDomain], which auto-adds the developer to
         *    `repoDomain.developers` via [com.inso_world.binocular.model.Developer]'s `init` block.
         *
         * @param pageable pagination parameters
         * @return a [Page] of [Repository] domain objects with developers populated
         */
        override fun findAll(pageable: Pageable): Page<Repository> {
            val result = repository.findAll(pageable)

            val content =
                result.content.map { repoEntity ->
                    projectMapper.toDomain(
                        requireNotNull(repoEntity.project) { "Repository needs Project set" },
                    )
                    val repoDomain = repositoryMapper.toDomain(repoEntity)
                    repoEntity.id?.let { key ->
                        developerRepository.findByRepositoryKey(key).forEach { devEntity ->
                            devEntity.repository = repoEntity
                            developerMapper.toDomain(devEntity)
                        }
                    }
                    repoDomain
                }

            return Page(content, result.totalElements, pageable)
        }

        override fun findByName(name: String): RepositoryEntity? = this.repositoryRepository.findByLocalPath(name)

        @OptIn(ExperimentalUuidApi::class)
        override fun findByIid(iid: Uuid): RepositoryEntity? = this.repositoryRepository.findByIid(iid)

        @OptIn(ExperimentalUuidApi::class)
        override fun findEntityByIid(iid: Uuid): RepositoryEntity? = this.repositoryRepository.findByIid(iid)

        fun create(entity: RepositoryEntity): RepositoryEntity {
            val savedEntity = repositoryRepository.save(entity)

            val project =
                entity.project
                    ?: throw IllegalStateException("RepositoryEntity.project not loaded from ArangoDB — @Ref field was null.")
            val existingProject = this.projectDao.findByName(project.name)
            if (existingProject == null) {
                this.projectDao.create(project)
            }
            // Save Commits
            entity.commits.forEach { commitRepository.save(it) }

            // Save Branches
            entity.branches.forEach { branchRepository.save(it) }

            // Save Remotes
            entity.remotes.forEach { remoteRepository.save(it) }

            // Save Files and Revisions
            entity.files.forEach { fileRepository.save(it) }
            entity.revisions.forEach { revisionRepository.save(it) }

            return savedEntity
        }
    }
