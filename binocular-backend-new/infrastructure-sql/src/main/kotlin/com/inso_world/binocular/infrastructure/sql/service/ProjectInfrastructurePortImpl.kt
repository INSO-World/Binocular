package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.persistence.exception.NotFoundException
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IProjectDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.mapper.ProjectMapper
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.User
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
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
        return super.updateEntity(managedEntity).let {
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
