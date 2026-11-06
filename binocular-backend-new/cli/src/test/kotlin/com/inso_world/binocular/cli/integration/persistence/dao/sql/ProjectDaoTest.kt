package com.inso_world.binocular.cli.integration.persistence.dao.sql

import com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceNoDataTest
import com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.util.ReflectionUtils.setField

internal class ProjectDaoTest(
    @Autowired val repositoryInfrastructurePort: RepositoryInfrastructurePort,
    @Autowired val projectInfrastructurePort: ProjectInfrastructurePort,
) : BasePersistenceTest() {
    @Nested
    inner class CleanDatabase : BasePersistenceNoDataTest() {
        @BeforeEach
        fun setup() {
        }

        @Test
        fun non_saved_should_return_no_projects() {
            val repos = projectInfrastructurePort.findAll()
            assertThat(repos).isEmpty()
        }

        @Test
        fun `project can exist without repository`() {
            // Given
            val project = Project(name = "Standalone Project").apply { description = "Project without repo" }

            // When
            val savedProject = projectInfrastructurePort.create(project)

            // Then
            assertAll(
//                { assertThat(savedProject.id).isNotNull() },
                { assertThat(savedProject.repo).isNull() },
                { assertThat(projectInfrastructurePort.findAll()).hasSize(1) },
                { assertThat(repositoryInfrastructurePort.findAll()).isEmpty() },
            )
        }

        @Test
        fun `project can exist with repository`() {
            // When
            val savedProject =
                projectInfrastructurePort.create(Project(name = "Project With Repo").apply { description = "Project with repo" })
            savedProject.repo =
                repositoryInfrastructurePort.create(Repository(localPath = "test-repo", project = savedProject))

            // Then
            assertAll(
//                { assertThat(savedProject.id).isNotNull() },
//                { assertThat(savedProject.repo?.id).isNotNull() },
//                { assertThat(savedProject.repo?.project?.id).isEqualTo(savedProject.id) },
                { assertThat(projectInfrastructurePort.findAll()).hasSize(1) },
                { assertThat(repositoryInfrastructurePort.findAll()).hasSize(1) },
            )
        }

        @Test
        @Disabled
        fun `project deletion cascades to repository`() {
            // Given
            val savedProject =
                projectInfrastructurePort.create(
                    Project(
                        name = "To Be Deleted",
                    ).apply { description = "Will be deleted with repo" }
                )
            savedProject.repo =
                repositoryInfrastructurePort.create(
                    Repository(
                        localPath = "cascading-repo",
                        project = savedProject
                    ))
            // updated dependencies, as not managed by JPA
            projectInfrastructurePort.update(savedProject)

            // When
            projectInfrastructurePort.delete(savedProject)

            // Then
            assertAll(
                { assertThat(projectInfrastructurePort.findAll()).isEmpty() },
                { assertThat(repositoryInfrastructurePort.findAll()).isEmpty() },
            )
        }

        @Test
        fun `project with null description can have repository`() {
            // When
            val savedProject = projectInfrastructurePort.create(Project(name = "Null Desc Project"))
            val savedRepo =
                repositoryInfrastructurePort.create(
                    Repository(
                        localPath = "null-desc-repo",
                        project = savedProject
                    ))
            savedProject.repo = savedRepo

            // Then
            assertAll(
                { assertThat(savedProject.description).isNull() },
//                { assertThat(savedRepo.project.id).isEqualTo(savedProject.id) },
                { assertThat(projectInfrastructurePort.findAll()).hasSize(1) },
                { assertThat(repositoryInfrastructurePort.findAll()).hasSize(1) },
            )
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.domain.data.DummyTestData#provideBlankStrings")
        fun `project with invalid name should fail`(invalidName: String) {
            // Given
            val project = Project(name = "invalidName").apply { description = "Empty name" }

            setField(
                Project::class.java.getDeclaredField("name"),
                project,
                invalidName
            )

            // When & Then - This should fail due to validation constraint
            // Note: This test documents expected behavior for invalid data
            assertThrows<jakarta.validation.ConstraintViolationException> {
                projectInfrastructurePort.create(project)
            }
            entityManager.clear()
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.domain.data.DummyTestData#provideAllowedStrings")
        fun `project with allowed names should be handled`(allowedName: String) {
            // When
            val savedProject =
                projectInfrastructurePort.create(Project(name = allowedName).apply { description = "Long name project" })
            val savedRepo =
                repositoryInfrastructurePort.create(
                    Repository(
                        localPath = "long-name-repo",
                        project = savedProject
                    ))
            savedProject.repo = savedRepo

            // Then
            assertAll(
                "check entities",
                { assertThat(savedProject.name).isEqualTo(allowedName) },
                { assertThat(savedRepo.project).isNotNull() },
                { assertThat(savedRepo.project?.id).isEqualTo(savedProject.id) },
            )
            assertAll(
                "check database numbers",
                { assertThat(projectInfrastructurePort.findAll()).hasSize(1) },
                { assertThat(repositoryInfrastructurePort.findAll()).hasSize(1) },
            )
        }

        @Test
        fun `duplicate project names should fail`() {

            assertDoesNotThrow {
                projectInfrastructurePort.create(
                    Project(
                        name = "Duplicate Name",
                    ).apply { description = "First project" },
                )
            }

            assertThrows<IllegalArgumentException> {
                projectInfrastructurePort.create(
                    Project(
                        name = "Duplicate Name",
                    ).apply { description = "Second project" },
                )
            }
            assertThat(projectInfrastructurePort.findAll()).hasSize(1)

            entityManager.clear()
        }
    }
}
