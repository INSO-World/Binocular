package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.persistence.exception.NotFoundException
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.sql.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IProjectDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.User
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
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

    override fun findByName(name: String): Project? =
        this.projectDao.findByName(name)?.let {
            val commitContext = mutableMapOf<String, Commit>()
            val branchContext = mutableMapOf<String, Branch>()
            val userContext = mutableMapOf<String, User>()

            this.projectMapper.toDomain(it, commitContext, branchContext, userContext)
        }

    override fun delete(value: Project) {
        val managedEntity =
            this.projectDao.findByName(value.name) ?: throw NotFoundException("Project ${value.name} not found")

        this.projectDao.delete(managedEntity)
    }

    override fun update(value: Project): Project {
        val managedEntity =
            this.projectDao.findByName(value.name) ?: throw NotFoundException("Project ${value.name} not found")

        run {
            val domainRepo = value.repo
            val entityRepo = managedEntity.repo

            when {
                // Case 1: Both repos exist - check if they're the same and update
                domainRepo != null && entityRepo != null -> {
                    if (domainRepo.name != entityRepo.name) {
                        throw IllegalArgumentException(
                            "Cannot update project with a different repository. Project '${managedEntity.uniqueKey()}' already has repository '${entityRepo.name}'",
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
                            mutableMapOf(),
                            mutableMapOf(),
                            mutableMapOf(),
                        )
                }

                // Case 3: Removing existing repo
//                domainRepo == null && entityRepo != null -> {
//                    managedEntity.repo = null
//                }

                // Case 4: No repo in either - nothing to do
                else -> { }
            }
        }

        return super.updateEntity(managedEntity).let {
            projectDao.flush()
            val commitContext = mutableMapOf<String, Commit>()
            val branchContext = mutableMapOf<String, Branch>()
            val userContext = mutableMapOf<String, User>()

            this.projectMapper.toDomain(it, commitContext, branchContext, userContext)
        }
    }

    override fun updateAndFlush(value: Project): Project {
        val managedEntity =
            this.projectDao.findByName(value.name) ?: throw NotFoundException("Project ${value.name} not found")

        return super.updateAndFlush(managedEntity).let {
            val commitContext = mutableMapOf<String, Commit>()
            val branchContext = mutableMapOf<String, Branch>()
            val userContext = mutableMapOf<String, User>()

            this.projectMapper.toDomain(it, commitContext, branchContext, userContext)
        }
    }

    override fun create(value: Project): Project =
        super
            .create(
                this.projectMapper.toEntity(value),
            ).let {
                this.projectDao.flush()
                val commitContext = mutableMapOf<String, Commit>()
                val branchContext = mutableMapOf<String, Branch>()
                val userContext = mutableMapOf<String, User>()

                this.projectMapper.toDomain(it, commitContext, branchContext, userContext)
            }

    override fun saveAll(values: Collection<Project>): Iterable<Project> {
        TODO("Not yet implemented")
    }

    override fun findAll(): Iterable<Project> {
        val commitContext = mutableMapOf<String, Commit>()
        val branchContext = mutableMapOf<String, Branch>()
        val userContext = mutableMapOf<String, User>()

        return super<AbstractInfrastructurePort>.findAllEntities().map {
            this.projectMapper.toDomain(it, commitContext, branchContext, userContext)
        }
    }

    override fun findAll(pageable: Pageable): Page<Project> {
        TODO("Not yet implemented")
    }

    override fun findById(id: String): Project? =
        super.findById(id.toLong())?.let {
            val commitContext = mutableMapOf<String, Commit>()
            val branchContext = mutableMapOf<String, Branch>()
            val userContext = mutableMapOf<String, User>()

            this.projectMapper.toDomain(it, commitContext, branchContext, userContext)
        }

    override fun deleteById(id: String) {
        super.deleteByEntityId(id)
    }

    override fun deleteAll() {
        super.deleteAllEntities()
    }
}
