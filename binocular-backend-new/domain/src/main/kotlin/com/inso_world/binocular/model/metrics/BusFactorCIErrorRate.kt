package com.inso_world.binocular.model.metrics

import com.inso_world.binocular.model.AbstractDomainObject
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Commit.Id
import com.inso_world.binocular.model.Commit.Key
import com.inso_world.binocular.model.Developer
import org.hibernate.validator.constraints.Range
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class BusFactorCIErrorRate(
    val module: String,
    @field:Range(min = 0) val busFactor: Int,
    @field:Range(min = 0, max = 1) val ciErrorRate: Double,
    val topAuthors: List<AuthorContribution>,
) : AbstractDomainObject<BusFactorCIErrorRate.Id, BusFactorCIErrorRate.Key>(
    Id(Uuid.random())
) {
    @JvmInline
    value class Id(val value: Uuid)

    data class Key(val module: String)

    override val uniqueKey: BusFactorCIErrorRate.Key
        get() = Key(module)
}
