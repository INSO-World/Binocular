package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import com.inso_world.binocular.model.Project
import org.springframework.stereotype.Component

@Component
class ProjectMapper : EntityMapper<Project, ProjectEntity> {
    override fun toEntity(domain: Project): ProjectEntity =
        ProjectEntity(
            id = domain.id,
            name = domain.name,
            description = domain.description,
        )

    override fun toDomain(entity: ProjectEntity): Project =
        Project(
            id = entity.id,
            name = entity.name,
            description = entity.description,
        )

    override fun toDomainList(entities: Iterable<ProjectEntity>): List<Project> = entities.map { toDomain(it) }
}
