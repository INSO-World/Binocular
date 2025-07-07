package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.BuildEntity
import com.inso_world.binocular.model.Build
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.util.Date

@Component
class BuildMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
    ) : EntityMapper<Build, BuildEntity> {
        @Lazy @Autowired
        private lateinit var commitMapper: CommitMapper

        /**
         * Converts a domain Build to an ArangoDB BuildEntity
         */
        override fun toEntity(domain: Build): BuildEntity =
            BuildEntity(
                id = domain.id,
                sha = domain.sha,
                ref = domain.ref,
                status = domain.status,
                tag = domain.tag,
                user = domain.user,
                userFullName = domain.userFullName,
                createdAt = domain.createdAt?.let { Date.from(it.toInstant(ZoneOffset.UTC)) },
                updatedAt = domain.updatedAt?.let { Date.from(it.toInstant(ZoneOffset.UTC)) },
                startedAt = domain.startedAt?.let { Date.from(it.toInstant(ZoneOffset.UTC)) },
                finishedAt = domain.finishedAt?.let { Date.from(it.toInstant(ZoneOffset.UTC)) },
                committedAt = domain.committedAt?.let { Date.from(it.toInstant(ZoneOffset.UTC)) },
                duration = domain.duration,
                jobs = domain.jobs,
                webUrl = domain.webUrl,
                // Relationships are handled by ArangoDB through edges
            )

        /**
         * Converts an ArangoDB BuildEntity to a domain Build
         *
         * Uses lazy loading proxies for relationships, which will only be loaded
         * when accessed. This provides a consistent API regardless of the database
         * implementation and avoids the N+1 query problem.
         */
        override fun toDomain(entity: BuildEntity): Build =
            Build(
                id = entity.id,
                sha = entity.sha,
                ref = entity.ref,
                status = entity.status,
                tag = entity.tag,
                user = entity.user,
                userFullName = entity.userFullName,
                createdAt =
                    entity.createdAt
                        ?.toInstant()
                        ?.atZone(ZoneOffset.UTC)
                        ?.toLocalDateTime(),
                updatedAt =
                    entity.updatedAt
                        ?.toInstant()
                        ?.atZone(ZoneOffset.UTC)
                        ?.toLocalDateTime(),
                startedAt =
                    entity.startedAt
                        ?.toInstant()
                        ?.atZone(ZoneOffset.UTC)
                        ?.toLocalDateTime(),
                finishedAt =
                    entity.finishedAt
                        ?.toInstant()
                        ?.atZone(ZoneOffset.UTC)
                        ?.toLocalDateTime(),
                committedAt =
                    entity.committedAt
                        ?.toInstant()
                        ?.atZone(ZoneOffset.UTC)
                        ?.toLocalDateTime(),
                duration = entity.duration,
                jobs = entity.jobs,
                webUrl = entity.webUrl,
                commits =
                    proxyFactory.createLazyList {
                        (entity.commits ?: emptyList()).map { commitEntity ->
                            commitMapper.toDomain(commitEntity)
                        }
                    },
            )

        /**
         * Converts a list of ArangoDB BuildEntity objects to a list of domain Build objects
         */
        override fun toDomainList(entities: Iterable<BuildEntity>): List<Build> = entities.map { toDomain(it) }
    }
