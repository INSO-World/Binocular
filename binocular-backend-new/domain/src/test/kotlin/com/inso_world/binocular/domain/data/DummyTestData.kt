package com.inso_world.binocular.domain.data

import org.junit.jupiter.params.provider.Arguments
import java.time.LocalDateTime
import java.util.stream.Stream

class DummyTestData {
    companion object {
        @JvmStatic
        fun provideBlankStrings(): Stream<Arguments> =
            Stream.of(
                Arguments.of(""), // Empty string
                Arguments.of("   "), // Spaces only
                Arguments.of("\t"), // Tab only
                Arguments.of("\n"), // Newline only
                Arguments.of(" \t\n "), // Mixed whitespace
                Arguments.of("\r\n"), // Carriage return + newline
            )

        @JvmStatic
        fun provideAllowedStrings(): Stream<Arguments> =
            Stream.of(
                Arguments.of("Namé-With-Ünicode-字符-123"),
                Arguments.of("  Trimmed Name  "),
                Arguments.of("Name-With_Special@Chars#123"),
                Arguments.of("A".repeat(255)),
            )

        @JvmStatic
        fun provideAllowedPastOrPresentDateTime(): Stream<Arguments> =
            Stream.of(
                Arguments.of(LocalDateTime.of(2021, 1, 1, 1, 1, 1, 1)),
                Arguments.of(LocalDateTime.now()),
                Arguments.of(LocalDateTime.now().minusSeconds(1)),
            )

        @JvmStatic
        fun provideInvalidPastOrPresentDateTime(): Stream<Arguments> =
            Stream.of(
                Arguments.of(LocalDateTime.now().plusSeconds(10)),
                Arguments.of(LocalDateTime.now().plusDays(1)),
                Arguments.of(LocalDateTime.now().plusWeeks(1)),
                Arguments.of(LocalDateTime.now().plusMonths(1)),
                Arguments.of(LocalDateTime.now().plusYears(1)),
            )
    }
}
