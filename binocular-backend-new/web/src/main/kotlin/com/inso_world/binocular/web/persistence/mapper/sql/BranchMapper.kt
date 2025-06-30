package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.persistence.entity.sql.BranchEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.FileMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.BranchFileConnectionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class BranchMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    private val branchFileConnectionRepository: BranchFileConnectionRepository,
    private val fileMapper: FileMapper
) : EntityMapper<Branch, BranchEntity> {

    /**
     * Converts a domain Branch to a SQL BranchEntity
     */
    override fun toEntity(domain: Branch): BranchEntity {
        return BranchEntity(
            id = domain.id,
            branch = domain.branch,
            active = domain.active,
            tracksFileRenames = domain.tracksFileRenames,
            latestCommit = domain.latestCommit
            // Note: Relationships are not directly mapped in SQL entity
        )
    }

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
            id = id,
            branch = entity.branch,
            active = entity.active,
            tracksFileRenames = entity.tracksFileRenames,
            latestCommit = entity.latestCommit,
            // Create lazy-loaded proxies for relationships that will load data from repositories when accessed
            files = proxyFactory.createLazyList { 
                branchFileConnectionRepository.findFilesByBranch(id).map { fileMapper.toDomain(it) } 
            }
        )
    }
}
