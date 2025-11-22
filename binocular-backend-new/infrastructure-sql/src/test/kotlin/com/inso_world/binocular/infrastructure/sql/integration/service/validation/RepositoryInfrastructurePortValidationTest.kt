package com.inso_world.binocular.infrastructure.sql.integration.service.validation

import com.inso_world.binocular.infrastructure.sql.integration.service.base.BaseServiceTest
import com.inso_world.binocular.infrastructure.sql.service.RepositoryInfrastructurePortImpl
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import com.inso_world.binocular.model.vcs.ReferenceCategory
import io.mockk.junit5.MockKExtension
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.stream.Stream

@ExtendWith(MockKExtension::class)
@Disabled
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
                    run {
                        val repository = Repository(localPath = "1", project = Project("test 1"))
                        val committer = User(name = "Committer-1", repository = repository).apply { email = "1@repo.invalid" }
                        val cmt = Commit(
                            sha = "", // invalid: should be 40 chars
                            authorDateTime = LocalDateTime.now(),
                            commitDateTime = LocalDateTime.now(),
                            message = "Valid message",
                            repository = repository,
                            committer = committer,
                        )
                        cmt
                    },
                    "sha",
                ),
                Arguments.of(
                    run {
                        val repository = Repository(localPath = "2", project = Project("test 2"))
                        val committer = User(name = "Committer-2", repository = repository).apply { email = "2@repo.invalid" }
                        val cmt = Commit(
                            sha = "a".repeat(39), // invalid: should be 40 chars
                            authorDateTime = LocalDateTime.now(),
                            commitDateTime = LocalDateTime.now(),
                            message = "Valid message",
                            repository = repository,
                            committer = committer,
                        )
                        cmt
                    },
                    "sha",
                ),
                Arguments.of(
                    run {
                        val repository = Repository(localPath = "3", project = Project("test 3"))
                        val committer = User(name = "Committer-3", repository = repository).apply { email = "3@repo.invalid" }
                        val cmt = Commit(
                            sha = "b".repeat(41), // invalid: should be 40 chars
                            authorDateTime = LocalDateTime.now(),
                            commitDateTime = LocalDateTime.now(),
                            message = "Valid message",
                            repository = repository,
                            committer = committer,
                        )
                        cmt
                    },
                    "sha",
                ),
                Arguments.of(
                    run {
                        val repository = Repository(localPath = "4", project = Project("test 4"))
                        val committer = User(name = "Committer-4", repository = repository).apply { email = "4@repo.invalid" }
                        val cmt = Commit(
                            sha = "c".repeat(40),
                            authorDateTime = LocalDateTime.now(),
                            commitDateTime = null, // invalid: NotNull
                            message = "Valid message",
                            repository = repository,
                            committer = committer,
                        )
                        cmt
                    },
                    "commitDateTime",
                ),
                Arguments.of(
                    run {
                        val repository = Repository(localPath = "5", project = Project("test 5"))
                        val committer = User(name = "Committer-5", repository = repository).apply { email = "5@repo.invalid" }
                        val cmt = Commit(
                            sha = "c".repeat(40),
                            authorDateTime = LocalDateTime.now(),
                            commitDateTime = LocalDateTime.now().plusSeconds(60), // invalid: Future
                            message = "Valid message",
                            repository = repository,
                            committer = committer,
                        )
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
//                        repository = null, // invalid: NotNull, TODO only invalid if coming out of mapper, going in is ok e.g. on create
//                    ),
//                    "repositoryId",
//                ),
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
                name = "test p",
            ).apply { id = "1" }
        val dummyRepo = invalidCommit.repository
        val dummyBranch =
            Branch(
                name = "branch",
                fullName = "refs/heads/branch",
                category = ReferenceCategory.LOCAL_BRANCH,
                repository = dummyRepo,
                head = invalidCommit
            )
//        invalidCommit.branches.add(dummyBranch)
//        dummyBranch.commits.add(invalidCommit)
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
                name = "test p",
            ).apply { id = "1" }
        val dummyRepo = invalidCommit.repository
        val dummyBranch =
            Branch(
                name = "branch",
                fullName = "refs/heads/branch",
                category = ReferenceCategory.LOCAL_BRANCH,
                repository = dummyRepo,
                head = invalidCommit
            )
//        invalidCommit.branches.add(dummyBranch)
//        dummyBranch.commits.add(invalidCommit)
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
                name = "test p",
            ).apply { id = "1" }
        val dummyRepo = invalidCommit.repository
        val dummyBranch =
            Branch(
                name = "branch",
                fullName = "refs/heads/branch",
                category = ReferenceCategory.LOCAL_BRANCH,
                repository = dummyRepo,
                head = invalidCommit
            )
//        invalidCommit.branches.add(dummyBranch)
//        dummyBranch.commits.add(invalidCommit)
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
