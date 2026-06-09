package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.MergeRequestEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import com.inso_world.binocular.model.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Mapper for MergeRequest domain objects.
 *
 * Converts between MergeRequest domain objects and MergeRequestEntity persistence entities for ArangoDB.
 * This mapper handles the conversion of merge request metadata, labels, mentions, and uses lazy loading
 * for related accounts, milestones, and notes.
 *
 * ## Design Principles
 * - **Single Responsibility**: Only converts MergeRequest structure
 * - **Lazy Loading**: Uses RelationshipProxyFactory for lazy-loaded relationships
 * - **Eager Mentions**: Eagerly maps mentions as they are typically accessed with the merge request
 * - **Context Management**: Uses MappingContext to prevent duplicate mappings
 *
 * ## Usage
 * This mapper is typically called by infrastructure ports and assemblers. It eagerly maps
 * mentions but uses lazy loading for accounts, milestones, and notes to optimize performance.
 */
@OptIn(ExperimentalUuidApi::class)
@Component
internal class MergeRequestMapper(
    private val mentionMapper: MentionMapper,
) : EntityMapper<MergeRequest, MergeRequestEntity> {
    @Autowired
    private lateinit var ctx: MappingContext

    companion object {
        private val logger by logger()
    }

    /**
     * Converts a MergeRequest domain object to MergeRequestEntity.
     *
     * Eagerly maps all mentions as they are typically accessed together with the merge request.
     *
     * @param domain The MergeRequest domain object to convert
     * @return The MergeRequestEntity with merge request metadata, labels, and mentions
     */
    override fun toEntity(domain: MergeRequest): MergeRequestEntity =
        MergeRequestEntity(
            id = domain.id,
            iid = domain.platformIid,
            title = domain.title,
            description = domain.description,
            createdAt = domain.createdAt,
            closedAt = domain.closedAt,
            updatedAt = domain.updatedAt,
            labels = domain.labels,
            state = domain.state,
            webUrl = domain.webUrl,
            mentions = domain.mentions.map { mentionMapper.toEntity(it) },
        )

    /**
     * Converts a MergeRequestEntity to MergeRequest domain object.
     *
     * Eagerly maps mentions and extracts IDs for related accounts, milestones, and notes.
     *
     * @param entity The MergeRequestEntity to convert
     * @return The MergeRequest domain object with eager mentions and IDs for relationships
     */
    override fun toDomain(entity: MergeRequestEntity): MergeRequest {
        // Fast-path: Check if already mapped
        ctx.findDomain<MergeRequest, MergeRequestEntity>(entity)?.let { return it }

        val domain = MergeRequest(
            project =
                entity.project?.let { Project.Id(it.iid!!) }
                    ?: ctx.findDomain<Project, MergeRequestEntity>(entity)?.iid
                    ?: error("Parent Project not found in entity or context for MergeRequest ${entity.iid}"),
            id = entity.id,
            platformIid = entity.iid,
            title = entity.title,
            description = entity.description,
            createdAt = entity.createdAt,
            closedAt = entity.closedAt,
            updatedAt = entity.updatedAt,
            labels = entity.labels,
            state = entity.state,
            webUrl = entity.webUrl,
            mentions = entity.mentions.map { mentionMapper.toDomain(it) },
            milestoneIds = entity.milestones.mapNotNull { it.id?.let { id -> Milestone.Id(Uuid.parse(id)) } }.toMutableSet(),
            noteIds = entity.notes.mapNotNull { it.id?.let { id -> Note.Id(Uuid.parse(id)) } }.toMutableSet(),
        )

        domain.accountIds.addAll(
            entity.accounts.mapNotNull { it.id?.let { id -> Account.Id(Uuid.parse(id)) } }
        )

        return domain
    }

        /**
         * Converts a list of ArangoDB MergeRequestEntity objects to a list of domain MergeRequest objects
         */
        override fun toDomainList(entities: Iterable<MergeRequestEntity>): List<MergeRequest> = entities.map { toDomain(it) }
    }
