package com.inso_world.binocular.model.validation

import com.inso_world.binocular.domain.data.DummyTestData
import com.inso_world.binocular.domain.data.MockTestDataProvider
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import com.inso_world.binocular.model.utils.ReflectionUtils.Companion.setField
import org.junit.jupiter.params.provider.Arguments
import java.time.LocalDateTime
import java.util.stream.Stream
import kotlin.streams.asStream

internal object ValidationTestData {
    @JvmStatic
    fun provideBlankStrings(): Stream<Arguments> = DummyTestData.provideBlankStrings()

    @JvmStatic
    fun provideInvalidPastOrPresentDateTime(): Stream<Arguments> = DummyTestData.provideInvalidPastOrPresentDateTime()

    @JvmStatic
    fun provideInvalidShaHex(): Stream<Arguments> = Stream.of(
            Arguments.of("a".repeat(38)),
            Arguments.of("a".repeat(39)),
            Arguments.of("a".repeat(41)),
            *(('g'..'z') + ('G'..'Z')).map {
                Arguments.of("$it".repeat(40))
            }.toTypedArray(),
            *(('g'..'z') + ('G'..'Z')).map {
                Arguments.of(it + "0".repeat(39))
            }.toTypedArray(),
        )

    private fun createDeveloper(repository: Repository, email: String = "test@example.com"): Developer =
        Developer(name = "Test Developer", email = email, repository = repository)

    private fun createSignature(developer: Developer, timestamp: LocalDateTime = LocalDateTime.now().minusSeconds(1)): Signature =
        Signature(developer = developer, timestamp = timestamp)

    @JvmStatic
    fun invalidCommitsModels(): Stream<Arguments> =
        Stream.of(
            Arguments.of(
                run {
                    val repository = Repository(localPath = "test repo", project = Project(name = "test project"))
                    val developer = createDeveloper(repository)
                    val signature = createSignature(developer)
                    val cmt = Commit(
                        sha = "a".repeat(40),
                        authorSignature = signature,
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
                    val developer = createDeveloper(repository, "test2@example.com")
                    val signature = createSignature(developer)
                    val cmt = Commit(
                        sha = "a".repeat(40),
                        authorSignature = signature,
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
                    val developer = createDeveloper(repository, "test3@example.com")
                    val signature = createSignature(developer)
                    val cmt = Commit(
                        sha = "a".repeat(40),
                        authorSignature = signature,
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
