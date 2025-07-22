package com.inso_world.binocular.infrastructure.sql.persistence.mapper

import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import jakarta.persistence.EntityManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

@Component
internal class CommitMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
        private val branchMapper: BranchMapper,
        @Lazy private val userMapper: UserMapper,
    ) {
        private val logger: Logger = LoggerFactory.getLogger(CommitMapper::class.java)

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
            commitContext: MutableMap<String, CommitEntity>,
            branchContext: MutableMap<String, BranchEntity>,
            userContext: MutableMap<String, UserEntity>,
        ): CommitEntity {
            commitContext[domain.sha]?.let {
                logger.trace("Commit-Cache hit for sha:${domain.sha}")
                return it
            }

            val entity by lazy {
                val e =
                    CommitEntity(
                        id = domain.id?.toLong(),
                        sha = domain.sha,
                        commitDateTime = domain.commitDateTime,
                        authorDateTime = domain.authorDateTime,
                        message = domain.message,
                        webUrl = domain.webUrl,
                        branch = domain.branch,
                        repository = repository,
                        parents =
                            proxyFactory.createLazyMutableSet {
                                domain.parents
                                    .map {
                                        commitContext
                                            .getOrPut(it.sha) {
                                                toEntity(it, repository, commitContext, branchContext, userContext)
                                            }
                                    }
                            },
                        branches = mutableSetOf(),
                    )
                return@lazy e
            }

            commitContext[domain.sha] = entity
            entity.branches =
                proxyFactory.createLazyMutableSet(
                    {
                        domain.branches
                            .map {
                                branchMapper.toEntity(
                                    it,
                                    repository,
                                    commitContext,
                                    branchContext,
                                )
                            }
                    },
                    { it ->
                        it.forEach { b -> b.commits.add(entity) }
                    },
                )
            entity.committer =
                domain.committer?.let { user ->
                    userMapper.toEntity(user, repository, commitContext, userContext)
                }
            entity.author =
                domain.author?.let { user ->
                    userMapper.toEntity(user, repository, commitContext, userContext)
                }

            repository.commits.add(entity)
            repository.commits.addAll(entity.parents)

            return entity
        }

        fun toDomain(
            entity: CommitEntity,
            repository: Repository,
            commitContext: MutableMap<String, Commit>,
            branchContext: MutableMap<String, Branch>,
            userContext: MutableMap<String, User>,
        ): Commit {
            if (commitContext.containsKey(entity.sha)) {
                return commitContext[entity.sha]!!
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
                        proxyFactory
                            .createLazyMutableSet(
                                {
                                    transactionTemplate.execute {
                                        // Reload the entity in a new session
                                        val freshEntity = entityManager.find(CommitEntity::class.java, entity.id)
                                        // Now the collection is attached to this session
                                        freshEntity.parents.map { it ->
                                            return@map commitContext.getOrPut(it.sha) {
                                                val e = toDomain(it, repository, commitContext, branchContext, userContext)
                                                e
                                            }
                                        }
                                    } ?: throw IllegalStateException("transaction should load parent entities")
                                },
                                { it ->
                                    it.forEach { p ->
                                        p.repositoryId = repository.id
                                        p.parents.forEach { pp -> pp.repositoryId = repository.id }
                                    }
                                },
                            ),
                )

            commitContext[domain.sha] = domain

            // Move the entire mapping of entity.branches to domain objects inside the transaction
            domain.branches =
                proxyFactory
                    .createLazyMutableSet(
                        {
                            val loadedBranches =
                                transactionTemplate.execute {
                                    // Reload the entity in a new session
                                    val freshEntity = entityManager.find(CommitEntity::class.java, entity.id)
                                    // Now the collection is attached to this session
                                    freshEntity.branches.map {
                                        branchMapper.toDomain(
                                            it,
                                            repository,
                                            commitContext,
                                            branchContext,
                                        )
                                    }
                                } ?: throw IllegalStateException("transaction should load branch entities")

                            // Lazy validation: only when branches are loaded
                            require(loadedBranches.isNotEmpty()) { "Branches of Commit ${domain.sha} must not be empty" }
                            loadedBranches
                        },
                        { it ->
                            // Lazy validation: only when branches are loaded
                            require(it.isNotEmpty()) { "Branches of Commit ${domain.sha} must not be empty" }
                            it.forEach { b -> b.commitShas.add(entity.sha) }
//                                TODO not working as expected, would need refresh of entity again
//                            transactionTemplate.execute { t ->
//                                require(it.size == entity.branches.size) {
//                                    "Branches of Commit ${domain.sha} do not match the entity size: actual ${it.size}, expected ${entity.branches.size}"
//                                }
//                            }
                        },
                    )
            domain.committer =
                entity.committer?.let { user ->
                    userContext.getOrPut(user.uniqueKey()) {
                        userMapper.toDomain(
                            user,
                            repository,
                            userContext,
                            commitContext,
                            branchContext,
                        )
                    }
                }
            domain.author =
                entity.author?.let { user ->
                    userContext.getOrPut(user.uniqueKey()) {
                        userMapper.toDomain(
                            user,
                            repository,
                            userContext,
                            commitContext,
                            branchContext,
                        )
                    }
                }

            return domain
        }
    }
