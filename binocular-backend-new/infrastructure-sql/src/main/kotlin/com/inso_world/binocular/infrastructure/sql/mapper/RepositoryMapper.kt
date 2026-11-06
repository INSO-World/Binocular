package com.inso_world.binocular.infrastructure.sql.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.toEntity
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.stereotype.Component

/**
 * Mapper for Repository aggregate root.
 *
 * Converts between Repository domain objects and RepositoryEntity persistence entities.
 * This is a **simple mapper** - it only handles basic conversion without orchestrating
 * child entity mapping. Use [RepositoryAssembler] for complete aggregate assembly.
 *
 * ## Design Principles
 * - **Single Responsibility**: Only converts Repository structure (not children)
 * - **Aggregate Boundaries**: Expects Project already in MappingContext (cross-aggregate reference)
 * - **No Orchestration**: Child entities (Commits, Branches) are mapped by assembler
 *
 * ## Usage
 * Prefer using [RepositoryAssembler] at the service layer. This mapper is called by the assembler
 * and is also used for `refreshDomain` operations after persistence.
 *
 * @see com.inso_world.binocular.infrastructure.sql.assembler.RepositoryAssembler
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
     * **Note**: This method does NOT map child entities (Commits, Branches). Use [RepositoryAssembler]
     * for complete aggregate assembly including children.
     *
     * @param domain The Repository domain object to convert
     * @return The RepositoryEntity (structure only, without children)
     * @throws IllegalStateException if Project is not in MappingContext
     */
    override fun toEntity(
        domain: Repository,
    ): RepositoryEntity {
        // Fast-path: if this Repository was already mapped in the current context, return it.
        ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(domain)?.let { return it }

        // IMPORTANT: Expect Project already in context (cross-aggregate reference).
        // Do NOT auto-map Project here - that's a separate aggregate.
        val owner: ProjectEntity = ctx.findEntity<Project.Key, Project, ProjectEntity>(domain.project)
            ?: throw IllegalStateException(
                "ProjectEntity must be mapped before RepositoryEntity. " +
                        "Ensure ProjectEntity is in MappingContext before calling toEntity()."
            )

        // Create entity and remember in context
        val entity = domain.toEntity(owner)
        ctx.remember(domain, entity)

        // Delegate to overload with explicit owner
        return entity
    }

    /**
     * Converts a RepositoryEntity to Repository domain object.
     *
     * **Precondition**: The referenced Project must already be mapped and present in MappingContext.
     * This enforces aggregate boundary - Project is a separate aggregate that must be handled first.
     *
     * **Note**: This method does NOT map child entities (Commits, Branches). Use [RepositoryAssembler]
     * for complete aggregate assembly including children.
     *
     * @param entity The RepositoryEntity to convert
     * @return The Repository domain object (structure only, without children)
     * @throws IllegalStateException if Project is not in MappingContext
     */
    override fun toDomain(
        entity: RepositoryEntity,
    ): Repository {
        // Fast-path: Check if already mapped
        ctx.findDomain<Repository, RepositoryEntity>(entity)?.let { return it }

        // IMPORTANT: Expect Project already in context (cross-aggregate reference).
        // Do NOT auto-map Project here - that's a separate aggregate.
        val owner = ctx.findDomain<Project, ProjectEntity>(entity.project)
            ?: throw IllegalStateException(
                "Project must be mapped before Repository. " +
                        "Ensure Project is in MappingContext before calling toDomain()."
            )

        val domain = entity.toDomain(owner)
        setField(
            domain.javaClass.superclass.getDeclaredField("iid"),
            domain,
            entity.iid
        )

        ctx.remember(domain, entity)

        return domain
    }

    fun refreshDomain(target: Repository, entity: RepositoryEntity): Repository {
        setField(
            target.javaClass.getDeclaredField("id"),
            target,
            entity.id?.toString()
        )

        return target
    }
}
