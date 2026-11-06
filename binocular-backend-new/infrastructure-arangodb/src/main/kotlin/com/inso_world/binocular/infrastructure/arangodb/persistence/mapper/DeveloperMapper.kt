package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.DeveloperEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.toEntity
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Repository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.stereotype.Component

/**
 * Mapper for Developer domain objects.
 *
 * Converts between Developer domain objects and DeveloperEntity persistence entities for ArangoDB.
 * This mapper intentionally keeps the conversion shallow; it does not traverse commit graphs.
 *
 * ## Design Principles
 * - **Single Responsibility**: Only converts Developer structure
 * - **No Deep Traversal**: Does not automatically map commit relationships
 * - **Context-Dependent**: Requires Repository already in MappingContext
 *
 * ## Usage
 * This mapper is typically called by infrastructure ports and assemblers. The `toDomain`
 * method requires the Repository to already exist in MappingContext to preserve the
 * repository reference.
 */
@Component
internal class DeveloperMapper : EntityMapper<Developer, DeveloperEntity> {
    @Autowired
    private lateinit var ctx: MappingContext

    companion object {
        private val logger by logger()
    }

    /**
     * Converts a Developer domain object to DeveloperEntity.
     *
     * **Precondition**: The referenced Repository must already be mapped and present in MappingContext.
     * This enforces aggregate boundary - Repository must be handled first.
     *
     * **Note**: This method does NOT map commit relationships or other deep structures.
     * Use assemblers for complete graph assembly.
     *
     * @param domain The Developer domain object to convert
     * @return The DeveloperEntity (structure only, without relationships)
     * @throws IllegalStateException if Repository is not in MappingContext
     */
    override fun toEntity(domain: Developer): DeveloperEntity {
        ctx.findEntity<Developer.Key, Developer, DeveloperEntity>(domain)?.let { return it }

        val owner = ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(domain.repository)
            ?: throw IllegalStateException(
                "RepositoryEntity must be mapped before DeveloperEntity. " +
                        "Ensure RepositoryEntity is in MappingContext before calling toEntity()."
            )

        val entity = domain.toEntity(owner)
        ctx.remember(domain, entity)
        return entity
    }

    /**
     * Converts a DeveloperEntity to Developer domain object.
     *
     * **Precondition**: The referenced Repository must already be mapped and present in MappingContext.
     * This enforces aggregate boundary - Repository must be handled first.
     *
     * **Note**: This method does NOT map commit relationships or other deep structures.
     * Use assemblers for complete graph assembly.
     *
     * @param entity The DeveloperEntity to convert
     * @return The Developer domain object (structure only, without relationships)
     * @throws IllegalStateException if Repository is not in MappingContext
     */
    override fun toDomain(entity: DeveloperEntity): Developer {
        ctx.findDomain<Developer, DeveloperEntity>(entity)?.let { return it }

        val owner = ctx.findDomain<Repository, RepositoryEntity>(entity.repository)
            ?: throw IllegalStateException(
                "Repository must be mapped before Developer. " +
                        "Ensure Repository is in MappingContext before calling toDomain()."
            )

        val domain = entity.toDomain(owner)
        setField(
            domain.javaClass.superclass.superclass.getDeclaredField("iid"),
            domain,
            entity.iid
        )
        ctx.remember(domain, entity)
        return domain
    }

    /**
     * Refreshes a Developer domain object with data from the corresponding entity.
     *
     * This method updates the domain object's ID from the entity after persistence.
     * It does NOT update nested objects - only top-level Developer properties.
     *
     * @param target The Developer domain object to refresh
     * @param entity The DeveloperEntity with updated data
     * @return The refreshed Developer domain object
     */
    fun refreshDomain(target: Developer, entity: DeveloperEntity): Developer {
        if (target.id.equals(entity.id)) {
            return target
        }
        setField(
            target.javaClass.getDeclaredField("id"),
            target,
            entity.id
        )
        return target
    }
}