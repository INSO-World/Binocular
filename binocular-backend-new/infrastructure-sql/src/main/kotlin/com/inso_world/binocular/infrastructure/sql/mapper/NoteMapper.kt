// package com.inso_world.binocular.infrastructure.sql.persistence.mapper
//
// import com.inso_world.binocular.core.persistence.mapper.EntityMapper
// import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
// import com.inso_world.binocular.infrastructure.sql.persistence.entity.NoteEntity
// import com.inso_world.binocular.model.Note
// import org.springframework.beans.factory.annotation.Autowired
// import org.springframework.context.annotation.Lazy
// import org.springframework.context.annotation.Profile
// import org.springframework.stereotype.Component
// import org.springframework.transaction.annotation.Transactional
//
// @Component
// class NoteMapper
//    @Autowired
//    constructor(
//        private val proxyFactory: RelationshipProxyFactory,
//        @Lazy private val accountMapper: AccountMapper,
//        @Lazy private val issueMapper: IssueMapper,
//        @Lazy private val mergeRequestMapper: MergeRequestMapper,
//    ) : EntityMapper<Note, NoteEntity> {
//        /**
//         * Converts a domain Note to a SQL NoteEntity
//         */
//        override fun toEntity(domain: Note): NoteEntity =
//            NoteEntity(
//                id = domain.id,
//                body = domain.body,
//                createdAt = domain.createdAt,
//                updatedAt = domain.updatedAt,
//                system = domain.system,
//                resolvable = domain.resolvable,
//                confidential = domain.confidential,
//                = domain.internal,
//                imported = domain.imported,
//                importedFrom = domain.importedFrom,
//                // Note: Relationships are not directly mapped in SQL entity
//            )
//
//        /**
//         * Converts a SQL NoteEntity to a domain Note
//         *
//         * Uses lazy loading proxies for relationships, which will only be loaded
//         * when accessed. This provides a consistent API regardless of the database
//         * implementation and avoids the N+1 query problem.
//         */
//        @Transactional(readOnly = true)
//        override fun toDomain(entity: NoteEntity): Note {
//            val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")
//
//            return Note(
//                id = id,
//                body = entity.body,
//                createdAt = entity.createdAt,
//                updatedAt = entity.updatedAt,
//                system = entity.system,
//                resolvable = entity.resolvable,
//                confidential = entity.confidential,
//                = entity.internal,
//                imported = entity.imported,
//                importedFrom = entity.importedFrom,
//                // Use direct entity relationships and map them to domain objects using the createLazyMappedList method
//                accounts =
//                    proxyFactory.createLazyMappedList(
//                        { entity.accounts },
//                        { accountMapper.toDomain(it) },
//                    ),
//                issues =
//                    proxyFactory.createLazyMappedList(
//                        { entity.issues },
//                        { issueMapper.toDomain(it) },
//                    ),
//                mergeRequests =
//                    proxyFactory.createLazyMappedList(
//                        { entity.mergeRequests },
//                        { mergeRequestMapper.toDomain(it) },
//                    ),
//            )
//        }
//
//        /**
//         * Converts a list of SQL NoteEntity objects to a list of domain Note objects
//         */
//        override fun toDomainList(entities: Iterable<NoteEntity>): List<Note> = entities.map { toDomain(it) }
//    }
