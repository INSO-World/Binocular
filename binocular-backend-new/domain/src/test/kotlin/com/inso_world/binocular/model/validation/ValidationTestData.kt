package com.inso_world.binocular.model.validation

import com.inso_world.binocular.data.MockTestDataProvider
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.utils.ReflectionUtils.Companion.setField
import org.junit.jupiter.params.provider.Arguments
import java.time.LocalDateTime
import java.util.stream.Stream
import kotlin.streams.asStream

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
                run {
                    val repository = Repository(localPath = "test repo", project = Project(name = "test project"))
                    val cmt = Commit(
                        sha = "a".repeat(40), // invalid: should be 40 chars
                        authorDateTime = LocalDateTime.now(),
                        commitDateTime = LocalDateTime.now(),
                        message = "Valid message",
                        repository = repository,
                    )
                    repository.commits.add(cmt)

                    // change field via reflection, otherwise constructor check fails
                    setField(cmt.javaClass.getDeclaredField("sha").apply { isAccessible = true }, cmt, "")

                    cmt
                },
                "sha",
            ),
            Arguments.of(
                run {
                    val repository = Repository(localPath = "2222222", project = Project(name = "test project"))
                    val cmt = Commit(
                        sha = "a".repeat(40),
                        authorDateTime = LocalDateTime.now(),
                        commitDateTime = LocalDateTime.now(),
                        message = "Valid message",
                        repository = repository,
                    )
                    repository.commits.add(cmt)
                    // invalid: should be 40 chars
                    // change field via reflection, otherwise constructor check fails
                    setField(cmt.javaClass.getDeclaredField("sha").apply { isAccessible = true }, cmt, "a".repeat(39))

                    cmt
                },
                "sha",
            ),
            Arguments.of(
                run {
                    val repository = Repository(localPath = "33333", project = Project(name = "test project"))
                    val cmt = Commit(
                        sha = "a".repeat(40),
                        authorDateTime = LocalDateTime.now(),
                        commitDateTime = LocalDateTime.now(),
                        message = "Valid message",
                        repository = repository,
                    )
                    repository.commits.add(cmt)

                    // invalid: should be 40 chars
                    // change field via reflection, otherwise constructor check fails
                    setField(cmt.javaClass.getDeclaredField("sha").apply { isAccessible = true }, cmt, "b".repeat(41))
                    cmt
                },
                "sha",
            ),
            Arguments.of(
                run {
                    val repository = Repository(localPath = "44444", project = Project(name = "test project"))
                    val cmt = Commit(
                        sha = "c".repeat(40),
                        authorDateTime = LocalDateTime.now(),
                        commitDateTime = LocalDateTime.now(), // invalid: NotNull
                        message = "Valid message",
                        repository = repository,
                    )
                    setField(
                        cmt.javaClass.getDeclaredField("commitDateTime"),
                        cmt,
                        null
                    )
                    repository.commits.add(cmt)
                    cmt
                },
                "commitDateTime",
            ),
            *provideInvalidPastOrPresentDateTime()
                .map {
                    Arguments.of(
                        run {
                            val repository = Repository(localPath = "5555", project = Project(name = "test project"))
                            val cmt = Commit(
                                sha = "c".repeat(40),
                                authorDateTime = LocalDateTime.now(),
                                commitDateTime = LocalDateTime.now(), // invalid: Future
                                message = "Valid message",
                                repository = repository,
                            )
                            setField(
                                cmt.javaClass.getDeclaredField("commitDateTime"),
                                cmt,
                                it.get()[0] as LocalDateTime
                            )
                            repository.commits.add(cmt)
                            cmt
                        },
                        "commitDateTime",
                    )
                }.toList()
                .toTypedArray(),
            *provideInvalidPastOrPresentDateTime()
                .map {
                    Arguments.of(
                        run {
                            val repository = Repository(localPath = "6666", project = Project(name = "test project"))
                            val cmt = Commit(
                                sha = "c".repeat(40),
                                authorDateTime = LocalDateTime.now(), // invalid: Future
                                commitDateTime = LocalDateTime.now(),
                                message = "Valid message",
                                repository = repository,
                            )
                            setField(
                                cmt.javaClass.getDeclaredField("authorDateTime"),
                                cmt,
                                it.get()[0] as LocalDateTime
                            )
                            repository.commits.add(cmt)
                            cmt
                        },
                        "authorDateTime",
                    )
                }.toList()
                .toTypedArray(),
        )

    @JvmStatic
    fun mockCommitModels(): Stream<Arguments> {
        return run {
            val project = Project(name = "test-project")
            val repository = Repository(
                localPath = "test",
                project = project,
            )
            return@run MockTestDataProvider(repository).commits.map {
                Arguments.of(it)
            }.asSequence().asStream()
        }
    }
}
