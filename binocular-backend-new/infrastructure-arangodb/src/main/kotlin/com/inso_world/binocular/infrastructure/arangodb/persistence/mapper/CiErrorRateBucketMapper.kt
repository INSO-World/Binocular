package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CiRateBucketEntity
import com.inso_world.binocular.model.metrics.CiRateBucket
import org.springframework.stereotype.Component

/**
 * Mapper for [CiRateBucket] aggregation projections.
 *
 * Unlike aggregate mappers (e.g. BranchMapper), this is a **pure projection mapper**:
 * - no identity (no iid / business key)
 * - no cross-aggregate references
 * - no MappingContext, no remember()
 *
 * It only copies the aggregated CI error-rate fields between the AQL result entity
 * and the domain value object.
 */
@Component
internal class CiErrorRateBucketMapper : EntityMapper<CiRateBucket, CiRateBucketEntity> {

    override fun toDomain(entity: CiRateBucketEntity): CiRateBucket =
        CiRateBucket(
            period = entity.period,
            failed = entity.failed,
            completed = entity.completed,
        )

    override fun toEntity(domain: CiRateBucket): CiRateBucketEntity =
        CiRateBucketEntity(
            period = domain.period,
            failed = domain.failed,
            completed = domain.completed,
        )
}
