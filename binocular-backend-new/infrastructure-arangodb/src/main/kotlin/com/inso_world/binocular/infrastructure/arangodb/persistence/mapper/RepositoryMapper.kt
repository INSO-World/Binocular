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
            projectId = domain.project?.id,
            name = domain.localPath
        )

    override fun toDomain(entity: RepositoryEntity): Repository =
        Repository(
            id = entity.id,
            project = entity.projectId?.let { Project(id = it, name = "unknown") } ?: Project(id = null, name = "unknown"),
            localPath = entity.name,
        )

    override fun toDomainList(entities: Iterable<RepositoryEntity>): List<Repository> = entities.map { toDomain(it) }
}
