package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.BranchEntity
import com.inso_world.binocular.model.Branch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class BranchMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
        @Lazy private val fileMapper: FileMapper,
    ) : EntityMapper<Branch, BranchEntity> {
        /**
         * Converts a domain Branch to an ArangoDB BranchEntity
         */
        override fun toEntity(domain: Branch): BranchEntity =
            BranchEntity(
                id = domain.id,
                branch = domain.name,
                active = domain.active,
                tracksFileRenames = domain.tracksFileRenames,
                latestCommit = domain.latestCommit,
                // Relationships are handled by ArangoDB through edges
            )

        /**
         * Converts an ArangoDB BranchEntity to a domain Branch
         *
         * Uses lazy loading proxies for relationships, which will only be loaded
         * when accessed. This provides a consistent API regardless of the database
         * implementation and avoids the N+1 query problem.
         */
        override fun toDomain(entity: BranchEntity): Branch =
            Branch(
                id = entity.id,
                name = entity.branch ?: "",
                active = entity.active,
                tracksFileRenames = entity.tracksFileRenames,
                latestCommit = entity.latestCommit,
                files =
                    proxyFactory.createLazyList {
                        (entity.files).map { fileEntity ->
                            fileMapper.toDomain(fileEntity)
                        }
                    },
            )

        /**
         * Converts a list of ArangoDB BranchEntity objects to a list of domain Branch objects
         */
        override fun toDomainList(entities: Iterable<BranchEntity>): List<Branch> = entities.map { toDomain(it) }
    }
