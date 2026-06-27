package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.IssueEntity
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Project
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.util.Date
import kotlin.uuid.ExperimentalUuidApi

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
                iid = domain.platformIid,
                title = domain.title,
                description = domain.description,
                createdAt = domain.createdAt?.let { Date.from(it.toInstant(ZoneOffset.UTC)) },
                closedAt = domain.closedAt?.let { Date.from(it.toInstant(ZoneOffset.UTC)) },
                updatedAt = domain.updatedAt?.let { Date.from(it.toInstant(ZoneOffset.UTC)) },
                labels = domain.labels,
                state = domain.state,
                webUrl = domain.webUrl,
                gid = domain.gid,
                mentions = domain.mentions.map { mentionMapper.toEntity(it) },
                accounts = domain.accounts.map { accountMapper.toEntity(it) }.toSet(),
                commits = domain.commits.map { commitMapper.toEntity(it) }.toSet(),
                milestones = domain.milestones.map { milestoneMapper.toEntity(it) }.toSet(),
                notes = domain.notes.map { noteMapper.toEntity(it) }.toSet(),
            )

        /**
         * Converts an IssueEntity to Issue domain object.
         *
         * Converts timestamp fields from Date to LocalDateTime. Eagerly maps mentions.
         * Relationships (milestones, notes, commits, accounts) are intentionally left empty:
         * Spring Data ArangoDB's auto-generated AQL for @Relations graph traversal does not
         * backtick-quote hyphenated collection names (e.g. `issues-milestones`), producing
         * a syntax error at runtime. The GraphQL layer resolves these relationships via
         * IssueResolver using separate, hand-written AQL queries instead.
         * Users are set as a true lazy list — only evaluated if domain.users is accessed directly.
         *
         * @param entity The IssueEntity to convert
         * @return The Issue domain object with eager mentions; relationship collections are empty
         */
        @OptIn(ExperimentalUuidApi::class)
        override fun toDomain(entity: IssueEntity): Issue {
            // Fast-path: Check if already mapped
            ctx.findDomain<Issue, IssueEntity>(entity)?.let { return it }

            val domain =
                Issue(
                    id = entity.id,
                    platformIid = entity.iid,
                    gid = entity.gid,
                    title = entity.title,
                    description = entity.description,
                    createdAt =
                        entity.createdAt
                            ?.toInstant()
                            ?.atZone(ZoneOffset.UTC)
                            ?.toLocalDateTime(),
                    closedAt =
                        entity.closedAt
                            ?.toInstant()
                            ?.atZone(ZoneOffset.UTC)
                            ?.toLocalDateTime(),
                    updatedAt =
                        entity.updatedAt
                            ?.toInstant()
                            ?.atZone(ZoneOffset.UTC)
                            ?.toLocalDateTime(),
                    labels = entity.labels,
                    state = entity.state,
                    webUrl = entity.webUrl,
                    mentions = entity.mentions.map { mentionMapper.toDomain(it) },
                    project =
                        entity.project?.let { Project.Id(it.iid!!) }
                            ?: ctx.findDomain<Project, IssueEntity>(entity)?.iid
                            ?: error("Parent Project not found in entity or context for Issue ${entity.iid}"),
                )

            // users is set as a true lazy list — not evaluated until domain.users is accessed.
            // If accessed, it will trigger @Relations AQL on `issues-users`; the GraphQL layer
            // resolves users via IssueResolver.users() instead of this field.
            domain.users =
                proxyFactory.createLazyList {
                    (entity.users ?: emptyList()).map { userEntity ->
                        userMapper.toDomain(userEntity)
                    }
                }

            return domain
        }

        override fun toDomainList(entities: Iterable<IssueEntity>): List<Issue> = entities.map { toDomain(it) }
    }
