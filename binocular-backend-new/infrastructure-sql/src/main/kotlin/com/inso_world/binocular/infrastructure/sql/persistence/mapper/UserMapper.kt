package com.inso_world.binocular.infrastructure.sql.persistence.mapper

import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
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
internal class UserMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
    ) {
        private val logger: Logger = LoggerFactory.getLogger(UserMapper::class.java)

        @Autowired
        @Lazy
        private lateinit var entityManager: EntityManager

        @Autowired
        @Lazy
        private lateinit var transactionTemplate: TransactionTemplate

        @Autowired
        @Lazy
        private lateinit var commitMapper: CommitMapper

        /**
         * Converts a domain User to a SQL UserEntity
         */
        fun toEntity(
            domain: User,
            repo: RepositoryEntity,
            commitContext: Map<String, CommitEntity>,
            userContext: MutableMap<String, UserEntity>,
        ): UserEntity {
            val userContextKey = "${repo.name},${domain.email}"
            userContext[userContextKey]?.let {
//                logger.debug("User-Cache hit: '$userContextKey'")
                return it
            }

            val entity =
                UserEntity(
                    id = domain.id?.toLong(),
//                gitSignature = domain.gitSignature,
                    email = domain.email,
                    name = domain.name,
                    repository = repo,
                    // Note: Relationships are not directly mapped in SQL entity
                )

            userContext[userContextKey] = entity

            entity.committedCommits =
                proxyFactory
                    .createLazyMutableSet(
                        {
                            val commitEntities =
                                domain.committedCommits
                                    .map {
                                        val cmt =
                                            commitContext[it.sha]
                                                ?: throw IllegalStateException("$it not found in context")
                                        cmt
                                    }
                            commitEntities
                        },
                        { it ->
                            it.forEach { c -> c.committer = entity }
                        },
                    )

            entity.authoredCommits =
                proxyFactory
                    .createLazyMutableSet(
                        {
                            val commitEntities =
                                domain.authoredCommits
                                    .map {
                                        val cmt =
                                            commitContext[it.sha]
                                                ?: throw IllegalStateException("Commit sha $it not found in context")
                                        cmt
                                    }
                            commitEntities
                        },
                        { it ->
                            it.forEach { c -> c.author = entity }
                        },
                    )
            repo.user.add(entity)

            return entity
        }

        /**
         * Converts a SQL UserEntity to a domain User
         *
         * Uses lazy loading proxies for relationships, which will only be loaded
         * when accessed. This provides a consistent API regardless of the database
         * implementation and avoids the N+1 query problem.
         */
        fun toDomain(
            entity: UserEntity,
            repository: Repository,
            userContext: MutableMap<String, User>,
            commitContext: MutableMap<String, Commit>,
            branchContext: MutableMap<String, Branch>,
        ): User {
            val userContextKey = entity.uniqueKey()
            userContext[userContextKey]?.let { return it }

            val domain =
                User(
                    id = entity.id?.toString(),
                    email = entity.email,
                    name = entity.name,
//                    gitSignature = "${entity.name} <${entity.email}>",
                    repository = repository,
                    // Use direct entity relationships and map them to domain objects using the createLazyMappedList method
//                commits =
//                    proxyFactory.createLazyMappedList(
//                        { entity.commits },
//                        { commitMapper.toDomain(it) },
//                    ),
//                issues =
//                    proxyFactory.createLazyMappedList(
//                        { entity.issues },
//                        { issueMapper.toDomain(it) },
//                    ),
//                files =
//                    proxyFactory.createLazyMappedList(
//                        { entity.commitFileConnections.mapNotNull { it.file } },
//                        { fileMapper.toDomain(it) },
//                    ),
                )
            userContext[userContextKey] = domain

            domain.committedCommits =
                proxyFactory.createLazyMutableSet(
                    {
                        transactionTemplate.execute {
                            // Reload the entity in a new session
                            val freshEntity = entityManager.find(UserEntity::class.java, entity.id)
                            // Now the collection is attached to this session
                            freshEntity.committedCommits.map {
                                commitMapper.toDomain(it, repository, commitContext, branchContext, userContext)
                            }
                        } ?: throw IllegalStateException("transaction should load branch committedCommits")
                    },
                    {
//                        it.forEach { u -> u.committer = u }
//                        require(it.size == entity.committedCommits.size) {
//                            "entity.committedCommits: Expected size of ${entity.committedCommits.size} does not match ${it.size}"
//                        }
                    },
                )

            domain.authoredCommits =
                proxyFactory.createLazyMutableSet(
                    {
                        transactionTemplate.execute {
                            // Reload the entity in a new session
                            val freshEntity = entityManager.find(UserEntity::class.java, entity.id)
                            // Now the collection is attached to this session
                            freshEntity.authoredCommits.map {
                                commitMapper.toDomain(it, repository, commitContext, branchContext, userContext)
                            }
                        } ?: throw IllegalStateException("transaction should load authoredCommits entities")
                    },
                    {
//                        require(it.size == entity.authoredCommits.size) {
//                            "entity.committedCommits: Expected size of ${entity.authoredCommits.size} does not match ${it.size}"
//                        }
                    },
                )

            return domain
        }
    }
