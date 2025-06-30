package com.inso_world.binocular.web.persistence.mapper.sql

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Mention
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestAccountConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestMilestoneConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestNoteConnectionDao
import com.inso_world.binocular.web.persistence.entity.sql.MergeRequestEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class MergeRequestMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    private val mergeRequestAccountConnectionDao: IMergeRequestAccountConnectionDao,
    private val mergeRequestMilestoneConnectionDao: IMergeRequestMilestoneConnectionDao,
    private val mergeRequestNoteConnectionDao: IMergeRequestNoteConnectionDao,
    private val objectMapper: ObjectMapper
) : EntityMapper<MergeRequest, MergeRequestEntity> {

    /**
     * Converts a domain MergeRequest to a SQL MergeRequestEntity
     */
    override fun toEntity(domain: MergeRequest): MergeRequestEntity {
        return MergeRequestEntity(
            id = domain.id,
            iid = domain.iid,
            title = domain.title,
            description = domain.description,
            createdAt = domain.createdAt,
            closedAt = domain.closedAt,
            updatedAt = domain.updatedAt,
            labels = domain.labels,
            state = domain.state,
            webUrl = domain.webUrl,
            mentionsJson = if (domain.mentions.isNotEmpty()) objectMapper.writeValueAsString(domain.mentions) else null
            // Note: Relationships are not directly mapped in SQL entity
        )
    }

    /**
     * Converts a SQL MergeRequestEntity to a domain MergeRequest
     * 
     * Uses lazy loading proxies for relationships, which will only be loaded
     * when accessed. This provides a consistent API regardless of the database
     * implementation and avoids the N+1 query problem.
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: MergeRequestEntity): MergeRequest {
        val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")

        // Parse mentions from JSON
        val mentions = if (entity.mentionsJson != null) {
            try {
                objectMapper.readValue<List<Mention>>(entity.mentionsJson!!)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        return MergeRequest(
            id = id,
            iid = entity.iid,
            title = entity.title,
            description = entity.description,
            createdAt = entity.createdAt,
            closedAt = entity.closedAt,
            updatedAt = entity.updatedAt,
            labels = entity.labels,
            state = entity.state,
            webUrl = entity.webUrl,
            mentions = mentions,
            // Create lazy-loaded proxies for relationships that will load data from DAOs when accessed
            accounts = proxyFactory.createLazyList { mergeRequestAccountConnectionDao.findAccountsByMergeRequest(id) },
            milestones = proxyFactory.createLazyList { mergeRequestMilestoneConnectionDao.findMilestonesByMergeRequest(id) },
            notes = proxyFactory.createLazyList { mergeRequestNoteConnectionDao.findNotesByMergeRequest(id) }
        )
    }

    /**
     * Converts a list of SQL MergeRequestEntity objects to a list of domain MergeRequest objects
     */
    override fun toDomainList(entities: Iterable<MergeRequestEntity>): List<MergeRequest> {
        return entities.map { toDomain(it) }
    }
}
