package com.inso_world.binocular.infrastructure.sql.mapper

import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.sql.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
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

            val entity by lazy {
                val e =
                    RepositoryEntity(
                        id = domain.id?.toLong(),
                        name = domain.name,
                        project = project,
                    )
//                e.commits =
                e.commits = commitMapper.toEntityGraph(domain.commits + domain.commits.flatMap { it.parents }, e)
//                    .map { it ->
//                        commitMapper.toEntity(it, e)
//                    }.toMutableSet()

//                e.branches =
                domain.branches
                    .map { it ->
                        branchMapper.toEntity(it, e)
                    }.toMutableSet()

//                e.user =
                domain.user
                    .map { it ->
                        userMapper.toEntity(it, e)
                    }.toMutableSet()

                e.project.repo = e

                return@lazy e
            }

            return entity
        }

        fun toDomain(
            entity: RepositoryEntity,
            project: Project?,
        ): Repository {
            val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")

            val domain =
                Repository(
                    id = id.toString(),
                    name = entity.name,
                    project = project,
                    commits = mutableSetOf(),
                    branches = mutableSetOf(),
                    user = mutableSetOf(),
                )

            // load the *entire* set of CommitEntity once
            val allCommits: Set<CommitEntity> =
                transactionTemplate.execute {
                    val fresh = entityManager.find(RepositoryEntity::class.java, entity.id)
                    fresh.commits
                } ?: throw IllegalStateException("Cannot load the entire set of CommitEntity once")

            // now map them in one go
            domain.commits = commitMapper.toDomainGraph(allCommits, domain).toMutableSet()

            // do similar bulk‑mapping for branches and user
            domain.branches =
                transactionTemplate.execute {
                    val fresh = entityManager.find(RepositoryEntity::class.java, entity.id)
                    fresh.branches.map { branchMapper.toDomain(it, domain) }.toMutableSet()
                } ?: throw IllegalStateException("Cannot bulk-map branches")

            domain.user = transactionTemplate.execute {
                val fresh = entityManager.find(RepositoryEntity::class.java, entity.id)
                fresh.user.map { userMapper.toDomain(it, domain) }.toMutableSet()
            } ?: throw IllegalStateException("Cannot bulk-map branches")

            return domain
        }
    }
