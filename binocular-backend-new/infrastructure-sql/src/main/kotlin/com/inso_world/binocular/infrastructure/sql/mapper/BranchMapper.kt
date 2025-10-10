package com.inso_world.binocular.infrastructure.sql.mapper

import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.sql.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.sql.mapper.context.MappingScope
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.toEntity
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Repository
import jakarta.persistence.EntityManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

@Component
internal class BranchMapper {
    private val logger: Logger = LoggerFactory.getLogger(BranchMapper::class.java)

    @Autowired
    private lateinit var commitMapper: CommitMapper

    @Autowired
    private lateinit var proxyFactory: RelationshipProxyFactory

    @Autowired
    @Lazy
    private lateinit var transactionTemplate: TransactionTemplate

    @Autowired
    private lateinit var mappingScope: MappingScope

    @Autowired
    @Lazy
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var ctx: MappingContext

    /**
     * Converts a domain Branch to a SQL BranchEntity
     */
    fun toEntity(domain: Branch): BranchEntity {
        val branchContextKey = domain.uniqueKey()
        ctx.entity.branch[branchContextKey]?.let {
            return it
        }

        val entity = domain.toEntity()
        ctx.entity.branch.computeIfAbsent(branchContextKey) { entity }

        // Commit is a child of Branch, hence it is mapped here
        domain.commits
            .map {
                requireNotNull(ctx.entity.commit[it.sha]) {
                    "Commit sha $it not found in context"
                }
            }.forEach {
                entity.addCommit(it)
            }

        return entity
    }

    /**
     * Converts a SQL BranchEntity to a domain Branch
     *
     * Uses lazy loading proxies for relationships, which will only be loaded
     * when accessed. This provides a consistent API regardless of the database
     * implementation and avoids the N+1 query problem.
     */
    fun toDomain(entity: BranchEntity): Branch {
        val branchContextKey = entity.uniqueKey()
        ctx.domain.branch[branchContextKey]?.let { return it }

        val domain = entity.toDomain()

        // Commit is a child of Branch, hence it is mapped here
        commitMapper.toDomainGraph(entity.commits.asSequence()).map {
            //  add all parents also to the branch (git behavior of git rev-list --count <rev>)
            (setOf(it) + it.parents).forEach { relative -> domain.commits.add(relative) }
//            domain.addCommit(it)
        }

        ctx.domain.branch.computeIfAbsent(branchContextKey) { domain }

        return domain
    }

    fun toDomainFull(
        entity: BranchEntity,
        repository: Repository,
    ): Branch {
        val branchContextKey = entity.uniqueKey()
        ctx.domain.branch[branchContextKey]?.let { return it }

        val domain = entity.toDomain()

        // Commit is a child of Branch, hence it is mapped here
        commitMapper.toDomainFull(entity.commits, repository).map {
            //  add all parents also to the branch (git behavior of git rev-list --count <rev>)
            (setOf(it) + it.parents).forEach { relative -> domain.commits.add(relative) }
        }

        repository.branches.add(domain)

        ctx.domain.branch.computeIfAbsent(branchContextKey) { domain }

        return domain
    }
}
