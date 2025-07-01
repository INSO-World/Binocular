package com.inso_world.binocular.web.persistence.mapper.arangodb

import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.persistence.entity.arangodb.NoteEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("nosql", "arangodb")
class NoteMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    @Lazy private val accountMapper: AccountMapper,
    @Lazy private val issueMapper: IssueMapper,
    @Lazy private val mergeRequestMapper: MergeRequestMapper
) : EntityMapper<Note, NoteEntity> {

    /**
     * Converts a domain Note to an ArangoDB NoteEntity
     */
    override fun toEntity(domain: Note): NoteEntity {
        return NoteEntity(
            id = domain.id,
            body = domain.body,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            system = domain.system,
            resolvable = domain.resolvable,
            confidential = domain.confidential,
            internal = domain.internal,
            imported = domain.imported,
            importedFrom = domain.importedFrom,
            // Relationships are handled by ArangoDB through edges
            accounts = null,
            issues = null,
            mergeRequests = null
        )
    }

    /**
     * Converts an ArangoDB NoteEntity to a domain Note
     * 
     * Uses lazy loading proxies for relationships, which will only be loaded
     * when accessed. This provides a consistent API regardless of the database
     * implementation and avoids the N+1 query problem.
     */
    override fun toDomain(entity: NoteEntity): Note {
        return Note(
            id = entity.id,
            body = entity.body,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            system = entity.system,
            resolvable = entity.resolvable,
            confidential = entity.confidential,
            internal = entity.internal,
            imported = entity.imported,
            importedFrom = entity.importedFrom,
            accounts = proxyFactory.createLazyList {
                (entity.accounts ?: emptyList()).map { accountEntity -> 
                    accountMapper.toDomain(accountEntity) 
                } 
            },
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
     * Converts a list of ArangoDB NoteEntity objects to a list of domain Note objects
     */
    override fun toDomainList(entities: Iterable<NoteEntity>): List<Note> {
        return entities.map { toDomain(it) }
    }
}
