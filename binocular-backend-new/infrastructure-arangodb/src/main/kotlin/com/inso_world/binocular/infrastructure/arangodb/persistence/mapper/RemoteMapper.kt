@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RemoteEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.vcs.Remote
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Mapper for Remote domain objects.
 */
@Component
internal class RemoteMapper(
    @Autowired @Lazy private val repositoryMapper: RepositoryMapper,
) : EntityMapper<Remote, RemoteEntity> {

    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    override fun toEntity(domain: Remote): RemoteEntity {
        val owner = domain.repository?.let { repositoryMapper.toEntity(it) } 
            ?: RepositoryEntity(
                iid = domain.repositoryId.value,
                localPath = "",
                project = ProjectEntity(iid = domain.repositoryId.value, name = "")
            )

        val entity = RemoteEntity(
            id = domain.id,
            name = domain.name,
            url = domain.url,
            repository = owner
        )

        return entity
    }

    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    override fun toDomain(entity: RemoteEntity): Remote {
        val repoDomain = repositoryMapper.toDomain(entity.repository)
        val domain = Remote(
            name = entity.name,
            url = entity.url,
            repositoryId = repoDomain.iid,
            iid = Remote.Id(Uuid.random()) 
        ).apply {
            id = entity.id
            repository = repoDomain
        }

        return domain
    }

    override fun toDomainList(entities: Iterable<RemoteEntity>): List<Remote> {
        return entities.map { this.toDomain(it) }
    }
}
