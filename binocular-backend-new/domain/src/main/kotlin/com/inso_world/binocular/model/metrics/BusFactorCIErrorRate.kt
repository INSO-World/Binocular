package com.inso_world.binocular.model.metrics

import com.inso_world.binocular.model.AbstractDomainObject
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Commit.Id
import com.inso_world.binocular.model.Commit.Key
import com.inso_world.binocular.model.Developer
import org.hibernate.validator.constraints.Range
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
/**
 * Domain model for one data point of the "Bus Factor vs. CI Error Rate" metric.
 * This class is database-agnostic and contains no persistence-specific annotations.
 *
 * Depending on the variant that produced it, [module] holds either a module path
 * (per-module analysis) or a time bucket label like "06/2026" (timeline analysis).
 *
 * @property module      the module path or time bucket this data point belongs to
 * @property busFactor   how many top authors together hold more than 50% of the commits
 *                       (0 = the remaining authors cannot reach 50%)
 * @property ciErrorRate share of failed builds (failed / completed), between 0.0 and 1.0
 * @property topAuthors  the authors that make up the bus factor, with their contribution share
 */
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
