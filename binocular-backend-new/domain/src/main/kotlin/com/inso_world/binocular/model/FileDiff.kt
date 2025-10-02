package com.inso_world.binocular.model

data class FileDiff(
    val pathBefore: String? = null, // null bei ADD
    val pathAfter: String? = null, // null bei DELETE
    val change: ChangeType,
    val stats: CommitDiff.Stats,
    var oldFileState: FileState?,
    var newFileState: FileState?,
//    val hunks: List<Hunk>, // optional, nur wenn du Textdiffs speicherst
) : AbstractDomainObject() {
    override fun uniqueKey(): String = "$pathBefore,$pathAfter,$change"

    init {
        when (change) {
            ChangeType.ADDITION ->
                require(oldFileState == null) {
                    "File change type must be ADDITION when oldFileState is NULL"
                }
            ChangeType.DELETION ->
                require(newFileState == null) {
                    "File change type DELETION requires newFileState to be NULL"
                }
            ChangeType.MODIFICATION -> {
                require(oldFileState != null && newFileState != null) {
                    "File change type MODIFICATION requires newFileState and oldFileState not to be NULL"
                }
                require(pathBefore == pathAfter) {
                    "File change type MODIFICATION requires pathBefore == pathAfter"
                }
            }
            ChangeType.REWRITE -> {
                require(oldFileState != null && newFileState != null) {
                    "File change type REWRITE requires newFileState and oldFileState not to be NULL"
                }
                require(pathBefore != pathAfter) {
                    "File change type REWRITE requires pathBefore != pathAfter"
                }
            }
        }
    }

    enum class ChangeType(
        label: String,
    ) {
        //    todo check in rust if this can simply be an enum at first, not string
        ADDITION("Addition"),
        DELETION("Deletion"),
        MODIFICATION("Modification"),
        REWRITE("Rewrite"),
        ;

        companion object {
            fun fromString(value: String): ChangeType =
                // erst nach label, dann nach NAME (z.B. "ADDITION")
                entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                    ?: valueOf(value.uppercase())
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileDiff

        if (pathBefore != other.pathBefore) return false
        if (pathAfter != other.pathAfter) return false
        if (change != other.change) return false
        if (stats != other.stats) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pathBefore?.hashCode() ?: 0
        result = 31 * result + (pathAfter?.hashCode() ?: 0)
        result = 31 * result + change.hashCode()
        result = 31 * result + stats.hashCode()
        return result
    }

    override fun toString(): String =
        "FileDiff(pathBefore=$pathBefore, pathAfter=$pathAfter, change=$change, stats=$stats, oldFile=${oldFileState?.file?.path}, newFile=${newFileState?.file?.path})"
}
