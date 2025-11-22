package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import com.inso_world.binocular.model.Project
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Mapper for Project aggregate root.
 *
 * Converts between Project domain objects and ProjectEntity persistence entities for ArangoDB.
 * This is a **simple mapper** - it only handles basic conversion without orchestrating
 * child entity mapping. Use assemblers for complete aggregate assembly if needed.
 *
 * ## Design Principles
 * - **Single Responsibility**: Only converts Project structure (not children)
 * - **No Orchestration**: Child entities (Repository) are mapped by assemblers
 * - **Context Management**: Uses MappingContext to prevent duplicate mappings
 *
 * ## Usage
 * This mapper is typically called by infrastructure ports and assemblers. Direct usage
 * is also supported for `refreshDomain` operations after persistence.
 */
@Component
internal class ProjectMapper : EntityMapper<Project, ProjectEntity> {
    @Autowired
    private lateinit var ctx: MappingContext

    companion object {
        private val logger by logger()
    }

    /**
     * Converts a Project domain object to ProjectEntity.
     *
     * **Note**: This method does NOT map child entities (Repository). Use assemblers
     * for complete aggregate assembly including children.
     *
     * @param domain The Project domain object to convert
     * @return The ProjectEntity (structure only, without children)
     */
    override fun toEntity(domain: Project): ProjectEntity {
        // Fast-path: if this Project was already mapped in the current context, return it.
        ctx.findEntity<Project.Key, Project, ProjectEntity>(domain)?.let { return it }

        val entity = ProjectEntity(
            id = domain.id,
            name = domain.name,
            description = domain.description
        )

        ctx.remember(domain, entity)
        return entity
    }

    /**
     * Converts a ProjectEntity to Project domain object.
     *
     * **Note**: This method does NOT map child entities (Repository). Use assemblers
     * for complete aggregate assembly including children.
     *
     * @param entity The ProjectEntity to convert
     * @return The Project domain object (structure only, without children)
     */
    override fun toDomain(entity: ProjectEntity): Project {
        // Fast-path: Check if already mapped
        ctx.findDomain<Project, ProjectEntity>(entity)?.let { return it }

        val domain = Project(
            name = entity.name
        ).apply {
            id = entity.id
            description = entity.description
        }

        ctx.remember(domain, entity)
        return domain
    }

    /**
     * Refreshes a Project domain object with data from the corresponding entity.
     *
     * This method updates the domain object's ID and description from the entity after persistence.
     * It does NOT update nested objects - only top-level Project properties.
     *
     * @param target The Project domain object to refresh
     * @param entity The ProjectEntity with updated data
     * @return The refreshed Project domain object
     */
    fun refreshDomain(target: Project, entity: ProjectEntity): Project {
        target.id = entity.id
        target.description = entity.description
        return target
    }
}
