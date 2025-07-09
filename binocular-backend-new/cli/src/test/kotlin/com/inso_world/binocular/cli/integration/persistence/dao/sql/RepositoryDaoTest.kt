package com.inso_world.binocular.cli.integration.persistence.dao.sql

import com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceNoDataTest
import com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceWithDataTest
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
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

@Transactional
internal class RepositoryDaoTest(
    @Autowired val repositoryDao: RepositoryInfrastructurePort,
    @Autowired val projectDao: ProjectInfrastructurePort,
) : BasePersistenceNoDataTest() {
    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    @Nested
    inner class CleanDatabase {
        @BeforeEach
        fun setup() {
        }

        @Test
        fun non_saved_should_return_no_repositories() {
            val repos = repositoryDao.findAll()
            assertThat(repos).isEmpty()
        }

        @Test
        fun `repository deletion leaves project intact`() {
            // Given
            val savedProject =
                projectDao.save(Project(name = "Surviving Project", description = "Will survive repo deletion"))
            val repository = Repository(id = null, name = "to-be-deleted-repo", projectId = savedProject.id)
            val savedRepo = repositoryDao.save(repository)

            // When
            repositoryDao.delete(savedRepo)

            // Then
            assertAll(
                { assertThat(repositoryDao.findAll()).isEmpty() },
                { assertThat(projectDao.findAll()).hasSize(1) },
//                { assertThat(projectDao.findById(savedProject.id!!)).isNotNull() },
//                { assertThat(projectDao.findById(savedProject.id!!)?.repo).isNull() },
            )
        }

        // Negative Tests - Invalid scenarios
        @Test
        fun `repository cannot exist without project`() {
            // Given
            val savedProject = projectDao.save(Project(name = "Temporary Project", description = "Will be deleted"))
            val repository = Repository(id = null, name = "orphaned-repo", projectId = savedProject.id)
            savedProject.repo = repositoryDao.save(repository)

            projectDao.delete(savedProject)

            // Verify both are deleted due to cascade
            assertAll(
                { assertThat(projectDao.findAll()).isEmpty() },
                { assertThat(repositoryDao.findAll()).isEmpty() },
            )
        }

        @Test
        fun `multiple repositories cannot reference same project`() {
            // Given
            val savedProject =
                projectDao.save(Project(name = "Shared Project", description = "Should only have one repo"))

            // When - First repository should be created successfully
            val firstRepo =
                transactionTemplate.execute {
                    val repo = repositoryDao.save(Repository(id = null, name = "first-repo", projectId = savedProject.id))
                    repo
                }

            // Then - Verify first repository was created
            assertAll(
                { assertThat(firstRepo).isNotNull() },
//                { assertThat(firstRepo?.id).isNotNull() },
                { assertThat(repositoryDao.findAll()).hasSize(1) },
            )

            // When - Second repository with same project should fail
            assertThrows<org.hibernate.exception.ConstraintViolationException> {
                repositoryDao.save(Repository(id = null, name = "second-repo", projectId = savedProject.id))
            }
            entityManager.clear()

            // Then - Verify only one repository still exists
            assertThat(repositoryDao.findAll()).hasSize(1)
        }

        // Mutation Tests - Testing edge cases and boundary conditions

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest#provideBlankStrings")
        fun `repository with invalid name should fail`(invalidName: String) {
            // When
            val savedProject = projectDao.save(Project(name = "Valid Project", description = "Valid project"))

            // Then - This should fail due to validation constraint
            assertThrows<jakarta.validation.ConstraintViolationException> {
                repositoryDao.save(Repository(id = null, name = invalidName, projectId = savedProject.id))
            }
            entityManager.clear()
        }

        @ParameterizedTest
        @MethodSource("com.inso_world.binocular.cli.integration.persistence.dao.sql.base.BasePersistenceTest#provideAllowedStrings")
        fun `repository with allowed names should be handled`(allowedName: String) {
            // When
            val savedProject = projectDao.save(Project(name = "Valid Project", description = "Valid project"))
            val savedRepo = repositoryDao.save(Repository(id = null, name = allowedName, projectId = savedProject.id))
            savedProject.repo = savedRepo

            // Then
            assertAll(
                { assertThat(savedRepo.name).isEqualTo(allowedName) },
//                { assertThat(savedRepo.project.id).isEqualTo(savedProject.id) },
                { assertThat(projectDao.findAll()).hasSize(1) },
                { assertThat(repositoryDao.findAll()).hasSize(1) },
            )
        }

        @Test
        fun `duplicate repository names should fail`() {
            // When
            val savedProject1 = projectDao.save(Project(name = "Project 1", description = "First project"))
            val savedProject2 = projectDao.save(Project(name = "Project 2", description = "Second project"))
//            assertAll(
//                {
            assertDoesNotThrow {
                repositoryDao.save(
                    Repository(
                        id = null,
                        name = "Duplicate Repo",
                        projectId = savedProject1.id,
                    ),
                )
            }
//                },
            // Then - This should fail due to unique constraint
//                {
            val ex =
                assertThrows<org.hibernate.exception.ConstraintViolationException> {
                    repositoryDao.save(Repository(id = null, name = "Duplicate Repo", projectId = savedProject2.id))
                }
//                },
//            )
            entityManager.clear()
        }
    }

    @Nested
    inner class FilledDatabase : BasePersistenceWithDataTest() {
        @Test
        fun `all saved, should return all repositories`() {
            val repos = repositoryDao.findAll()
            assertAll(
                { assertThat(repos).isNotEmpty() },
                { assertThat(repos).hasSize(2) },
            )
        }

        @Test
        fun `all saved, should return all projects`() {
            val projects = projectDao.findAll()
            assertAll(
                { assertThat(projects).isNotEmpty() },
                { assertThat(projects).hasSize(2) },
            )
        }
    }
}
