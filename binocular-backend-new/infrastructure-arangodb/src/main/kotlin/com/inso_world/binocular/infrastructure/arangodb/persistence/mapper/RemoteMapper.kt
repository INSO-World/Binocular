package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RemoteEntity
import com.inso_world.binocular.model.vcs.Remote
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

/**
 * Mapper for Remote domain objects.
 */
@Component
internal class RemoteMapper : EntityMapper<Remote, RemoteEntity> {

    @Autowired

    @Lazy
    @Autowired
    private lateinit var repositoryMapper: RepositoryMapper

    override fun toEntity(domain: Remote): RemoteEntity {

        val entity = RemoteEntity(
            id = domain.id,
            name = domain.name,
            url = domain.url,
            repository = repositoryMapper.toEntity(domain.repository)
        )

        return entity
    }

    override fun toDomain(entity: RemoteEntity): Remote {

        val domain = Remote(
            name = entity.name,
            url = entity.url,
            repository = repositoryMapper.toDomain(entity.repository)
        ).apply {
            id = entity.id
        }

        return domain
    }

    override fun toDomainList(entities: Iterable<RemoteEntity>): List<Remote> = entities.map { toDomain(it) }
}
