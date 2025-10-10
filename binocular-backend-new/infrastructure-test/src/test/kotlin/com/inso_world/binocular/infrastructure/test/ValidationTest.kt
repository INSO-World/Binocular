package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.core.service.UserInfrastructurePort
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

/**
 * Verifies that domain constraints are enforced via the core ports.
 * Only imports from core and model are used, as requested.
 */
class ValidationTest : BaseInfrastructureSpringTest() {

    @Autowired lateinit var projectPort: ProjectInfrastructurePort
    @Autowired lateinit var repositoryPort: RepositoryInfrastructurePort
    @Autowired lateinit var branchPort: BranchInfrastructurePort
    @Autowired lateinit var commitPort: CommitInfrastructurePort
    @Autowired lateinit var userPort: UserInfrastructurePort

    private lateinit var validProject: Project
    private lateinit var validRepository: Repository

    @BeforeEach
    fun setup() {
        // Clean state
        userPort.deleteAll()
        branchPort.deleteAll()
        commitPort.deleteAll()
        repositoryPort.deleteAll()
        projectPort.deleteAll()

        // Baseline valid project/repository for tests that need them
        validProject = projectPort.create(Project(name = "proj-valid"))
        validRepository = repositoryPort.create(Repository(name = "repo-valid", project = validProject))
    }

    @AfterEach
    fun cleanup() {
        userPort.deleteAll()
        branchPort.deleteAll()
        commitPort.deleteAll()
        repositoryPort.deleteAll()
        projectPort.deleteAll()
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
            repositoryPort.create(Repository(name = "", project = validProject))
        }
    }

    @Test
    fun `repository project must not be null`() {
        assertThrows(ConstraintViolationException::class.java) {
            repositoryPort.create(Repository(name = "repo-no-project", project = null))
        }
    }

    // Branch validations
    @Test
    fun `branch name must not be blank`() {
        assertThrows(ConstraintViolationException::class.java) {
            branchPort.create(Branch(name = "", repository = validRepository))
        }
    }

    @Test
    fun `branch repository must not be null`() {
        assertThrows(ConstraintViolationException::class.java) {
            branchPort.create(Branch(name = "main", repository = null))
        }
    }

    // Commit validations
    @Test
    fun `commit sha must be exactly 40 chars`() {
        assertThrows(ConstraintViolationException::class.java) {
            commitPort.create(
                Commit(
                    sha = "abc", // invalid length
                    authorDateTime = LocalDateTime.now(),
                    commitDateTime = LocalDateTime.now(),
                    repository = validRepository,
                )
            )
        }
    }

    @Test
    fun `commit commitDateTime must not be null`() {
        assertThrows(ConstraintViolationException::class.java) {
            commitPort.create(
                Commit(
                    sha = "a".repeat(40),
                    authorDateTime = LocalDateTime.now(),
                    commitDateTime = null, // invalid
                    repository = validRepository,
                )
            )
        }
    }

    @Test
    fun `commit repository must not be null`() {
        assertThrows(ConstraintViolationException::class.java) {
            commitPort.create(
                Commit(
                    sha = "a".repeat(40),
                    authorDateTime = LocalDateTime.now(),
                    commitDateTime = LocalDateTime.now(),
                    repository = null, // invalid
                )
            )
        }
    }

    // User validations
    @Test
    fun `user name must not be blank`() {
        assertThrows(ConstraintViolationException::class.java) {
            userPort.create(User(name = "", email = "x@example.com", repository = validRepository))
        }
    }

    @Test
    fun `user repository must not be null`() {
        assertThrows(ConstraintViolationException::class.java) {
            userPort.create(User(name = "Alice", email = "a@example.com", repository = null))
        }
    }
}
