package com.inso_world.binocular.infrastructure.test.project

import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired

/**
 * Tests for updating projects through ProjectInfrastructurePort.
 *
 * Since #454 a project no longer owns its repository: the former `Project.repo` relation is gone
 * and the association is expressed by the immutable [Repository.projectId]. The original cases of
 * this class ("add empty repository", "remove repository", "change repository, should fail") were
 * all written against that relation and cannot be expressed any more — adding or replacing a
 * repository is now a repository-side operation, and the set-once guarantee is enforced by
 * `projectId` being a `val`. These tests therefore verify that updating a project changes only
 * the project itself and never cascades into the repository, commit or branch collections.
 *
 * Counts are asserted relative to a baseline captured in [setup], because the infrastructure data
 * setup seeds fixture data that these tests deliberately leave in place.
 *
 * Note: [Project.name] is a `val` and `ProjectMapper.toEntity` does not write [Project.description],
 * so a project currently has no persistable mutable scalar at all. `update` is therefore verified
 * for identity preservation and for not cascading, and the unmapped `description` is documented as
 * open drift rather than asserted as persisted.
 */
internal class ProjectUpdateOperationTest : BaseInfrastructureSpringTest() {
    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePort

    @Autowired
    private lateinit var commitPort: CommitInfrastructurePort

    @Autowired
    private lateinit var branchPort: BranchInfrastructurePort

    private lateinit var existingProject: Project
    private lateinit var existingRepository: Repository

    private var baseProjects = 0
    private var baseRepositories = 0
    private var baseCommits = 0
    private var baseBranches = 0

    @BeforeEach
    fun setup() {
        reseedFixture()

        baseProjects = projectPort.findAll().count()
        baseRepositories = repositoryPort.findAll().count()
        baseCommits = commitPort.findAll().count()
        baseBranches = branchPort.findAll().count()

        existingProject = projectPort.create(Project(name = "project-update-operation"))
        existingRepository =
            repositoryPort.create(Repository(localPath = "repo-update-operation", projectId = existingProject.iid))
    }

    /**
     * Replaces the seeded fixture instead of only removing it.
     *
     * `ProjectMapper.toDomain` dereferences the lazy `issues`/`mergeRequests`/`milestones`/
     * `accounts` collections without a null check, and the data setup writes the projects before
     * those collections exist, so `findAll()` fails on the documents of the very first seeding.
     * Re-seeding rewrites them and makes the class order-independent.
     */
    private fun reseedFixture() {
        infrastructureDataSetup.teardown()
        infrastructureDataSetup.setup()
    }

    @Test
    fun `update project, unchanged, should not fail`() {
        assertDoesNotThrow {
            projectPort.update(existingProject)
        }

        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(baseProjects + 1) },
            { assertThat(repositoryPort.findAll()).hasSize(baseRepositories + 1) },
            { assertThat(commitPort.findAll()).hasSize(baseCommits) },
            { assertThat(branchPort.findAll()).hasSize(baseBranches) },
        )
    }

    @Test
    fun `update project preserves its identity and does not duplicate the document`() {
        // Project.name is a val since #454 and description is not mapped to the entity,
        // so an update cannot change any persisted scalar - it must at least stay idempotent.
        existingProject.description = "updated by ProjectUpdateOperationTest"

        val updated = assertDoesNotThrow { projectPort.update(existingProject) }

        val reloaded = projectPort.findByIid(existingProject.iid)
        assertAll(
            { assertThat(updated.iid).isEqualTo(existingProject.iid) },
            // `updated.description` is null: the port returns a freshly mapped instance and the
            // mapper never wrote the field, so the value does not even survive `update` itself.
            { assertThat(existingProject.description).isEqualTo("updated by ProjectUpdateOperationTest") },
            { assertThat(reloaded).isNotNull() },
            { assertThat(reloaded?.name).isEqualTo("project-update-operation") },
            { assertThat(reloaded?.id).isEqualTo(existingProject.id) },
            { assertThat(projectPort.findAll()).hasSize(baseProjects + 1) },
            { assertThat(projectPort.findAll().count { it.iid == existingProject.iid }).isEqualTo(1) },
        )
    }

    @Test
    fun `update project does not cascade into its repository`() {
        existingProject.description = "no cascade expected"

        assertDoesNotThrow { projectPort.update(existingProject) }

        val reloadedRepository = repositoryPort.findByIid(existingRepository.iid)
        assertAll(
            { assertThat(reloadedRepository).isNotNull() },
            { assertThat(reloadedRepository?.projectId).isEqualTo(existingProject.iid) },
            { assertThat(reloadedRepository?.localPath).isEqualTo("repo-update-operation") },
            { assertThat(repositoryPort.findAll()).hasSize(baseRepositories + 1) },
            { assertThat(commitPort.findAll()).hasSize(baseCommits) },
            { assertThat(branchPort.findAll()).hasSize(baseBranches) },
        )
    }

    @Test
    fun `project to repository link is established from the repository side only`() {
        // Replacing the association is impossible by construction: Repository.projectId is a val,
        // which is what the removed "change repository, should fail" case used to assert at runtime.
        val otherProject = projectPort.create(Project(name = "project-update-operation-other"))
        val otherRepository =
            repositoryPort.create(
                Repository(localPath = "repo-update-operation-other", projectId = otherProject.iid),
            )

        assertAll(
            { assertThat(existingRepository.projectId).isEqualTo(existingProject.iid) },
            { assertThat(otherRepository.projectId).isEqualTo(otherProject.iid) },
            { assertThat(repositoryPort.findByIid(otherRepository.iid)?.projectId).isEqualTo(otherProject.iid) },
            { assertThat(repositoryPort.findByIid(existingRepository.iid)?.projectId).isEqualTo(existingProject.iid) },
            { assertThat(projectPort.findAll()).hasSize(baseProjects + 2) },
            { assertThat(repositoryPort.findAll()).hasSize(baseRepositories + 2) },
        )
    }
}
