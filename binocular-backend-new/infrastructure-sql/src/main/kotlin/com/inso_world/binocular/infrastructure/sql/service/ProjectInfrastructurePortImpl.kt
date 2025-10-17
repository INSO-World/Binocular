package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.persistence.exception.NotFoundException
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.sql.mapper.context.MappingSession
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IProjectDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.model.Project
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

@Service
@Validated
internal class ProjectInfrastructurePortImpl(
    @Autowired private val projectMapper: ProjectMapper,
    @Autowired private val projectDao: IProjectDao,
) : AbstractInfrastructurePort<Project, ProjectEntity, Long>(Long::class),
    ProjectInfrastructurePort {
    @Lazy
    @Autowired
    private lateinit var repositoryMapper: RepositoryMapper

    @Lazy
    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePortImpl

    @PostConstruct
    fun init() {
        super.dao = projectDao
//        super.mapper = projectMapper
    }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findByName(name: String): Project? =
        this.projectDao.findByName(name)?.let {
            this.projectMapper.toDomain(it)
        }

    @Transactional
    override fun delete(value: Project) {
        val managedEntity =
            this.projectDao.findByName(value.name) ?: throw NotFoundException("Project ${value.name} not found")

        this.projectDao.delete(managedEntity)
    }

    @MappingSession
    @Transactional
    override fun update(value: Project): Project {
        val managedEntity =
            this.projectDao.findByName(value.name) ?: throw NotFoundException("Project ${value.name} not found")

        run {
            val domainRepo = value.repo
            val entityRepo = managedEntity.repo

            when {
                // Case 1: Both repos exist - check if they're the same and update
                domainRepo != null && entityRepo != null -> {
                    if (domainRepo.localPath != entityRepo.localPath) {
                        throw IllegalArgumentException(
                            "Cannot update project with a different repository. Project '${managedEntity.uniqueKey()}' already has repository '${entityRepo.localPath}'",
                        )
                    }
                    // Don't create a new entity, just update the existing one's fields

                    repositoryPort.update(domainRepo)
                }

                // Case 2: Adding a new repo where none existed
                domainRepo != null && entityRepo == null -> {
                    managedEntity.repo =
                        repositoryMapper.toEntity(
                            domain = domainRepo,
                            project = managedEntity,
                        )
                }

                // Case 3: Removing existing repo
//                domainRepo == null && entityRepo != null -> {
//                    managedEntity.repo = null
//                }

                // Case 4: No repo in either - nothing to do
                else -> {}
            }
        }

        return super.updateEntity(managedEntity).let {
            projectDao.flush()
            this.projectMapper.toDomain(it)
        }
    }

    @MappingSession
    @Transactional
    override fun updateAndFlush(value: Project): Project {
        val managedEntity =
            this.projectDao.findByName(value.name) ?: throw NotFoundException("Project ${value.name} not found")

        return super.updateAndFlush(managedEntity).let {
            this.projectMapper.toDomain(it)
        }
    }

    @MappingSession
    @Transactional
    override fun create(value: Project): Project =
        super
            .create(
                this.projectMapper.toEntity(value),
            ).let {
                this.projectDao.flush()
                this.projectMapper.toDomain(it)
            }

    @MappingSession
    @Transactional
    override fun saveAll(values: Collection<Project>): Iterable<Project> {
        return values.map { this.create(it) }
//        return values.map {
//            this.projectMapper.toEntity(it)
//        }.let { entities ->
//            return@let super.saveAll(entities)
//        }.map {
//            this.projectMapper.toDomain(it)
//        }
    }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findAll(): Iterable<Project> =
        super<AbstractInfrastructurePort>.findAllEntities().map {
            this.projectMapper.toDomain(it)
        }

    override fun findAll(pageable: Pageable): Page<Project> {
        TODO("Not yet implemented")
    }

    @MappingSession
    @Transactional(readOnly = true)
    override fun findById(id: String): Project? =
        super.findById(id.toLong())?.let {
            this.projectMapper.toDomain(it)
        }

    @Transactional
    override fun deleteById(id: String) {
        super.deleteByEntityId(id)
    }

    @Transactional
    override fun deleteAll() {
        super.deleteAllEntities()
    }
}
