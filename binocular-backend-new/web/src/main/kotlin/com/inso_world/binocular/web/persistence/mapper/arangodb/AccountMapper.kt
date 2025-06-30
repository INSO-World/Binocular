package com.inso_world.binocular.web.persistence.mapper.arangodb

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.persistence.entity.arangodb.AccountEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("nosql", "arangodb")
class AccountMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    @Lazy private val issueMapper: IssueMapper,
    @Lazy private val mergeRequestMapper: MergeRequestMapper,
    @Lazy private val noteMapper: NoteMapper
) : EntityMapper<Account, AccountEntity> {

    /**
     * Converts a domain Account to an ArangoDB AccountEntity
     */
    override fun toEntity(domain: Account): AccountEntity {
        return AccountEntity(
            id = domain.id,
            platform = domain.platform,
            login = domain.login,
            name = domain.name,
            avatarUrl = domain.avatarUrl,
            url = domain.url,
            // Relationships are handled by ArangoDB through edges
            issues = null,
            mergeRequests = null,
            notes = null
        )
    }

    /**
     * Converts an ArangoDB AccountEntity to a domain Account
     *
     * Uses lazy loading proxies for relationships, which will only be loaded
     * when accessed. This provides a consistent API regardless of the database
     * implementation and avoids the N+1 query problem.
     */
    override fun toDomain(entity: AccountEntity): Account {
        return Account(
            id = entity.id,
            platform = entity.platform,
            login = entity.login,
            name = entity.name,
            avatarUrl = entity.avatarUrl,
            url = entity.url,
            issues = proxyFactory.createLazyList {
                (entity.issues ?: emptyList()).map { issueEntity ->
                    issueMapper.toDomain(issueEntity)
                }
            },
            mergeRequests = proxyFactory.createLazyList {
                (entity.mergeRequests ?: emptyList()).map { mergeRequestEntity ->
                    mergeRequestMapper.toDomain(mergeRequestEntity)
                }
            },
            notes = proxyFactory.createLazyList {
                (entity.notes ?: emptyList()).map { noteEntity ->
                    noteMapper.toDomain(noteEntity)
                }
            }
        )
    }

    /**
     * Converts a list of ArangoDB AccountEntity objects to a list of domain Account objects
     */
    override fun toDomainList(entities: Iterable<AccountEntity>): List<Account> {
        return entities.map { toDomain(it) }
    }
}
