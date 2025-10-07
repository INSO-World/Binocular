package com.inso_world.binocular.model

/**
 * Domain model for a File, representing a file in a Git repository.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class File(
    var id: String? = null,
    var path: String,
    val states: MutableSet<FileState> = mutableSetOf(),
) : AbstractDomainObject() {
    @Deprecated("legacy")
    lateinit var webUrl: String

    @Deprecated("legacy")
    val maxLength: Int
        get() =
            states
                .mapNotNull { it.content?.length }
                .takeIf { it.isNotEmpty() }
                ?.reduce { acc, n -> if (n > acc) n else acc } ?: Int.MIN_VALUE

    // Relationships
    @Deprecated("legacy")
    val commits: List<Commit>
        get() = states.map { it.commit }

    @Deprecated("legacy")
    val branches: List<Branch>
        get() = states.map { it.commit }.flatMap { it.branches }

    @Deprecated("legacy")
    var modules: List<Module> = emptyList()

    @Deprecated("legacy")
    val relatedFiles: List<File> = emptyList()

    @Deprecated("legacy")
    val users: List<User>
        get() = states.map { it.commit }.flatMap { it.users }

    override fun uniqueKey(): String = path

    override fun toString(): String = "File(states=$states, path='$path', id=$id)"
}
