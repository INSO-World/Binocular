package com.inso_world.binocular.cli.integration.persistence.dao.sql

import com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceNoDataTest
import com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest
import com.inso_world.binocular.core.service.ProjectInfrastructurePort
import com.inso_world.binocular.core.service.RepositoryInfrastructurePort
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
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

internal class ProjectDaoTest(
    @Autowired val repositoryDao: RepositoryInfrastructurePort,
    @Autowired val projectDao: ProjectInfrastructurePort,
) : BasePersistenceTest() {
    @Nested
    inner class CleanDatabase : BasePersistenceNoDataTest() {
        @BeforeEach
        fun setup() {
        }

        @Test
        fun non_saved_should_return_no_projects() {
            val repos = projectDao.findAll()
            assertThat(repos).isEmpty()
        }

        @Test
        fun `project can exist without repository`() {
            // Given
            val project = Project(name = "Standalone Project", description = "Project without repo")

            // When
            val savedProject = projectDao.save(project)

            // Then
            assertAll(
//                { assertThat(savedProject.id).isNotNull() },
                { assertThat(savedProject.repo).isNull() },
                { assertThat(projectDao.findAll()).hasSize(1) },
                { assertThat(repositoryDao.findAll()).isEmpty() },
            )
        }

        @Test
        fun `project can exist with repository`() {
            // When
            val savedProject = projectDao.save(Project(name = "Project With Repo", description = "Project with repo"))
            savedProject.repo =
                repositoryDao.save(Repository(id = null, name = "test-repo", projectId = savedProject.id))

            // Then
            assertAll(
//                { assertThat(savedProject.id).isNotNull() },
//                { assertThat(savedProject.repo?.id).isNotNull() },
//                { assertThat(savedProject.repo?.project?.id).isEqualTo(savedProject.id) },
                { assertThat(projectDao.findAll()).hasSize(1) },
                { assertThat(repositoryDao.findAll()).hasSize(1) },
            )
        }

        @Test
        fun `project deletion cascades to repository`() {
            // Given
            val savedProject =
                projectDao.save(Project(name = "To Be Deleted", description = "Will be deleted with repo"))
            savedProject.repo =
                repositoryDao.save(Repository(id = null, name = "cascading-repo", projectId = savedProject.id))
            // updated dependencies, as not managed by JPA
            projectDao.update(savedProject)

            // When
            projectDao.delete(savedProject)

            // Then
            assertAll(
                { assertThat(projectDao.findAll()).isEmpty() },
                { assertThat(repositoryDao.findAll()).isEmpty() },
            )
        }

        @Test
        fun `project with null description can have repository`() {
            // When
            val savedProject = projectDao.save(Project(name = "Null Desc Project"))
            val savedRepo =
                repositoryDao.save(Repository(id = null, name = "null-desc-repo", projectId = savedProject.id))
            savedProject.repo = savedRepo

            // Then
            assertAll(
                { assertThat(savedProject.description).isNull() },
//                { assertThat(savedRepo.project.id).isEqualTo(savedProject.id) },
                { assertThat(projectDao.findAll()).hasSize(1) },
                { assertThat(repositoryDao.findAll()).hasSize(1) },
            )
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest#provideBlankStrings")
        fun `project with invalid name should fail`(invalidName: String) {
            // Given
            val project = Project(name = invalidName, description = "Empty name")

            // When & Then - This should fail due to validation constraint
            // Note: This test documents expected behavior for invalid data
            assertThrows<jakarta.validation.ConstraintViolationException> {
                projectDao.save(project)
            }
            entityManager.clear()
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest#provideAllowedStrings")
        fun `project with allowed names should be handled`(allowedName: String) {
            // When
            val savedProject = projectDao.save(Project(name = allowedName, description = "Long name project"))
            val savedRepo =
                repositoryDao.save(Repository(id = null, name = "long-name-repo", projectId = savedProject.id))
            savedProject.repo = savedRepo

            // Then
            assertAll(
                { assertThat(savedProject.name).isEqualTo(allowedName) },
                { assertThat(savedRepo.projectId).isEqualTo(savedProject.id) },
                { assertThat(projectDao.findAll()).hasSize(1) },
                { assertThat(repositoryDao.findAll()).hasSize(1) },
            )
        }

        @Test
        fun `duplicate project names should fail`() {
            assertAll(
                {
                    assertDoesNotThrow {
                        projectDao.save(
                            Project(
                                name = "Duplicate Name",
                                description = "First project",
                            ),
                        )
                    }
                },
                {
                    assertThrows<org.hibernate.exception.ConstraintViolationException> {
                        projectDao.save(
                            Project(
                                name = "Duplicate Name",
                                description = "Second project",
                            ),
                        )
                    }
                },
            )
            entityManager.clear()
        }
    }
}
