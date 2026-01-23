package com.inso_world.binocular.infrastructure.test.repository

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
 * Tests for saving repositories through RepositoryInfrastructurePort.
 * Verifies that repositories with commits, branches, and developers are persisted correctly
 * while maintaining domain model semantics.
 */
internal class RepositorySaveOperation : BaseInfrastructureSpringTest() {
    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePort

    @Autowired
    private lateinit var commitPort: CommitInfrastructurePort

    @Autowired
    private lateinit var userPort: UserInfrastructurePort

    @Autowired
    private lateinit var branchPort: BranchInfrastructurePort

    private lateinit var project: Project

    @BeforeEach
    fun setup() {
        super.baseTearDown()
        project = projectPort.create(Project(name = "test-project"))
    }

    @Test
    fun `save plain repository, expecting in database`() {
        val repository = Repository(
            localPath = "path/to/repo",
            project = project,
        )

        assertDoesNotThrow {
            repositoryPort.create(repository)
        }

        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(1) },
            { assertThat(repositoryPort.findAll()).hasSize(1) },
            { assertThat(commitPort.findAll()).hasSize(0) },
            { assertThat(branchPort.findAll()).hasSize(0) },
        )
    }

    @Test
    fun `save repository with one commit, expecting in database`() {
        val repository = Repository(localPath = "path/to/repo-2", project = project)
        val developer = Developer(name = "test-user-a", email = "a@test.com", repository = repository)
        val commit = Commit(
            sha = "a".repeat(40),
            message = "Initial commit",
            authorSignature = Signature(developer = developer, timestamp = LocalDateTime.of(2020, 1, 1, 0, 0, 0)),
            repository = repository,
        )
        val branch = Branch(
            name = "main",
            fullName = "refs/heads/main",
            category = ReferenceCategory.LOCAL_BRANCH,
            repository = repository,
            head = commit,
        )

        val savedRepo = repositoryPort.create(repository)

        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(1) },
            { assertThat(repositoryPort.findAll()).hasSize(1) },
            { assertThat(commitPort.findAll()).hasSize(1) },
            { assertThat(branchPort.findAll()).hasSize(1) },
            { assertThat(userPort.findAll()).hasSize(1) },
        )
        assertThat(commitPort.findAll().toList()[0]).usingRecursiveComparison()
            .ignoringCollectionOrder()
            .ignoringFieldsMatchingRegexes(".*id", ".*repositoryId", ".*project")
            .isEqualTo(savedRepo.commits.toList()[0])
        assertAll(
            "check commit relationship",
            { assertThat(commitPort.findAll().toList()[0].id).isNotNull() },
            { assertThat(commitPort.findAll().toList()[0].repository).isNotNull() },
            { assertThat(commitPort.findAll().toList()[0].repository.id).isNotNull() },
            { assertThat(commitPort.findAll().toList()[0].repository.id).isEqualTo(savedRepo.id) },
        )
    }

    @Test
    fun `save repository with one commit with one parent, expecting both in database`() {
        val repository = Repository(localPath = "path/to/repo-3", project = project)
        val developerA = Developer(name = "user-a", email = "a@test.com", repository = repository)
        val developerB = Developer(name = "user-b", email = "b@test.com", repository = repository)

        val cmtB = Commit(
            sha = "b".repeat(40),
            message = "Parent commit",
            authorSignature = Signature(developer = developerB, timestamp = LocalDateTime.of(2020, 1, 1, 0, 0, 0)),
            repository = repository,
        )
        val cmtA = Commit(
            sha = "a".repeat(40),
            message = "Child commit",
            authorSignature = Signature(developer = developerA, timestamp = LocalDateTime.of(2020, 1, 2, 0, 0, 0)),
            repository = repository,
        )
        cmtA.parents.add(cmtB)

        val branch = Branch(
            name = "feature/test",
            fullName = "refs/heads/feature/test",
            category = ReferenceCategory.LOCAL_BRANCH,
            repository = repository,
            head = cmtA,
        )

        val savedRepo = repositoryPort.create(repository)

        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(1) },
            { assertThat(repositoryPort.findAll()).hasSize(1) },
            { assertThat(commitPort.findAll()).hasSize(2) },
            { assertThat(branchPort.findAll()).hasSize(1) },
            { assertThat(userPort.findAll()).hasSize(2) },
        )

        // Verify parent commit
        val savedParent = commitPort.findAll().find { it.sha == "b".repeat(40) }
        assertThat(savedParent).isNotNull()
        assertThat(savedParent!!.children).hasSize(1)
        assertThat(savedParent.children.first().sha).isEqualTo("a".repeat(40))

        // Verify child commit
        val savedChild = commitPort.findAll().find { it.sha == "a".repeat(40) }
        assertThat(savedChild).isNotNull()
        assertThat(savedChild!!.parents).hasSize(1)
        assertThat(savedChild.parents.first().sha).isEqualTo("b".repeat(40))

        assertAll(
            "check commit relationship",
            { assertThat(commitPort.findAll().map { it.id }).doesNotContainNull() },
            { assertThat(commitPort.findAll().map { it.repository.id }).doesNotContainNull() },
            { assertThat(commitPort.findAll().map { it.repository.id }).containsOnly(savedRepo.id) },
        )
    }

    @Test
    fun `save repository with one commit with two parents, expecting all in database`() {
        val repository = Repository(localPath = "path/to/repo-4", project = project)
        val developerA = Developer(name = "user-a", email = "a@test.com", repository = repository)
        val developerB = Developer(name = "user-b", email = "b@test.com", repository = repository)
        val developerC = Developer(name = "user-c", email = "author@test.com", repository = repository)

        val cmtB = Commit(
            sha = "b".repeat(40),
            message = "Parent 1",
            authorSignature = Signature(developer = developerB, timestamp = LocalDateTime.of(2020, 1, 1, 0, 0, 0)),
            repository = repository,
        )
        val cmtC = Commit(
            sha = "c".repeat(40),
            message = "Parent 2",
            authorSignature = Signature(developer = developerC, timestamp = LocalDateTime.of(2020, 1, 1, 0, 0, 0)),
            repository = repository,
        )
        val cmtA = Commit(
            sha = "a".repeat(40),
            message = "Merge commit",
            authorSignature = Signature(developer = developerA, timestamp = LocalDateTime.of(2020, 1, 2, 0, 0, 0)),
            repository = repository,
        )
        cmtA.parents.add(cmtB)
        cmtA.parents.add(cmtC)

        val branch = Branch(
            name = "main",
            fullName = "refs/heads/main",
            category = ReferenceCategory.LOCAL_BRANCH,
            repository = repository,
            head = cmtA,
        )

        assertThat(repository.developers).hasSize(3)

        val savedRepo = repositoryPort.create(repository)

        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(1) },
            { assertThat(repositoryPort.findAll()).hasSize(1) },
            { assertThat(commitPort.findAll()).hasSize(3) },
            { assertThat(branchPort.findAll()).hasSize(1) },
            { assertThat(userPort.findAll()).hasSize(3) },
        )

        // Verify merge commit
        val mergeCommit = commitPort.findAll().find { it.sha == "a".repeat(40) }
        assertThat(mergeCommit).isNotNull()
        assertThat(mergeCommit!!.parents).hasSize(2)

        // Verify parent commits
        val parent1 = commitPort.findAll().find { it.sha == "b".repeat(40) }
        val parent2 = commitPort.findAll().find { it.sha == "c".repeat(40) }
        assertThat(parent1).isNotNull()
        assertThat(parent2).isNotNull()
        assertThat(parent1!!.children).contains(mergeCommit)
        assertThat(parent2!!.children).contains(mergeCommit)

        // Verify parent 2 has both author and committer set
        assertThat(parent2.author).isNotNull()
        assertThat(parent2.committer).isNotNull()
        assertThat(parent2.author).isEqualTo(parent2.committer)
    }

    @Test
    fun `save repository with two commits, expecting in database`() {
        val repository = Repository(localPath = "path/to/repo-5", project = project)

        val developerA = Developer(name = "user-a", email = "a@test.com", repository = repository)
        val developerB = Developer(name = "user-b", email = "b@test.com", repository = repository)

        val cmtA = Commit(
            sha = "a".repeat(40),
            message = "First commit",
            authorSignature = Signature(developer = developerA, timestamp = LocalDateTime.of(2020, 1, 1, 0, 0, 0)),
            repository = repository,
        )
        val cmtB = Commit(
            sha = "b".repeat(40),
            message = "Second commit",
            authorSignature = Signature(developer = developerB, timestamp = LocalDateTime.of(2020, 1, 2, 0, 0, 0)),
            repository = repository,
        )

        // Make cmtB a child of cmtA
        cmtB.parents.add(cmtA)

        val branch = Branch(
            name = "test branch",
            fullName = "refs/heads/test-branch",
            category = ReferenceCategory.LOCAL_BRANCH,
            repository = repository,
            head = cmtB,
        )

        assertAll(
            "check model before save",
            { assertThat(branch.commits).hasSize(2).withFailMessage("branch.commits should contain both commits") },
            { assertThat(repository.branches).hasSize(1).withFailMessage("repository.branches") },
            { assertThat(repository.commits).hasSize(2).withFailMessage("repository.commits") },
            { assertThat(repository.developers).hasSize(2).withFailMessage("repository.developers") },
        )

        val savedEntity = assertDoesNotThrow {
            repositoryPort.create(repository)
        }

        assertAll(
            "check saved entity",
            { assertThat(savedEntity.branches).hasSize(1) },
            { assertThat(savedEntity.commits).hasSize(2) },
        )

        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(1) },
            { assertThat(repositoryPort.findAll()).hasSize(1) },
            { assertThat(commitPort.findAll()).hasSize(2) },
            { assertThat(branchPort.findAll()).hasSize(1) },
            { assertThat(userPort.findAll()).hasSize(2) },
        )
    }
}