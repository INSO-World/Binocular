package com.inso_world.binocular.infrastructure.test.project

import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BasePortNoDataTest
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import com.inso_world.binocular.model.vcs.ReferenceCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

/**
 * Integration tests for Project persistence update operations via ProjectInfrastructurePort.
 * Tests verify that the project adapter correctly handles repository association changes
 * while respecting the domain model's set-once repo invariant.
 */
@Tag("project")
internal class ProjectUpdateOperationTest : BasePortNoDataTest() {
    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePort

    @Autowired
    private lateinit var commitPort: CommitInfrastructurePort

    @Autowired
    private lateinit var branchPort: BranchInfrastructurePort

    private lateinit var existingProject: Project

    @BeforeEach
    fun setup() {
        existingProject = projectPort.create(Project(name = "test project"))
    }

    /**
     * Counts all developers across all repositories in the database.
     * Developers are part of the Repository aggregate, so we iterate through repositories.
     */
    private fun countDevelopers(): Int = repositoryPort.findAll().sumOf { it.developers.size }

    /**
     * Updating a project with no changes should not fail.
     * The project has no repository attached, so the adapter's "no repo in either" branch is exercised.
     */
    @Test
    fun `update project, unchanged, should not fail`() {
        assertDoesNotThrow {
            projectPort.update(existingProject)
        }

        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(1) },
            { assertThat(repositoryPort.findAll()).hasSize(0) },
            { assertThat(commitPort.findAll()).hasSize(0) },
            { assertThat(branchPort.findAll()).hasSize(0) },
            { assertThat(countDevelopers()).isEqualTo(0) },
        )
    }

    /**
     * Adding a new repository to a project that previously had none should persist the repo.
     * This is the primary test for the MappingContext seeding fix in ProjectInfrastructurePortImpl.update().
     */
    @Test
    fun `update project, add empty repository`() {
        assertThat(existingProject.repo).isNull()

        val saved =
            run {
                Repository(
                    localPath = "test repository",
                    project = existingProject,
                )
                projectPort.update(existingProject)
            }
        assertThat(saved.repo).isNotNull()
        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(1) },
            { assertThat(repositoryPort.findAll()).hasSize(1) },
            { assertThat(commitPort.findAll()).hasSize(0) },
            { assertThat(branchPort.findAll()).hasSize(0) },
            { assertThat(countDevelopers()).isEqualTo(0) },
        )
    }

    /**
     * Attempting to set repo to null should throw IllegalArgumentException at the domain level.
     * The domain invariant prevents nulling the repo, so update() is never reached.
     */
    @Test
    fun `update project, remove repository, should throw IllegalArgumentException`() {
        assertThrows<IllegalArgumentException> {
            existingProject.repo = null
        }
        assertAll(
            "check database numbers unchanged",
            { assertThat(projectPort.findAll()).hasSize(1) },
            { assertThat(repositoryPort.findAll()).hasSize(0) },
            { assertThat(commitPort.findAll()).hasSize(0) },
            { assertThat(branchPort.findAll()).hasSize(0) },
            { assertThat(countDevelopers()).isEqualTo(0) },
        )
    }

    /**
     * Attempting to assign a different repository to a project that already has one
     * should throw IllegalArgumentException at the domain level (set-once guard).
     */
    @Test
    fun `update project, change repository, should throw IllegalArgumentException`() {
        // First attach an initial repo
        Repository(
            localPath = "initial repository",
            project = existingProject,
        )
        projectPort.update(existingProject)

        assertThat(existingProject.repo).isNotNull()

        // Attempting to assign a different repository should fail at the domain level
        assertThrows<IllegalArgumentException> {
            existingProject.repo =
                Repository(
                    localPath = "new repository",
                    project = Project(name = "other project"),
                )
        }
        assertAll(
            "check database numbers unchanged",
            { assertThat(projectPort.findAll()).hasSize(1) },
            { assertThat(repositoryPort.findAll()).hasSize(1) },
            { assertThat(commitPort.findAll()).hasSize(0) },
            { assertThat(branchPort.findAll()).hasSize(0) },
            { assertThat(countDevelopers()).isEqualTo(0) },
        )
    }

    /**
     * Adding commits, branches, and developers to an existing repository should persist all entities.
     * Uses the modern Developer class (not deprecated User).
     * Note: Developer constructor auto-adds itself to repository.developers.
     */
    @Test
    fun `update project, add commits to repository, expect in database`() {
        // First attach and persist an initial repository
        Repository(
            localPath = "initial repository",
            project = existingProject,
        )
        projectPort.update(existingProject)

        val repository = requireNotNull(existingProject.repo)

        // Developer auto-adds itself to repository.developers via constructor init block
        val developer =
            Developer(
                name = "test",
                email = "test@example.com",
                repository = repository,
            )

        val cmt =
            Commit(
                sha = "1234567890123456789012345678901234567890",
                message = "test commit",
                authorSignature =
                    Signature(
                        developer,
                        timestamp = LocalDateTime.of(2025, 7, 13, 1, 1),
                    ),
                repository = repository,
            )

        val branch =
            Branch(
                name = "test branch",
                fullName = "origin/test branch",
                category = ReferenceCategory.REMOTE_BRANCH,
                head = cmt,
                repository = repository,
            )

        repository.commits.add(cmt)
        repository.branches.add(branch)

        assertAll(
            "check model",
            { assertThat(existingProject.repo).isNotNull() },
            { assertThat(existingProject.repo?.commits).hasSize(1) },
            { assertThat(existingProject.repo?.developers).hasSize(1) },
            { assertThat(existingProject.repo?.branches).hasSize(1) },
        )

        assertDoesNotThrow {
            projectPort.update(existingProject)
        }
        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(1) },
            { assertThat(repositoryPort.findAll()).hasSize(1) },
            { assertThat(commitPort.findAll()).hasSize(1) },
            { assertThat(branchPort.findAll()).hasSize(1) },
            { assertThat(countDevelopers()).isEqualTo(1) },
        )
    }
}
