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
 * Mapper between the per-module CI error-rate aggregation entity and its domain value object.
 *
 * This is a **pure projection mapper**: it only exists to copy the aggregated numbers
 * ([CiRatePerModuleEntity] <-> [CiRatePerModule]) that come out of an AQL query.
 *
 * Unlike the "real" aggregate mappers (e.g. BranchMapper), it deliberately has:
 * - no identity (no iid / business key),
 * - no references to other aggregates,
 * - no MappingContext and no remember() calls.
 *
 * So there is just a plain field-by-field copy in both directions.
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
