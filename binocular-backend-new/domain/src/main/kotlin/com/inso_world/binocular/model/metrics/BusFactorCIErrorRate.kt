package com.inso_world.binocular.model.metrics

import com.inso_world.binocular.model.AbstractDomainObject
import com.inso_world.binocular.model.Developer
import org.hibernate.validator.constraints.Range
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class BusFactorCIErrorRate(
    val id: String,
    @field:Range(min = 0) val busFactor: Int,
    @field:Range(min = 0, max = 1) val ciErrorRate: Double,
    val topAuthors: List<AuthorContribution>
) {

}
