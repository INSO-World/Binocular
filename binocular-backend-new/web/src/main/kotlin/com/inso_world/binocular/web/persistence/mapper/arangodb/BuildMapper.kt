package com.inso_world.binocular.web.persistence.mapper.arangodb

import com.inso_world.binocular.web.entity.Build
import com.inso_world.binocular.web.persistence.entity.arangodb.BuildEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("nosql", "arangodb")
class BuildMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    @Lazy private val commitMapper: CommitMapper
) : EntityMapper<Build, BuildEntity> {

    /**
     * Converts a domain Build to an ArangoDB BuildEntity
     */
    override fun toEntity(domain: Build): BuildEntity {
        return BuildEntity(
            id = domain.id,
            sha = domain.sha,
            ref = domain.ref,
            status = domain.status,
            tag = domain.tag,
            user = domain.user,
            userFullName = domain.userFullName,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            startedAt = domain.startedAt,
            finishedAt = domain.finishedAt,
            committedAt = domain.committedAt,
            duration = domain.duration,
            jobs = domain.jobs,
            webUrl = domain.webUrl,
            // Relationships are handled by ArangoDB through edges
            commits = null
        )
    }

    /**
     * Converts an ArangoDB BuildEntity to a domain Build
     * 
     * Uses lazy loading proxies for relationships, which will only be loaded
     * when accessed. This provides a consistent API regardless of the database
     * implementation and avoids the N+1 query problem.
     */
    override fun toDomain(entity: BuildEntity): Build {
        return Build(
            id = entity.id,
            sha = entity.sha,
            ref = entity.ref,
            status = entity.status,
            tag = entity.tag,
            user = entity.user,
            userFullName = entity.userFullName,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            startedAt = entity.startedAt,
            finishedAt = entity.finishedAt,
            committedAt = entity.committedAt,
            duration = entity.duration,
            jobs = entity.jobs,
            webUrl = entity.webUrl,
            commits = proxyFactory.createLazyList {
                (entity.commits ?: emptyList()).map { commitEntity -> 
                    commitMapper.toDomain(commitEntity) 
                } 
            }
        )
    }

    /**
     * Converts a list of ArangoDB BuildEntity objects to a list of domain Build objects
     */
    override fun toDomainList(entities: Iterable<BuildEntity>): List<Build> {
        return entities.map { toDomain(it) }
    }
}
