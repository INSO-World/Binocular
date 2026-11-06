package com.inso_world.binocular.model

/**
 * Domain model for Stats, representing the addition and deletion statistics of a commit.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class Stats(
    var additions: Long,
    var deletions: Long,
    var kind: StatsKind? = null,
) {
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
