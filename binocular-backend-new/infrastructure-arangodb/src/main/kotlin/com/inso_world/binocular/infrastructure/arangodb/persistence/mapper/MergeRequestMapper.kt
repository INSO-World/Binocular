package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.MergeRequestEntity
import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Project
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import kotlin.uuid.ExperimentalUuidApi

/**
 * Mapper for MergeRequest domain objects.
 *
 * Converts between MergeRequest domain objects and MergeRequestEntity persistence entities for ArangoDB.
 * Relationship collections (accounts, milestones, notes) are always empty after toDomain —
 * Spring Data ArangoDB's @Relations AQL generation does not backtick-quote hyphenated collection
 * names, causing syntax errors. Resolvers load relationships via hand-written AQL instead.
 *
 * ## Design Principles
 * - **Single Responsibility**: Only converts MergeRequest structure
 * - **Eager Mentions**: Eagerly maps mentions as they are typically accessed with the merge request
 * - **Context Management**: Uses MappingContext to prevent duplicate mappings
 */
@OptIn(ExperimentalUuidApi::class)
@Component
internal class MergeRequestMapper
    @Autowired
    constructor(
        @Lazy private val milestoneMapper: MilestoneMapper,
        @Lazy private val noteMapper: NoteMapper,
        @Lazy private val accountMapper: AccountMapper,
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
         * Relationships to accounts, milestones, and notes are not persisted in the entity - they
         * relationship fields on MergeRequestEntity are not loaded in toDomain.
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
                accounts = domain.accounts.map { accountMapper.toEntity(it) }.toSet(),
                milestones = domain.milestones.map { milestoneMapper.toEntity(it) }.toSet(),
                notes = domain.notes.map { noteMapper.toEntity(it) }.toSet(),
            )

        /**
         * Converts a MergeRequestEntity to MergeRequest domain object.
         *
         * Eagerly maps mentions. Relationships (milestones, notes, accounts) are intentionally
         * left empty: Spring Data ArangoDB's auto-generated AQL for @Relations graph traversal
         * does not backtick-quote hyphenated collection names (e.g. `mergeRequests-milestones`),
         * producing a syntax error at runtime. The GraphQL layer resolves these relationships via
         * MergeRequestResolver using separate, hand-written AQL queries instead.
         *
         * @param entity The MergeRequestEntity to convert
         * @return The MergeRequest domain object with eager mentions; relationship collections are empty
         */
        override fun toDomain(entity: MergeRequestEntity): MergeRequest {
            // Fast-path: Check if already mapped
            ctx.findDomain<MergeRequest, MergeRequestEntity>(entity)?.let { return it }

            return MergeRequest(
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
            )
        }

        /**
         * Converts a list of ArangoDB MergeRequestEntity objects to a list of domain MergeRequest objects
         */
        override fun toDomainList(entities: Iterable<MergeRequestEntity>): List<MergeRequest> = entities.map { toDomain(it) }
    }
