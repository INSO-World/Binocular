package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.IssueEntity
import com.inso_world.binocular.model.Issue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.util.Date

@Component
class IssueMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
        @Lazy private val accountMapper: AccountMapper,
        @Lazy private val milestoneMapper: MilestoneMapper,
        @Lazy private val noteMapper: NoteMapper,
        @Lazy private val userMapper: UserMapper,
        private val mentionMapper: MentionMapper,
    ) : EntityMapper<Issue, IssueEntity> {
        @Lazy @Autowired
        private lateinit var commitMapper: CommitMapper

        /**
         * Converts a domain Issue to an ArangoDB IssueEntity
         */
        override fun toEntity(domain: Issue): IssueEntity =
            IssueEntity(
                id = domain.id,
                iid = domain.iid,
                title = domain.title,
                description = domain.description,
                createdAt = domain.createdAt?.let { Date.from(it.toInstant(ZoneOffset.UTC)) },
                closedAt = domain.closedAt?.let { Date.from(it.toInstant(ZoneOffset.UTC)) },
                updatedAt = domain.updatedAt?.let { Date.from(it.toInstant(ZoneOffset.UTC)) },
                labels = domain.labels,
                state = domain.state,
                webUrl = domain.webUrl,
                mentions = (domain.mentions).map { mention ->
                    mentionMapper.toEntity(mention)
                },
                // Relationships are handled by ArangoDB through edges
            )

        /**
         * Converts an ArangoDB IssueEntity to a domain Issue
         *
         * Uses lazy loading proxies for relationships, which will only be loaded
         * when accessed. This provides a consistent API regardless of the database
         * implementation and avoids the N+1 query problem.
         */
        override fun toDomain(entity: IssueEntity): Issue =
            Issue(
                id = entity.id,
                iid = entity.iid,
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
                mentions = (entity.mentions).map { mentionEntity ->
                    mentionMapper.toDomain(mentionEntity)
                },
                accounts =
                    proxyFactory.createLazyList {
                        (entity.accounts ?: emptyList()).map { accountEntity ->
                            accountMapper.toDomain(accountEntity)
                        }
                    },
                commits =
                    proxyFactory.createLazyList {
                        (entity.commits ?: emptyList()).map { commitEntity ->
                            commitMapper.toDomain(commitEntity)
                        }
                    },
                milestones =
                    proxyFactory.createLazyList {
                        (entity.milestones ?: emptyList()).map { milestoneEntity ->
                            milestoneMapper.toDomain(milestoneEntity)
                        }
                    },
                notes =
                    proxyFactory.createLazyList {
                        (entity.notes ?: emptyList()).map { noteEntity ->
                            noteMapper.toDomain(noteEntity)
                        }
                    },
                users =
                    proxyFactory.createLazyList {
                        (entity.users ?: emptyList()).map { userEntity ->
                            userMapper.toDomain(userEntity)
                        }
                    },
            )

        /**
         * Converts a list of ArangoDB IssueEntity objects to a list of domain Issue objects
         */
        override fun toDomainList(entities: Iterable<IssueEntity>): List<Issue> = entities.map { toDomain(it) }
    }
