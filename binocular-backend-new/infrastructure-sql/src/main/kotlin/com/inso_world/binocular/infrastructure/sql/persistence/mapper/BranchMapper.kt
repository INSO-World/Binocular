package com.inso_world.binocular.infrastructure.sql.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.model.Branch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
internal class BranchMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
//        @Lazy private val fileMapper: FileMapper,
    ) : EntityMapper<Branch, BranchEntity> {
        /**
         * Converts a domain Branch to a SQL BranchEntity
         */
        override fun toEntity(domain: Branch): BranchEntity =
            BranchEntity(
//                id = domain.id,
                name = domain.name,
//                active = domain.active,
//                tracksFileRenames = domain.tracksFileRenames,
//                latestCommit = domain.latestCommit,
                // Note: Relationships are not directly mapped in SQL entity
            )

        /**
         * Converts a SQL BranchEntity to a domain Branch
         *
         * Uses lazy loading proxies for relationships, which will only be loaded
         * when accessed. This provides a consistent API regardless of the database
         * implementation and avoids the N+1 query problem.
         */
        @Transactional(readOnly = true)
        override fun toDomain(entity: BranchEntity): Branch {
            val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")

            return Branch(
//                id = id,
                name = entity.name,
//                active = entity.active,
//                tracksFileRenames = entity.tracksFileRenames,
//                latestCommit = entity.latestCommit,
                // Use direct entity relationships and map them to domain objects using the createLazyMappedList method
//                files =
//                    proxyFactory.createLazyMappedList(
//                        { entity.files },
//                        { fileMapper.toDomain(it) },
//                    ),
            )
        }
    }
