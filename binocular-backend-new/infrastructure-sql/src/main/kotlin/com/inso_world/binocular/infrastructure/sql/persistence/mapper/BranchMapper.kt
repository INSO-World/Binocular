package com.inso_world.binocular.infrastructure.sql.persistence.mapper

import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.model.Branch
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
internal class BranchMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
    ) {
        private val logger: Logger = LoggerFactory.getLogger(BranchMapper::class.java)

        @Autowired
        @Lazy
        private lateinit var transactionTemplate: TransactionTemplate

        @Autowired
        @Lazy
        private lateinit var entityManager: EntityManager

        /**
         * Converts a domain Branch to a SQL BranchEntity
         */
        fun toEntity(
            domain: Branch,
            repo: RepositoryEntity,
            commitContext: Map<String, CommitEntity>,
            branchContext: MutableMap<String, BranchEntity>,
        ): BranchEntity {
            val branchContextKey = "${repo.name},${domain.name}"
            branchContext[branchContextKey]?.let {
                return it
            }

            val entity =
                BranchEntity(
                    id = domain.id?.toLong(),
                    name = domain.name,
//                active = domain.active,
//                tracksFileRenames = domain.tracksFileRenames,
//                latestCommit = domain.latestCommit,
                    repository = repo,
                    // Note: Relationships are not directly mapped in SQL entity
                )
            branchContext[branchContextKey] = entity

            entity.commits =
                proxyFactory.createLazyMutableSet(
                    {
                        val commitEntities =
                            domain.commitShas.map {
                                val cmt =
                                    commitContext[it] ?: throw IllegalStateException("Commit sha $it not found in context")
                                cmt
                            }
                        commitEntities
                    },
                    { it ->
                        require(it.size == domain.commitShas.size, { "Some SHAs are missing for branch" })
                        it.forEach { c -> c.branches.add(entity) }
                    },
                )
            repo.branches.add(entity)

            return entity
        }

        /**
         * Converts a SQL BranchEntity to a domain Branch
         *
         * Uses lazy loading proxies for relationships, which will only be loaded
         * when accessed. This provides a consistent API regardless of the database
         * implementation and avoids the N+1 query problem.
         */
        fun toDomain(
            entity: BranchEntity,
            repository: Repository,
            commitContext: Map<String, Commit>,
            branchContext: MutableMap<String, Branch>,
        ): Branch {
            val branchContextKey = entity.uniqueKey()
            branchContext[branchContextKey]?.let { return it }

            val domain =
                Branch(
                    id = entity.id?.toString(),
                    name = entity.name,
//                active = entity.active,
//                tracksFileRenames = entity.tracksFileRenames,
//                latestCommit = entity.latestCommit,
                    // Use direct entity relationships and map them to domain objects using the createLazyMappedList method
//                files =
//                    proxyFactory.createLazyMappedList(
//                        { entity.files },
//                        { fileMapper.toDomain(it) },
//                    ),
                    commitShas =
                        proxyFactory.createLazyMutableSet(
                            {
                                transactionTemplate.execute {
                                    // Reload the entity in a new session
                                    val freshEntity = entityManager.find(BranchEntity::class.java, entity.id)
                                    // Now the collection is attached to this session
                                    freshEntity.commits.map { it.sha }
                                } ?: throw IllegalStateException("transaction should load branch entities")
                            },
                            { shas ->
                                // Lazy validation: only when branches are loaded
                                require(shas.isNotEmpty()) { "SHAs of Branch ${entity.name} must not be empty" }
//                                TODO not working as expected, would need refresh of entity again
//                                transactionTemplate.execute {
//                                    require(
//                                        shas.size == entity.commits.size,
//                                    ) { "SHAs of Branch ${entity.name} must match the original size of entity" }
//                                }
                            },
                        ),
                    repositoryId = repository.id,
                )
            branchContext[branchContextKey] = domain

            return domain
        }
    }
