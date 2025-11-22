package com.inso_world.binocular.infrastructure.sql.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RemoteEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.toEntity
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.vcs.Remote
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.stereotype.Component

@Component
internal class RemoteMapper : EntityMapper<Remote, RemoteEntity> {
    @Autowired
    private lateinit var ctx: MappingContext

    override fun toEntity(domain: Remote): RemoteEntity {
        ctx.findEntity<Remote.Key, Remote, RemoteEntity>(domain)?.let { return it }

        val repository = ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(domain.repository)
            ?: throw IllegalStateException(
                "RepositoryEntity must be mapped before RemoteEntity. " +
                        "Ensure RepositoryEntity is in MappingContext before calling toEntity()."
            )

        val entity = domain.toEntity(repository)
        ctx.remember(domain, entity)

        return entity
    }

    override fun toDomain(entity: RemoteEntity): Remote {
        ctx.findDomain<Remote, RemoteEntity>(entity)?.let { return it }

        val repository = ctx.findDomain<Repository, RepositoryEntity>(entity.repository)
            ?: throw IllegalStateException(
                "Repository must be mapped before Remote. " +
                        "Ensure Repository is in MappingContext before calling toDomain()."
            )

        val domain = entity.toDomain(repository)
        setField(
            domain.javaClass.superclass.getDeclaredField("iid"),
            domain,
            entity.iid
        )

        ctx.remember(domain, entity)
        return domain
    }
}
