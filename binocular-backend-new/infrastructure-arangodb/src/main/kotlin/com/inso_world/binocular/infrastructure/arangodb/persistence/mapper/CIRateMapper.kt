package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CiRateBucketEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CiRatePerModuleEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ModuleAuthorCountEntity
import com.inso_world.binocular.model.metrics.AuthorCountPerModule
import com.inso_world.binocular.model.metrics.BusFactorCIErrorRate
import com.inso_world.binocular.model.metrics.CiRateBucket
import com.inso_world.binocular.model.metrics.CiRatePerModule
import jakarta.validation.Valid
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
internal class CIRateMapper : EntityMapper<CiRatePerModule, CiRatePerModuleEntity> {

    override fun toDomain(entity: CiRatePerModuleEntity): @Valid CiRatePerModule =
        CiRatePerModule(
            module = entity.module,
            failed = entity.failed,
            completed = entity.completed,
        )

    override fun toEntity(domain: CiRatePerModule): @Valid CiRatePerModuleEntity =
        CiRatePerModuleEntity(
            module = domain.module,
            failed = domain.failed,
            completed = domain.completed,
        )

}
