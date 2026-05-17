package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RevisionEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.toEntity
import com.inso_world.binocular.model.Revision
import org.springframework.stereotype.Component

@Component
internal class RevisionMapper : EntityMapper<Revision, RevisionEntity> {

    override fun toEntity(domain: Revision): RevisionEntity = domain.toEntity()

    override fun toDomain(entity: RevisionEntity): Revision = entity.toDomain()
}
