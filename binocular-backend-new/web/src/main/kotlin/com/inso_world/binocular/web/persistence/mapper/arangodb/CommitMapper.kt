package com.inso_world.binocular.web.persistence.mapper.arangodb

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.persistence.entity.arangodb.CommitEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("nosql", "arangodb")
class CommitMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    @Lazy private val moduleMapper: ModuleMapper,
    @Lazy private val buildMapper: BuildMapper,
    @Lazy private val fileMapper: FileMapper,
    @Lazy private val userMapper: UserMapper,
    @Lazy private val issueMapper: IssueMapper
) : EntityMapper<Commit, CommitEntity> {

    /**
     * Converts a domain Commit to an ArangoDB CommitEntity
     */
    override fun toEntity(domain: Commit): CommitEntity {
        return CommitEntity(
            id = domain.id,
            sha = domain.sha,
            date = domain.date,
            message = domain.message,
            webUrl = domain.webUrl,
            branch = domain.branch,
            stats = domain.stats,
            // Relationships are handled by ArangoDB through edges
            parents = null,
            children = null,
            builds = null,
            files = null,
            modules = null,
            users = null,
            issues = null
        )
    }

    /**
     * Converts an ArangoDB CommitEntity to a domain Commit
     * 
     * Uses lazy loading proxies for relationships, which will only be loaded
     * when accessed. This provides a consistent API regardless of the database
     * implementation and avoids the N+1 query problem.
     */
    override fun toDomain(entity: CommitEntity): Commit {
        return Commit(
            id = entity.id,
            sha = entity.sha,
            date = entity.date,
            message = entity.message,
            webUrl = entity.webUrl,
            branch = entity.branch,
            stats = entity.stats,
            parents = proxyFactory.createLazyList {
                (entity.parents ?: emptyList()).map { parentEntity -> 
                    toDomain(parentEntity) 
                } 
            },
            children = proxyFactory.createLazyList { 
                (entity.children ?: emptyList()).map { childEntity -> 
                    toDomain(childEntity) 
                } 
            },
            builds = proxyFactory.createLazyList { 
                (entity.builds ?: emptyList()).map { buildEntity -> 
                    buildMapper.toDomain(buildEntity) 
                } 
            },
            files = proxyFactory.createLazyList { 
                (entity.files ?: emptyList()).map { fileEntity -> 
                    fileMapper.toDomain(fileEntity) 
                } 
            },
            modules = proxyFactory.createLazyList { 
                (entity.modules ?: emptyList()).map { moduleEntity -> 
                    moduleMapper.toDomain(moduleEntity) 
                } 
            },
            users = proxyFactory.createLazyList { 
                (entity.users ?: emptyList()).map { userEntity -> 
                    userMapper.toDomain(userEntity) 
                } 
            },
            issues = proxyFactory.createLazyList { 
                (entity.issues ?: emptyList()).map { issueEntity -> 
                    issueMapper.toDomain(issueEntity) 
                } 
            }
        )
    }

    /**
     * Converts a list of ArangoDB CommitEntity objects to a list of domain Commit objects
     */
    override fun toDomainList(entities: Iterable<CommitEntity>): List<Commit> {
        return entities.map { toDomain(it) }
    }
}
