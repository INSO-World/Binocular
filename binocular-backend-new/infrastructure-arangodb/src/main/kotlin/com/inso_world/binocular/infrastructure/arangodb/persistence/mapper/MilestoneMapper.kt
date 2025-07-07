package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.MilestoneEntity
import com.inso_world.binocular.model.Milestone
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class MilestoneMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
        @Lazy private val issueMapper: IssueMapper,
        @Lazy private val mergeRequestMapper: MergeRequestMapper,
    ) : EntityMapper<Milestone, MilestoneEntity> {
        /**
         * Converts a domain Milestone to an ArangoDB MilestoneEntity
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
                // Relationships are handled by ArangoDB through edges
            )

        /**
         * Converts an ArangoDB MilestoneEntity to a domain Milestone
         *
         * Uses lazy loading proxies for relationships, which will only be loaded
         * when accessed. This provides a consistent API regardless of the database
         * implementation and avoids the N+1 query problem.
         */
        override fun toDomain(entity: MilestoneEntity): Milestone =
            Milestone(
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

        /**
         * Converts a list of ArangoDB MilestoneEntity objects to a list of domain Milestone objects
         */
        override fun toDomainList(entities: Iterable<MilestoneEntity>): List<Milestone> = entities.map { toDomain(it) }
    }
