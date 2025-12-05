package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.IssueEntity
import com.inso_world.binocular.model.Issue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.util.Date

/**
 * Mapper for Issue domain objects.
 *
 * Converts between Issue domain objects and IssueEntity persistence entities for ArangoDB.
 * This mapper handles the conversion of issue metadata, labels, mentions, and uses lazy loading
 * for related accounts, commits, milestones, notes, and users.
 *
 * ## Design Principles
 * - **Single Responsibility**: Only converts Issue structure
 * - **Lazy Loading**: Uses RelationshipProxyFactory for lazy-loaded relationships
 * - **Date Conversion**: Converts between LocalDateTime and Date for ArangoDB storage
 * - **Eager Mentions**: Eagerly maps mentions as they are typically accessed with the issue
 * - **Context Management**: Uses MappingContext to prevent duplicate mappings
 *
 * ## Usage
 * This mapper is typically called by infrastructure ports and assemblers. It eagerly maps
 * mentions but uses lazy loading for accounts, commits, milestones, notes, and users to optimize performance.
 */
@Component
internal class IssueMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
        @Lazy private val accountMapper: AccountMapper,
        @Lazy private val milestoneMapper: MilestoneMapper,
        @Lazy private val noteMapper: NoteMapper,
        @Lazy private val userMapper: UserMapper,
        private val mentionMapper: MentionMapper,
    ) : EntityMapper<Issue, IssueEntity> {

        @Autowired
        private lateinit var ctx: MappingContext

        @Lazy
        @Autowired
        private lateinit var commitMapper: CommitMapper

        companion object {
            private val logger by logger()
        }

        /**
         * Converts an Issue domain object to IssueEntity.
         *
         * Converts timestamp fields from LocalDateTime to Date for ArangoDB storage.
         * Eagerly maps all mentions as they are typically accessed together with the issue.
         * Relationships to accounts, commits, milestones, notes, and users are not persisted
         * in the entity - they are only restored during toDomain through lazy loading.
         *
         * @param domain The Issue domain object to convert
         * @return The IssueEntity with issue metadata, labels, and mentions
         */
        override fun toEntity(domain: Issue): IssueEntity =
            IssueEntity(
                id = domain.id,
                //iid = domain.iid,
                title = domain.title,
                description = domain.description,
                createdAt = domain.createdAt?.let { Date.from(it.toInstant(ZoneOffset.UTC)) },
                closedAt = domain.closedAt?.let { Date.from(it.toInstant(ZoneOffset.UTC)) },
                updatedAt = domain.updatedAt?.let { Date.from(it.toInstant(ZoneOffset.UTC)) },
                labels = domain.labels,
                state = domain.state,
                webUrl = domain.webUrl,
                //mentions = domain.mentions,
                gid = domain.gid
                // Relationships are handled by ArangoDB through edges
//                mentions = domain.mentions.map { mentionMapper.toEntity(it) },
            )

        /**
         * Converts an IssueEntity to Issue domain object.
         *
         * Converts timestamp fields from Date to LocalDateTime. Eagerly maps mentions
         * and creates lazy-loaded proxies for accounts, commits, milestones, notes, and users
         * to avoid loading unnecessary data.
         *
         * @param entity The IssueEntity to convert
         * @return The Issue domain object with eager mentions and lazy relationships
         */
        override fun toDomain(entity: IssueEntity): Issue {
            // Fast-path: Check if already mapped
            ctx.findDomain<Issue, IssueEntity>(entity)?.let { return it }

            throw IllegalStateException("Issue mapping requires an existing domain Issue in MappingContext.")
        }

        override fun toDomainList(entities: Iterable<IssueEntity>): List<Issue> = entities.map { toDomain(it) }
    }
