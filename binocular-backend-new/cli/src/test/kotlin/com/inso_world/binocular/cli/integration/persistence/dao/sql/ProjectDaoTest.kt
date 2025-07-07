package com.inso_world.binocular.cli.integration.persistence.dao.sql

import com.inso_world.binocular.cli.entity.Project
import com.inso_world.binocular.cli.entity.Repository
import com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceNoDataTest
import com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest
import com.inso_world.binocular.cli.persistence.dao.sql.interfaces.IProjectDao
import com.inso_world.binocular.cli.persistence.dao.sql.interfaces.IRepositoryDao
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
    @Autowired val repositoryDao: IRepositoryDao,
    @Autowired val projectDao: IProjectDao,
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
            val savedProject = projectDao.create(project)

            // Then
            assertAll(
                { assertThat(savedProject.id).isNotNull() },
                { assertThat(savedProject.repo).isNull() },
                { assertThat(projectDao.findAll()).hasSize(1) },
                { assertThat(repositoryDao.findAll()).isEmpty() },
            )
        }

        @Test
        fun `project can exist with repository`() {
            // When
            val savedProject = projectDao.create(Project(name = "Project With Repo", description = "Project with repo"))
            savedProject.repo = repositoryDao.create(Repository(name = "test-repo", project = savedProject))

            // Then
            assertAll(
                { assertThat(savedProject.id).isNotNull() },
                { assertThat(savedProject.repo?.id).isNotNull() },
                { assertThat(savedProject.repo?.project?.id).isEqualTo(savedProject.id) },
                { assertThat(projectDao.findAll()).hasSize(1) },
                { assertThat(repositoryDao.findAll()).hasSize(1) },
            )
        }

        @Test
        fun `project deletion cascades to repository`() {
            // Given
            val savedProject =
                projectDao.create(Project(name = "To Be Deleted", description = "Will be deleted with repo"))
            val repository = Repository(name = "cascading-repo", project = savedProject)
            savedProject.repo = repositoryDao.create(repository)

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
            val savedProject = projectDao.create(Project(name = "Null Desc Project", description = null))
            val savedRepo = repositoryDao.create(Repository(name = "null-desc-repo", project = savedProject))
            savedProject.repo = savedRepo

            // Then
            assertAll(
                { assertThat(savedProject.description).isNull() },
                { assertThat(savedRepo.project.id).isEqualTo(savedProject.id) },
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
                projectDao.create(project)
            }
            entityManager.clear()
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest#provideAllowedStrings")
        fun `project with allowed names should be handled`(allowedName: String) {
            // When
            val savedProject = projectDao.create(Project(name = allowedName, description = "Long name project"))
            val savedRepo = repositoryDao.create(Repository(name = "long-name-repo", project = savedProject))
            savedProject.repo = savedRepo

            // Then
            assertAll(
                { assertThat(savedProject.name).isEqualTo(allowedName) },
                { assertThat(savedRepo.project.id).isEqualTo(savedProject.id) },
                { assertThat(projectDao.findAll()).hasSize(1) },
                { assertThat(repositoryDao.findAll()).hasSize(1) },
            )
        }

        @Test
        fun `duplicate project names should fail`() {
            assertAll(
                {
                    assertDoesNotThrow {
                        projectDao.create(
                            Project(
                                name = "Duplicate Name",
                                description = "First project",
                            ),
                        )
                    }
                },
                {
                    assertThrows<org.hibernate.exception.ConstraintViolationException> {
                        projectDao.create(
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
