package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.infrastructure.arangodb.assembler.ProjectAssembler
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.ProjectDao
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Project
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
internal class ProjectInfrastructurePortImpl : ProjectInfrastructurePort,
    AbstractInfrastructurePort<Project, String>() {

    @PostConstruct
    fun init() {
        super.dao = projectDao
    }
    companion object {
        val logger by logger()
    }

    @Autowired
    private lateinit var projectDao: ProjectDao

    @Autowired
    private lateinit var projectAssembler: ProjectAssembler

    override fun findAll(): Iterable<Project> {
        return this.projectDao.findAll()
    }

    override fun findAll(pageable: Pageable): Page<Project> {
        return this.projectDao.findAll(pageable)
    }

    override fun findById(id: String): Project? {
        return this.projectDao.findById(id)
    }

    @MappingSession
    override fun create(value: Project): Project {
        val entity = projectAssembler.toEntity(value)
        val savedProject = this.projectDao.create(value)
        return savedProject
    }

    override fun saveAll(values: Collection<Project>): Iterable<Project> {
        return this.projectDao.saveAll(values)
    }

    @MappingSession
    override fun findByName(name: String): Project? {
        return this.projectDao.findByName(name)
    }

    override fun update(value: Project): Project {
        TODO("Not yet implemented")
    }

    override fun findByIid(iid: Project.Id): Project? {
        TODO("Not yet implemented")
    }
}
