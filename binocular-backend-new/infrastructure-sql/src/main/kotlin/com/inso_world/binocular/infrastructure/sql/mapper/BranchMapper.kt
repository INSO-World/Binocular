package com.inso_world.binocular.infrastructure.sql.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.toEntity
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.stereotype.Component

/**
 * Mapper for Branch domain objects.
 *
 * Converts between Branch domain objects and BranchEntity persistence entities.
 * This is a **simple mapper** - it only handles basic conversion without orchestrating
 * complex relationships.
 *
 * ## Design Principles
 * - **Single Responsibility**: Only converts Branch structure
 * - **Aggregate Boundaries**: Expects Repository and Commit already in MappingContext (cross-aggregate references)
 * - **No Deep Traversal**: Does not map entire commit history or file structures
 *
 * ## Usage
 * This mapper is typically called by infrastructure ports and assemblers. Direct usage
 * is also supported for `refreshDomain` operations after persistence.
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
     * **Precondition**: The referenced Repository must already be mapped and present in MappingContext.
     * The referenced head Commit must also be mapped and present in MappingContext.
     * This enforces aggregate boundaries - Repository and Commit are separate aggregates.
     *
     * **Note**: This method does NOT map child entities or traverse relationships deeply.
     *
     * @param domain The Branch domain object to convert
     * @return The BranchEntity (structure only)
     * @throws IllegalStateException if Repository or head Commit is not in MappingContext
     */
    override fun toEntity(domain: Branch): BranchEntity {
        // Fast-path: if this Branch was already mapped in the current context, return it.
        ctx.findEntity<Branch.Key, Branch, BranchEntity>(domain)?.let {
            return it
        }
        // IMPORTANT: Expect Repository already in context (cross-aggregate reference).
        // Do NOT auto-map Repository here - that's a separate aggregate.
        val owner = ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(domain.repository)
            ?: throw IllegalStateException(
                "RepositoryEntity must be mapped before BranchEntity. " +
                        "Ensure RepositoryEntity is in MappingContext before calling toDomain()."
            )
        // IMPORTANT: Expect Commit already in context (cross-aggregate reference).
        // Do NOT auto-map Commit here - that's a separate aggregate.
        val head = ctx.findEntity<Commit.Key, Commit, CommitEntity>(domain.head)
            ?: throw IllegalStateException(
                "CommitEntity must be mapped before BranchEntity. " +
                        "Ensure CommitEntity is in MappingContext before calling toDomain()."
            )

        val entity = domain.toEntity(owner, head)
        ctx.remember(domain, entity)

        return entity
    }

    /**
     * Converts a BranchEntity to Branch domain object.
     *
     * **Precondition**: The referenced Repository must already be mapped and present in MappingContext.
     * The referenced head Commit must also be mapped and present in MappingContext.
     * This enforces aggregate boundaries - Repository and Commit are separate aggregates.
     *
     * **Note**: This method does NOT map child entities or traverse relationships deeply.
     *
     * @param entity The BranchEntity to convert
     * @return The Branch domain object (structure only)
     * @throws IllegalStateException if Repository or head Commit is not in MappingContext
     */
    override fun toDomain(entity: BranchEntity): @Valid Branch {
        // Fast-path: if this Branch was already mapped in the current context, return it.
        ctx.findDomain<Branch, BranchEntity>(entity)?.let { return it }

        // IMPORTANT: Expect Repository already in context (cross-aggregate reference).
        // Do NOT auto-map Repository here - that's a separate aggregate.
        val owner = ctx.findDomain<Repository, RepositoryEntity>(entity.repository)
            ?: throw IllegalStateException(
                "Repository must be mapped before Branch. " +
                        "Ensure Repository is in MappingContext before calling toDomain()."
            )
        // IMPORTANT: Expect Commit already in context (cross-aggregate reference).
        // Do NOT auto-map Commit here - that's a separate aggregate.
        val head = ctx.findDomain<Commit, CommitEntity>(entity.head)
            ?: throw IllegalStateException(
                "Commit must be mapped before Branch. " +
                        "Ensure Commit is in MappingContext before calling toDomain()."
            )

        val domain = entity.toDomain(owner, head)
        setField(
            domain.javaClass.superclass.superclass.getDeclaredField("iid"),
            domain,
            entity.iid
        )
        ctx.remember(domain, entity)

        return domain
    }

    /**
     * Refreshes a Branch domain object with data from the corresponding entity.
     *
     * This method updates the domain object's ID from the entity after persistence.
     * It does NOT update nested objects - only top-level Branch properties.
     *
     * @param target The Branch domain object to refresh
     * @param entity The BranchEntity with updated data
     * @return The refreshed Branch domain object
     */
    fun refreshDomain(target: Branch, entity: BranchEntity): Branch {
        setField(
            target.javaClass.getDeclaredField("id"),
            target,
            entity.id?.toString()
        )
        return target
    }
}
