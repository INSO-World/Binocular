package com.inso_world.binocular.infrastructure.test.commit

import com.inso_world.binocular.core.integration.base.InfrastructureDataSetup
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.UserInfrastructurePort
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

        val developer = Developer(name = "user 1", email = "user@example.com", repository = repository)
        val baseCommit = Commit(
            sha = "1234567890123456789012345678901234567890",
            message = "test commit",
            authorSignature = Signature(developer = developer, timestamp = LocalDateTime.of(2020, 1, 1, 0, 0, 0)),
            repository = repository,
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
        val parentDeveloper = Developer(name = "parent author", email = "parent@example.com", repository = repository)
        val parentCommit = Commit(
            sha = "0".repeat(40),
            message = "parent commit",
            authorSignature = Signature(developer = parentDeveloper, timestamp = LocalDateTime.of(2019, 12, 31, 23, 59, 59)),
            repository = repository,
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
        val childDeveloper = Developer(name = "child author", email = "child@example.com", repository = repository)
        val childCommit = Commit(
            sha = "f".repeat(40),
            message = "child commit",
            authorSignature = Signature(developer = childDeveloper, timestamp = LocalDateTime.of(2020, 1, 2, 0, 0, 0)),
            repository = repository,
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
    fun `update commit with complex parent graph`() {
        // Create a merge scenario: commit has two parents
        val developer1 = Developer(name = "user1", email = "user1@example.com", repository = repository)
        val developer2 = Developer(name = "user2", email = "user2@example.com", repository = repository)

        val parent1 = Commit(
            sha = "a".repeat(40),
            message = "parent 1",
            authorSignature = Signature(developer = developer1, timestamp = LocalDateTime.of(2020, 1, 1, 0, 0, 0)),
            repository = repository,
        )
        val parent2 = Commit(
            sha = "b".repeat(40),
            message = "parent 2",
            authorSignature = Signature(developer = developer2, timestamp = LocalDateTime.of(2020, 1, 1, 0, 0, 0)),
            repository = repository,
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