package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.persistence.entity.arangodb.IssueEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.MergeRequestEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.NoteEntity
import com.inso_world.binocular.web.persistence.entity.sql.AccountEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.IssueMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.MergeRequestMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.NoteMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.IssueAccountConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.MergeRequestAccountConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.NoteAccountConnectionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class AccountMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    private val issueAccountConnectionRepository: IssueAccountConnectionRepository,
    private val mergeRequestAccountConnectionRepository: MergeRequestAccountConnectionRepository,
    private val noteAccountConnectionRepository: NoteAccountConnectionRepository,
    private val issueMapper: IssueMapper,
    private val mergeRequestMapper: MergeRequestMapper,
    private val noteMapper: NoteMapper
) : EntityMapper<Account, AccountEntity> {

    /**
     * Converts a domain Account to a SQL AccountEntity
     */
    override fun toEntity(domain: Account): AccountEntity {
        return AccountEntity(
            id = domain.id,
            platform = domain.platform,
            login = domain.login,
            name = domain.name,
            avatarUrl = domain.avatarUrl,
            url = domain.url
            // Note: Relationships are not directly mapped in SQL entity
        )
    }

    /**
     * Converts a SQL AccountEntity to a domain Account
     * 
     * Uses lazy loading proxies for relationships, which will only be loaded
     * when accessed. This provides a consistent API regardless of the database
     * implementation and avoids the N+1 query problem.
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: AccountEntity): Account {
        val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")

        return Account(
            id = id,
            platform = entity.platform,
            login = entity.login,
            name = entity.name,
            avatarUrl = entity.avatarUrl,
            url = entity.url,
            // Create lazy-loaded proxies for relationships that will load data from repositories when accessed
            issues = proxyFactory.createLazyList { 
                val issues = issueAccountConnectionRepository.findIssuesByAccount(id)
                if (issues.isNotEmpty() && issues[0] is IssueEntity) {
                    issues.map { issueMapper.toDomain(it as IssueEntity) }
                } else {
                    issues as List<Issue>
                }
            },
            mergeRequests = proxyFactory.createLazyList { 
                val mergeRequests = mergeRequestAccountConnectionRepository.findMergeRequestsByAccount(id)
                if (mergeRequests.isNotEmpty() && mergeRequests[0] is MergeRequestEntity) {
                    mergeRequests.map { mergeRequestMapper.toDomain(it as MergeRequestEntity) }
                } else {
                    mergeRequests as List<MergeRequest>
                }
            },
            notes = proxyFactory.createLazyList { 
                val notes = noteAccountConnectionRepository.findNotesByAccount(id)
                if (notes.isNotEmpty() && notes[0] is NoteEntity) {
                    notes.map { noteMapper.toDomain(it as NoteEntity) }
                } else {
                    notes as List<Note>
                }
            }
        )
    }

    /**
     * Converts a list of SQL AccountEntity objects to a list of domain Account objects
     */
    override fun toDomainList(entities: Iterable<AccountEntity>): List<Account> {
        return entities.map { toDomain(it) }
    }
}
