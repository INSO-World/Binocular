package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.AuthorPeriodCountEntity
import com.inso_world.binocular.model.metrics.AuthorPeriodCount
import org.springframework.stereotype.Component

/**
 * Mapper for [AuthorPeriodCount] aggregation projections (bus-factor input).
 *
 * Pure projection mapper — no identity, no cross-aggregate references,
 * no MappingContext. Plain field-to-field copy of the AQL aggregation result.
 */
@Component
internal class AuthorCommitCountMapper : EntityMapper<AuthorPeriodCount, AuthorPeriodCountEntity> {

    override fun toDomain(entity: AuthorPeriodCountEntity): AuthorPeriodCount =
        AuthorPeriodCount(
            period = entity.period,
            gitSignature = entity.gitSignature,
            count = entity.count,
        )

    override fun toEntity(domain: AuthorPeriodCount): AuthorPeriodCountEntity =
        AuthorPeriodCountEntity(
            period = domain.period,
            gitSignature = domain.gitSignature,
            count = domain.count,
        )
}
