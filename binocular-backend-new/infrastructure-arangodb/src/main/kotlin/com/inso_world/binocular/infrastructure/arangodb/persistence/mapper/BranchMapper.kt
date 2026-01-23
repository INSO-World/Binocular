package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Reference
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import com.inso_world.binocular.model.vcs.ReferenceCategory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Mapper for Branch domain objects.
 *
 * Converts between Branch domain objects and BranchEntity persistence entities for ArangoDB.
 * This is a **simple mapper** - it only handles basic conversion without orchestrating
 * complex relationships.
 *
 * ## Design Principles
 * - **Single Responsibility**: Only converts Branch structure
 * - **Aggregate Boundaries**: Expects Repository already in MappingContext (cross-aggregate reference)
 * - **No Deep Traversal**: Does not map entire commit history or file structures
 *
 * ## Usage
 * This mapper is typically called by infrastructure ports and assemblers.
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
     * This enforces aggregate boundary - Repository is a separate aggregate that must be handled first.
     *
     * **Note**: This method does NOT map child entities or traverse relationships deeply.
     *
     * @param domain The Branch domain object to convert
     * @return The BranchEntity (structure only)
     * @throws IllegalStateException if Repository is not in MappingContext
     */
    @OptIn(ExperimentalUuidApi::class)
    override fun toEntity(domain: Branch): BranchEntity {
        // Fast-path: if this Branch was already mapped in the current context, return it.
        ctx.findEntity<Branch.Key, Branch, BranchEntity>(domain)?.let { return it }

        // IMPORTANT: Expect Repository already in context (cross-aggregate reference).
        val repositoryEntity = ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(domain.repository)
            ?: throw IllegalStateException(
                "RepositoryEntity must be mapped before BranchEntity. " +
                        "Ensure RepositoryEntity is in MappingContext before calling toEntity()."
            )

        val entity = BranchEntity(
            id = domain.id,
            iid = domain.iid.value,
            name = domain.name,
            fullName = domain.fullName,
            category = domain.category.name,
            active = domain.active,
            tracksFileRenames = domain.tracksFileRenames,
            latestCommit = domain.latestCommit,
            repository = repositoryEntity
        )

        ctx.remember(domain, entity)
        return entity
    }

    /**
     * Converts a BranchEntity to Branch domain object.
     *
     * **Precondition**: The Branch must already exist in MappingContext. This is necessary because
     * the BranchEntity does not store the head commit reference, which is required by
     * the Branch domain model. This reference must be preserved from the original domain object.
     *
     * **Alternative**: The referenced Repository must be in MappingContext to construct a placeholder.
     *
     * **Note**: This method does NOT map child entities or traverse relationships deeply.
     *
     * @param entity The BranchEntity to convert
     * @return The Branch domain object from MappingContext or a newly constructed one
     * @throws IllegalStateException if neither Branch nor Repository is in MappingContext
     */
    override fun toDomain(entity: BranchEntity): Branch {
        // Fast-path: Check if already mapped - preferred to preserve head commit reference
        ctx.findDomain<Branch, BranchEntity>(entity)?.let { domain ->
            domain.id = entity.id
            domain.active = entity.active
            domain.tracksFileRenames = entity.tracksFileRenames
            ctx.remember(domain, entity)
            return domain
        }

        // Try to find Repository in context
        val repository = ctx.findDomain<Repository, RepositoryEntity>(entity.repository)
            ?: throw IllegalStateException(
                "Repository must be mapped before Branch. " +
                        "Ensure Repository is in MappingContext before calling toDomain()."
            )

        // Construct placeholder head commit (will be replaced by actual commit during full assembly)
        val sha = entity.latestCommit?.takeIf { it.length == 40 } ?: "0".repeat(40)
        val developer =
            Developer(
                name = "placeholder",
                email = "placeholder@example.com",
                repository = repository,
            )
        val head =
            Commit(
                sha = sha,
                authorSignature = Signature(developer = developer, timestamp = LocalDateTime.now()),
                repository = repository,
            )

        // Create domain with iid from entity
        val domain = Branch(
            name = entity.name,
            fullName = entity.fullName,
            category = ReferenceCategory.valueOf(entity.category),
            repository = repository,
            head = head,
        ).apply {
            id = entity.id
            active = entity.active
            tracksFileRenames = entity.tracksFileRenames
        }

        @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
        setField(
            domain.javaClass.superclass.getDeclaredField("iid"),
            domain,
            entity.iid
        )

        ctx.remember(domain, entity)
        return domain
    }
}
