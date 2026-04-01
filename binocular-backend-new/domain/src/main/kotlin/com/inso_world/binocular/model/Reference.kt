package com.inso_world.binocular.model

import com.inso_world.binocular.model.vcs.ReferenceCategory
import jakarta.validation.constraints.NotNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
abstract class Reference<Key>(
    @field:NotNull
    open val category: ReferenceCategory,
    @field:NotNull
    open val repository: Repository
) : AbstractDomainObject<Reference.Id, Key>(
    Id(Uuid.random())
) {
    @JvmInline
    value class Id(val value: Uuid)

    override fun equals(other: Any?): Boolean = super.equals(other)

    override fun hashCode(): Int = super.hashCode()
}
