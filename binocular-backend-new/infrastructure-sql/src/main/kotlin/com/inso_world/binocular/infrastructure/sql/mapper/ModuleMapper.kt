// package com.inso_world.binocular.infrastructure.sql.persistence.mapper
//
// import com.inso_world.binocular.core.persistence.mapper.EntityMapper
// import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
// import com.inso_world.binocular.infrastructure.sql.persistence.entity.ModuleEntity
// import org.springframework.beans.factory.annotation.Autowired
// import org.springframework.context.annotation.Lazy
// import org.springframework.context.annotation.Profile
// import org.springframework.stereotype.Component
// import org.springframework.transaction.annotation.Transactional
//
// @Component
// class ModuleMapper
//    @Autowired
//    constructor(
//        private val proxyFactory: RelationshipProxyFactory,
//        @Lazy private val commitMapper: CommitMapper,
//        @Lazy private val fileMapper: FileMapper,
//    ) : EntityMapper<com.inso_world.binocular.model.Module, ModuleEntity> {
//        /**
//         * Converts a domain Module to a SQL ModuleEntity
//         */
//        override fun toEntity(domain: com.inso_world.binocular.model.Module): ModuleEntity =
//            ModuleEntity(
//                id = domain.id,
//                path = domain.path,
//                // Note: Relationships are not directly mapped in SQL entity
//            )
//
//        /**
//         * Converts a SQL ModuleEntity to a domain Module
//         *
//         * Uses lazy loading proxies for relationships, which will only be loaded
//         * when accessed. This provides a consistent API regardless of the database
//         * implementation and avoids the N+1 query problem.
//         */
//        @Transactional(readOnly = true)
//        override fun toDomain(entity: ModuleEntity): com.inso_world.binocular.model.Module {
//            val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")
//
//            return com.inso_world.binocular.model.Module(
//                id = id,
//                path = entity.path,
//                // Use direct entity relationships and map them to domain objects using the createLazyMappedList method
//                commits =
//                    proxyFactory.createLazyMappedList(
//                        { entity.commits },
//                        { commitMapper.toDomain(it) },
//                    ),
//                files =
//                    proxyFactory.createLazyMappedList(
//                        { entity.files },
//                        { fileMapper.toDomain(it) },
//                    ),
//                childModules =
//                    proxyFactory.createLazyMappedList(
//                        { entity.childModules },
//                        { toDomain(it) },
//                    ),
//                parentModules =
//                    proxyFactory.createLazyMappedList(
//                        { entity.parentModules },
//                        { toDomain(it) },
//                    ),
//            )
//        }
//
//        /**
//         * Converts a list of SQL ModuleEntity objects to a list of domain Module objects
//         */
//        override fun toDomainList(entities: Iterable<ModuleEntity>): List<com.inso_world.binocular.model.Module> =
//            entities.map { toDomain(it) }
//    }
