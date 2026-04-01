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
): AbstractDomainObject<Module.Id, Module.Key>(
    Id(Uuid.random())
){
    @JvmInline
    value class Id(val value: Uuid)

    // TODO work in progress, just for compatibility
    data class Key(val key: String) // value object for lookups

    override val uniqueKey: Key
        get() = TODO("Not yet implemented")
}
