package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RemoteEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.toEntity
import com.inso_world.binocular.model.vcs.Remote
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
internal class RemoteMapper : EntityMapper<Remote, RemoteEntity> {
    @Autowired
    private lateinit var ctx: MappingContext

    override fun toEntity(domain: Remote): RemoteEntity {
        ctx.findEntity<Remote.Key, Remote, RemoteEntity>(domain)?.let { return it }
        val entity = domain.toEntity("")
        ctx.remember(domain, entity)
        return entity
    }

    override fun toDomain(entity: RemoteEntity): Remote {
        ctx.findDomain<Remote, RemoteEntity>(entity)?.let { return it }
        val domain = entity.toDomain()
        ctx.remember(domain, entity)
        return domain
    }
}
