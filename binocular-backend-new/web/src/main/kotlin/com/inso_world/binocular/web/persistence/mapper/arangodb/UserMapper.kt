package com.inso_world.binocular.web.persistence.mapper.arangodb

import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.persistence.entity.arangodb.UserEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("nosql", "arangodb")
class UserMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    @Lazy private val commitMapper: CommitMapper,
    @Lazy private val issueMapper: IssueMapper,
    @Lazy private val fileMapper: FileMapper
) : EntityMapper<User, UserEntity> {

    /**
     * Converts a domain User to an ArangoDB UserEntity
     */
    override fun toEntity(domain: User): UserEntity {
        return UserEntity(
            id = domain.id,
            gitSignature = domain.gitSignature,
            // Relationships are handled by ArangoDB through edges
            commits = null,
            issues = null,
            files = null
        )
    }

    /**
     * Converts an ArangoDB UserEntity to a domain User
     * 
     * Uses lazy loading proxies for relationships, which will only be loaded
     * when accessed. This provides a consistent API regardless of the database
     * implementation and avoids the N+1 query problem.
     */
    override fun toDomain(entity: UserEntity): User {
        return User(
            id = entity.id,
            gitSignature = entity.gitSignature,
            commits = proxyFactory.createLazyList {
                (entity.commits ?: emptyList()).map { commitEntity -> 
                    commitMapper.toDomain(commitEntity) 
                } 
            },
            issues = proxyFactory.createLazyList { 
                (entity.issues ?: emptyList()).map { issueEntity -> 
                    issueMapper.toDomain(issueEntity) 
                } 
            },
            files = proxyFactory.createLazyList { 
                (entity.files ?: emptyList()).map { fileEntity -> 
                    fileMapper.toDomain(fileEntity) 
                } 
            }
        )
    }

    /**
     * Converts a list of ArangoDB UserEntity objects to a list of domain User objects
     */
    override fun toDomainList(entities: Iterable<UserEntity>): List<User> {
        return entities.map { toDomain(it) }
    }
}
