package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CommitEntity
import com.inso_world.binocular.model.Commit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.util.Date

/**
 * Mapper for Commit domain objects.
 *
 * Converts between Commit domain objects and CommitEntity persistence entities for ArangoDB.
 * This is a **simple mapper** - it only handles basic conversion without orchestrating
 * complex relationships like full commit history graphs.
 *
 * ## Design Principles
 * - **Single Responsibility**: Only converts Commit structure
 * - **No Deep Traversal**: Does not automatically map entire parent/child commit graphs
 * - **Context-Dependent toDomain**: Requires Commit already in context (preserves repository and committer references)
 *
 * ## Usage
 * This mapper is typically called by infrastructure ports and assemblers. The `toDomain`
 * method requires the Commit to already exist in MappingContext to preserve references
 * that cannot be reconstructed from the entity alone.
 */
@Component
internal class CommitMapper: EntityMapper<Commit, CommitEntity> {

    @Autowired
    private lateinit var statsMapper: StatsMapper

    @Autowired
    private lateinit var ctx: MappingContext

    companion object {
        private val logger by logger()
    }

    /**
     * Converts a Commit domain object to CommitEntity.
     *
     * Converts the commit date from LocalDateTime to Date for ArangoDB storage.
     * Maps commit statistics using the StatsMapper if present.
     *
     * **Note**: This method does NOT map parent/child commit relationships or branches.
     * Use assemblers for complete commit graph assembly.
     *
     * @param domain The Commit domain object to convert
     * @return The CommitEntity (structure only, without relationships)
     */
    override fun toEntity(domain: Commit): CommitEntity {
        // Fast-path: if this Commit was already mapped in the current context, return it.
        ctx.findEntity<Commit.Key, Commit, CommitEntity>(domain)?.let { return it }

        val entity =
            CommitEntity(
                id = domain.id,
                sha = domain.sha,
                date = domain.commitDateTime?.let { Date.from(it.toInstant(ZoneOffset.UTC)) },
                message = domain.message,
                webUrl = domain.webUrl,
                stats = domain.stats?.let { statsMapper.toEntity(it) },
                branch = domain.branch,
            )

        ctx.remember(domain, entity)
        return entity
    }

    /**
     * Converts a CommitEntity to Commit domain object.
     *
     * **Precondition**: The Commit must already exist in MappingContext. This is necessary because
     * the CommitEntity does not store the repository or committer references, which are required by
     * the Commit domain model. These references must be preserved from the original domain object.
     *
     * **Note**: This method does NOT map parent/child commit relationships or branches.
     * Use assemblers for complete commit graph assembly.
     *
     * @param entity The CommitEntity to convert
     * @return The Commit domain object from MappingContext
     * @throws IllegalStateException if Commit is not already in MappingContext
     */
    override fun toDomain(entity: CommitEntity): Commit {
        // Fast-path: Check if already mapped - required for repository and committer references
        ctx.findDomain<Commit, CommitEntity>(entity)?.let { return it }

        throw IllegalStateException(
            "Commit mapping requires an existing domain Commit in MappingContext. " +
                    "Ensure Commit is created with repository and committer before calling toDomain()."
        )
    }

    override fun toDomainList(entities: Iterable<CommitEntity>): List<Commit> = entities.map { toDomain(it) }
}
