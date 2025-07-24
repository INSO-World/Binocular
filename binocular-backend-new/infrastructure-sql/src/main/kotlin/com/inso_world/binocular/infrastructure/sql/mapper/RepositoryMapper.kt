package com.inso_world.binocular.infrastructure.sql.mapper

import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
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
internal class RepositoryMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
        @Lazy private val commitMapper: CommitMapper,
        @Lazy private val branchMapper: BranchMapper,
        @Lazy private val userMapper: UserMapper,
    ) {
        @Autowired
        @Lazy
        private lateinit var entityManager: EntityManager

        @Autowired
        @Lazy
        private lateinit var transactionTemplate: TransactionTemplate

        private val logger: Logger = LoggerFactory.getLogger(RepositoryMapper::class.java)

        fun toEntity(
            domain: Repository,
            project: ProjectEntity,
            commitContext: MutableMap<String, CommitEntity> = mutableMapOf(),
            branchContext: MutableMap<String, BranchEntity> = mutableMapOf(),
            userContext: MutableMap<String, UserEntity> = mutableMapOf(),
        ): RepositoryEntity {
            logger.debug("toEntity({})", domain)

            val entity by lazy {
                val e =
                    RepositoryEntity(
                        id = domain.id?.toLong(),
                        name = domain.name,
                        project = project,
                    )
//                e.commits =
                domain.commits
                    .map { it ->
                        commitMapper.toEntity(it, e, commitContext, branchContext, userContext)
                    }.toMutableSet()

//                e.branches =
                domain.branches
                    .map { it ->
                        branchMapper.toEntity(it, e, commitContext, branchContext)
                    }.toMutableSet()

//                e.user =
                domain.user
                    .map { it ->
                        userMapper.toEntity(it, e, commitContext, userContext)
                    }.toMutableSet()

                e.project.repo = e

                return@lazy e
            }

            return entity
        }

        fun toDomain(
            entity: RepositoryEntity,
            project: Project?,
            commitContext: MutableMap<String, Commit>,
            branchContext: MutableMap<String, Branch>,
            userContext: MutableMap<String, User>,
        ): Repository {
            val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")

            val domain =
                Repository(
                    id = id.toString(),
                    name = entity.name,
                    project = project,
                )

            domain.commits =
                proxyFactory.createLazyMutableSet({
                    transactionTemplate.execute {
                        // Reload the entity in a new session
                        val freshEntity = entityManager.find(RepositoryEntity::class.java, entity.id)
                        // Now the collection is attached to this session
                        freshEntity.commits.map {
                            commitMapper.toDomain(
                                it,
                                domain,
                                commitContext,
                                branchContext,
                                userContext,
                            )
                        }
                    } ?: throw IllegalStateException("transaction should load repository.commits")
                }, {})

            domain.branches =
                proxyFactory.createLazyMutableSet({
                    transactionTemplate.execute {
                        // Reload the entity in a new session
                        val freshEntity = entityManager.find(RepositoryEntity::class.java, entity.id)
                        // Now the collection is attached to this session
                        freshEntity.branches.map { branchMapper.toDomain(it, domain, commitContext, branchContext) }
                    } ?: throw IllegalStateException("transaction should load repository.branches")
                }, {})

            domain.user =
                proxyFactory
                    .createLazyMutableSet(
                        {
                            transactionTemplate.execute {
                                // Reload the entity in a new session
                                val freshEntity = entityManager.find(RepositoryEntity::class.java, entity.id)
                                // Now the collection is attached to this session
                                freshEntity.user.map {
                                    userMapper.toDomain(
                                        it,
                                        domain,
                                        userContext,
                                        commitContext,
                                        branchContext,
                                    )
                                }
                            } ?: throw IllegalStateException("transaction should load repository.user")
                        },
                        {
                        },
                    )

            return domain
        }
    }
