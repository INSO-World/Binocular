// package com.inso_world.binocular.infrastructure.sql.persistence.mapper
//
// import com.inso_world.binocular.core.persistence.mapper.EntityMapper
// import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
// import com.inso_world.binocular.infrastructure.sql.persistence.entity.MergeRequestEntity
// import com.inso_world.binocular.model.MergeRequest
// import org.springframework.beans.factory.annotation.Autowired
// import org.springframework.context.annotation.Lazy
// import org.springframework.context.annotation.Profile
// import org.springframework.stereotype.Component
// import org.springframework.transaction.annotation.Transactional
//
// @Component
// class MergeRequestMapper
//    @Autowired
//    constructor(
//        private val proxyFactory: RelationshipProxyFactory,
//        @Lazy private val accountMapper: AccountMapper,
//        @Lazy private val milestoneMapper: MilestoneMapper,
//        @Lazy private val noteMapper: NoteMapper,
//    ) : EntityMapper<MergeRequest, MergeRequestEntity> {
//        /**
//         * Converts a domain MergeRequest to a SQL MergeRequestEntity
//         */
//        override fun toEntity(domain: MergeRequest): MergeRequestEntity {
//            val entity =
//                MergeRequestEntity(
//                    id = domain.id,
//                    iid = domain.iid,
//                    title = domain.title,
//                    description = domain.description,
//                    createdAt = domain.createdAt,
//                    closedAt = domain.closedAt,
//                    updatedAt = domain.updatedAt,
//                    labels = domain.labels,
//                    state = domain.state,
//                    webUrl = domain.webUrl,
//                    // Note: Relationships are not directly mapped in SQL entity
//                )
//
//            // Set mentions
//            entity.setDomainMentions(domain.mentions)
//
//            return entity
//        }
//
//        /**
//         * Converts a SQL MergeRequestEntity to a domain MergeRequest
//         *
//         * Uses lazy loading proxies for relationships, which will only be loaded
//         * when accessed. This provides a consistent API regardless of the database
//         * implementation and avoids the N+1 query problem.
//         */
//        @Transactional(readOnly = true)
//        override fun toDomain(entity: MergeRequestEntity): MergeRequest {
//            val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")
//
//            // Get mentions from entity
//            val mentions = entity.getDomainMentions()
//
//            return MergeRequest(
//                id = id,
//                iid = entity.iid,
//                title = entity.title,
//                description = entity.description,
//                createdAt = entity.createdAt,
//                closedAt = entity.closedAt,
//                updatedAt = entity.updatedAt,
//                labels = entity.labels,
//                state = entity.state,
//                webUrl = entity.webUrl,
//                mentions = mentions,
//                // Use direct entity relationships and map them to domain objects using the new createLazyMappedList method
//                accounts =
//                    proxyFactory.createLazyMappedList(
//                        { entity.accounts },
//                        { accountMapper.toDomain(it) },
//                    ),
//                milestones =
//                    proxyFactory.createLazyMappedList(
//                        { entity.milestones },
//                        { milestoneMapper.toDomain(it) },
//                    ),
//                notes =
//                    proxyFactory.createLazyMappedList(
//                        { entity.notes },
//                        { noteMapper.toDomain(it) },
//                    ),
//            )
//        }
//
//        /**
//         * Converts a list of SQL MergeRequestEntity objects to a list of domain MergeRequest objects
//         */
//        override fun toDomainList(entities: Iterable<MergeRequestEntity>): List<MergeRequest> = entities.map { toDomain(it) }
//    }
