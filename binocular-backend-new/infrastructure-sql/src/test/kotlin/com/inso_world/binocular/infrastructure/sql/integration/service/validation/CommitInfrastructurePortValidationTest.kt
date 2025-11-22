package com.inso_world.binocular.infrastructure.sql.integration.service.validation

import com.inso_world.binocular.infrastructure.sql.integration.service.base.BaseServiceTest
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.service.CommitInfrastructurePortImpl
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import com.inso_world.binocular.model.vcs.ReferenceCategory
import io.mockk.junit5.MockKExtension
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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExtendWith(MockKExtension::class)
@OptIn(ExperimentalUuidApi::class)
@Disabled
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
                        val repository = Repository(localPath = "1", project = Project("test")).apply { id = "1" }
                        val committer = User(name = "Committer-1", repository = repository).apply { email = "1@test.invalid" }
                        val cmt = Commit(
                            sha = "", // invalid: should be 40 chars
                            authorDateTime = LocalDateTime.now(),
                            commitDateTime = LocalDateTime.now(),
                            message = "Valid message",
                            repository = repository,
                            committer = committer
                        )
                        repository.commits.add(cmt)
                        cmt
                    },
                    "sha",
                ),
                Arguments.of(
                    run {
                        val repository = Repository(localPath = "2", project = Project("test")).apply { id = "2" }
                        val committer = User(name = "Committer-2", repository = repository).apply { email = "2@test.invalid" }
                        val cmt = Commit(
                            sha = "a".repeat(39), // invalid: should be 40 chars
                            authorDateTime = LocalDateTime.now(),
                            commitDateTime = LocalDateTime.now(),
                            message = "Valid message",
                            repository = repository,
                            committer = committer
                        )
                        repository.commits.add(cmt)
                        cmt
                    },
                    "sha",
                ),
                Arguments.of(
                    run {
                        val repository = Repository(localPath = "3", project = Project("test")).apply { id = "3" }
                        val committer = User(name = "Committer-3", repository = repository).apply { email = "3@test.invalid" }
                        val cmt = Commit(
                            sha = "b".repeat(41), // invalid: should be 40 chars
                            authorDateTime = LocalDateTime.now(),
                            commitDateTime = LocalDateTime.now(),
                            message = "Valid message",
                            repository = repository,
                            committer = committer,
                        )
                        repository.commits.add(cmt)
                        cmt
                    },
                    "sha",
                ),
                Arguments.of(
                    run {
                        val repository = Repository(localPath = "4", project = Project("test")).apply { id = "4" }
                        val committer = User(name = "Committer-4", repository = repository).apply { email = "4@test.invalid" }
                        val cmt = Commit(
                            sha = "c".repeat(40),
                            authorDateTime = LocalDateTime.now(),
                            commitDateTime = null, // invalid: NotNull
                            message = "Valid message",
                            repository = repository,
                            committer = committer,
                        )
                        repository.commits.add(cmt)
                        cmt
                    },
                    "commitDateTime",
                ),
                Arguments.of(
                    run {
                        val repository = Repository(localPath = "5", project = Project("test")).apply { id = "5" }
                        val committer = User(name = "Committer-5", repository = repository).apply { email = "5@test.invalid" }
                        val cmt = Commit(
                            sha = "c".repeat(40),
                            authorDateTime = LocalDateTime.now(),
                            commitDateTime = LocalDateTime.now().plusSeconds(30), // invalid: Future
                            message = "Valid message",
                            repository = repository,
                            committer = committer,
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
                iid = Repository.Id(Uuid.random()),
                localPath = "test r",
                project =
                    ProjectEntity(
                        iid = Project.Id(Uuid.random()),
                        name = "test p",
                    ).apply { id = 1 },
            ).apply { id = 1 }
        val dummyBranch =
            Branch(
                name = "branch",
                fullName = "refs/heads/branch",
                category = ReferenceCategory.LOCAL_BRANCH,
                repository = invalidCommit.repository,
                head = invalidCommit
            )
//        invalidCommit.branches.add(dummyBranch)
//        dummyBranch.commits.add(invalidCommit)

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
                iid = Repository.Id(Uuid.random()),
                localPath = "test r",
                project =
                    ProjectEntity(
                        iid = Project.Id(Uuid.random()),
                        name = "test p",
                    ).apply { id = 1 },
            ).apply { id = 1 }
        val dummyBranch =
            Branch(
                name = "branch",
                fullName = "refs/heads/branch",
                category = ReferenceCategory.LOCAL_BRANCH,
                head = invalidCommit,
                repository = invalidCommit.repository,
            )
//        invalidCommit.branches.add(dummyBranch)
//        dummyBranch.commits.add(invalidCommit)

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
                iid = Repository.Id(Uuid.random()),
                localPath = "test r",
                project =
                    ProjectEntity(
                        iid = Project.Id(Uuid.random()),
                        name = "test p",
                    ).apply { id = 1 },
            ).apply { id = 1 }
        val dummyBranch =
            Branch(
                name = "branch",
                fullName = "refs/heads/branch",
                category = ReferenceCategory.LOCAL_BRANCH,
                repository = invalidCommit.repository,
                head = invalidCommit,
            )
//        invalidCommit.branches.add(dummyBranch)
//        dummyBranch.commits.add(invalidCommit)

        val e =
            assertThrows<jakarta.validation.ConstraintViolationException> {
                commitPort.delete(invalidCommit)
            }
        assertThat(e.constraintViolations).hasSize(1)
        assertThat(e.message).contains("delete.value.$propertyPath:")
    }
}
