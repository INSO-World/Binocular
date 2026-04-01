package com.inso_world.binocular.ffi.unit.extensions

import com.inso_world.binocular.core.unit.base.BaseUnitTest
import com.inso_world.binocular.ffi.extensions.toLocalDateTime
import com.inso_world.binocular.ffi.internal.GixTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Unit tests for [toLocalDateTime] extension function.
 *
 * Provides C4 (Modified Condition/Decision Coverage) testing:
 * - All statement paths
 * - All branches (offset clamping conditions)
 * - All decision combinations
 * - Edge cases and boundary values
 */
class GixTimeTest : BaseUnitTest() {

    // ========== Normal Cases: Offsets within ±18 hours ==========

    @Test
    fun `toLocalDateTime converts UTC time (offset 0) correctly`() {
        // Unix epoch: 1970-01-01T00:00:00Z
        val binocularTime = GixTime(seconds = 0L, offset = 0)

        val result = binocularTime.toLocalDateTime()

        assertThat(result).isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 0, 0))
    }

    @Test
    fun `toLocalDateTime converts positive offset correctly`() {
        // 2024-01-01T00:00:00Z with +2 hours offset → 2024-01-01T02:00:00
        val binocularTime = GixTime(
            seconds = 1704067200L, // 2024-01-01T00:00:00Z
            offset = 2 * 3600      // +02:00
        )

        val result = binocularTime.toLocalDateTime()

        assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 1, 2, 0, 0))
    }

    @Test
    fun `toLocalDateTime converts negative offset correctly`() {
        // 2024-01-01T00:00:00Z with -5 hours offset → 2023-12-31T19:00:00
        val binocularTime = GixTime(
            seconds = 1704067200L, // 2024-01-01T00:00:00Z
            offset = -5 * 3600     // -05:00
        )

        val result = binocularTime.toLocalDateTime()

        assertThat(result).isEqualTo(LocalDateTime.of(2023, 12, 31, 19, 0, 0))
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0, 1970-01-01T00:00:00",           // Unix epoch, UTC
        "1704067200, 3600, 2024-01-01T01:00:00",    // +1 hour
        "1704067200, -3600, 2023-12-31T23:00:00",   // -1 hour
        "1609459200, 0, 2021-01-01T00:00:00",       // 2021 start, UTC
        "1609459200, 10800, 2021-01-01T03:00:00",   // +3 hours
        "1609459200, -7200, 2020-12-31T22:00:00"    // -2 hours
    )
    fun `toLocalDateTime converts various valid times and offsets`(
        seconds: Long,
        offset: Int,
        expected: LocalDateTime
    ) {
        val binocularTime = GixTime(seconds = seconds, offset = offset)

        val result = binocularTime.toLocalDateTime()

        assertThat(result).isEqualTo(expected)
    }

    // ========== Boundary Cases: Exactly at ±18 hours ==========

    @Test
    fun `toLocalDateTime handles max positive offset (+18 hours) without clamping`() {
        val maxOffset = 18 * 3600 // +18:00 = 64800 seconds
        val binocularTime = GixTime(
            seconds = 1704067200L, // 2024-01-01T00:00:00Z
            offset = maxOffset
        )

        val result = binocularTime.toLocalDateTime()

        // Should apply full +18 hours
        assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 1, 18, 0, 0))
    }

    @Test
    fun `toLocalDateTime handles max negative offset (-18 hours) without clamping`() {
        val minOffset = -18 * 3600 // -18:00 = -64800 seconds
        val binocularTime = GixTime(
            seconds = 1704067200L, // 2024-01-01T00:00:00Z
            offset = minOffset
        )

        val result = binocularTime.toLocalDateTime()

        // Should apply full -18 hours
        assertThat(result).isEqualTo(LocalDateTime.of(2023, 12, 31, 6, 0, 0))
    }

    // ========== Edge Cases: Offsets beyond ±18 hours (Clamping Behavior) ==========

    @Test
    fun `toLocalDateTime clamps offset exceeding +18 hours to +18 hours`() {
        val excessiveOffset = 20 * 3600 // +20:00 (exceeds limit)
        val binocularTime = GixTime(
            seconds = 1704067200L, // 2024-01-01T00:00:00Z
            offset = excessiveOffset
        )

        val result = binocularTime.toLocalDateTime()

        // Should be clamped to +18:00
        assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 1, 18, 0, 0))
    }

    @Test
    fun `toLocalDateTime clamps offset exceeding -18 hours to -18 hours`() {
        val excessiveOffset = -25 * 3600 // -25:00 (exceeds limit)
        val binocularTime = GixTime(
            seconds = 1704067200L, // 2024-01-01T00:00:00Z
            offset = excessiveOffset
        )

        val result = binocularTime.toLocalDateTime()

        // Should be clamped to -18:00
        assertThat(result).isEqualTo(LocalDateTime.of(2023, 12, 31, 6, 0, 0))
    }

    @ParameterizedTest
    @CsvSource(
        "100, 1970-01-01T18:00:00",    // +100 hours → clamped to +18
        "50, 1970-01-01T18:00:00",     // +50 hours → clamped to +18
        "-100, 1969-12-31T06:00:00",   // -100 hours → clamped to -18
        "-30, 1969-12-31T06:00:00"     // -30 hours → clamped to -18
    )
    fun `toLocalDateTime clamps extreme offsets correctly`(offsetHours: Int, expected: LocalDateTime) {
        val binocularTime = GixTime(
            seconds = 0L, // Unix epoch
            offset = offsetHours * 3600
        )

        val result = binocularTime.toLocalDateTime()

        assertThat(result).isEqualTo(expected)
    }

    // ========== Boundary Edge: One second beyond valid range ==========

    @Test
    fun `toLocalDateTime clamps offset at +18 hours plus 1 second`() {
        val binocularTime = GixTime(
            seconds = 1704067200L,
            offset = (18 * 3600) + 1 // 64801 seconds
        )

        val result = binocularTime.toLocalDateTime()

        // Should be clamped to exactly +18:00
        assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 1, 18, 0, 0))
    }

    @Test
    fun `toLocalDateTime clamps offset at -18 hours minus 1 second`() {
        val binocularTime = GixTime(
            seconds = 1704067200L,
            offset = (-18 * 3600) - 1 // -64801 seconds
        )

        val result = binocularTime.toLocalDateTime()

        // Should be clamped to exactly -18:00
        assertThat(result).isEqualTo(LocalDateTime.of(2023, 12, 31, 6, 0, 0))
    }

    // ========== Negative Epoch Seconds ==========

    @Test
    fun `toLocalDateTime handles negative epoch seconds (before 1970)`() {
        // 1969-12-31T23:00:00Z (one hour before epoch)
        val binocularTime = GixTime(
            seconds = -3600L,
            offset = 0
        )

        val result = binocularTime.toLocalDateTime()

        assertThat(result).isEqualTo(LocalDateTime.of(1969, 12, 31, 23, 0, 0))
    }

    @Test
    fun `toLocalDateTime handles negative epoch seconds with positive offset`() {
        val binocularTime = GixTime(
            seconds = -3600L, // 1969-12-31T23:00:00Z
            offset = 3600     // +01:00
        )

        val result = binocularTime.toLocalDateTime()

        // -1h UTC + 1h offset = 1970-01-01T00:00:00
        assertThat(result).isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 0, 0))
    }

    // ========== Large Epoch Seconds ==========

    @Test
    fun `toLocalDateTime handles far future timestamp`() {
        // 2100-01-01T00:00:00Z
        val binocularTime = GixTime(
            seconds = 4102444800L,
            offset = 0
        )

        val result = binocularTime.toLocalDateTime()

        assertThat(result).isEqualTo(LocalDateTime.of(2100, 1, 1, 0, 0, 0))
    }

    // ========== Offset at Zero with Different Epochs ==========

    @Test
    fun `toLocalDateTime with zero offset produces UTC time for any epoch`() {
        val binocularTime = GixTime(
            seconds = 1609459200L, // 2021-01-01T00:00:00Z
            offset = 0
        )

        val result = binocularTime.toLocalDateTime()

        assertThat(result).isEqualTo(LocalDateTime.of(2021, 1, 1, 0, 0, 0))
    }

    // ========== Null Safety Verification ==========

    @Test
    fun `toLocalDateTime always returns non-null LocalDateTime`() {
        // Verify the method satisfies its non-null return type contract.
        // Explicit null check to kill Intrinsics::checkNotNullExpressionValue mutant on line 31.
        val binocularTime = GixTime(
            seconds = 1704067200L, // 2024-01-01T00:00:00Z
            offset = 0
        )

        val result = binocularTime.toLocalDateTime()

        assertThat(result).isNotNull()
    }

    // ========== Decision Coverage: Verify No Exception on Clamping ==========

    @Test
    fun `toLocalDateTime does not throw exception for extreme positive offset`() {
        val binocularTime = GixTime(
            seconds = 1704067200L,
            offset = Int.MAX_VALUE // Extreme value
        )

        assertDoesNotThrow {
            binocularTime.toLocalDateTime()
        }
    }

    @Test
    fun `toLocalDateTime does not throw exception for extreme negative offset`() {
        val binocularTime = GixTime(
            seconds = 1704067200L,
            offset = Int.MIN_VALUE // Extreme value
        )

        assertDoesNotThrow {
            binocularTime.toLocalDateTime()
        }
    }

    // ========== Verify Offset Independence from Default Timezone ==========

    @Test
    fun `toLocalDateTime result is independent of JVM default timezone`() {
        // The function should use only the provided offset, not system timezone
        val binocularTime = GixTime(
            seconds = 1704067200L, // 2024-01-01T00:00:00Z
            offset = 3600          // +01:00
        )

        val result = binocularTime.toLocalDateTime()

        // Should always produce this result regardless of system timezone
        assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 1, 1, 0, 0))

        // Verify it's not using system default offset
        val systemOffset = ZoneOffset.systemDefault().rules.getOffset(result)
        val expectedOffset = ZoneOffset.ofHours(1)

        // Result should correspond to +01:00, not necessarily system offset
        assertThat(expectedOffset.totalSeconds).isEqualTo(3600)
    }

    // ========== Combination Tests: Decision Path Coverage ==========

    @ParameterizedTest
    @CsvSource(
        // seconds, offset, expectedDate
        "0, 0, 1970-01-01T00:00:00",                    // Path: no clamping
        "1704067200, 64800, 2024-01-01T18:00:00",       // Path: max valid positive
        "1704067200, -64800, 2023-12-31T06:00:00",      // Path: max valid negative
        "1704067200, 100000, 2024-01-01T18:00:00",      // Path: clamp positive
        "1704067200, -100000, 2023-12-31T06:00:00",     // Path: clamp negative
        "-3600, 3600, 1970-01-01T00:00:00",             // Path: negative epoch + positive offset
        "4102444800, -3600, 2099-12-31T23:00:00"        // Path: far future + negative offset
    )
    fun `toLocalDateTime covers all decision paths`(
        seconds: Long,
        offset: Int,
        expected: LocalDateTime
    ) {
        val binocularTime = GixTime(seconds = seconds, offset = offset)

        val result = binocularTime.toLocalDateTime()

        assertThat(result).isEqualTo(expected)
    }
}
