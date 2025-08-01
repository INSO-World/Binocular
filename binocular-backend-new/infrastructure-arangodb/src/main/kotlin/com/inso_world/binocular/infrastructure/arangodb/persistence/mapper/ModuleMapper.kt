package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.ModuleEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class ModuleMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
        @Lazy private val fileMapper: FileMapper,
    ) : EntityMapper<com.inso_world.binocular.model.Module, ModuleEntity> {
        @Lazy @Autowired
        private lateinit var commitMapper: CommitMapper

        /**
         * Converts a domain Module to an ArangoDB ModuleEntity
         */
        override fun toEntity(domain: com.inso_world.binocular.model.Module): ModuleEntity =
            ModuleEntity(
                id = domain.id,
                path = domain.path,
                // Relationships are handled by ArangoDB through edges
            )

        /**
         * Converts an ArangoDB ModuleEntity to a domain Module
         *
         * Uses lazy loading proxies for relationships, which will only be loaded
         * when accessed. This provides a consistent API regardless of the database
         * implementation and avoids the N+1 query problem.
         */
        override fun toDomain(entity: ModuleEntity): com.inso_world.binocular.model.Module =
            com.inso_world.binocular.model.Module(
                id = entity.id,
                path = entity.path,
                commits =
                    proxyFactory.createLazyList {
                        (entity.commits ?: emptyList()).map { commitEntity ->
                            commitMapper.toDomain(commitEntity)
                        }
                    },
                files =
                    proxyFactory.createLazyList {
                        (entity.files ?: emptyList()).map { fileEntity ->
                            fileMapper.toDomain(fileEntity)
                        }
                    },
                childModules =
                    proxyFactory.createLazyList {
                        (entity.childModules ?: emptyList()).map { childModuleEntity ->
                            toDomain(childModuleEntity)
                        }
                    },
                parentModules =
                    proxyFactory.createLazyList {
                        (entity.parentModules ?: emptyList()).map { parentModuleEntity ->
                            toDomain(parentModuleEntity)
                        }
                    },
            )

        /**
         * Converts a list of ArangoDB ModuleEntity objects to a list of domain Module objects
         */
        override fun toDomainList(entities: Iterable<ModuleEntity>): List<com.inso_world.binocular.model.Module> =
            entities.map { toDomain(it) }
    }
