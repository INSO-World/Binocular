package com.inso_world.binocular.infrastructure.sql.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.toEntity
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.stereotype.Component

/**
 * Mapper for Commit domain objects.
 *
 * Converts between Commit domain objects and CommitEntity persistence entities.
 * This is a **simple mapper** - it only handles basic conversion without orchestrating
 * complex relationships like full commit history graphs.
 *
 * ## Design Principles
 * - **Single Responsibility**: Only converts Commit structure
 * - **Aggregate Boundaries**: Expects Repository already in MappingContext (cross-aggregate reference)
 * - **No Deep Traversal**: Does not automatically map entire parent/child commit graphs
 *
 * ## Usage
 * This mapper is typically called by infrastructure ports and assemblers. Direct usage
 * is also supported for `refreshDomain` operations after persistence.
 */
@Component
internal class CommitMapper : EntityMapper<Commit, CommitEntity> {
    @Autowired
    private lateinit var ctx: MappingContext

    @Autowired
    private lateinit var developerMapper: DeveloperMapper

    companion object {
        private val logger by logger()
    }

    /**
     * Converts a Commit domain object to CommitEntity.
     *
     * **Precondition**: The referenced Repository must already be mapped and present in MappingContext.
     * This enforces aggregate boundaries - Repository is a separate aggregate.
     *
     * **Note**: This method does NOT map parent/child commit relationships or branches.
     * Use assemblers for complete commit graph assembly.
     *
     * @param domain The Commit domain object to convert
     * @return The CommitEntity (structure only, without relationships)
     * @throws IllegalStateException if Repository is not in MappingContext
     */
    override fun toEntity(domain: Commit): CommitEntity {
        // Fast-path: if this Commit was already mapped in the current context, return it.
        ctx.findEntity<Commit.Key, Commit, CommitEntity>(domain)?.let { return it }

        // IMPORTANT: Expect Repository already in context (cross-aggregate reference).
        // Do NOT auto-map Repository here - that's a separate aggregate.
        val owner = ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(domain.repository)
            ?: throw IllegalStateException(
                "RepositoryEntity must be mapped before CommitEntity. " +
                        "Ensure CommitEntity is in MappingContext before calling toDomain()."
            )

        val authorEntity = developerMapper.toEntity(domain.author)
        val committerEntity = developerMapper.toEntity(domain.committer)

        val entity = domain.toEntity(
            repository = owner,
            author = authorEntity,
            committer = committerEntity,
        )
        ctx.remember(domain, entity)

        return entity
    }

    /**
     * Converts a CommitEntity to Commit domain object.
     *
     * **Precondition**: The referenced Repository must already be mapped and present in MappingContext.
     * This enforces aggregate boundaries - Repository is a separate aggregate.
     *
     * **Note**: This method does NOT map parent/child commit relationships or branches.
     * Use assemblers for complete commit graph assembly.
     *
     * @param entity The CommitEntity to convert
     * @return The Commit domain object (structure only, without relationships)
     * @throws IllegalStateException if Repository is not in MappingContext
     */
    override fun toDomain(entity: CommitEntity): Commit {
        ctx.findDomain<Commit, CommitEntity>(entity)?.let { return it }

        // IMPORTANT: Expect Repository already in context (cross-aggregate reference).
        // Do NOT auto-map Repository here - that's a separate aggregate.
        val owner = ctx.findDomain<Repository, RepositoryEntity>(entity.repository)
            ?: throw IllegalStateException(
                "Repository must be mapped before Commit. " +
                        "Ensure Repository is in MappingContext before calling toDomain()."
            )

        val author = developerMapper.toDomain(entity.author)
        val committer = developerMapper.toDomain(entity.committer)

        val domain = entity.toDomain(owner, author, committer)
        setField(
            domain.javaClass.superclass.getDeclaredField("iid"),
            domain,
            entity.iid
        )
        ctx.remember(domain, entity)

        return domain
    }

    /**
     * Refreshes a Commit domain object with data from the corresponding entity.
     *
     * This method updates the domain object's ID from the entity after persistence.
     * It also recursively refreshes parent and child commits, as well as author/committer.
     *
     * **Note**: This method performs recursive updates on parent/child relationships.
     *
     * @param target The Commit domain object to refresh
     * @param entity The CommitEntity with updated data
     * @return The refreshed Commit domain object
     */
    fun refreshDomain(target: Commit, entity: CommitEntity): Commit {
        setField(
            target.javaClass.getDeclaredField("id"),
            target,
            entity.id?.toString()
        )

        return target
    }
}
