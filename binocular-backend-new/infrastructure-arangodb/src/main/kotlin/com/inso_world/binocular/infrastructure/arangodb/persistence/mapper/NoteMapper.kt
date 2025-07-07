package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.NoteEntity
import com.inso_world.binocular.model.Note
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class NoteMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
        @Lazy private val accountMapper: AccountMapper,
        @Lazy private val issueMapper: IssueMapper,
        @Lazy private val mergeRequestMapper: MergeRequestMapper,
    ) : EntityMapper<Note, NoteEntity> {
        /**
         * Converts a domain Note to an ArangoDB NoteEntity
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
                // Relationships are handled by ArangoDB through edges
            )

        /**
         * Converts an ArangoDB NoteEntity to a domain Note
         *
         * Uses lazy loading proxies for relationships, which will only be loaded
         * when accessed. This provides a consistent API regardless of the database
         * implementation and avoids the N+1 query problem.
         */
        override fun toDomain(entity: NoteEntity): Note =
            Note(
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

        /**
         * Converts a list of ArangoDB NoteEntity objects to a list of domain Note objects
         */
        override fun toDomainList(entities: Iterable<NoteEntity>): List<Note> = entities.map { toDomain(it) }
    }
