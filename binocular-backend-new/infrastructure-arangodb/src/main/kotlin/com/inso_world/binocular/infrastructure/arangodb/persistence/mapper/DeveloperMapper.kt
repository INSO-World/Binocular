package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.DeveloperEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.toEntity
import com.inso_world.binocular.model.Developer
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.stereotype.Component

@Component
internal class DeveloperMapper : EntityMapper<Developer, DeveloperEntity> {

    override fun toEntity(domain: Developer): DeveloperEntity = domain.toEntity()

    override fun toDomain(entity: DeveloperEntity): Developer {
        val domain = entity.toDomain()
        setField(
            domain.javaClass.superclass.superclass.getDeclaredField("iid"),
            domain,
            entity.iid,
        )
        return domain
    }

    fun refreshDomain(target: Developer, entity: DeveloperEntity): Developer {
        if (target.id.equals(entity.id)) return target
        setField(
            target.javaClass.getDeclaredField("id"),
            target,
            entity.id,
        )
        return target
    }
}
