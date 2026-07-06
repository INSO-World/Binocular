package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CiRateBucketEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ModuleAuthorCountEntity
import com.inso_world.binocular.model.metrics.AuthorCountPerModule
import com.inso_world.binocular.model.metrics.BusFactorCIErrorRate
import com.inso_world.binocular.model.metrics.CiRateBucket
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
internal class AuthorCountMapper : EntityMapper<AuthorCountPerModule, ModuleAuthorCountEntity> {

    override fun toDomain(entity: ModuleAuthorCountEntity): AuthorCountPerModule =
        AuthorCountPerModule(
            module = entity.module,
            gitSignature = entity.gitSignature,
            count = entity.count
        )

    override fun toEntity(domain: AuthorCountPerModule): ModuleAuthorCountEntity =
        ModuleAuthorCountEntity(
            module = domain.module,
            gitSignature = domain.gitSignature,
            count = domain.count
        )

}
