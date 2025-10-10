package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.springframework.stereotype.Component

@Component
class RepositoryMapper : EntityMapper<Repository, RepositoryEntity> {
    override fun toEntity(domain: Repository): RepositoryEntity =
        RepositoryEntity(
            id = domain.id,
            name = domain.name,
            projectId = domain.project?.id,
        )

    override fun toDomain(entity: RepositoryEntity): Repository =
        Repository(
            id = entity.id,
            name = entity.name,
            project = entity.projectId?.let { Project(id = it, name = "") },
        )

    override fun toDomainList(entities: Iterable<RepositoryEntity>): List<Repository> = entities.map { toDomain(it) }
}
