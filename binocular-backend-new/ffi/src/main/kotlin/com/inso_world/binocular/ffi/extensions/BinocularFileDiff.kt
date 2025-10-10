package com.inso_world.binocular.ffi.pojos

import com.inso_world.binocular.ffi.internal.BString
import com.inso_world.binocular.ffi.internal.BinocularChangeType
import com.inso_world.binocular.ffi.internal.BinocularFileDiff
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.CommitDiff
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.FileDiff
import com.inso_world.binocular.model.FileState

private fun BString.asPath(): String = toString()

internal fun BinocularFileDiff.toDomain(
    stats: CommitDiff.Stats,
    child: Commit,
    parent: Commit?,
): FileDiff {
    var pathBefore: String? = null
    var pathAfter: String? = null
    val changeType =
        when (this.change) {
            is BinocularChangeType.Addition -> {
                pathAfter = (this.change as BinocularChangeType.Addition).location.asPath()
                FileDiff.ChangeType.ADDITION
            }

            is BinocularChangeType.Deletion -> {
                pathBefore = (this.change as BinocularChangeType.Deletion).location.asPath()
                FileDiff.ChangeType.DELETION
            }

            is BinocularChangeType.Modification -> {
                pathBefore = (this.change as BinocularChangeType.Modification).location.asPath()
                pathAfter = (this.change as BinocularChangeType.Modification).location.asPath()
                FileDiff.ChangeType.MODIFICATION
            }

            is BinocularChangeType.Rewrite -> {
                pathBefore = (this.change as BinocularChangeType.Rewrite).sourceLocation.asPath()
                pathAfter = (this.change as BinocularChangeType.Rewrite).location.asPath()

                FileDiff.ChangeType.REWRITE
            }
        }

    val diff =
        FileDiff(
            pathBefore = pathBefore,
            pathAfter = pathAfter,
            change = changeType,
            stats = stats,
            oldFileState =
                pathBefore?.let {
                    val file =
                        File(
                            path = it,
                        )
                    val state =
                        FileState(
                            content = this.oldFileContent,
                            commit = requireNotNull(parent) { "parent commit not present" },
                            file = file,
                        )
                    file.states.add(state)

                    return@let state
                },
            newFileState =
                pathAfter?.let {
                    val file =
                        File(
                            path = it,
                        )
                    val state =
                        FileState(
                            content = this.newFileContent,
                            commit = child,
                            file = file,
                        )
                    file.states.add(state)

                    return@let state
                },
        )
    return diff
}
