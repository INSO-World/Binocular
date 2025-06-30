package com.inso_world.binocular.web.persistence.mapper.sql

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.inso_world.binocular.web.entity.Build
import com.inso_world.binocular.web.persistence.entity.sql.BuildEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.CommitMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitBuildConnectionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class BuildMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    private val commitBuildConnectionRepository: CommitBuildConnectionRepository,
    private val objectMapper: ObjectMapper,
    private val commitMapper: CommitMapper
) : EntityMapper<Build, BuildEntity> {

    /**
     * Converts a domain Build to a SQL BuildEntity
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
            webUrl = domain.webUrl,
            // Convert jobs list to JSON
            jobsJson = if (domain.jobs != null) objectMapper.writeValueAsString(domain.jobs) else null
            // Note: Relationships are not directly mapped in SQL entity
        )
    }

    /**
     * Converts a SQL BuildEntity to a domain Build
     * 
     * Uses lazy loading proxies for relationships, which will only be loaded
     * when accessed. This provides a consistent API regardless of the database
     * implementation and avoids the N+1 query problem.
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: BuildEntity): Build {
        val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")

        // Convert JSON to jobs list
        val jobs = if (entity.jobsJson != null) {
            objectMapper.readValue(entity.jobsJson, object : TypeReference<List<Build.Job>>() {})
        } else {
            null
        }

        return Build(
            id = id,
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
            jobs = jobs,
            webUrl = entity.webUrl,
            // Create lazy-loaded proxies for relationships that will load data from repositories when accessed
            commits = proxyFactory.createLazyList { 
                commitBuildConnectionRepository.findCommitsByBuild(id).map { commitMapper.toDomain(it) } 
            }
        )
    }
}
