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
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.ReflectionUtils.setField
import java.time.LocalDateTime

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
                sha = "a".repeat(40),
                message = "message",
                repository = validRepository,
                commitDateTime = LocalDateTime.now(),
                committer = User(name = "a", repository = validRepository),
            )
        )
        repositoryPort.update(validRepository)
        validCommit = validRepository.commits.first()
    }

    // Project validations
    @Test
    fun `project name must not be blank`() {
        assertThrows(ConstraintViolationException::class.java) {
            projectPort.create(Project(name = ""))
        }
    }

    // Repository validations
    @Test
    fun `repository name must not be blank`() {
        assertThrows(ConstraintViolationException::class.java) {
            repositoryPort.create(Repository(localPath = "", project = validProject))
        }
    }

    // Branch validations
    @Test
    fun `branch name must not be blank`() {
        assertThrows(ConstraintViolationException::class.java) {
            branchPort.create(Branch(name = "", fullName = "asdf", repository = validRepository, category = ReferenceCategory.LOCAL_BRANCH, head = validCommit))
        }
    }

    @Test
    fun `branch repository must not be null`() {
        val branch = Branch(name = "main", fullName = "refs/heads/main", repository = validRepository, category = ReferenceCategory.LOCAL_BRANCH, head = validCommit)
        setField(branch.javaClass.getDeclaredField("repository"), branch, null)
        assertThrows(ConstraintViolationException::class.java) {
            branchPort.create(branch)
        }
    }

    // Commit validations
    @Test
    fun `commit sha must be exactly 40 chars`() {
        val committer = User(name = "Test Committer", repository = validRepository).apply { email = "committer@test.com" }
        assertThrows(ConstraintViolationException::class.java) {
            commitPort.create(
                Commit(
                    sha = "abc", // invalid length
                    authorDateTime = LocalDateTime.now(),
                    commitDateTime = LocalDateTime.now(),
                    repository = validRepository,
                    committer = committer,
                ),
            )
        }
    }

    @Test
    fun `commit commitDateTime must not be null`() {
        val committer = User(name = "Test Committer", repository = validRepository).apply { email = "committer@test.com" }
        assertThrows(ConstraintViolationException::class.java) {
            commitPort.create(
                Commit(
                    sha = "a".repeat(40),
                    authorDateTime = LocalDateTime.now(),
                    commitDateTime = null, // invalid
                    repository = validRepository,
                    committer = committer,
                ),
            )
        }
    }

    @Test
    fun `commit repository must not be null`() {
        val committer = User(name = "Test Committer", repository = validRepository).apply { email = "committer@test.com" }
        val commit = Commit(
            sha = "a".repeat(40),
            authorDateTime = LocalDateTime.now(),
            commitDateTime = LocalDateTime.now(),
            repository = validRepository,
            committer = committer,
        )

        // invalid
        setField(commit.javaClass.getDeclaredField("repository"), commit, null)
        assertThrows(ConstraintViolationException::class.java) {
            commitPort.create(commit)
        }
    }

    // User validations
    @Test
    fun `user name must not be blank`() {
        assertThrows(ConstraintViolationException::class.java) {
            userPort.create(User(name = "", repository = validRepository).apply { email = "x@example.com" })
        }
    }

    @Test
    fun `user repository must not be null`() {
        val user = User(name = "Alice", repository = validRepository).apply { email = "a@example.com" }

        setField(user.javaClass.getDeclaredField("repository"), user, null)
        assertThrows(ConstraintViolationException::class.java) {
            userPort.create(user)
        }
    }

    @Test
    fun `commit author must belong to same repository as commit`() {
        val differentProject = projectPort.create(Project(name = "different-project"))
        val differentRepository = repositoryPort.create(Repository(localPath = "different-repo", project = differentProject))
        val authorFromDifferentRepo = User(name = "Different Author", repository = differentRepository).apply { email = "different@test.com" }

        val committer = User(name = "Test Committer", repository = validRepository).apply { email = "committer@test.com" }
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
        val differentRepository = repositoryPort.create(Repository(localPath = "different-repo-2", project = differentProject))
        val committerFromDifferentRepo = User(name = "Different Committer", repository = differentRepository).apply { email = "different@test.com" }

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
