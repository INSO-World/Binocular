@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package com.inso_world.binocular.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Domain model for a Module, representing a code module or package in the codebase.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
@OptIn(ExperimentalUuidApi::class)
data class Module(
    var id: String? = null,
    var path: String,
    // Relationships
    var commits: List<Commit> = emptyList(),
    var files: List<File> = emptyList(),
    var childModules: List<Module> = emptyList(),
    var parentModules: List<Module> = emptyList(),
    override val iid: Module.Id = Id(Uuid.random()),
): AbstractDomainObject<Module.Id, Module.Key>(
    iid
){
    @JvmInline
    value class Id(override val value: Uuid) : DomainId

    data class Key(val path: String)

    override val uniqueKey: Key
        get() = Key(path)

    override fun equals(other: Any?) = super.equals(other)
    override fun hashCode() = super.hashCode()
}
