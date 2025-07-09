package com.inso_world.binocular.infrastructure.arangodb.service

import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.model.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ProjectInfrastructurePortImpl : ProjectInfrastructurePort {
    var logger: Logger = LoggerFactory.getLogger(ProjectInfrastructurePortImpl::class.java)

    override fun findAll(): Iterable<Project> {
        TODO("Not yet implemented")
    }

    override fun findAll(pageable: Pageable): Page<Project> {
        TODO("Not yet implemented")
    }

    override fun findById(id: String): Project? {
        TODO("Not yet implemented")
    }

    override fun save(entity: Project): Project {
        TODO("Not yet implemented")
    }

    override fun saveAll(entities: Collection<Project>): Iterable<Project> {
        TODO("Not yet implemented")
    }

    override fun delete(entity: Project) {
        TODO("Not yet implemented")
    }

    override fun findByName(name: String): Project? {
        TODO("Not yet implemented")
    }

    override fun update(entity: Project): Project {
        TODO("Not yet implemented")
    }

    override fun updateAndFlush(entity: Project): Project {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: String) {
        TODO("Not yet implemented")
    }
}
