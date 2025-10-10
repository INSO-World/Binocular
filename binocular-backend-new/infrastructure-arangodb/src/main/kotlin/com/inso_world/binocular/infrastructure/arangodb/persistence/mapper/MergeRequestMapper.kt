package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.MergeRequestEntity
import com.inso_world.binocular.model.MergeRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class MergeRequestMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
        @Lazy private val milestoneMapper: MilestoneMapper,
        @Lazy private val noteMapper: NoteMapper,
        @Lazy private val accountMapper: AccountMapper,
        private val mentionMapper: MentionMapper,
    ) : EntityMapper<MergeRequest, MergeRequestEntity> {
        /**
         * Converts a domain MergeRequest to an ArangoDB MergeRequestEntity
         */
        override fun toEntity(domain: MergeRequest): MergeRequestEntity =
            MergeRequestEntity(
                id = domain.id,
                iid = domain.iid,
                title = domain.title,
                description = domain.description,
                createdAt = domain.createdAt,
                closedAt = domain.closedAt,
                updatedAt = domain.updatedAt,
                labels = domain.labels,
                state = domain.state,
                webUrl = domain.webUrl,
                mentions = (domain.mentions).map { mention ->
                    mentionMapper.toEntity(mention)
                },
                // Relationships are handled by ArangoDB through edges
            )

        /**
         * Converts an ArangoDB MergeRequestEntity to a domain MergeRequest
         *
         * Uses lazy loading proxies for relationships, which will only be loaded
         * when accessed. This provides a consistent API regardless of the database
         * implementation and avoids the N+1 query problem.
         */
        override fun toDomain(entity: MergeRequestEntity): MergeRequest =
            MergeRequest(
                id = entity.id,
                iid = entity.iid,
                title = entity.title,
                description = entity.description,
                createdAt = entity.createdAt,
                closedAt = entity.closedAt,
                updatedAt = entity.updatedAt,
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
            )

        /**
         * Converts a list of ArangoDB MergeRequestEntity objects to a list of domain MergeRequest objects
         */
        override fun toDomainList(entities: Iterable<MergeRequestEntity>): List<MergeRequest> = entities.map { toDomain(it) }
    }
