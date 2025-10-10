package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.MentionEntity
import com.inso_world.binocular.model.Mention
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.util.Date

@Component
class MentionMapper
constructor(
) : EntityMapper<Mention, MentionEntity> {
    /**
     * Converts a domain Job to an ArangoDB JobEntity
     */
    override fun toEntity(domain: Mention): MentionEntity =
        MentionEntity(
            commit = domain.commit,
            createdAt = domain.createdAt?.let { Date.from(it.toInstant(ZoneOffset.UTC)) },
            closes = domain.closes
        )

    //TODO: add java documentation
    override fun toDomain(entity: MentionEntity): Mention =
        Mention(
            commit = entity.commit,
            createdAt = entity.createdAt?.toInstant()
                ?.atZone(ZoneOffset.UTC)
                ?.toLocalDateTime(),
            closes = entity.closes
        )

}
