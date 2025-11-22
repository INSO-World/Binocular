package com.inso_world.binocular.infrastructure.test.project

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
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

/**
 * Tests for saving projects through ProjectInfrastructurePort.
 * Verifies that projects with and without repositories are persisted correctly.
 */
internal class ProjectSaveOperation : BaseInfrastructureSpringTest() {
    @Autowired
    private lateinit var projectPort: ProjectInfrastructurePort

    @Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePort

    @Autowired
    private lateinit var commitPort: CommitInfrastructurePort

    @Autowired
    private lateinit var branchPort: BranchInfrastructurePort

    @Autowired
    private lateinit var userPort: UserInfrastructurePort

    @BeforeEach
    fun setup() {
        super.baseTearDown()
    }

    @Test
    fun `save plain project, expecting in database`() {
        val repositoryProject =
            projectPort.create(
                Project(
                    name = "test project",
                ),
            )
        assertAll(
            { assertThat(projectPort.findAll()).hasSize(1) },
            { assertThat(projectPort.findAll().toList()[0].id).isNotNull() },
            {
                assertThat(projectPort.findAll().toList()[0])
                    .usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(repositoryProject)
            },
            { assertThat(repositoryPort.findAll()).hasSize(0) },
            { assertThat(commitPort.findAll()).hasSize(0) },
            { assertThat(branchPort.findAll()).hasSize(0) },
        )
    }

    @Test
    fun `save project with repository, expecting in database`() {
        val project = Project(name = "test project")
        val repository = Repository(
            localPath = "test repository",
            project = project,
        )

        val createdProject = projectPort.create(project)

        assertThat(projectPort.findAll()).hasSize(1)
        run {
            val elem = projectPort.findAll().toList()[0]
            assertThat(elem).isSameAs(requireNotNull(elem.repo).project)
            assertThat(elem)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(createdProject)
            assertThat(elem.repo).isNotNull()
            assertThat(elem.repo?.id).isNotNull()
            assertThat(elem.repo)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFields("id", "project")
                .isEqualTo(repository)
        }
        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(1) },
            { assertThat(repositoryPort.findAll()).hasSize(1) },
            { assertThat(commitPort.findAll()).hasSize(0) },
            { assertThat(branchPort.findAll()).hasSize(0) },
            { assertThat(userPort.findAll()).hasSize(0) },
        )
    }

    @Test
    fun `save project with repository and commits, expecting in database`() {
        val project = Project(name = "test project")
        val repository = Repository(
            localPath = "test repository",
            project = project,
        )
        val user = User(name = "test", repository = repository).apply {
            email = "test@example.com"
        }
        val cmt = Commit(
            sha = "1234567890123456789012345678901234567890",
            message = "test commit",
            commitDateTime = LocalDateTime.of(2025, 7, 13, 1, 1),
            repository = repository,
            committer = user,
        )
        val branch = Branch(
            name = "test branch",
            fullName = "refs/heads/test-branch",
            category = ReferenceCategory.LOCAL_BRANCH,
            repository = repository,
            head = cmt,
        )

        val repositoryProject = projectPort.create(project)

        assertAll(
            "check database numbers",
            { assertThat(projectPort.findAll()).hasSize(1) },
            { assertThat(repositoryPort.findAll()).hasSize(1) },
            { assertThat(commitPort.findAll()).hasSize(1) },
            { assertThat(branchPort.findAll()).hasSize(1) },
            { assertThat(userPort.findAll()).hasSize(1) },
        )
        run {
            val elem = commitPort.findAll().toList()[0]
            assertThat(elem)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFieldsMatchingRegexes(".*id", ".*repositoryId", ".*project")
                .isEqualTo(cmt)
        }
        run {
            assertThat(
                commitPort.findAll().toList()[0],
            ).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFieldsMatchingRegexes(".*id", ".*repositoryId", ".*project")
                .isEqualTo(repositoryProject.repo?.commits?.toList()[0])
        }
        assertAll(
            "check ids",
            { assertThat(commitPort.findAll().toList()[0].id).isNotNull() },
            { assertThat(commitPort.findAll().toList()[0].repository).isNotNull() },
            { assertThat(commitPort.findAll().toList()[0].repository?.id).isNotNull() },
            { assertThat(commitPort.findAll().toList()[0].repository?.id).isEqualTo(repositoryProject.repo?.id) },
        )
    }
}
