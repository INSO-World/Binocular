package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.infrastructure.arangodb.InfrastructureConfig
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.ProjectMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.RepositoryMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.ProjectRepository
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
 * This component provides a [seed] method that fetches the default entities and maps them
 * into the current MappingContext session.
 *
 * ### Initialisation order
 * [init] runs as `@PostConstruct` for fail-fast validation: it ensures the default project
 * (created by `V000_AddProject` and `V000_AddRepository` migrations) exists at startup.
 * The `@DependsOn("migrationRunner")` annotation ensures
 * [com.inso_world.binocular.infrastructure.arangodb.migration.MigrationRunner] completes
 * its own `@PostConstruct` (and therefore all migrations) before this seeder initialises.
 *
 * ### Why seed() re-fetches
 * Caching the entity proxies from `@PostConstruct` would cause stale-proxy failures after
 * database teardown/re-seed cycles (e.g., integration tests). Re-fetching on every [seed]
 * call is a cheap indexed lookup and guarantees fresh, valid references.
 *
 * ### Migration note
 * This is an ArangoDB-specific workaround. When migrating to PostgreSQL, delete this file
 * and remove the `seeder.seed()` calls from [MappedArangoDbDao].
 */
@Component
@DependsOn("migrationRunner")
internal class DefaultMappingContextSeeder(
    private val projectRepository: ProjectRepository,
    private val repositoryRepository: RepositoryRepository,
    private val infraConfig: InfrastructureConfig,
    private val projectMapper: ProjectMapper,
    private val repositoryMapper: RepositoryMapper,
) {
    @PostConstruct
    fun init() {
        repositoryRepository.findByProject_Name(infraConfig.arangodb.migration.defaultProjectName)
            ?: error(
                "Default repository not found for project '${infraConfig.arangodb.migration.defaultProjectName}'. " +
                        "Set binocular.arangodb.migration.defaultProjectName to the existing project name in this database. " +
                        "Ensure V000_AddProject migration has run for fresh deployments.",
            )
    }

    /**
     * Maps the default project and repository into the current MappingContext session.
     *
     * Re-fetches both [ProjectEntity] and [RepositoryEntity] from the database on every call
     * to guarantee fresh entity references. The project is fetched directly via [ProjectRepository]
     * rather than through [RepositoryEntity.project] (a `@Ref(lazy=true)` proxy) because the
     * proxy holds a stale document id after test teardown/re-seed cycles.
     * Call this before any entity-to-domain conversion that requires Repository in context.
     */
    fun seed() {
        val project =
            projectRepository.findByName(infraConfig.arangodb.migration.defaultProjectName)
                ?: return
        val repo =
            repositoryRepository.findByProject_Name(infraConfig.arangodb.migration.defaultProjectName)
                ?: return
        projectMapper.toDomain(project)
        repositoryMapper.toDomain(repo)
    }
}
