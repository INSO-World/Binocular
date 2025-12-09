package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.toEntity
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Developer
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Signature
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date
import kotlin.uuid.ExperimentalUuidApi

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
internal class CommitMapper : EntityMapper<Commit, CommitEntity> {

    @Autowired
    private lateinit var developerMapper: DeveloperMapper

    @Autowired
    private lateinit var repositoryMapper: RepositoryMapper

    @Autowired
    private lateinit var projectMapper: ProjectMapper

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

        // Ensure the owning project is mapped before the repository
        ctx.findEntity<Project.Key, Project, ProjectEntity>(domain.repository.project)
            ?: projectMapper.toEntity(domain.repository.project)

        val repositoryEntity = ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(domain.repository)
            ?: repositoryMapper.toEntity(domain.repository)
        val author = developerMapper.toEntity(domain.author)
        val committer = developerMapper.toEntity(domain.committer)

        val entity =
            domain.toEntity(
                repository = repositoryEntity,
                author = author,
                committer = committer,
            ).apply {
                date = domain.commitDateTime.let { Date.from(it.toInstant(ZoneOffset.UTC)) }
                branch = domain.branch
                stats = domain.stats?.let { statsMapper.toEntity(it) }
            }

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
    @OptIn(ExperimentalUuidApi::class)
    override fun toDomain(entity: CommitEntity): Commit {
        // Fast-path: Check if already mapped - required for repository and committer references
        ctx.findDomain<Commit, CommitEntity>(entity)?.let { domain ->
            domain.id = entity.id
            domain.branch = entity.branch
            domain.webUrl = entity.webUrl
            domain.stats = entity.stats?.let { statsMapper.toDomain(it) }

            setField(
                domain.javaClass.superclass.getDeclaredField("iid"),
                domain,
                entity.iid,
            )
            ctx.remember(domain, entity)
            return domain
        }

        // Map owning project/repository first to satisfy commit invariants
        val project = ctx.findDomain<Project, ProjectEntity>(entity.repository.project)
            ?: projectMapper.toDomain(entity.repository.project)
        // Ensure repository mapping sees an already mapped project
        val repository = ctx.findDomain<Repository, RepositoryEntity>(entity.repository)
            ?: repositoryMapper.toDomain(entity.repository)

        // Derive developer information from related users if available; otherwise fall back to a placeholder
        val developer =
            entity.users.firstOrNull()?.let { user ->
                Developer(
                    name = user.name,
                    email = user.email,
                    repository = repository,
                )
            } ?: Developer(
                name = "unknown",
                email = "unknown@example.com",
                repository = repository,
            )

        val timestamp =
            entity.date
                ?.toInstant()
                ?.atZone(ZoneOffset.UTC)
                ?.toLocalDateTime()
                ?: LocalDateTime.now()

        val signature = Signature(developer = developer, timestamp = timestamp)

        val domain =
            Commit(
                sha = entity.sha,
                authorSignature = signature,
                committerSignature = signature,
                repository = repository,
                message = entity.message,
            ).apply {
                id = entity.id
                webUrl = entity.webUrl
                branch = entity.branch
                stats = entity.stats?.let { statsMapper.toDomain(it) }
            }

        setField(
            domain.javaClass.superclass.getDeclaredField("iid"),
            domain,
            Commit.Id(entity.iid),
        )
        ctx.remember(domain, entity)
        return domain
    }

    override fun toDomainList(entities: Iterable<CommitEntity>): List<Commit> = entities.map { toDomain(it) }
}
