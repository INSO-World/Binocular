package com.inso_world.binocular.infrastructure.sql.mapper

import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.sql.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.sql.mapper.context.MappingScope
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import jakarta.persistence.EntityManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

@Component
internal class CommitMapper {
    private val logger: Logger = LoggerFactory.getLogger(CommitMapper::class.java)

    @Autowired
    private lateinit var ctx: MappingContext

    @Autowired
    private lateinit var mappingScope: MappingScope

    @Autowired
    @Lazy
    private lateinit var userMapper: UserMapper

    @Autowired
    @Lazy
    private lateinit var proxyFactory: RelationshipProxyFactory

    @Autowired
    @Lazy
    private lateinit var branchMapper: BranchMapper

    @Autowired
    @Lazy
    private lateinit var transactionTemplate: TransactionTemplate

    @Autowired
    @Lazy
    private lateinit var entityManager: EntityManager

    /**
     * Converts a domain Commit to a SQL CommitEntity
     */
    fun toEntity(
        domain: Commit,
        repository: RepositoryEntity,
    ): CommitEntity {
        ctx.entity.commit[domain.sha]?.let {
            logger.trace("toEntity: Commit-Cache hit for sha: ${domain.sha}")
            return it
        }

        val entity by lazy {
            val entityId = domain.id?.toLong()
            val e =
                CommitEntity(
                    id = entityId,
                    sha = domain.sha,
                    commitDateTime = domain.commitDateTime,
                    authorDateTime = domain.authorDateTime,
                    message = domain.message,
                    webUrl = domain.webUrl,
                    branch = domain.branch,
                    repository = repository,
                    parents = mutableSetOf(),
                    children = mutableSetOf(),
                    branches = mutableSetOf(),
                )
            return@lazy e
        }

        // The entity is only put into the context when it's fully created to avoid partial objects.
        // If it's a new entity, it's added here, if it's from cache, it's returned above.
        ctx.entity.commit.computeIfAbsent(domain.sha) { entity }

        entity.branches =
            proxyFactory.createLazyMutableSet(
                {
                    domain.branches
                        .map { branchMapper.toEntity(it, repository) }
                },
                { it ->
                    it.forEach { b -> b.commits.add(entity) }
                },
            )
        entity.parents =
            run {
                return@run domain.parents
                    .map { parent ->
                        run {
                            val existingEntity = ctx.entity.commit[parent.sha]
                            if (existingEntity != null) {
                                return@map existingEntity
                            }
                        }
                        run {
                            // this avoids concurrent modification in the context
                            val newEntity = toEntity(parent, repository)
                            ctx.entity.commit.computeIfAbsent(parent.sha) { newEntity }
                            return@map ctx.entity.commit[parent.sha]
                                ?: throw IllegalStateException("Parent commit must be mapped here")
                        }
                    }.toMutableSet()
            }
        entity.children =
            run {
                return@run domain.children
                    .map { child ->
                        run {
                            val existingEntity = ctx.entity.commit[child.sha]
                            if (existingEntity != null) {
                                return@map existingEntity
                            }
                        }
                        run {
                            // this avoids concurrent modification in the context
                            val newEntity = toEntity(child, repository)
                            ctx.entity.commit.computeIfAbsent(child.sha) { newEntity }
                            return@map ctx.entity.commit[child.sha]
                                ?: throw IllegalStateException("Child commit must be mapped here")
                        }
                    }.toMutableSet()
            }
        entity.committer =
            domain.committer?.let { user ->
                userMapper.toEntity(user, repository)
            }
        entity.author =
            domain.author?.let { user ->
                userMapper.toEntity(user, repository)
            }

        repository.commits.add(entity)
        repository.commits.addAll(entity.parents)

        return entity
    }

    fun toDomain(
        entity: CommitEntity,
        repository: Repository,
    ): Commit {
        ctx.domain.commit[entity.sha]?.let {
            logger.trace("toDomain: Commit-Cache hit for sha: ${entity.sha}")
            return it
        }

        val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")

        val domain =
            Commit(
                id = id.toString(),
                sha = entity.sha,
                commitDateTime = entity.commitDateTime,
                authorDateTime = entity.authorDateTime,
                message = entity.message,
                webUrl = entity.webUrl,
                branch = entity.branch,
                repositoryId = repository.id,
                parents =
//                    proxyFactory
//                        .createLazyMutableSet(
                    run {
//                                transactionTemplate.execute {
//                                     Reload the entity in a new session
                        val freshEntity = entityManager.find(CommitEntity::class.java, entity.id)
                        // Now the collection is attached to this session
                        val loadedParents =
                            freshEntity.parents.map { it ->
                                run {
                                    val domain = ctx.domain.commit[it.sha]
                                    if (domain != null) {
                                        return@map domain
                                    }
                                }
                                run {
                                    val domain = toDomain(it, repository)
                                    ctx.domain.commit.computeIfAbsent(it.sha) { domain }
                                    return@map ctx.domain.commit[it.sha]
                                        ?: throw IllegalStateException("Parent commit must be mapped here")
                                }
                            }
                        loadedParents.toMutableSet()
//                                } ?: throw IllegalStateException("transaction should load parent entities")
//                            },
//                            { it ->
//                                it.forEach { p ->
//                                    p.repositoryId = repository.id
//                                    p.parents.forEach { pp -> pp.repositoryId = repository.id }
//                                }
                    },
//                        ),
//                        ),
            )

        // Move the entire mapping of entity.branches to domain objects inside the transaction
        domain.branches =
//            proxyFactory
//                .createLazyMutableSet(
            run {
                val loadedBranches =
                    transactionTemplate.execute {
                        // Reload the entity in a new session
                        val freshEntity = entityManager.find(CommitEntity::class.java, entity.id)
                        // Now the collection is attached to this session
                        freshEntity.branches.map { branchMapper.toDomain(it, repository) }
                    } ?: throw IllegalStateException("transaction should load branch entities")

                // Lazy validation: only when branches are loaded
                require(loadedBranches.isNotEmpty()) { "Branches of Commit ${domain.sha} must not be empty" }
                loadedBranches.toMutableSet()
//                    },
//                    { it ->
//                         Lazy validation: only when branches are loaded
//                        require(it.isNotEmpty()) { "Branches of Commit ${domain.sha} must not be empty" }
//                        it.forEach { b -> b.commitShas.add(entity.sha) }
            }
//                )
        domain.committer =
            entity.committer?.let { user ->
                run {
                    val domain = ctx.domain.user[user.uniqueKey()]
                    if (domain != null) {
                        return@let domain
                    }
                }
                run {
                    userMapper.toDomain(user, repository)
                }
            }
        domain.author =
            entity.author?.let { user ->
                run {
                    val domain = ctx.domain.user[user.uniqueKey()]
                    if (domain != null) {
                        return@let domain
                    }
                }
                run {
                    userMapper.toDomain(user, repository)
                }
            }

        // The domain object is only put into the context when it's fully created and its lazy collections are being initialized
        ctx.domain.commit.computeIfAbsent(domain.sha) { domain } // Removed this line

        domain.children =
//                    proxyFactory
//                        .createLazyMutableSet(
            run {
//                                transactionTemplate.execute {
//                                     Reload the entity in a new session
                val freshEntity = entityManager.find(CommitEntity::class.java, entity.id)
                // Now the collection is attached to this session
                val loadedChildren =
                    freshEntity.children.map { it ->
                        run {
                            val domain = ctx.domain.commit[it.sha]
                            if (domain != null) {
                                return@map domain
                            }
                        }
                        run {
                            val domain = toDomain(it, repository)
                            ctx.domain.commit.computeIfAbsent(it.sha) { domain }
                            return@map ctx.domain.commit[it.sha]
                                ?: throw IllegalStateException("Child commit must be mapped here")
                        }
                    }
                loadedChildren.toMutableSet()
//                                } ?: throw IllegalStateException("transaction should load parent entities")
//                            },
//                            { it ->
//                                it.forEach { p ->
//                                    p.repositoryId = repository.id
//                                    p.parents.forEach { pp -> pp.repositoryId = repository.id }
//                                }
            }

        return domain
    }
}
