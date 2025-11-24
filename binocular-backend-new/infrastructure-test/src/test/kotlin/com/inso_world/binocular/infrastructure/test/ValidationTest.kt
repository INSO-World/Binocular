package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import com.inso_world.binocular.model.vcs.ReferenceCategory
import jakarta.validation.ConstraintViolationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.ReflectionUtils.setField
import java.time.LocalDateTime
import kotlin.reflect.jvm.javaField

/**
 * Verifies that domain constraints are enforced via the infrastructure ports.
 * Tests ensure that Jakarta validation annotations and domain-level invariants
 * are properly validated when persisting entities through infrastructure ports.
 *
 * These tests focus on validation constraints defined in the domain models:
 * - @NotBlank, @NotNull, @Size, @PastOrPresent annotations
 * - Domain-level require() checks
 * - Repository consistency checks
 */
internal class ValidationTest : BaseInfrastructureSpringTest() {
    @Autowired
    lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    lateinit var repositoryPort: RepositoryInfrastructurePort

    @Autowired
    lateinit var branchPort: BranchInfrastructurePort

    @Autowired
    lateinit var commitPort: CommitInfrastructurePort

    @Autowired
    lateinit var userPort: UserInfrastructurePort

    private lateinit var validProject: Project
    private lateinit var validRepository: Repository
    private lateinit var validCommit: Commit

    @BeforeEach
    fun setup() {
        // Baseline valid project/repository for tests that need them
        validProject = projectPort.create(Project(name = "proj-valid"))
        validRepository = repositoryPort.create(Repository(localPath = "repo-valid", project = validProject))
        validRepository.commits.add(
            Commit(
                sha = "af".repeat(20),
                message = "message",
                repository = validRepository,
                commitDateTime = LocalDateTime.now(),
                committer = User(name = "a", repository = validRepository).apply { email = "a@a.a" },
            )
        )
        repositoryPort.update(validRepository)
        validCommit = validRepository.commits.first()
    }

    // Project validations
    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.data.DummyTestData#provideBlankStrings")
    fun `project name must not be blank`(name: String) {
        val project = Project(name = "name")
        setField(Project::name.javaField!!.apply { isAccessible = true }, project, name)
        assertThrows(ConstraintViolationException::class.java) {
            projectPort.create(project)
        }
    }

    // Repository validations
    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.data.DummyTestData#provideBlankStrings")
    fun `repository name must not be blank`(name: String) {
        val repository = validRepository
        setField(Repository::localPath.javaField!!.apply { isAccessible = true }, repository, name)
        val ex = assertThrows(ConstraintViolationException::class.java) {
            repositoryPort.create(repository)
        }
        assertThat(ex.constraintViolations.size).isEqualTo(1)
        assertThat(ex.constraintViolations.first().propertyPath.toString()).isEqualTo("create.value.localPath")
    }

    // Branch validations
    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.data.DummyTestData#provideBlankStrings")
    fun `branch name must not be blank`(name: String) {
        val branch = Branch(
            name = "name",
            fullName = "asdf",
            repository = validRepository,
            category = ReferenceCategory.LOCAL_BRANCH,
            head = validCommit
        )
        setField(Branch::name.javaField!!.apply { isAccessible = true }, branch, name)
        val ex = assertThrows(ConstraintViolationException::class.java) {
            branchPort.create(branch)
        }
        assertThat(ex.constraintViolations.size).isEqualTo(1)
        assertThat(ex.constraintViolations.first().propertyPath.toString()).isEqualTo("create.value.name")
    }

    @Test
    fun `branch repository must not be null`() {
        val branch = Branch(
            name = "main",
            fullName = "refs/heads/main",
            repository = validRepository,
            category = ReferenceCategory.LOCAL_BRANCH,
            head = validCommit
        )
        setField(Branch::repository.javaField!!.apply { isAccessible = true }, branch, null)
        val ex = assertThrows(ConstraintViolationException::class.java) {
            branchPort.create(branch)
        }
        assertThat(ex.constraintViolations.size).isEqualTo(1)
        assertThat(ex.constraintViolations.first().propertyPath.toString()).isEqualTo("create.value.repository")
    }

    // Commit validations
    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.model.validation.ValidationTestData#provideInvalidShaHex")
    fun `commit sha must be exactly 40 chars`(sha: String) {
        val commit = run {
            val committer =
                User(name = "Test Committer", repository = validRepository).apply { email = "committer@test.com" }
            return@run Commit(
                sha = "a".repeat(40), // invalid length
                authorDateTime = LocalDateTime.now(),
                commitDateTime = LocalDateTime.now(),
                repository = validRepository,
                committer = committer,
            )
        }
        setField(Commit::sha.javaField!!.apply { isAccessible = true }, commit, sha)
        val ex = assertThrows(ConstraintViolationException::class.java) {
            commitPort.create(commit)
        }

        assertThat(ex.constraintViolations).hasSize(1)
        assertThat(ex.constraintViolations.first().propertyPath.toString()).contains(".sha")
    }

    @Test
    fun `commit commitDateTime must not be null`() {
        val committer =
            User(name = "Test Committer", repository = validRepository).apply { email = "committer@test.com" }
        val commit = Commit(
            sha = "fa".repeat(20),
            authorDateTime = LocalDateTime.now(),
            commitDateTime = LocalDateTime.now(),
            repository = validRepository,
            committer = committer,
        )
        // null is invalid
        setField(Commit::commitDateTime.javaField!!.apply { isAccessible = true }, commit, null)
        val ex = assertThrows(ConstraintViolationException::class.java) {
            commitPort.create(commit)
        }
        assertThat(ex.constraintViolations.size).isEqualTo(1)
        assertThat(ex.constraintViolations.first().propertyPath.toString()).isEqualTo("create.value.commitDateTime")
    }

    @Test
    fun `commit repository must not be null`() {
        val committer =
            User(name = "Test Committer", repository = validRepository).apply { email = "committer@test.com" }
        val commit = Commit(
            sha = "a".repeat(40),
            authorDateTime = LocalDateTime.now(),
            commitDateTime = LocalDateTime.now(),
            repository = validRepository,
            committer = committer,
        )

        // invalid
        setField(Commit::repository.javaField!!.apply { isAccessible = true }, commit, null)
        val ex = assertThrows(ConstraintViolationException::class.java) {
            commitPort.create(commit)
        }
        assertThat(ex.constraintViolations.size).isEqualTo(1)
        assertThat(ex.constraintViolations.first().propertyPath.toString()).isEqualTo("create.value.repository")
    }

    // User validations
    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.data.DummyTestData#provideBlankStrings")
    fun `user name must not be blank`(name: String) {
        val user = User(name = "a", repository = validRepository).apply { email = "x@example.com" }
        setField(User::name.javaField!!.apply { isAccessible = true }, user, name)
        val ex = assertThrows(ConstraintViolationException::class.java) {
            userPort.create(user)
        }
        assertThat(ex.constraintViolations.size).isEqualTo(1)
        assertThat(ex.constraintViolations.first().propertyPath.toString()).isEqualTo("create.value.name")
    }

    @Test
    fun `user repository must not be null`() {
        val user = User(name = "Alice", repository = validRepository).apply { email = "a@example.com" }

        setField(User::repository.javaField!!.apply { isAccessible = true }, user, null)
        val ex = assertThrows(ConstraintViolationException::class.java) {
            userPort.create(user)
        }
        assertThat(ex.constraintViolations.size).isEqualTo(1)
        assertThat(ex.constraintViolations.first().propertyPath.toString()).isEqualTo("create.value.repository")
    }

    @Test
    fun `commit author must belong to same repository as commit`() {
        val differentProject = projectPort.create(Project(name = "different-project"))
        val differentRepository =
            repositoryPort.create(Repository(localPath = "different-repo", project = differentProject))
        val authorFromDifferentRepo =
            User(name = "Different Author", repository = differentRepository).apply { email = "different@test.com" }

        val committer =
            User(name = "Test Committer", repository = validRepository).apply { email = "committer@test.com" }
        val commit = Commit(
            sha = "b".repeat(40),
            commitDateTime = LocalDateTime.now(),
            repository = validRepository,
            committer = committer,
        )

        assertThrows(IllegalArgumentException::class.java) {
            commit.author = authorFromDifferentRepo
        }
    }

    @Test
    fun `branch head must belong to same repository as branch`() {
        val differentProject = projectPort.create(Project(name = "different-project-2"))
        val differentRepository =
            repositoryPort.create(Repository(localPath = "different-repo-2", project = differentProject))
        val committerFromDifferentRepo =
            User(name = "Different Committer", repository = differentRepository).apply { email = "different@test.com" }

        val headFromDifferentRepo = Commit(
            sha = "c".repeat(40),
            commitDateTime = LocalDateTime.now(),
            repository = differentRepository,
            committer = committerFromDifferentRepo,
        )

        assertThrows(IllegalArgumentException::class.java) {
            Branch(
                name = "test-branch",
                fullName = "refs/heads/test-branch",
                repository = validRepository,
                category = ReferenceCategory.LOCAL_BRANCH,
                head = headFromDifferentRepo
            )
        }
    }
}
