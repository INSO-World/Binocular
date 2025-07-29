package com.inso_world.binocular.infrastructure.sql.mapper

import com.inso_world.binocular.infrastructure.sql.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.toEntity
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
internal class UserMapper {
    private val logger: Logger = LoggerFactory.getLogger(UserMapper::class.java)

    @Autowired
    private lateinit var ctx: MappingContext

    @Autowired
    private lateinit var commitMapper: CommitMapper

    /**
     * Converts a domain User to a SQL UserEntity
     */
    fun toEntity(domain: User): UserEntity {
        val userContextKey = domain.uniqueKey()
        ctx.entity.user[userContextKey]?.let {
            logger.trace("toEntity: User-Cache hit: '$userContextKey'")
            return it
        }

        val entity = domain.toEntity()

        ctx.entity.user.computeIfAbsent(userContextKey) { entity }

        return entity
    }

    /**
     * Converts a SQL UserEntity to a domain User
     *
     * Uses lazy loading proxies for relationships, which will only be loaded
     * when accessed. This provides a consistent API regardless of the database
     * implementation and avoids the N+1 query problem.
     */
    fun toDomain(entity: UserEntity): User {
        val userContextKey = entity.uniqueKey()
        ctx.domain.user[userContextKey]?.let {
            logger.trace("toDomain: User-Cache hit: '$userContextKey'")
            return it
        }

        val domain = entity.toDomain()

        ctx.domain.user.computeIfAbsent(userContextKey) { domain }

        return domain
    }

    fun toDomainFull(
        entity: UserEntity,
        repository: Repository,
    ): User {
        val mappedDomain = toDomain(entity)

        mappedDomain.committedCommits.addAll(
            commitMapper.toDomainFull(entity.committedCommits, repository),
        )
        mappedDomain.authoredCommits.addAll(
            commitMapper.toDomainFull(entity.authoredCommits, repository),
        )

        return mappedDomain
    }
}
