package com.inso_world.binocular.web.persistence.mapper.sql

import com.fasterxml.jackson.databind.ObjectMapper
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueAccountConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueCommitConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueMilestoneConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueNoteConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueUserConnectionDao
import com.inso_world.binocular.web.persistence.entity.sql.IssueEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class IssueMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    private val issueAccountConnectionDao: IIssueAccountConnectionDao,
    private val issueCommitConnectionDao: IIssueCommitConnectionDao,
    private val issueMilestoneConnectionDao: IIssueMilestoneConnectionDao,
    private val issueNoteConnectionDao: IIssueNoteConnectionDao,
    private val issueUserConnectionDao: IIssueUserConnectionDao,
    private val objectMapper: ObjectMapper
) : EntityMapper<Issue, IssueEntity> {

    /**
     * Converts a domain Issue to a SQL IssueEntity
     */
    override fun toEntity(domain: Issue): IssueEntity {
        val entity = IssueEntity(
            id = domain.id,
            iid = domain.iid,
            title = domain.title,
            description = domain.description,
            createdAt = domain.createdAt,
            closedAt = domain.closedAt,
            updatedAt = domain.updatedAt,
            state = domain.state,
            webUrl = domain.webUrl
            // Note: Relationships are not directly mapped in SQL entity
        )

        // Convert lists to JSON
        entity.setLabels(domain.labels, objectMapper)
        entity.setMentions(domain.mentions, objectMapper)

        return entity
    }

    /**
     * Converts a SQL IssueEntity to a domain Issue
     * 
     * Uses lazy loading proxies for relationships, which will only be loaded
     * when accessed. This provides a consistent API regardless of the database
     * implementation and avoids the N+1 query problem.
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: IssueEntity): Issue {
        val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")

        return Issue(
            id = id,
            iid = entity.iid,
            title = entity.title,
            description = entity.description,
            createdAt = entity.createdAt,
            closedAt = entity.closedAt,
            updatedAt = entity.updatedAt,
            labels = entity.getLabels(objectMapper),
            state = entity.state,
            webUrl = entity.webUrl,
            mentions = entity.getMentions(objectMapper),
            // Create lazy-loaded proxies for relationships that will load data from DAOs when accessed
            accounts = proxyFactory.createLazyList { issueAccountConnectionDao.findAccountsByIssue(id) },
            commits = proxyFactory.createLazyList { issueCommitConnectionDao.findCommitsByIssue(id) },
            milestones = proxyFactory.createLazyList { issueMilestoneConnectionDao.findMilestonesByIssue(id) },
            notes = proxyFactory.createLazyList { issueNoteConnectionDao.findNotesByIssue(id) },
            users = proxyFactory.createLazyList { issueUserConnectionDao.findUsersByIssue(id) }
        )
    }
}
