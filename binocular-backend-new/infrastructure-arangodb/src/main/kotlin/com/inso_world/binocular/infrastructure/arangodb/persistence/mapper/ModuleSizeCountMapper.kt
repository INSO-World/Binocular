package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ModuleSizeCountEntity
import com.inso_world.binocular.model.metrics.ModuleSizeCount
import org.springframework.stereotype.Component

@Component
internal class ModuleSizeCountMapper : EntityMapper<ModuleSizeCount, ModuleSizeCountEntity> {

    override fun toDomain(entity: ModuleSizeCountEntity): ModuleSizeCount =
        ModuleSizeCount(
            module = entity.module,
            loc = entity.loc,
            changeFrequency = entity.changeFrequency,
        )

    override fun toEntity(domain: ModuleSizeCount): ModuleSizeCountEntity =
        ModuleSizeCountEntity(
            module = domain.module,
            loc = domain.loc,
            changeFrequency = domain.changeFrequency,
        )
}
