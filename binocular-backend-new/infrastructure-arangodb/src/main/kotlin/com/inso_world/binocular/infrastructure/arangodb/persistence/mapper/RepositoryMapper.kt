package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.toEntity
import com.inso_world.binocular.model.Repository
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.stereotype.Component
import kotlin.uuid.ExperimentalUuidApi

@Component
@OptIn(ExperimentalUuidApi::class)
internal class RepositoryMapper : EntityMapper<Repository, RepositoryEntity> {

    override fun toEntity(domain: Repository): RepositoryEntity {
        val project = ProjectEntity(
            id = null,
            iid = domain.projectId.value,
            name = "",
        )
        return domain.toEntity(project)
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun toDomain(entity: RepositoryEntity): Repository {
        val projectId = com.inso_world.binocular.model.Project.Id(kotlin.uuid.Uuid.parse(entity.projectId))
        val domain = entity.toDomain(projectId)
        setField(
            domain.javaClass.superclass.getDeclaredField("iid"),
            domain,
            entity.iid,
        )
        return domain
    }

    fun refreshDomain(target: Repository, entity: RepositoryEntity): Repository {
        setField(
            target.javaClass.getDeclaredField("id"),
            target,
            entity.id,
        )
        return target
    }
}
