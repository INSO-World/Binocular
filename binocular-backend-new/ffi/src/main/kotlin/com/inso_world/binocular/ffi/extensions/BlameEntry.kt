package com.inso_world.binocular.ffi.extensions

import com.inso_world.binocular.internal.BinocularBlameEntry
import com.inso_world.binocular.internal.BinocularBlameResult
import com.inso_world.binocular.model.vcs.BlameEntry

internal fun BinocularBlameEntry.toDomain(): BlameEntry =
    BlameEntry(
        this.startInBlamedFile.toInt(),
        this.startInSourceFile.toInt(),
        this.len.toInt(),
        this.commitId,
    )

internal fun BinocularBlameResult.toDomain() {
}
