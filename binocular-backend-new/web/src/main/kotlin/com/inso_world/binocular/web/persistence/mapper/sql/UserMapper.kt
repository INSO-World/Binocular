package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.persistence.entity.sql.UserEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.CommitMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.FileMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.IssueMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitFileUserConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitUserConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueUserConnectionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class UserMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    private val commitUserConnectionRepository: CommitUserConnectionRepository,
    private val issueUserConnectionRepository: IssueUserConnectionRepository,
    private val commitFileUserConnectionRepository: CommitFileUserConnectionRepository,
    private val commitMapper: CommitMapper,
    private val issueMapper: IssueMapper,
    private val fileMapper: FileMapper
) : EntityMapper<User, UserEntity> {

    /**
     * Converts a domain User to a SQL UserEntity
     */
    override fun toEntity(domain: User): UserEntity {
        return UserEntity(
            id = domain.id,
            gitSignature = domain.gitSignature
            // Note: Relationships are not directly mapped in SQL entity
        )
    }

    /**
     * Converts a SQL UserEntity to a domain User
     * 
     * Uses lazy loading proxies for relationships, which will only be loaded
     * when accessed. This provides a consistent API regardless of the database
     * implementation and avoids the N+1 query problem.
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: UserEntity): User {
        val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")

        return User(
            id = id,
            gitSignature = entity.gitSignature,
            // Create lazy-loaded proxies for relationships that will load data from repositories when accessed
            commits = proxyFactory.createLazyList { 
                commitUserConnectionRepository.findCommitsByUser(id).map { commitMapper.toDomain(it) } 
            },
            issues = proxyFactory.createLazyList { 
                issueUserConnectionRepository.findIssuesByUser(id).map { issueMapper.toDomain(it) } 
            },
            files = proxyFactory.createLazyList { 
                commitFileUserConnectionRepository.findCommitFilesByUser(id).map { fileMapper.toDomain(it) } 
            }
        )
    }

    /**
     * Converts a list of SQL UserEntity objects to a list of domain User objects
     */
    override fun toDomainList(entities: Iterable<UserEntity>): List<User> {
        return entities.map { toDomain(it) }
    }
}
