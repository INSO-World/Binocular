package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.MilestoneEntity
import com.inso_world.binocular.model.Milestone
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

/**
 * Mapper for Milestone domain objects.
 *
 * Converts between Milestone domain objects and MilestoneEntity persistence entities for ArangoDB.
 * This mapper handles the conversion of milestone metadata and uses lazy loading for related
 * issues and merge requests.
 *
 * ## Design Principles
 * - **Single Responsibility**: Only converts Milestone structure
 * - **Lazy Loading**: Uses RelationshipProxyFactory for lazy-loaded relationships (issues, merge requests)
 * - **Context Management**: Uses MappingContext to prevent duplicate mappings
 *
 * ## Usage
 * This mapper is typically called by infrastructure ports and assemblers. It uses lazy loading
 * for issues and merge requests to optimize performance when accessing milestone metadata.
 */
@Component
internal class MilestoneMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
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
         * restored during toDomain through lazy loading.
         *
         * @param domain The Milestone domain object to convert
         * @return The MilestoneEntity with milestone metadata
         */
        override fun toEntity(domain: Milestone): MilestoneEntity =
            MilestoneEntity(
                id = domain.id,
                iid = domain.iid,
                title = domain.title,
                description = domain.description,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt,
                startDate = domain.startDate,
                dueDate = domain.dueDate,
                state = domain.state,
                expired = domain.expired,
                webUrl = domain.webUrl,
            )

        /**
         * Converts a MilestoneEntity to Milestone domain object.
         *
         * Creates lazy-loaded proxies for issues and merge requests to avoid loading
         * unnecessary data when only milestone metadata is needed.
         *
         * @param entity The MilestoneEntity to convert
         * @return The Milestone domain object with lazy issues and merge requests
         */
        override fun toDomain(entity: MilestoneEntity): Milestone {
            // Fast-path: Check if already mapped
            ctx.findDomain<Milestone, MilestoneEntity>(entity)?.let { return it }

            return Milestone(
                id = entity.id,
                iid = entity.iid,
                title = entity.title,
                description = entity.description,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                startDate = entity.startDate,
                dueDate = entity.dueDate,
                state = entity.state,
                expired = entity.expired,
                webUrl = entity.webUrl,
                issues =
                    proxyFactory.createLazyList {
                        (entity.issues ?: emptyList()).map { issueEntity ->
                            issueMapper.toDomain(issueEntity)
                        }
                    },
                mergeRequests =
                    proxyFactory.createLazyList {
                        (entity.mergeRequests ?: emptyList()).map { mergeRequestEntity ->
                            mergeRequestMapper.toDomain(mergeRequestEntity)
                        }
                    },
            )
        }

        override fun toDomainList(entities: Iterable<MilestoneEntity>): List<Milestone> = entities.map { toDomain(it) }
    }
