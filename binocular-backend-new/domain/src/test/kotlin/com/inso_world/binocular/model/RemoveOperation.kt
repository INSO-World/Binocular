package com.inso_world.binocular.model

import com.inso_world.binocular.data.MockTestDataProvider
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class RemoveOperation {
    companion object {
        private val mockTestDataProvider = MockTestDataProvider(
            Repository(
                localPath = "mockTestDataProvider repo",
                project = Project(name = "mockTestDataProvider project")
            )
        )

        @JvmStatic
        fun provideModelCollections(): Stream<Arguments> = Stream.of(
            *run {
                val repo = mockTestDataProvider.repository

                return@run listOf(
                    repo.commits,
                    repo.user,
                    repo.branches,
                ).map { Arguments.of(it) }.toTypedArray()
            },
            *run {
                val cmt = mockTestDataProvider.commitBySha.getValue("a".repeat(40))

                return@run listOf(
                    cmt.children,
                    cmt.parents,
//                    cmt.issues,
//                    cmt.files,
//                    cmt.builds,
//                    cmt.modules,
                ).map { Arguments.of(it) }.toTypedArray()
            },
            *run {
                val branch = mockTestDataProvider.branchByName.getValue("origin/feature/test")

                return@run listOf(
                    branch.files,
                ).map { Arguments.of(it) }.toTypedArray()
            },
            *run {
                val user = mockTestDataProvider.userByEmail.getValue("a@test.com")

                return@run listOf(
                    user.committedCommits,
                    user.authoredCommits,
                    user.files,
//                    user.issues,
                ).map { Arguments.of(it) }.toTypedArray()
            }
        )
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.model.RemoveOperation#provideModelCollections")
    fun `try removeAll, should fail`(
        obj: NonRemovingMutableSet<*>
    ) {
        assertThrows<UnsupportedOperationException> {
            obj.removeAll(setOf())
        }
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.model.RemoveOperation#provideModelCollections")
    fun `try retainAll branch, should fail`(
        obj: NonRemovingMutableSet<*>
    ) {
        assertThrows<UnsupportedOperationException> {
            obj.retainAll(setOf())
        }
    }

    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.model.RemoveOperation#provideModelCollections")
    fun `try remove via iterator branch, should fail`(obj: NonRemovingMutableSet<*>) {
        assertThrows<UnsupportedOperationException> {
            obj.iterator().remove()
        }
    }
}
