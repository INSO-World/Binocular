package com.inso_world.binocular.model

import jakarta.validation.constraints.NotNull

class CommitDiff(
    val source: Commit,
    val target: Commit?,
    var files: Set<FileDiff>,
    @field:NotNull
    var repository: Repository? = null,
) : AbstractDomainObject() {
    override fun uniqueKey(): String = "${source.sha},${target?.sha}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommitDiff

        if (source != other.source) return false
        if (target != other.target) return false
//        if (stats != other.stats) return false

        return true
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + target.hashCode()
        return result
    }

    override fun toString(): String = "CommitDiff(source=${source.sha}, target=${target?.sha})"

    data class Stats(
        var additions: Long,
        var deletions: Long,
        var kind: StatsKind? = null,
    )

    enum class StatsKind(
        label: String,
    ) {
        //    todo check in rust if this can simply be an enum at first, not string
        ADDITION("Addition"),
        DELETION("Deletion"),
        MODIFICATION("Modification"),
        ;

        companion object {
            fun fromString(value: String): StatsKind =
                // erst nach label, dann nach NAME (z.B. "ADDITION")
                entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                    ?: valueOf(value.uppercase())
        }
    }
}
