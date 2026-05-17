package com.inso_world.binocular.infrastructure.sql.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.sql.persistence.entity.DeveloperEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.toEntity
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Repository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.stereotype.Component

/**
 * Mapper for Developer domain objects.
 *
 * Converts between Developer domain objects and DeveloperEntity persistence entities.
 * This mapper intentionally keeps the conversion shallow; it does not traverse commit graphs.
 */
@Component
internal class DeveloperMapper : EntityMapper<Developer, DeveloperEntity> {
    @Autowired
    private lateinit var ctx: MappingContext

    companion object {
        private val logger by logger()
    }

    override fun toEntity(domain: Developer): DeveloperEntity {
        ctx.findEntity<Developer.Key, Developer, DeveloperEntity>(domain)?.let { return it }

        val entity = domain.toEntity()
        ctx.remember(domain, entity)
        return entity
    }

    override fun toDomain(entity: DeveloperEntity): Developer {
        ctx.findDomain<Developer, DeveloperEntity>(entity)?.let { return it }

        val domain = entity.toDomain()
        setField(
            domain.javaClass.superclass.getDeclaredField("iid"),
            domain,
            entity.iid
        )
        ctx.remember(domain, entity)
        return domain
    }

    fun refreshDomain(target: Developer, entity: DeveloperEntity): Developer {
        if (target.id.equals(entity.id?.toString())) {
            return target
        }
        setField(
            target.javaClass.getDeclaredField("id"),
            target,
            entity.id?.toString()
        )
        return target
    }
}
