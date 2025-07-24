package com.inso_world.binocular.infrastructure.sql.integration.service.validation

import com.inso_world.binocular.infrastructure.sql.integration.service.base.BaseServiceTest
import com.inso_world.binocular.infrastructure.sql.service.RepositoryInfrastructurePortImpl
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import io.mockk.junit5.MockKExtension
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.stream.Stream

@ExtendWith(MockKExtension::class)
internal class RepositoryInfrastructurePortValidationTest : BaseServiceTest() {
    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePortImpl

    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    companion object {
        @JvmStatic
        fun invalidCommitsForEntity(): Stream<Arguments> =
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
                Arguments.of(
                    Commit(
                        id = null,
                        sha = "c".repeat(40),
                        authorDateTime = LocalDateTime.now(),
                        commitDateTime = LocalDateTime.now().plusSeconds(30), // invalid: Future
                        message = "Valid message",
                        repositoryId = "1",
                    ),
                    "commitDateTime",
                ),
                Arguments.of(
                    Commit(
                        id = null,
                        sha = "d".repeat(40),
                        authorDateTime = LocalDateTime.now(),
                        commitDateTime = LocalDateTime.now(),
                        message = null, // invalid: NotBlank
                        repositoryId = "1",
                    ),
                    "message",
                ),
                Arguments.of(
                    Commit(
                        id = null,
                        sha = "d".repeat(40),
                        authorDateTime = LocalDateTime.now(),
                        commitDateTime = LocalDateTime.now(),
                        message = " ", // invalid: NotBlank
                        repositoryId = "1",
                    ),
                    "message",
                ),
                Arguments.of(
                    Commit(
                        id = null,
                        sha = "d".repeat(40),
                        authorDateTime = LocalDateTime.now(),
                        commitDateTime = LocalDateTime.now(),
                        message = "", // invalid: NotBlank
                        repositoryId = "1",
                    ),
                    "message",
                ),
                Arguments.of(
                    Commit(
                        id = null,
                        sha = "e".repeat(40),
                        authorDateTime = LocalDateTime.now(),
                        commitDateTime = LocalDateTime.now(),
                        message = "Valid message",
                        repositoryId = null, // invalid: NotNull, TODO only invalid if coming out of mapper, going in is ok e.g. on create
                    ),
                    "repositoryId",
                ),
            )
    }

    @ParameterizedTest
    @MethodSource("invalidCommitsForEntity")
    fun `create should produce violations for single invalid commit`(
        invalidCommit: Commit,
        propertyPath: String,
    ) {
        val dummyProject =
            Project(
                id = "1",
                name = "test p",
            )
        val dummyRepo =
            Repository(
                id = "1",
                name = "test r",
                project = dummyProject,
            )
        val dummyBranch =
            Branch(
                name = "branch",
                repositoryId = dummyRepo.id.toString(),
            )
        invalidCommit.branches.add(dummyBranch)
        dummyBranch.commitShas.add(invalidCommit.sha)
        dummyRepo.commits.add(invalidCommit)
        dummyRepo.branches.add(dummyBranch)
        dummyProject.repo = dummyRepo

        val e =
            assertThrows<ConstraintViolationException> {
                repositoryPort.create(dummyRepo)
            }
        assertThat(e.constraintViolations).hasSize(1)
        assertThat(e.message).contains("create.value.commits[].$propertyPath:")
    }

    @ParameterizedTest
    @MethodSource("invalidCommitsForEntity")
    fun `update should produce violations for single invalid commit`(
        invalidCommit: Commit,
        propertyPath: String,
    ) {
        val dummyProject =
            Project(
                id = "1",
                name = "test p",
            )
        val dummyRepo =
            Repository(
                id = "1",
                name = "test r",
                project = dummyProject,
            )
        val dummyBranch =
            Branch(
                name = "branch",
                repositoryId = dummyRepo.id.toString(),
            )
        invalidCommit.branches.add(dummyBranch)
        dummyBranch.commitShas.add(invalidCommit.sha)
        dummyRepo.commits.add(invalidCommit)
        dummyRepo.branches.add(dummyBranch)
        dummyProject.repo = dummyRepo

        val e =
            assertThrows<ConstraintViolationException> {
                repositoryPort.update(dummyRepo)
            }
        assertThat(e.constraintViolations).hasSize(1)
        assertThat(e.message).contains("update.value.commits[].$propertyPath:")
    }

    @ParameterizedTest
    @MethodSource("invalidCommitsForEntity")
    fun `delete should produce violations for single invalid commit`(
        invalidCommit: Commit,
        propertyPath: String,
    ) {
        val dummyProject =
            Project(
                id = "1",
                name = "test p",
            )
        val dummyRepo =
            Repository(
                id = "1",
                name = "test r",
                project = dummyProject,
            )
        val dummyBranch =
            Branch(
                name = "branch",
                repositoryId = dummyRepo.id.toString(),
            )
        invalidCommit.branches.add(dummyBranch)
        dummyBranch.commitShas.add(invalidCommit.sha)
        dummyRepo.commits.add(invalidCommit)
        dummyRepo.branches.add(dummyBranch)
        dummyProject.repo = dummyRepo

        val e =
            assertThrows<ConstraintViolationException> {
                repositoryPort.delete(dummyRepo)
            }
        assertThat(e.constraintViolations).hasSize(1)
        assertThat(e.message).contains("delete.value.commits[].$propertyPath:")
    }
}
