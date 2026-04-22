package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
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

    @MappingSession
    override fun findAll(): Iterable<Project> = this.projectDao.findAll()

    @MappingSession
    override fun findAll(pageable: Pageable): Page<Project> = this.projectDao.findAll(pageable)

    @MappingSession
    override fun findById(id: String): Project? = this.projectDao.findById(id)

    override fun create(value: Project): Project {
        val project = this.projectDao.create(value)
        return project
    }

    override fun saveAll(values: Collection<Project>): Iterable<Project> = this.projectDao.saveAll(values)

    @MappingSession
    override fun findByName(name: String): Project? = this.projectDao.findByName(name)

    override fun update(value: Project): Project {
        TODO("Not yet implemented")
    }

    @MappingSession
    override fun findByIid(iid: Project.Id): Project? {
        TODO("Not yet implemented")
    }
}
