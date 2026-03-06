package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.AccountEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.PlatformEntity
import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Platform
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

/**
 * Mapper for Account domain objects.
 *
 * Converts between Account domain objects and AccountEntity persistence entities for ArangoDB.
 * This mapper handles the conversion and uses lazy loading proxies for relationships to issues,
 * merge requests, and notes.
 *
 * ## Design Principles
 * - **Single Responsibility**: Only converts Account structure
 * - **Lazy Loading**: Uses RelationshipProxyFactory for lazy-loaded relationships
 * - **Context Management**: Uses MappingContext to prevent duplicate mappings
 *
 * ## Usage
 * This mapper is typically called by infrastructure ports and assemblers. It supports
 * lazy loading of related entities through proxy patterns.
 */
@Component
internal class AccountMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
        @Lazy private val issueMapper: IssueMapper,
        @Lazy private val mergeRequestMapper: MergeRequestMapper,
        @Lazy private val noteMapper: NoteMapper,
    ) : EntityMapper<Account, AccountEntity> {

        @Autowired
        private lateinit var ctx: MappingContext

        companion object {
            private val logger by logger()
        }

        /**
         * Converts an Account domain object to AccountEntity.
         *
         * Maps basic account properties including platform, login, name, avatar URL, and web URL.
         * Note that relationships (issues, merge requests, notes) are not included in the entity
         * and are only restored during toDomain through lazy loading.
         *
         * @param domain The Account domain object to convert
         * @return The AccountEntity (structure only, without relationships)
         */
        override fun toEntity(domain: Account): AccountEntity =
            AccountEntity(
                id = domain.id,
                platform = domain.platform?.let { toPlatformEntity(it) },
                login = domain.login,
                name = domain.name,
                avatarUrl = domain.avatarUrl,
                url = domain.url,
            )

        /**
         * Converts an AccountEntity to Account domain object.
         *
         * Creates an Account with lazy-loaded relationships to issues, merge requests, and notes.
         * The relationships are loaded on-demand using proxy patterns to avoid N+1 query problems.
         *
         * @param entity The AccountEntity to convert
         * @return The Account domain object with lazy-loaded relationships
         */
        override fun toDomain(entity: AccountEntity): Account {
            // Fast-path: Check if already mapped
            ctx.findDomain<Account, AccountEntity>(entity)?.let { return it }

            val domain =
                Account(
                    id = entity.id,
                    platform = entity.platform?.let { toPlatform(it) },
                    login = entity.login,
                    name = entity.name,
                    avatarUrl = entity.avatarUrl,
                    url = entity.url,
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
                    notes =
                        proxyFactory.createLazyList {
                            (entity.notes ?: emptyList()).map { noteEntity ->
                                noteMapper.toDomain(noteEntity)
                            }
                        },
                )

            return domain
        }

    private fun toPlatformEntity(platform: Platform): PlatformEntity? {
        if (platform == Platform.GitHub) {
            return PlatformEntity.GitHub
        } else if (platform == Platform.GitLab) {
            return PlatformEntity.GitLab
        }
        return null
    }

    private fun toPlatform(platformEntity: PlatformEntity): Platform? {
        if (platformEntity == PlatformEntity.GitHub) {
            return Platform.GitHub
        } else if (platformEntity == PlatformEntity.GitLab) {
            return Platform.GitLab
        }
        return null
    }
    }
