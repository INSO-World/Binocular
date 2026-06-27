package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.MilestoneEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ProjectEntity
import com.inso_world.binocular.model.Milestone
import com.inso_world.binocular.model.Project
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import kotlin.uuid.ExperimentalUuidApi

/**
 * Mapper for Milestone domain objects.
 *
 * Converts between Milestone domain objects and MilestoneEntity persistence entities for ArangoDB.
 * Relationship collections (issues, mergeRequests) are always empty after toDomain —
 * Spring Data ArangoDB's @Relations AQL generation does not backtick-quote hyphenated collection
 * names, causing syntax errors. Resolvers load relationships via hand-written AQL instead.
 *
 * ## Design Principles
 * - **Single Responsibility**: Only converts Milestone structure
 * - **Context Management**: Uses MappingContext to prevent duplicate mappings
 */
@Component
internal class MilestoneMapper
    @Autowired
    constructor(
        @Lazy private val issueMapper: IssueMapper,
        @Lazy private val mergeRequestMapper: MergeRequestMapper,
    ) : EntityMapper<Milestone, MilestoneEntity> {
        @Autowired
        private lateinit var ctx: MappingContext

        companion object {
            private val logger by logger()
        }

        /**
         * Converts a Milestone domain object to MilestoneEntity.
         *
         * Maps all milestone properties including metadata, dates, and state. Relationships
         * to issues and merge requests are not persisted in the entity - they are only
         * relationship fields on MilestoneEntity are not loaded in toDomain.
         *
         * @param domain The Milestone domain object to convert
         * @return The MilestoneEntity with milestone metadata
         */
        override fun toEntity(domain: Milestone): MilestoneEntity {
            val entity =
                MilestoneEntity(
                    id = domain.id,
                    iid = domain.platformIid,
                    title = domain.title,
                    description = domain.description,
                    createdAt = domain.createdAt,
                    updatedAt = domain.updatedAt,
                    startDate = domain.startDate,
                    dueDate = domain.dueDate,
                    state = domain.state,
                    expired = domain.expired,
                    webUrl = domain.webUrl,
                    project = ctx.findEntity<Project.Key, Project, ProjectEntity>(Project(domain.project.toString())),
                    issues = domain.issues.map { issueMapper.toEntity(it) }.toSet(),
                    mergeRequests = domain.mergeRequests.map { mergeRequestMapper.toEntity(it) }.toSet(),
                )
            return entity
        }

        /**
         * Converts a MilestoneEntity to Milestone domain object.
         *
         * Relationships (issues, mergeRequests) are intentionally left empty:
         * Spring Data ArangoDB's auto-generated AQL for @Relations graph traversal does not
         * backtick-quote hyphenated collection names (e.g. `issues-milestones`),
         * producing a syntax error at runtime. Resolvers load these via separate hand-written AQL.
         *
         * @param entity The MilestoneEntity to convert
         * @return The Milestone domain object; relationship collections are empty
         */
        @OptIn(ExperimentalUuidApi::class)
        override fun toDomain(entity: MilestoneEntity): Milestone {
            // Fast-path: Check if already mapped
            ctx.findDomain<Milestone, MilestoneEntity>(entity)?.let { return it }

            return Milestone(
                id = entity.id,
                platformIid = entity.iid,
                title = entity.title,
                description = entity.description,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                startDate = entity.startDate,
                dueDate = entity.dueDate,
                state = entity.state,
                expired = entity.expired,
                webUrl = entity.webUrl,
                project =
                    entity.project?.let { Project.Id(it.iid!!) }
                        ?: ctx.findDomain<Project, MilestoneEntity>(entity)?.iid
                        ?: error("Parent Project not found in entity or context for Milestone ${entity.iid}"),
            )
        }

        override fun toDomainList(entities: Iterable<MilestoneEntity>): List<Milestone> = entities.map { toDomain(it) }
    }
