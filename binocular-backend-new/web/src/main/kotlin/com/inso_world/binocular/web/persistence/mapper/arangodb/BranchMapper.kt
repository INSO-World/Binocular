package com.inso_world.binocular.web.persistence.mapper.arangodb

import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.persistence.entity.arangodb.BranchEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.FileEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("nosql", "arangodb")
class BranchMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    @Lazy private val fileMapper: FileMapper
) : EntityMapper<Branch, BranchEntity> {

    /**
     * Converts a domain Branch to an ArangoDB BranchEntity
     */
    override fun toEntity(domain: Branch): BranchEntity {
        return BranchEntity(
            id = domain.id,
            branch = domain.branch,
            active = domain.active,
            tracksFileRenames = domain.tracksFileRenames,
            latestCommit = domain.latestCommit,
            // Relationships are handled by ArangoDB through edges
            files = null
        )
    }

    /**
     * Converts an ArangoDB BranchEntity to a domain Branch
     * 
     * Uses lazy loading proxies for relationships, which will only be loaded
     * when accessed. This provides a consistent API regardless of the database
     * implementation and avoids the N+1 query problem.
     */
    override fun toDomain(entity: BranchEntity): Branch {
        return Branch(
            id = entity.id,
            branch = entity.branch,
            active = entity.active,
            tracksFileRenames = entity.tracksFileRenames,
            latestCommit = entity.latestCommit,
            files = proxyFactory.createLazyList {
                (entity.files ?: emptyList()).map { fileEntity -> 
                    fileMapper.toDomain(fileEntity) 
                } 
            }
        )
    }

    /**
     * Converts a list of ArangoDB BranchEntity objects to a list of domain Branch objects
     */
    override fun toDomainList(entities: Iterable<BranchEntity>): List<Branch> {
        return entities.map { toDomain(it) }
    }
}
