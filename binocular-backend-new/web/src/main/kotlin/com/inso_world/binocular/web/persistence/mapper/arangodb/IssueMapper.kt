package com.inso_world.binocular.web.persistence.mapper.arangodb

import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.persistence.entity.arangodb.IssueEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("nosql", "arangodb")
class IssueMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    @Lazy private val accountMapper: AccountMapper,
    @Lazy private val milestoneMapper: MilestoneMapper,
    @Lazy private val noteMapper: NoteMapper,
    @Lazy private val commitMapper: CommitMapper,
    @Lazy private val userMapper: UserMapper
) : EntityMapper<Issue, IssueEntity> {

    /**
     * Converts a domain Issue to an ArangoDB IssueEntity
     */
    override fun toEntity(domain: Issue): IssueEntity {
        return IssueEntity(
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
            mentions = domain.mentions,
            // Relationships are handled by ArangoDB through edges
            accounts = null,
            commits = null,
            milestones = null,
            notes = null,
            users = null
        )
    }

    /**
     * Converts an ArangoDB IssueEntity to a domain Issue
     * 
     * Uses lazy loading proxies for relationships, which will only be loaded
     * when accessed. This provides a consistent API regardless of the database
     * implementation and avoids the N+1 query problem.
     */
    override fun toDomain(entity: IssueEntity): Issue {
        return Issue(
            id = entity.id,
            iid = entity.iid,
            title = entity.title,
            description = entity.description,
            createdAt = entity.createdAt,
            closedAt = entity.closedAt,
            updatedAt = entity.updatedAt,
            labels = entity.labels,
            state = entity.state,
            webUrl = entity.webUrl,
            mentions = entity.mentions,
            accounts = proxyFactory.createLazyList {
                (entity.accounts ?: emptyList()).map { accountEntity -> 
                    accountMapper.toDomain(accountEntity) 
                } 
            },
            commits = proxyFactory.createLazyList { 
                (entity.commits ?: emptyList()).map { commitEntity -> 
                    commitMapper.toDomain(commitEntity) 
                } 
            },
            milestones = proxyFactory.createLazyList { 
                (entity.milestones ?: emptyList()).map { milestoneEntity -> 
                    milestoneMapper.toDomain(milestoneEntity) 
                } 
            },
            notes = proxyFactory.createLazyList { 
                (entity.notes ?: emptyList()).map { noteEntity -> 
                    noteMapper.toDomain(noteEntity) 
                } 
            },
            users = proxyFactory.createLazyList { 
                (entity.users ?: emptyList()).map { userEntity -> 
                    userMapper.toDomain(userEntity) 
                } 
            }
        )
    }

    /**
     * Converts a list of ArangoDB IssueEntity objects to a list of domain Issue objects
     */
    override fun toDomainList(entities: Iterable<IssueEntity>): List<Issue> {
        return entities.map { toDomain(it) }
    }
}
