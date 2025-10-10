package com.inso_world.binocular.infrastructure.sql.mapper

import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.sql.exception.IllegalMappingStateException
import com.inso_world.binocular.infrastructure.sql.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.toEntity
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
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
        private lateinit var ctx: MappingContext

        @Autowired
        @Lazy
        private lateinit var transactionTemplate: TransactionTemplate

        private val logger: Logger = LoggerFactory.getLogger(RepositoryMapper::class.java)

        fun toEntity(
            domain: Repository,
            project: ProjectEntity,
        ): RepositoryEntity {
            logger.debug("toEntity({})", domain)

            val entity = domain.toEntity(project)

            run {
                val allCommitsOfDomain =
                    (domain.commits + domain.commits.flatMap { it.parents } + domain.commits.flatMap { it.children })
                commitMapper
                    .toEntityGraph(allCommitsOfDomain.asSequence())
//                    wire up commit->repository
                    .also { it.forEach { c -> entity.addCommit(c) } }
//                    wire up commit->user
                allCommitsOfDomain.associateBy(Commit::sha).values.forEach { cmt ->
                    val commitEntity =
                        requireNotNull(ctx.entity.commit[cmt.sha]) {
                            "Cannot map Commit$cmt with its entity ${cmt.sha}"
                        }
                    cmt.committer
                        ?.let { user ->
//                            commitEntity.add(userMapper.toEntity(user))
                            commitEntity.committer = userMapper.toEntity(user)
                        }
                    cmt.author
                        ?.let { user ->
//                            commitEntity.add(userMapper.toEntity(user))
                            commitEntity.author = userMapper.toEntity(user)
                        }
//                    wire up commit->branch
                    cmt.branches.forEach { branch ->
                        commitEntity.addBranch(branchMapper.toEntity(branch))
                    }
                }
            }

//              wire up repository->branch
            domain.branches
                .map { it ->
                    branchMapper.toEntity(it)
                }.also { it.forEach { b -> entity.addBranch(b) } }
                .toMutableSet()

//              wire up repository->user
            domain.user
                .map { it ->
                    userMapper.toEntity(it)
                }.also { it.forEach { u -> entity.addUser(u) } }
                .toMutableSet()

            entity.project.repo = entity

            return entity
        }

        fun toDomain(
            entity: RepositoryEntity,
            project: Project?,
        ): Repository {
            val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")

            val domain = entity.toDomain(project)

            // load the *entire* set of CommitEntity once
            val allCommits: Set<CommitEntity> =
                transactionTemplate.execute {
                    val fresh = entityManager.find(RepositoryEntity::class.java, entity.id)
                    fresh.commits
                } ?: throw IllegalStateException("Cannot load the entire set of CommitEntity once")

            // now map them in one go
            domain.commits.addAll(
                commitMapper.toDomainGraph(allCommits.asSequence()),
            )
            domain.commits.forEach { it.repository = domain }

            // do similar bulk‑mapping for branches and user
            domain.branches.addAll(
                transactionTemplate.execute {
                    val fresh = entityManager.find(RepositoryEntity::class.java, entity.id)
                    fresh.branches.map { branchMapper.toDomain(it) }.toMutableSet()
                } ?: throw IllegalStateException("Cannot bulk-map branches"),
            )
//            domain.branches.forEach {
//                it.repository = domain
//            }

            domain.user.addAll(
                transactionTemplate.execute {
                    val domCommitMap = domain.commits.associateBy { it.sha }
                    val fresh = entityManager.find(RepositoryEntity::class.java, entity.id)
                    fresh.user
                        .map { userEntity ->
                            val u =
                                userMapper
                                    .toDomain(userEntity)
                                    .apply {
                                        this.committedCommits.addAll(
                                            userEntity.committedCommits
                                                .map {
                                                    domCommitMap[it.sha]
                                                        ?: throw IllegalMappingStateException(
                                                            "Commit ${it.sha} was not mapped (committedCommits)",
                                                        )
                                                },
                                        )
                                        this.authoredCommits.addAll(
                                            userEntity.authoredCommits
                                                .map {
                                                    domCommitMap[it.sha]
                                                        ?: throw IllegalMappingStateException(
                                                            "Commit ${it.sha} was not mapped (authoredCommits)",
                                                        )
                                                },
                                        )
                                    }
                            u
                        }.toMutableSet()
                } ?: throw IllegalStateException("Cannot bulk-map user"),
            )
//            domain.user.forEach { it.repository = domain }

            return domain
        }
    }
