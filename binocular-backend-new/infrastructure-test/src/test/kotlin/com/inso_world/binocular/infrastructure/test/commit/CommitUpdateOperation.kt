package com.inso_world.binocular.infrastructure.test.commit

import com.inso_world.binocular.core.integration.base.InfrastructureDataSetup
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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

/**
 * Tests for updating commits through CommitInfrastructurePort.
 * Verifies that commit updates preserve domain model semantics including
 * parent/child relationships and repository consistency.
 */
internal class CommitUpdateOperation : BaseInfrastructureSpringTest() {
    @Autowired
    private lateinit var infrastructureDataSetup: InfrastructureDataSetup

    @Autowired
    private lateinit var branchPort: BranchInfrastructurePort

    @Autowired
    private lateinit var userPort: UserInfrastructurePort

    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePort

    @Autowired
    private lateinit var commitPort: CommitInfrastructurePort

    private lateinit var repository: Repository
    private lateinit var project: Project
    private lateinit var savedCommit: Commit

    @BeforeEach
    fun setup() {
        infrastructureDataSetup.teardown()

        val tempProject = Project(name = "test project")
        val tempRepo = Repository(
            localPath = "test repository",
            project = tempProject,
        )
        this.project = projectPort.create(tempProject)
        this.repository = this.project.repo ?: throw IllegalStateException("test repository cannot be null")

        val user = User(name = "user 1", repository = repository).apply {
            email = "user@example.com"
        }
        val baseCommit = Commit(
            sha = "1234567890123456789012345678901234567890",
            message = "test commit",
            commitDateTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
            repository = repository,
            committer = user,
        )
        // Create a branch to ensure repository has at least one branch
        Branch(
            name = "fixed branch",
            fullName = "refs/heads/fixed-branch",
            category = ReferenceCategory.LOCAL_BRANCH,
            repository = repository,
            head = baseCommit,
        )

        this.savedCommit = commitPort.create(baseCommit)
    }

    @Test
    fun `update commit unchanged, should not fail`() {
        assertDoesNotThrow {
            commitPort.update(savedCommit)
        }
        assertAll(
            "check database numbers",
            { assertThat(repositoryPort.findAll()).hasSize(1) },
            { assertThat(commitPort.findAll()).hasSize(1) },
            { assertThat(branchPort.findAll()).hasSize(1) },
            { assertThat(userPort.findAll()).hasSize(1) },
        )
    }

    @Test
    fun `update commit, add parent relationship`() {
        // Create a parent commit
        val parentUser = User(name = "parent author", repository = repository).apply {
            email = "parent@example.com"
        }
        val parentCommit = Commit(
            sha = "0".repeat(40),
            message = "parent commit",
            commitDateTime = LocalDateTime.of(2019, 12, 31, 23, 59, 59),
            repository = repository,
            committer = parentUser,
        )
        val savedParent = commitPort.create(parentCommit)

        // Add parent relationship
        savedCommit.parents.add(savedParent)

        val updatedEntity = assertDoesNotThrow {
            commitPort.update(savedCommit)
        }

        assertAll(
            "check parent-child relationship",
            { assertThat(updatedEntity.parents).hasSize(1) },
            { assertThat(updatedEntity.parents).contains(savedParent) },
            { assertThat(savedParent.children).contains(updatedEntity) },
        )

        assertAll(
            "check database numbers",
            { assertThat(commitPort.findAll()).hasSize(2) },
            { assertThat(userPort.findAll()).hasSize(2) },
        )
    }

    @Test
    fun `update commit, add child relationship`() {
        // Create a child commit
        val childUser = User(name = "child author", repository = repository).apply {
            email = "child@example.com"
        }
        val childCommit = Commit(
            sha = "f".repeat(40),
            message = "child commit",
            commitDateTime = LocalDateTime.of(2020, 1, 2, 0, 0, 0),
            repository = repository,
            committer = childUser,
        )
        val savedChild = commitPort.create(childCommit)

        // Add child relationship
        savedCommit.children.add(savedChild)

        val updatedEntity = assertDoesNotThrow {
            commitPort.update(savedCommit)
        }

        assertAll(
            "check parent-child relationship",
            { assertThat(updatedEntity.children).hasSize(1) },
            { assertThat(updatedEntity.children).contains(savedChild) },
            { assertThat(savedChild.parents).contains(updatedEntity) },
        )

        assertAll(
            "check database numbers",
            { assertThat(commitPort.findAll()).hasSize(2) },
            { assertThat(userPort.findAll()).hasSize(2) },
        )
    }

    @Test
    fun `update commit, set author when not already set`() {
        // Create a new author
        val author = User(name = "author user", repository = repository).apply {
            email = "author@example.com"
        }

        // Set author on the commit
        savedCommit.author = author

        val updatedEntity = assertDoesNotThrow {
            commitPort.update(savedCommit)
        }

        assertAll(
            "check author is set",
            { assertThat(updatedEntity.author).isNotNull() },
            { assertThat(updatedEntity.author).isEqualTo(author) },
            { assertThat(author.authoredCommits).contains(updatedEntity) },
        )

        assertAll(
            "check database numbers",
            { assertThat(userPort.findAll()).hasSize(2) }, // committer + author
        )
    }

    @Test
    fun `update commit with complex parent graph`() {
        // Create a merge scenario: commit has two parents
        val user1 = User(name = "user1", repository = repository).apply { email = "user1@example.com" }
        val user2 = User(name = "user2", repository = repository).apply { email = "user2@example.com" }

        val parent1 = Commit(
            sha = "a".repeat(40),
            message = "parent 1",
            commitDateTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
            repository = repository,
            committer = user1,
        )
        val parent2 = Commit(
            sha = "b".repeat(40),
            message = "parent 2",
            commitDateTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
            repository = repository,
            committer = user2,
        )

        val savedParent1 = commitPort.create(parent1)
        val savedParent2 = commitPort.create(parent2)

        // Add both parents to savedCommit
        savedCommit.parents.add(savedParent1)
        savedCommit.parents.add(savedParent2)

        val updatedEntity = assertDoesNotThrow {
            commitPort.update(savedCommit)
        }

        assertAll(
            "check parent relationships",
            { assertThat(updatedEntity.parents).hasSize(2) },
            { assertThat(updatedEntity.parents).containsExactlyInAnyOrder(savedParent1, savedParent2) },
            { assertThat(savedParent1.children).contains(updatedEntity) },
            { assertThat(savedParent2.children).contains(updatedEntity) },
        )

        assertAll(
            "check database numbers",
            { assertThat(commitPort.findAll()).hasSize(3) },
            { assertThat(userPort.findAll()).hasSize(3) },
        )
    }
}
