package com.inso_world.binocular.ffi.extensions

import com.inso_world.binocular.ffi.internal.BinocularTime
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/** Translate generator time (epoch seconds + offset) to LocalDateTime. */
internal fun BinocularTime.toLocalDateTime(): LocalDateTime {
    val offset = ZoneOffset.ofTotalSeconds(this.offset)
    return Instant.ofEpochSecond(this.seconds).atOffset(offset).toLocalDateTime()
}
