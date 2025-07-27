package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Commit
import org.junit.jupiter.params.provider.Arguments
import java.time.LocalDateTime
import java.util.stream.Stream

internal object ValidationTestData {
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
    fun provideInvalidPastOrPresentDateTime(): Stream<Arguments> =
        Stream.of(
            Arguments.of(LocalDateTime.now().plusSeconds(10)),
            Arguments.of(LocalDateTime.now().plusDays(1)),
            Arguments.of(LocalDateTime.now().plusWeeks(1)),
            Arguments.of(LocalDateTime.now().plusMonths(1)),
            Arguments.of(LocalDateTime.now().plusYears(1)),
        )

    @JvmStatic
    fun invalidCommitsModels(): Stream<Arguments> =
        Stream.of(
            Arguments.of(
                Commit(
                    id = null,
                    sha = "", // invalid: should be 40 chars
                    authorDateTime = LocalDateTime.now(),
                    commitDateTime = LocalDateTime.now(),
                    message = "Valid message",
                    repositoryId = "1",
                ),
                "sha",
            ),
            Arguments.of(
                Commit(
                    id = null,
                    sha = "a".repeat(39), // invalid: should be 40 chars
                    authorDateTime = LocalDateTime.now(),
                    commitDateTime = LocalDateTime.now(),
                    message = "Valid message",
                    repositoryId = "1",
                ),
                "sha",
            ),
            Arguments.of(
                Commit(
                    id = null,
                    sha = "b".repeat(41), // invalid: should be 40 chars
                    authorDateTime = LocalDateTime.now(),
                    commitDateTime = LocalDateTime.now(),
                    message = "Valid message",
                    repositoryId = "1",
                ),
                "sha",
            ),
            Arguments.of(
                Commit(
                    id = null,
                    sha = "c".repeat(40),
                    authorDateTime = LocalDateTime.now(),
                    commitDateTime = null, // invalid: NotNull
                    message = "Valid message",
                    repositoryId = "1",
                ),
                "commitDateTime",
            ),
            *provideInvalidPastOrPresentDateTime()
                .map {
                    Arguments.of(
                        Commit(
                            id = null,
                            sha = "c".repeat(40),
                            authorDateTime = LocalDateTime.now(),
                            commitDateTime = it.get()[0] as LocalDateTime, // invalid: Future
                            message = "Valid message",
                            repositoryId = "1",
                        ),
                        "commitDateTime",
                    )
                }.toList()
                .toTypedArray(),
            *provideInvalidPastOrPresentDateTime()
                .map {
                    Arguments.of(
                        Commit(
                            id = null,
                            sha = "c".repeat(40),
                            authorDateTime = it.get()[0] as LocalDateTime, // invalid: Future
                            commitDateTime = LocalDateTime.now(),
                            message = "Valid message",
                            repositoryId = "1",
                        ),
                        "authorDateTime",
                    )
                }.toList()
                .toTypedArray(),
            // Add all blank string cases from provideBlankStrings()
//            *provideBlankStrings()
//                .map {
//                    Arguments.of(
//                        Commit(
//                            id = null,
//                            sha = "d".repeat(40),
//                            authorDateTime = LocalDateTime.now(),
//                            commitDateTime = LocalDateTime.now(),
//                            message = it.get()[0] as String, // extract the blank string
//                            repositoryId = "1",
//                        ),
//                        "message",
//                    )
//                }.toList()
//                .toTypedArray(),
//            run {
//            }
//            Arguments.of(
//                Commit(
//                    id = null,
//                    sha = "e".repeat(40),
//                    authorDateTime = LocalDateTime.now(),
//                    commitDateTime = LocalDateTime.now(),
//                    message = "Valid message",
//                    repositoryId = null, // invalid: NotNull, TODO only invalid if coming out of mapper, going in is ok e.g. on create
//                ),
//                "repositoryId",
//            ),
        )
}
