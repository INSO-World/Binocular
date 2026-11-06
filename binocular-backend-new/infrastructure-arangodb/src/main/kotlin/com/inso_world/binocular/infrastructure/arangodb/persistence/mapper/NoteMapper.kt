package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.NoteEntity
import com.inso_world.binocular.model.Note
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

/**
 * Mapper for Note domain objects.
 *
 * Converts between Note domain objects and NoteEntity persistence entities for ArangoDB.
 * This mapper handles the conversion of note metadata and uses lazy loading for related
 * accounts, issues, and merge requests.
 *
 * ## Design Principles
 * - **Single Responsibility**: Only converts Note structure
 * - **Lazy Loading**: Uses RelationshipProxyFactory for lazy-loaded relationships
 * - **Context Management**: Uses MappingContext to prevent duplicate mappings
 *
 * ## Usage
 * This mapper is typically called by infrastructure ports and assemblers. It uses lazy loading
 * for accounts, issues, and merge requests to optimize performance.
 */
@Component
internal class NoteMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
        @Lazy private val accountMapper: AccountMapper,
        @Lazy private val issueMapper: IssueMapper,
        @Lazy private val mergeRequestMapper: MergeRequestMapper,
    ) : EntityMapper<Note, NoteEntity> {

        @Autowired
        private lateinit var ctx: MappingContext

        companion object {
            private val logger by logger()
        }

        /**
         * Converts a Note domain object to NoteEntity.
         *
         * Maps all note properties including body, timestamps, flags (system, resolvable, etc.),
         * and import metadata. Relationships to accounts, issues, and merge requests are not
         * persisted in the entity - they are only restored during toDomain through lazy loading.
         *
         * @param domain The Note domain object to convert
         * @return The NoteEntity with note metadata
         */
        override fun toEntity(domain: Note): NoteEntity =
            NoteEntity(
                id = domain.id,
                body = domain.body,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt,
                system = domain.system,
                resolvable = domain.resolvable,
                confidential = domain.confidential,
                internal = domain.internal,
                imported = domain.imported,
                importedFrom = domain.importedFrom,
            )

        /**
         * Converts a NoteEntity to Note domain object.
         *
         * Creates lazy-loaded proxies for accounts, issues, and merge requests to avoid loading
         * unnecessary data when only note metadata is needed.
         *
         * @param entity The NoteEntity to convert
         * @return The Note domain object with lazy relationships
         */
        override fun toDomain(entity: NoteEntity): Note {
            // Fast-path: Check if already mapped
            ctx.findDomain<Note, NoteEntity>(entity)?.let { return it }

            return Note(
                id = entity.id,
                body = entity.body,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                system = entity.system,
                resolvable = entity.resolvable,
                confidential = entity.confidential,
                internal = entity.internal,
                imported = entity.imported,
                importedFrom = entity.importedFrom,
                accounts =
                    proxyFactory.createLazyList {
                        (entity.accounts ?: emptyList()).map { accountEntity ->
                            accountMapper.toDomain(accountEntity)
                        }
                    },
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

        override fun toDomainList(entities: Iterable<NoteEntity>): List<Note> = entities.map { toDomain(it) }
    }
