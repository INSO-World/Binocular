package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.BranchEntity
import com.inso_world.binocular.model.Branch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Mapper for Branch domain objects.
 *
 * Converts between Branch domain objects and BranchEntity persistence entities for ArangoDB.
 * This is a **simple mapper** - it only handles basic conversion without orchestrating
 * complex relationships.
 *
 * ## Design Principles
 * - **Single Responsibility**: Only converts Branch structure
 * - **No Deep Traversal**: Does not map entire commit history or file structures
 * - **Context-Dependent toDomain**: Requires Branch already in context (preserves repository and head commit references)
 *
 * ## Usage
 * This mapper is typically called by infrastructure ports and assemblers. The `toDomain`
 * method requires the Branch to already exist in MappingContext to preserve invariants
 * and references that cannot be reconstructed from the entity alone.
 */
@Component
internal class BranchMapper : EntityMapper<Branch, BranchEntity> {

    @Autowired
    private lateinit var ctx: MappingContext

    companion object {
        private val logger by logger()
    }

    /**
     * Converts a Branch domain object to BranchEntity.
     *
     * Maps the branch name, active status, file rename tracking setting, and latest commit SHA.
     *
     * **Note**: This method does NOT map child entities or traverse relationships deeply.
     *
     * @param domain The Branch domain object to convert
     * @return The BranchEntity (structure only)
     */
    override fun toEntity(domain: Branch): BranchEntity {
        // Fast-path: if this Branch was already mapped in the current context, return it.
        ctx.findEntity<Branch.Key, Branch, BranchEntity>(domain)?.let { return it }

        val entity =
            BranchEntity(
                id = domain.id,
                branch = domain.name,
                active = domain.active,
                tracksFileRenames = domain.tracksFileRenames,
                latestCommit = domain.latestCommit,
            )

        ctx.remember(domain, entity)
        return entity
    }

    /**
     * Converts a BranchEntity to Branch domain object.
     *
     * **Precondition**: The Branch must already exist in MappingContext. This is necessary because
     * the BranchEntity does not store the repository or head commit references, which are required by
     * the Branch domain model. These references must be preserved from the original domain object.
     *
     * **Note**: This method does NOT map child entities or traverse relationships deeply.
     *
     * @param entity The BranchEntity to convert
     * @return The Branch domain object from MappingContext
     * @throws IllegalStateException if Branch is not already in MappingContext
     */
    override fun toDomain(entity: BranchEntity): Branch {
        // Fast-path: Check if already mapped - required to preserve repository and head commit references
        ctx.findDomain<Branch, BranchEntity>(entity)?.let { return it }

        throw IllegalStateException(
            "Branch mapping requires an existing Branch in MappingContext to preserve invariants. " +
                    "Ensure Branch is created with repository and head commit before calling toDomain()."
        )
    }
}
