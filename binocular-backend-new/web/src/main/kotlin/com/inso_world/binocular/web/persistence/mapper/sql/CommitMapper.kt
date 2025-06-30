package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.persistence.entity.sql.CommitEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class CommitMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    @Lazy private val buildMapper: BuildMapper,
    @Lazy private val fileMapper: FileMapper,
    @Lazy private val moduleMapper: ModuleMapper,
    @Lazy private val userMapper: UserMapper,
    @Lazy private val issueMapper: IssueMapper
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
        entity.setStats(domain.stats)

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
            stats = entity.getStats(),
            // Use direct entity relationships and map them to domain objects using the new createLazyMappedList method
            parents = proxyFactory.createLazyMappedList(
                { entity.parentCommits },
                { toDomain(it) }
            ),
            children = proxyFactory.createLazyMappedList(
                { entity.childCommits },
                { toDomain(it) }
            ),
            builds = proxyFactory.createLazyMappedList(
                { entity.builds },
                { buildMapper.toDomain(it) }
            ),
            files = proxyFactory.createLazyMappedList(
                { entity.files },
                { fileMapper.toDomain(it) }
            ),
            modules = proxyFactory.createLazyMappedList(
                { entity.modules },
                { moduleMapper.toDomain(it) }
            ),
            users = proxyFactory.createLazyMappedList(
                { entity.users },
                { userMapper.toDomain(it) }
            ),
            issues = proxyFactory.createLazyMappedList(
                { entity.issues },
                { issueMapper.toDomain(it) }
            )
        )
    }
}
