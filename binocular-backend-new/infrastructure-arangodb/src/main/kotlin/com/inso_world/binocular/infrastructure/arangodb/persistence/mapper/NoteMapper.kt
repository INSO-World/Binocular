package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.NoteEntity
import com.inso_world.binocular.model.Note
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

/**
 * Mapper for Note domain objects.
 *
 * Converts between Note domain objects and NoteEntity persistence entities for ArangoDB.
 * Relationship collections (accounts, issues, mergeRequests) are always empty after toDomain —
 * Spring Data ArangoDB's @Relations AQL generation does not backtick-quote hyphenated collection
 * names, causing syntax errors. Resolvers load relationships via hand-written AQL instead.
 *
 * ## Design Principles
 * - **Single Responsibility**: Only converts Note structure
 * - **Context Management**: Uses MappingContext to prevent duplicate mappings
 */
@Component
internal class NoteMapper
    @Autowired
    constructor(
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
         * persisted in the entity - relationship fields on NoteEntity are not loaded in toDomain.
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
                accounts = domain.accounts.map { accountMapper.toEntity(it) }.toSet(),
                issues = domain.issues.map { issueMapper.toEntity(it) }.toSet(),
                mergeRequests = domain.mergeRequests.map { mergeRequestMapper.toEntity(it) }.toSet(),
            )

        /**
         * Converts a NoteEntity to Note domain object.
         *
         * Relationships (issues, mergeRequests, accounts) are intentionally left empty:
         * Spring Data ArangoDB's auto-generated AQL for @Relations graph traversal does not
         * backtick-quote hyphenated collection names (e.g. `issues-notes`, `notes-accounts`),
         * producing a syntax error at runtime. Resolvers load these via separate hand-written AQL.
         *
         * @param entity The NoteEntity to convert
         * @return The Note domain object; relationship collections are empty
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
            )
        }

        override fun toDomainList(entities: Iterable<NoteEntity>): List<Note> = entities.map { toDomain(it) }
    }
