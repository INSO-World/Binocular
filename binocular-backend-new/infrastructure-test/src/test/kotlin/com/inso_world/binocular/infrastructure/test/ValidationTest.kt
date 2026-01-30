package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import com.inso_world.binocular.model.vcs.ReferenceCategory
import jakarta.validation.ConstraintViolationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.util.ReflectionUtils.setField
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

    private lateinit var validProject: Project
    private lateinit var validRepository: Repository
    private lateinit var validCommit: Commit

    @BeforeEach
    fun setup() {
        // Baseline valid project/repository for tests that need them
        validProject = projectPort.create(Project(name = "proj-valid"))
        validRepository = repositoryPort.create(Repository(localPath = "repo-valid", project = validProject))
        val developer = Developer(name = "a", email = "a@a.a", repository = validRepository)
        Commit(
            sha = "af".repeat(20),
            message = "message",
            authorSignature = Signature(developer = developer, timestamp = LocalDateTime.now()),
            repository = validRepository,
        )
        repositoryPort.update(validRepository)
        validCommit = validRepository.commits.first()
    }

    // Project validations
    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.data.DummyTestData#provideBlankStrings")
    fun `project name must not be blank`(name: String) {
        val project = Project(name = "name")
        setField(Project::name.javaField!!, project, name)
        assertThrows(ConstraintViolationException::class.java) {
            projectPort.create(project)
        }
    }

    // Repository validations
    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.data.DummyTestData#provideBlankStrings")
    fun `repository name must not be blank`(name: String) {
        val repository = validRepository
        setField(Repository::localPath.javaField!!, repository, name)
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
        setField(Branch::name.javaField!!, branch, name)
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
        setField(Branch::repository.javaField!!, branch, null)
        val ex = assertThrows<ConstraintViolationException> {
//            branchPort.create(branch)
            repositoryPort.update(validRepository)
        }
        assertThat(ex.constraintViolations.size).isEqualTo(1)
        assertThat(ex.constraintViolations.first().propertyPath.toString()).isEqualTo("create.value.repository")
    }

    // Commit validations
    @ParameterizedTest
    @MethodSource("com.inso_world.binocular.model.validation.ValidationTestData#provideInvalidShaHex")
    fun `commit sha must be exactly 40 chars`(sha: String) {
        val commit = run {
            val developer = Developer(name = "Test Committer", email = "committer@test.com", repository = validRepository)
            return@run Commit(
                sha = "a".repeat(40),
                authorSignature = Signature(developer = developer, timestamp = LocalDateTime.now()),
                repository = validRepository,
            )
        }
        setField(Commit::sha.javaField!!, commit, sha)
        val ex = assertThrows(ConstraintViolationException::class.java) {
            commitPort.create(commit)
        }

        assertThat(ex.constraintViolations).hasSize(1)
        assertThat(ex.constraintViolations.first().propertyPath.toString()).contains(".sha")
    }

    @Test
    fun `commit authorSignature must not be null`() {
        val developer = Developer(name = "Test Committer", email = "committer@test.com", repository = validRepository)
        val commit = Commit(
            sha = "fa".repeat(20),
            authorSignature = Signature(developer = developer, timestamp = LocalDateTime.now()),
            repository = validRepository,
        )
        // null is invalid
        setField(Commit::authorSignature.javaField!!, commit, null)
        val ex = assertThrows(ConstraintViolationException::class.java) {
            commitPort.create(commit)
        }
        assertThat(ex.constraintViolations.size).isEqualTo(1)
        assertThat(ex.constraintViolations.first().propertyPath.toString()).isEqualTo("create.value.authorSignature")
    }

    @Test
    fun `commit repository must not be null`() {
        val developer = Developer(name = "Test Committer", email = "committer@test.com", repository = validRepository)
        val commit = Commit(
            sha = "a".repeat(40),
            authorSignature = Signature(developer = developer, timestamp = LocalDateTime.now()),
            repository = validRepository,
        )

        // invalid
        setField(Commit::repository.javaField!!, commit, null)
        val ex = assertThrows(ConstraintViolationException::class.java) {
            commitPort.create(commit)
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
            Developer(name = "Different Author", email = "different@test.com", repository = differentRepository)

        // Creating a commit with an author from a different repository should fail at construction
        val developer = Developer(name = "Test Committer", email = "committer@test.com", repository = validRepository)
        assertThrows(IllegalArgumentException::class.java) {
            Commit(
                sha = "b".repeat(40),
                authorSignature = Signature(developer = authorFromDifferentRepo, timestamp = LocalDateTime.now()),
                repository = validRepository,
            )
        }
    }

    @Test
    fun `branch head must belong to same repository as branch`() {
        val differentProject = projectPort.create(Project(name = "different-project-2"))
        val differentRepository =
            repositoryPort.create(Repository(localPath = "different-repo-2", project = differentProject))
        val developerFromDifferentRepo =
            Developer(name = "Different Committer", email = "different@test.com", repository = differentRepository)

        val headFromDifferentRepo = Commit(
            sha = "c".repeat(40),
            authorSignature = Signature(developer = developerFromDifferentRepo, timestamp = LocalDateTime.now()),
            repository = differentRepository,
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
