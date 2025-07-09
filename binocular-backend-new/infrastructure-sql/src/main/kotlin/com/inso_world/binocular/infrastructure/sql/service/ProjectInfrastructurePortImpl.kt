package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IProjectDao
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.mapper.ProjectMapper
import com.inso_world.binocular.model.Project
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
internal class ProjectInfrastructurePortImpl(
    @Autowired private val projectMapper: ProjectMapper,
    @Autowired private val projectDao: IProjectDao,
) : AbstractInfrastructurePort<Project, ProjectEntity, Long>(Long::class),
    ProjectInfrastructurePort {
    @PostConstruct
    fun init() {
        super.dao = projectDao
        super.mapper = projectMapper
    }

    override fun findByName(name: String): Project? =
        this.projectDao.findByName(name)?.let {
            this.projectMapper.toDomain(it)
        }
}
