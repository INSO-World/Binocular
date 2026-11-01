package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import com.inso_world.binocular.model.User
import com.inso_world.binocular.model.vcs.ReferenceCategory
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

/**
 * Verifies that domain constraints are enforced.
 *
 * Since #454 the aggregates guard their invariants in `init` and the owning relations
 * ([Repository.projectId], [Branch.repositoryId], [Commit.repositoryId], [User.repositoryId])
 * are non-nullable, so a violation is rejected at construction time rather than by the port.
 * The former "<relation> must not be null" cases are therefore compile-time guarantees now.
 */
internal class ValidationTest : BaseInfrastructureSpringTest() {
    @Autowired lateinit var projectPort: ProjectInfrastructurePort

    @Autowired lateinit var repositoryPort: RepositoryInfrastructurePort

    private lateinit var validProject: Project
    private lateinit var validRepository: Repository

    @BeforeEach
    fun setup() {
        validProject = projectPort.create(Project(name = "proj-valid"))
        validRepository = repositoryPort.create(Repository(localPath = "repo-valid", projectId = validProject.iid))
    }

    // Project validations
    @Test
    fun `project name must not be blank`() {
        assertThrows(IllegalArgumentException::class.java) {
            Project(name = "")
        }
    }

    // Repository validations
    @Test
    fun `repository localPath must not be blank`() {
        assertThrows(IllegalArgumentException::class.java) {
            Repository(localPath = "", projectId = validProject.iid)
        }
    }

    @Test
    fun `repository localPath must not be whitespace only`() {
        assertThrows(IllegalArgumentException::class.java) {
            Repository(localPath = "   ", projectId = validProject.iid)
        }
    }

    // Branch validations
    @Test
    fun `branch name must not be blank`() {
        assertThrows(IllegalArgumentException::class.java) {
            branch(name = "", fullName = "refs/heads/main")
        }
    }

    @Test
    fun `branch fullName must not be blank`() {
        assertThrows(IllegalArgumentException::class.java) {
            branch(name = "main", fullName = "")
        }
    }

    @Test
    fun `branch headSha must not be blank`() {
        assertThrows(IllegalArgumentException::class.java) {
            branch(name = "main", fullName = "refs/heads/main", headSha = "")
        }
    }

    // Commit validations
    @Test
    fun `commit sha must be exactly 40 chars`() {
        assertThrows(IllegalArgumentException::class.java) {
            commit(sha = "abc")
        }
    }

    @Test
    fun `commit sha must be hexadecimal`() {
        assertThrows(IllegalArgumentException::class.java) {
            commit(sha = "z".repeat(40))
        }
    }

    // User validations
    @Test
    fun `user name must not be blank`() {
        assertThrows(IllegalArgumentException::class.java) {
            User(name = "", repositoryId = validRepository.iid)
        }
    }

    @Test
    fun `user email must not be blank`() {
        val user = User(name = "Alice", repositoryId = validRepository.iid)
        assertThrows(IllegalArgumentException::class.java) {
            user.email = "   "
        }
    }

    private fun branch(
        name: String,
        fullName: String,
        headSha: String = "a".repeat(40),
    ): Branch =
        Branch(
            name = name,
            fullName = fullName,
            category = ReferenceCategory.LOCAL_BRANCH,
            repositoryId = validRepository.iid,
            headSha = headSha,
        )

    private fun commit(sha: String): Commit {
        val developer = Developer(name = "Val Idator", email = "val@example.com", repositoryId = validRepository.iid)
        return Commit(
            sha = sha,
            authorSignature = Signature(developerId = developer.iid, timestamp = LocalDateTime.now()),
            repositoryId = validRepository.iid,
        )
    }
}
