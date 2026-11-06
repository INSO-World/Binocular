package com.inso_world.binocular.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Domain model for a File, representing a file in a Git repository.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
@OptIn(ExperimentalUuidApi::class)
data class File(
    var path: String,
    val revisions: MutableSet<Revision> = mutableSetOf(),
) : AbstractDomainObject<File.Id, File.Key>(
    Id(Uuid.random())
) {
    @JvmInline
    value class Id(val value: Uuid)

    data class Key(val path: String) // value object for lookups

    // some database dependent id
    @Deprecated("Avoid using database specific id, use business key .iid", ReplaceWith("iid"))
    var id: String? = null

    @Deprecated("legacy")
    lateinit var webUrl: String

    @Deprecated("legacy")
    val maxLength: Int
        get() =
            revisions
                .mapNotNull { it.content?.length }
                .takeIf { it.isNotEmpty() }
                ?.reduce { acc, n -> if (n > acc) n else acc } ?: Int.MIN_VALUE

    // Relationships
    @Deprecated("legacy")
    val commits: List<Commit>
        get() = revisions.map { it.commit }

    @Deprecated("legacy")
    val branches: List<Branch> = emptyList()

    @Deprecated("legacy")
    var modules: List<Module> = emptyList()

    @Deprecated("legacy")
    val relatedFiles: List<File> = emptyList()

    @Deprecated("legacy")
    val users: List<Developer>
        get() = revisions.map { it.commit }.flatMap { it.users }

    override val uniqueKey: Key
        get() = Key(this.path)

    override fun toString(): String = "File(states=$revisions, path='$path', id=$id)"
}
