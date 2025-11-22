package com.inso_world.binocular.model

import com.inso_world.binocular.model.vcs.ReferenceCategory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
abstract class Reference<Key>(
    open val category: ReferenceCategory,
    open val repository: Repository
) : AbstractDomainObject<Reference.Id, Key>(
    Id(Uuid.random())
) {
    @JvmInline
    value class Id(val value: Uuid)

    override fun equals(other: Any?): Boolean = super.equals(other)

    override fun hashCode(): Int = super.hashCode()
}
