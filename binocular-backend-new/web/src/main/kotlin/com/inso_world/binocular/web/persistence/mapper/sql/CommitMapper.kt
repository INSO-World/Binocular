package com.inso_world.binocular.web.persistence.mapper.sql

import com.fasterxml.jackson.databind.ObjectMapper
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.persistence.entity.sql.CommitEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.BuildMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.CommitMapper as ArangoCommitMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.FileMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.IssueMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.ModuleMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.UserMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class CommitMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    private val objectMapper: ObjectMapper,
    private val commitCommitConnectionRepository: CommitCommitConnectionRepository,
    private val commitBuildConnectionRepository: CommitBuildConnectionRepository,
    private val commitFileConnectionRepository: CommitFileConnectionRepository,
    private val commitModuleConnectionRepository: CommitModuleConnectionRepository,
    private val commitUserConnectionRepository: CommitUserConnectionRepository,
    private val issueCommitConnectionRepository: IssueCommitConnectionRepository,
    private val arangoCommitMapper: ArangoCommitMapper,
    private val buildMapper: BuildMapper,
    private val fileMapper: FileMapper,
    private val moduleMapper: ModuleMapper,
    private val userMapper: UserMapper,
    private val issueMapper: IssueMapper
) : EntityMapper<Commit, CommitEntity> {

    /**
     * Converts a domain Commit to a SQL CommitEntity
     */
    override fun toEntity(domain: Commit): CommitEntity {
        val entity = CommitEntity(
            id = domain.id,
            sha = domain.sha,
            date = domain.date,
            message = domain.message,
            webUrl = domain.webUrl,
            branch = domain.branch
            // Note: Relationships are not directly mapped in SQL entity
        )

        // Handle stats separately using the helper method
        entity.setStats(domain.stats, objectMapper)

        return entity
    }

    /**
     * Converts a SQL CommitEntity to a domain Commit
     * 
     * Uses lazy loading proxies for relationships, which will only be loaded
     * when accessed. This provides a consistent API regardless of the database
     * implementation and avoids the N+1 query problem.
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: CommitEntity): Commit {
        val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")

        return Commit(
            id = id,
            sha = entity.sha,
            date = entity.date,
            message = entity.message,
            webUrl = entity.webUrl,
            branch = entity.branch,
            stats = entity.getStats(objectMapper),
            // Create lazy-loaded proxies for relationships that will load data from repositories when accessed
            parents = proxyFactory.createLazyList { 
                commitCommitConnectionRepository.findParentCommitsByChildCommit(id).map { arangoCommitMapper.toDomain(it) } 
            },
            children = proxyFactory.createLazyList { 
                commitCommitConnectionRepository.findChildCommitsByParentCommit(id).map { arangoCommitMapper.toDomain(it) } 
            },
            builds = proxyFactory.createLazyList { 
                commitBuildConnectionRepository.findBuildsByCommit(id).map { buildMapper.toDomain(it) } 
            },
            files = proxyFactory.createLazyList { 
                commitFileConnectionRepository.findFilesByCommit(id).map { fileMapper.toDomain(it) } 
            },
            modules = proxyFactory.createLazyList { 
                commitModuleConnectionRepository.findModulesByCommit(id).map { moduleMapper.toDomain(it) } 
            },
            users = proxyFactory.createLazyList { 
                commitUserConnectionRepository.findUsersByCommit(id).map { userMapper.toDomain(it) } 
            },
            issues = proxyFactory.createLazyList { 
                issueCommitConnectionRepository.findIssuesByCommit(id).map { issueMapper.toDomain(it) } 
            }
        )
    }
}
