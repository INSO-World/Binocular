package com.inso_world.binocular.model.metrics

import com.inso_world.binocular.model.AbstractDomainObject
import org.hibernate.validator.constraints.Range
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ModuleSizeChangeFrequency(
    val module: String,
    @field:Range(min = 0) val loc: Long,
    @field:Range(min = 0) val changeFrequency: Long,
) : AbstractDomainObject<ModuleSizeChangeFrequency.Id, ModuleSizeChangeFrequency.Key>(
    Id(Uuid.random())
) {
    @JvmInline
    value class Id(val value: Uuid)

    data class Key(val module: String)

    override val uniqueKey: Key
        get() = Key(module)
}
