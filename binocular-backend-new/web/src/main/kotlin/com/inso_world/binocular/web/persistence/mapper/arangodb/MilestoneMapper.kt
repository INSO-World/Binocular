package com.inso_world.binocular.web.persistence.mapper.arangodb

import com.inso_world.binocular.web.entity.Milestone
import com.inso_world.binocular.web.persistence.entity.arangodb.MilestoneEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("nosql", "arangodb")
class MilestoneMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    @Lazy private val issueMapper: IssueMapper,
    @Lazy private val mergeRequestMapper: MergeRequestMapper
) : EntityMapper<Milestone, MilestoneEntity> {

    /**
     * Converts a domain Milestone to an ArangoDB MilestoneEntity
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
            webUrl = domain.webUrl,
            // Relationships are handled by ArangoDB through edges
            issues = null,
            mergeRequests = null
        )
    }

    /**
     * Converts an ArangoDB MilestoneEntity to a domain Milestone
     * 
     * Uses lazy loading proxies for relationships, which will only be loaded
     * when accessed. This provides a consistent API regardless of the database
     * implementation and avoids the N+1 query problem.
     */
    override fun toDomain(entity: MilestoneEntity): Milestone {
        return Milestone(
            id = entity.id,
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
            issues = proxyFactory.createLazyList {
                (entity.issues ?: emptyList()).map { issueEntity -> 
                    issueMapper.toDomain(issueEntity) 
                } 
            },
            mergeRequests = proxyFactory.createLazyList { 
                (entity.mergeRequests ?: emptyList()).map { mergeRequestEntity -> 
                    mergeRequestMapper.toDomain(mergeRequestEntity) 
                } 
            }
        )
    }

    /**
     * Converts a list of ArangoDB MilestoneEntity objects to a list of domain Milestone objects
     */
    override fun toDomainList(entities: Iterable<MilestoneEntity>): List<Milestone> {
        return entities.map { toDomain(it) }
    }
}
