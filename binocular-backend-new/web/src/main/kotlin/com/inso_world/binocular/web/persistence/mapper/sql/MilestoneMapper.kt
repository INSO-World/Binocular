package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.Milestone
import com.inso_world.binocular.web.persistence.entity.sql.MilestoneEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.IssueMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.MergeRequestMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueMilestoneConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.MergeRequestMilestoneConnectionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class MilestoneMapper @Autowired constructor(
  private val proxyFactory: RelationshipProxyFactory,
  private val issueMilestoneConnectionRepository: IssueMilestoneConnectionRepository,
  private val mergeRequestMilestoneConnectionRepository: MergeRequestMilestoneConnectionRepository,
  private val issueMapper: IssueMapper,
  private val mergeRequestMapper: MergeRequestMapper
) : EntityMapper<Milestone, MilestoneEntity> {

  /**
     * Converts a domain Milestone to a SQL MilestoneEntity
     */
    override fun toEntity(domain: Milestone): MilestoneEntity {
        return MilestoneEntity(
            id = domain.id,
            iid = domain.iid,
            title = domain.title,
            description = domain.description,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            startDate = domain.startDate,
            dueDate = domain.dueDate,
            state = domain.state,
            expired = domain.expired,
            webUrl = domain.webUrl
            // Note: Relationships are not directly mapped in SQL entity
        )
    }

    /**
     * Converts a SQL MilestoneEntity to a domain Milestone
     * 
     * Uses lazy loading proxies for relationships, which will only be loaded
     * when accessed. This provides a consistent API regardless of the database
     * implementation and avoids the N+1 query problem.
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: MilestoneEntity): Milestone {
        val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")

        return Milestone(
            id = id,
            iid = entity.iid,
            title = entity.title,
            description = entity.description,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            startDate = entity.startDate,
            dueDate = entity.dueDate,
            state = entity.state,
            expired = entity.expired,
            webUrl = entity.webUrl,
            // Create lazy-loaded proxies for relationships that will load data from repositories when accessed
            issues = proxyFactory.createLazyList { issueMilestoneConnectionRepository.findIssuesByMilestone(id).map { issueMapper.toDomain(it) }  },
            mergeRequests = proxyFactory.createLazyList { mergeRequestMilestoneConnectionRepository.findMergeRequestsByMilestone(id).map { mergeRequestMapper.toDomain(it) }  }
        )
    }

    /**
     * Converts a list of SQL MilestoneEntity objects to a list of domain Milestone objects
     */
    override fun toDomainList(entities: Iterable<MilestoneEntity>): List<Milestone> {
        return entities.map { toDomain(it) }
    }
}
