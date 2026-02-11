package com.inso_world.binocular.ffi.extensions

import com.inso_world.binocular.ffi.internal.GixTime
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Convert FFI time (epoch seconds + fixed UTC offset in seconds) into a wall-clock [LocalDateTime].
 *
 * ### Semantics
 * - Interprets [seconds] as Unix epoch seconds and [offset] as a **fixed** UTC offset (in seconds).
 * - Applies the offset to the instant and returns the corresponding local date-time at that offset.
 * - Independent of the JVM default time zone (uses only the provided offset).
 *
 * ### Edge cases
 * - Offsets outside the supported ±18:00 range are **clamped** to that range to avoid
 *   `DateTimeException` when constructing a [ZoneOffset].
 * - If sub-second precision is added to the FFI in the future (e.g., `nanos`), extend the
 *   `Instant.ofEpochSecond(seconds, nanos)` call accordingly.
 *
 * @return Local date-time at the provided fixed offset.
 * @see com.inso_world.binocular.ffi.extensions.toDomain for usage in commit mapping.
 */
internal fun GixTime.toLocalDateTime(): LocalDateTime {
    // ZoneOffset supports ±18 hours; clamp defensively
    val safeOffsetSeconds = this.offset.coerceIn(-18 * 3600, 18 * 3600)
    val zoneOffset = ZoneOffset.ofTotalSeconds(safeOffsetSeconds)

    // If GixTime later exposes sub-second precision, use ofEpochSecond(seconds, nanos)
    return Instant.ofEpochSecond(this.seconds).atOffset(zoneOffset).toLocalDateTime()
}
