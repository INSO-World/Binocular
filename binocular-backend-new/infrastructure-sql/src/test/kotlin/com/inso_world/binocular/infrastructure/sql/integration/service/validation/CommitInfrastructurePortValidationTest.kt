package com.inso_world.binocular.infrastructure.sql.integration.service.validation

import com.inso_world.binocular.infrastructure.sql.integration.service.base.BaseServiceTest
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.service.CommitInfrastructurePortImpl
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import io.mockk.junit5.MockKExtension
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
internal class CommitInfrastructurePortValidationTest : BaseServiceTest() {
    @Autowired
    private lateinit var commitPort: CommitInfrastructurePortImpl

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
                    run {
                        val repository = Repository(id = "1", localPath = "1")
                        val cmt = Commit(
                            id = null,
                            sha = "", // invalid: should be 40 chars
                            authorDateTime = LocalDateTime.now(),
                            commitDateTime = LocalDateTime.now(),
                            message = "Valid message",
                        )
                        repository.commits.add(cmt)
                        cmt
                    },
                    "sha",
                ),
                Arguments.of(
                    run {
                        val repository = Repository(id = "2", localPath = "2")
                        val cmt = Commit(
                            id = null,
                            sha = "a".repeat(39), // invalid: should be 40 chars
                            authorDateTime = LocalDateTime.now(),
                            commitDateTime = LocalDateTime.now(),
                            message = "Valid message",
                        )
                        repository.commits.add(cmt)
                        cmt
                    },
                    "sha",
                ),
                Arguments.of(
                    run {
                        val repository = Repository(id = "3", localPath = "3")
                        val cmt = Commit(
                            id = null,
                            sha = "b".repeat(41), // invalid: should be 40 chars
                            authorDateTime = LocalDateTime.now(),
                            commitDateTime = LocalDateTime.now(),
                            message = "Valid message",
                        )
                        repository.commits.add(cmt)
                        cmt
                    },
                    "sha",
                ),
                Arguments.of(
                    run {
                        val repository = Repository(id = "4", localPath = "4")
                        val cmt = Commit(
                            id = null,
                            sha = "c".repeat(40),
                            authorDateTime = LocalDateTime.now(),
                            commitDateTime = null, // invalid: NotNull
                            message = "Valid message",
                        )
                        repository.commits.add(cmt)
                        cmt
                    },
                    "commitDateTime",
                ),
                Arguments.of(
                    run {
                        val repository = Repository(id = "5", localPath = "5")
                        val cmt = Commit(
                            id = null,
                            sha = "c".repeat(40),
                            authorDateTime = LocalDateTime.now(),
                            commitDateTime = LocalDateTime.now().plusSeconds(30), // invalid: Future
                            message = "Valid message",
                        )
                        repository.commits.add(cmt)
                        cmt
                    },
                    "commitDateTime",
                ),
//                Arguments.of(
//                    Commit(
//                        id = null,
//                        sha = "d".repeat(40),
//                        authorDateTime = LocalDateTime.now(),
//                        commitDateTime = LocalDateTime.now(),
//                        message = null, // invalid: NotBlank
//                        repositoryId = "1",
//                    ),
//                    "message",
//                ),
//                Arguments.of(
//                    Commit(
//                        id = null,
//                        sha = "d".repeat(40),
//                        authorDateTime = LocalDateTime.now(),
//                        commitDateTime = LocalDateTime.now(),
//                        message = " ", // invalid: NotBlank
//                        repositoryId = "1",
//                    ),
//                    "message",
//                ),
//                Arguments.of(
//                    Commit(
//                        id = null,
//                        sha = "d".repeat(40),
//                        authorDateTime = LocalDateTime.now(),
//                        commitDateTime = LocalDateTime.now(),
//                        message = "", // invalid: NotBlank
//                        repositoryId = "1",
//                    ),
//                    "message",
//                ),
//                Arguments.of(
//                    Commit(
//                        id = null,
//                        sha = "e".repeat(40),
//                        authorDateTime = LocalDateTime.now(),
//                        commitDateTime = LocalDateTime.now(),
//                        message = "Valid message",
//                        repositoryId = null, // invalid: NotNull, TODO only invalid if coming out of mapper, going in is ok e.g. on create
//                    ),
//                    "repositoryId",
//                ),
            )

//        @JvmStatic
//        fun invalidCommitsForDomain(): Stream<Commit> = invalidCommitsForEntity()
    }

    @ParameterizedTest
    @MethodSource("invalidCommitsForEntity")
    fun `create should produce violations for single invalid property`(
        invalidCommit: Commit,
        propertyPath: String,
    ) {
        val dummyRepo =
            RepositoryEntity(
                id = 1,
                localPath = "test r",
                project =
                    ProjectEntity(
                        id = 1,
                        name = "test p",
                    ),
            )
        val dummyBranch =
            Branch(
                name = "branch",
                repository = dummyRepo.toDomain(null),
            )
        invalidCommit.branches.add(dummyBranch)
        dummyBranch.commits.add(invalidCommit)

        val e =
            assertThrows<jakarta.validation.ConstraintViolationException> {
                commitPort.create(invalidCommit)
            }
        assertThat(e.constraintViolations).hasSize(1)
        assertThat(e.message).contains("create.value.$propertyPath:")
    }

    @ParameterizedTest
    @MethodSource("invalidCommitsForEntity")
    fun `update should produce violations for single invalid property`(
        invalidCommit: Commit,
        propertyPath: String,
    ) {
        val dummyRepo =
            RepositoryEntity(
                id = 1,
                localPath = "test r",
                project =
                    ProjectEntity(
                        id = 1,
                        name = "test p",
                    ),
            )
        val dummyBranch =
            Branch(
                name = "branch",
                repository = dummyRepo.toDomain(null),
            )
        invalidCommit.branches.add(dummyBranch)
        dummyBranch.commits.add(invalidCommit)

        val e =
            assertThrows<jakarta.validation.ConstraintViolationException> {
                commitPort.update(invalidCommit)
            }
        assertThat(e.constraintViolations).hasSize(1)
        assertThat(e.message).contains("update.value.$propertyPath:")
    }

    @ParameterizedTest
    @MethodSource("invalidCommitsForEntity")
    fun `delete should produce violations for single invalid property`(
        invalidCommit: Commit,
        propertyPath: String,
    ) {
        val dummyRepo =
            RepositoryEntity(
                id = 1,
                localPath = "test r",
                project =
                    ProjectEntity(
                        id = 1,
                        name = "test p",
                    ),
            )
        val dummyBranch =
            Branch(
                name = "branch",
                repository = dummyRepo.toDomain(null),
            )
        invalidCommit.branches.add(dummyBranch)
        dummyBranch.commits.add(invalidCommit)

        val e =
            assertThrows<jakarta.validation.ConstraintViolationException> {
                commitPort.delete(invalidCommit)
            }
        assertThat(e.constraintViolations).hasSize(1)
        assertThat(e.message).contains("delete.value.$propertyPath:")
    }
}
