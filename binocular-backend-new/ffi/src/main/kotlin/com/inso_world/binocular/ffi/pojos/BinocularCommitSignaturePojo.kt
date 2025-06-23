package com.inso_world.binocular.ffi.pojos

import com.inso_world.binocular.internal.BinocularSig
import com.inso_world.binocular.internal.BinocularTime

data class BinocularCommitSignaturePojo(
    var name: String,
    var email: String,
    var time: BinocularTimePojo,
)

internal fun BinocularSig.toPojo(): BinocularCommitSignaturePojo =
    BinocularCommitSignaturePojo(
        name = this.name,
        email = this.email,
        time = this.time.toPojo(),
    )

data class BinocularTimePojo(
    var seconds: Long,
    var offset: Int,
)

internal fun BinocularTime.toPojo(): BinocularTimePojo = BinocularTimePojo(seconds, offset)
