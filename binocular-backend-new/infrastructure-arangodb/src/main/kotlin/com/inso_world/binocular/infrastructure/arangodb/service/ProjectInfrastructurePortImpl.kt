package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.ProjectDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.RepositoryDao
import com.inso_world.binocular.model.Project
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ProjectInfrastructurePortImpl : ProjectInfrastructurePort {
    companion object {
        val logger by logger()
    }

    @Autowired
    private lateinit var projectDao: ProjectDao
    override fun findAll(): Iterable<Project> {
        return this.projectDao.findAll()
    }

    override fun findAll(pageable: Pageable): Page<Project> {
        return this.projectDao.findAll(pageable)
    }

    override fun findById(id: String): Project? {
        return this.projectDao.findById(id)
    }

    override fun create(value: Project): Project {
        val project = this.projectDao.create(value)
        return project
    }

    override fun saveAll(values: Collection<Project>): Iterable<Project> {
        return this.projectDao.saveAll(values)
    }

    override fun delete(value: Project) {
        this.projectDao.delete(value)
    }

    override fun findByName(name: String): Project? {
        return this.projectDao.findByName(name)
    }

    override fun update(value: Project): Project {
        TODO("Not yet implemented")
    }

    override fun updateAndFlush(value: Project): Project {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: String) {
        this.projectDao.deleteById(id)
    }

    override fun deleteAll() {
        this.projectDao.deleteAll()
    }
}
