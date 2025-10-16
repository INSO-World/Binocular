package com.inso_world.binocular.infrastructure.test.repository

import com.inso_world.binocular.core.integration.base.BaseFixturesIntegrationTest
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BasePortNoDataTest
import com.inso_world.binocular.infrastructure.test.repository.base.BasePortWithDataTest
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import jakarta.validation.ConstraintViolationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException

internal class RepositoryInfrastructurePortTest() : BasePortNoDataTest() {
    @all:Autowired
    private lateinit var repositoryPort: RepositoryInfrastructurePort

    @all:Autowired
    private lateinit var projectPort: ProjectInfrastructurePort

    @Nested
    inner class CleanDatabase {
        @BeforeEach
        fun setup() {
        }

        @Test
        fun `non saved, should return no repositories`() {
            val repos = repositoryPort.findAll()
            assertThat(repos).isEmpty()
        }

        @Test
        fun `repository deletion leaves project intact`() {
            // Given
            val savedProject =
                projectPort.create(Project(name = "Surviving Project", description = "Will survive repo deletion"))
            val savedRepo =
                repositoryPort.create(Repository(id = null, localPath = "to-be-deleted-repo", project = savedProject))
            // updated dependencies, as not managed by JPA
            savedProject.repo = savedRepo
            projectPort.update(savedProject)

            // When
            repositoryPort.delete(savedRepo)

            // Then
            assertAll(
                { assertThat(repositoryPort.findAll()).isEmpty() },
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(projectPort.findById(savedProject.id!!)).isNotNull() },
                { assertThat(projectPort.findById(savedProject.id!!)?.repo).isNull() },
            )
        }

        // Negative Tests - Invalid scenarios
        @Test
        fun `repository cannot exist without project`() {
            // Given
            val savedProject = projectPort.create(Project(name = "Temporary Project", description = "Will be deleted"))
            val repository = Repository(id = null, localPath = "orphaned-repo", project = savedProject)
            savedProject.repo = repositoryPort.create(repository)
            // updated dependencies, as not managed by JPA
            projectPort.update(savedProject)

            assertAll(
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
            )

            projectPort.delete(savedProject)

            // Verify both are deleted due to cascade
            assertAll(
                { assertThat(projectPort.findAll()).isEmpty() },
                { assertThat(repositoryPort.findAll()).isEmpty() },
            )
        }

        @Test
        fun `multiple repositories cannot reference same project`() {
            // Given
            val savedProject =
                projectPort.create(Project(name = "Shared Project", description = "Should only have one repo"))

            // When - First repository should be created successfully
            val firstRepo =
                repositoryPort.create(Repository(id = null, localPath = "first-repo", project = savedProject))

//            entityManager.flush()
//            entityManager.clear()
            // Then - Verify first repository was created
            assertAll(
                { assertThat(firstRepo).isNotNull() },
//                { assertThat(firstRepo?.id).isNotNull() },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
            )

            // When - Second repository with same project should fail
            val ex = assertThrows<IllegalArgumentException> {
                repositoryPort.create(Repository(id = null, localPath = "second-repo", project = savedProject))
            }

            // Then - Verify only one repository still exists
            assertThat(repositoryPort.findAll()).hasSize(1)
        }

        // Mutation Tests - Testing edge cases and boundary conditions

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.core.data.DummyTestData#provideBlankStrings")
        fun `repository with invalid name should fail`(invalidName: String) {
            // When
            val savedProject = projectPort.create(Project(name = "Valid Project", description = "Valid project"))

            // Then - This should fail due to validation constraint
            assertThrows<ConstraintViolationException> {
                repositoryPort.create(Repository(id = null, localPath = invalidName, project = savedProject))
            }
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.core.data.DummyTestData#provideAllowedStrings")
        fun `repository with allowed names should be handled`(allowedName: String) {
            // When
            val savedProject = projectPort.create(Project(name = "Valid Project", description = "Valid project"))
            val savedRepo =
                repositoryPort.create(Repository(id = null, localPath = allowedName, project = savedProject))
            savedProject.repo = savedRepo
            projectPort.update(savedProject)

            // Then
            assertAll(
                { assertThat(savedRepo.localPath).isEqualTo(allowedName) },
                { assertThat(savedRepo.project).isNotNull() },
                { assertThat(savedRepo.project?.id).isEqualTo(savedProject.id) },
                { assertThat(projectPort.findAll()).hasSize(1) },
                { assertThat(repositoryPort.findAll()).hasSize(1) },
            )
        }

        @Test
        fun `duplicate repository names should fail`() {
            // When
            val savedProject1 = projectPort.create(Project(name = "Project 1", description = "First project"))
            val savedProject2 = projectPort.create(Project(name = "Project 2", description = "Second project"))

            assertDoesNotThrow {
                repositoryPort.create(
                    Repository(
                        id = null,
                        localPath = "Duplicate Repo",
                        project = savedProject1,
                    ),
                )
            }
//            entityManager.flush()
//            entityManager.clear()

            // Then - This should fail due to unique constraint
            val ex = assertThrows<DataIntegrityViolationException> {
                repositoryPort.create(Repository(id = null, localPath = "Duplicate Repo", project = savedProject2))
            }
            assertThat(repositoryPort.findAll()).hasSize(1)
//            entityManager.clear()
        }
    }

    @Nested
    inner class FilledDatabase : BasePortWithDataTest() {

        @BeforeEach
        fun setUp() {
            requireNotNull(
                prepare(
                    "${BaseFixturesIntegrationTest.Companion.FIXTURES_PATH}/${BaseFixturesIntegrationTest.Companion.SIMPLE_REPO}",
                    projectName = BaseFixturesIntegrationTest.Companion.SIMPLE_PROJECT_NAME,
                    branch = Branch(name = "master")
                ).repo
            ) {
                "${BaseFixturesIntegrationTest.Companion.FIXTURES_PATH}/${BaseFixturesIntegrationTest.Companion.SIMPLE_REPO} repository cannot be null"
            }
            requireNotNull(
                prepare(
                    "${BaseFixturesIntegrationTest.Companion.FIXTURES_PATH}/${BaseFixturesIntegrationTest.Companion.OCTO_REPO}",
                    projectName = BaseFixturesIntegrationTest.Companion.OCTO_PROJECT_NAME,
                    branch = Branch(name = "master")
                ).repo
            ) {
                "${BaseFixturesIntegrationTest.Companion.FIXTURES_PATH}/${BaseFixturesIntegrationTest.Companion.OCTO_REPO} repository cannot be null"
            }
        }

        @Test
        fun `all saved, should return all repositories`() {
            val repos = repositoryPort.findAll()
            assertAll(
                { assertThat(repos).isNotEmpty() },
                { assertThat(repos).hasSize(2) },
            )
        }

        @Test
        fun `all saved, should return all projects`() {
            val projects = projectPort.findAll()
            assertAll(
                { assertThat(projects).isNotEmpty() },
                { assertThat(projects).hasSize(2) },
            )
        }
    }
}
