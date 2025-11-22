package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Mapper for Repository aggregate root.
 *
 * Converts between Repository domain objects and RepositoryEntity persistence entities for ArangoDB.
 * This is a **simple mapper** - it only handles basic conversion without orchestrating
 * child entity mapping. Use assemblers for complete aggregate assembly.
 *
 * ## Design Principles
 * - **Single Responsibility**: Only converts Repository structure (not children)
 * - **Aggregate Boundaries**: Expects Project already in MappingContext (cross-aggregate reference)
 * - **No Orchestration**: Child entities (Commits, Branches) are mapped by assembler
 *
 * ## Usage
 * This mapper is typically called by infrastructure ports and assemblers. Direct usage
 * is also supported for `refreshDomain` operations after persistence.
 */
@Component
internal class RepositoryMapper : EntityMapper<Repository, RepositoryEntity> {
    @Autowired
    private lateinit var ctx: MappingContext

    companion object {
        private val logger by logger()
    }

    /**
     * Converts a Repository domain object to RepositoryEntity.
     *
     * **Precondition**: The referenced Project must already be mapped and present in MappingContext.
     * This enforces aggregate boundary - Project is a separate aggregate that must be handled first.
     *
     * **Note**: This method does NOT map child entities (Commits, Branches). Use assemblers
     * for complete aggregate assembly including children.
     *
     * @param domain The Repository domain object to convert
     * @return The RepositoryEntity (structure only, without children)
     * @throws IllegalStateException if Project is not in MappingContext
     */
    override fun toEntity(domain: Repository): RepositoryEntity {
        // Fast-path: if this Repository was already mapped in the current context, return it.
        ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(domain)?.let { return it }

        // IMPORTANT: Expect Project already in context (cross-aggregate reference).
        // Do NOT auto-map Project here - that's a separate aggregate.
        val projectEntity = ctx.findEntity<Project.Key, Project, ProjectEntity>(domain.project)
            ?: throw IllegalStateException(
                "ProjectEntity must be mapped before RepositoryEntity. " +
                        "Ensure ProjectEntity is in MappingContext before calling toEntity()."
            )

        val entity = RepositoryEntity(
            id = domain.id,
            name = domain.localPath.trim()
        ).apply {
            project = projectEntity
        }

        ctx.remember(domain, entity)
        return entity
    }

    /**
     * Converts a RepositoryEntity to Repository domain object.
     *
     * **Precondition**: The referenced Project must already be mapped and present in MappingContext.
     * This enforces aggregate boundary - Project is a separate aggregate that must be handled first.
     *
     * **Note**: This method does NOT map child entities (Commits, Branches). Use assemblers
     * for complete aggregate assembly including children.
     *
     * @param entity The RepositoryEntity to convert
     * @return The Repository domain object (structure only, without children)
     * @throws IllegalStateException if Project is not in MappingContext
     */
    override fun toDomain(entity: RepositoryEntity): Repository {
        // Fast-path: Check if already mapped
        ctx.findDomain<Repository, RepositoryEntity>(entity)?.let { return it }

        // IMPORTANT: Expect Project already in context (cross-aggregate reference).
        // Do NOT auto-map Project here - that's a separate aggregate.
        val project = ctx.findDomain<Project, ProjectEntity>(entity.project)
            ?: throw IllegalStateException(
                "Project must be mapped before Repository. " +
                        "Ensure Project is in MappingContext before calling toDomain()."
            )

        val domain = Repository(
            localPath = entity.name.trim(),
            project = project
        ).apply {
            id = entity.id
        }

        ctx.remember(domain, entity)
        return domain
    }

    /**
     * Refreshes a Repository domain object with data from the corresponding entity.
     *
     * This method updates the domain object's ID from the entity after persistence.
     * It does NOT update nested objects - only top-level Repository properties.
     *
     * @param target The Repository domain object to refresh
     * @param entity The RepositoryEntity with updated data
     * @return The refreshed Repository domain object
     */
    fun refreshDomain(target: Repository, entity: RepositoryEntity): Repository {
        target.id = entity.id
        return target
    }
}
