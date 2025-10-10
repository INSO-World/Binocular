package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.StatsEntity
import com.inso_world.binocular.model.Stats
import org.springframework.stereotype.Component


@Component
class StatsMapper
    constructor(
    ) : EntityMapper<Stats, StatsEntity> {

        /**
         * Converts a domain Job to an ArangoDB JobEntity
         */
        override fun toEntity(domain: Stats): StatsEntity =
            StatsEntity(
                additions = domain.additions,
                deletions = domain.deletions
            )

        //TODO: add java documentation
        override fun toDomain(entity: StatsEntity): Stats =
            Stats(
                additions = entity.additions,
                deletions = entity.deletions
            )

    }
