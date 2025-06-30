package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.persistence.entity.sql.UserEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class UserMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    @Lazy private val commitMapper: CommitMapper,
    @Lazy private val issueMapper: IssueMapper,
    @Lazy private val fileMapper: FileMapper
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
            // Use direct entity relationships and map them to domain objects using the createLazyMappedList method
            commits = proxyFactory.createLazyMappedList(
                { entity.commits },
                { commitMapper.toDomain(it) }
            ),
            issues = proxyFactory.createLazyMappedList(
                { entity.issues },
                { issueMapper.toDomain(it) }
            ),
            files = proxyFactory.createLazyMappedList(
                { entity.commitFileConnections.mapNotNull { it.file } },
                { fileMapper.toDomain(it) }
            )
        )
    }

    /**
     * Converts a list of SQL UserEntity objects to a list of domain User objects
     */
    override fun toDomainList(entities: Iterable<UserEntity>): List<User> {
        return entities.map { toDomain(it) }
    }
}
