package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.infrastructure.arangodb.InfrastructureConfig
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.RepositoryRepository
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component

/**
 * Seeds the per-session [MappingContext][com.inso_world.binocular.core.persistence.mapper.context.MappingContext]
 * with the default project and repository entities.
 *
 * ### Why this exists
 * ArangoDB entity mappers require the owning Repository (and its Project) to already be
 * present in the MappingContext before converting child entities such as Commits or Developers.
 * This component loads the default entities once at startup and provides a [seed] method that
 * maps them into the current MappingContext session.
 *
 * ### Initialisation order
 * [init] runs as `@PostConstruct`. It looks up the default repository which is created by
 * `V000_AddProject` and `V000_AddRepository` migrations. The `@DependsOn("migrationRunner")`
 * annotation ensures [com.inso_world.binocular.infrastructure.arangodb.migration.MigrationRunner]
 * completes its own `@PostConstruct` (and therefore all migrations) before this seeder
 * initialises. Without it, Spring may initialise the seeder first and the lookup throws.
 *
 * ### Migration note
 * This is an ArangoDB-specific workaround. When migrating to PostgreSQL, delete this file
 * and remove the `seeder.seed()` calls from [MappedArangoDbDao].
 */
@Component
@DependsOn("migrationRunner")
internal class DefaultMappingContextSeeder(
    private val repositoryRepository: RepositoryRepository,
    private val infraConfig: InfrastructureConfig,
    private val projectMapper: ProjectMapper,
    private val repositoryMapper: RepositoryMapper,
) {
    private lateinit var defaultProject: ProjectEntity
    private lateinit var defaultRepository: RepositoryEntity

    @PostConstruct
    fun init() {
        val repo =
            repositoryRepository.findByProject_Name(infraConfig.arangodb.migration.defaultProjectName)
                ?: error(
                    "Default repository not found for project '${infraConfig.arangodb.migration.defaultProjectName}'. " +
                        "Set binocular.arangodb.migration.defaultProjectName to the existing project name in this database. " +
                        "Ensure V000_AddProject migration has run for fresh deployments.",
                )
        defaultProject = repo.project
        defaultRepository = repo
    }

    /**
     * Maps the default project and repository into the current MappingContext session.
     *
     * Call this before any entity-to-domain conversion that requires Repository in context.
     */
    fun seed() {
        projectMapper.toDomain(defaultProject)
        repositoryMapper.toDomain(defaultRepository)
    }
}
