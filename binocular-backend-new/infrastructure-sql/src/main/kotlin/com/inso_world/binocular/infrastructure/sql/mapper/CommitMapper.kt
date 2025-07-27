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
    @Deprecated("use toEntityGraph")
    fun toEntity(
        root: Commit,
        repository: RepositoryEntity,
    ): CommitEntity {
        // --- PHASE 1: discover & create all nodes, _and_ build domainBySha for O(1) lookup ---
        val domainBySha = mutableMapOf<String, Commit>()
        val queue = ArrayDeque<Commit>()
        queue += root

        while (queue.isNotEmpty()) {
            val domain = queue.removeFirst()

            // skip if we’ve already seen this SHA
            if (domainBySha.putIfAbsent(domain.sha, domain) != null) {
                continue
            }

            // instantiate the entity (no relations yet)
            val entity =
                CommitEntity(
                    id = domain.id?.toLong(),
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
                    committer = null,
                    author = null,
                )

            // register in the identity map
            ctx.entity.commit[domain.sha] = entity

            // schedule neighbors
            queue += domain.parents
            queue += domain.children
        }

        // local helper — now an O(1) hash lookup
        fun findDomainBySha(sha: String): Commit? = domainBySha[sha]

        // --- PHASE 2: wire up all relationships ---

        // 2a) parents & children
        ctx.entity.commit.values.forEach { entity ->
            val dom =
                findDomainBySha(entity.sha)
                    ?: throw IllegalStateException("Missing domain for sha=${entity.sha}")

            entity.parents.addAll(
                dom.parents.map { parentDom ->
                    val parentEntity = ctx.entity.commit[parentDom.sha]!!
                    parentEntity.children.add(entity)
                    parentEntity
                },
            )
            entity.children.addAll(
                dom.children.map { childDom ->
                    val childEntity = ctx.entity.commit[childDom.sha]!!
                    childEntity.parents.add(entity)
                    childEntity
                },
            )
        }

        // 2b) branches, committer, author
        ctx.entity.commit.values.forEach { entity ->
            val dom = findDomainBySha(entity.sha)!!

            entity.branches.addAll(
                dom.branches.map {
                    branchMapper.toEntity(it, repository).also { b -> b.commits.add(entity) }
                },
            )
            entity.committer =
                dom.committer?.let {
                    userMapper.toEntity(it, repository).also { u -> u.committedCommits.add(entity) }
                }
            entity.author =
                dom.author?.let {
                    userMapper.toEntity(it, repository).also { u -> u.authoredCommits.add(entity) }
                }
        }

        // 2c) finally, populate repository.commits
        repository.commits.addAll(ctx.entity.commit.values)

        // return the root node
        return ctx.entity.commit[root.sha]
            ?: throw IllegalStateException("Root was not mapped")
    }

    @Deprecated("use toDomainGraph")
    fun toDomain(
        root: CommitEntity,
        repository: Repository,
    ): Commit {
        if (ctx.domain.commit.contains(root.sha)) {
            return ctx.domain.commit[root.sha]!!
        }
        // --- PHASE 1: discover & create all nodes, _and_ build domainBySha for O(1) lookup ---
        val entityBySha = mutableMapOf<String, CommitEntity>()
        val queue = ArrayDeque<CommitEntity>()
        queue += root

        while (queue.isNotEmpty()) {
            val entity = queue.removeFirst()

            // skip if we’ve already seen this SHA
            if (entityBySha.putIfAbsent(entity.sha, entity) != null) {
                continue
            }

            // instantiate the entity (no relations yet)
            val domain =
                Commit(
                    id = entity.id?.toString(),
                    sha = entity.sha,
                    commitDateTime = entity.commitDateTime,
                    authorDateTime = entity.authorDateTime,
                    message = entity.message,
                    webUrl = entity.webUrl,
                    branch = entity.branch,
                    repositoryId = repository.id,
                    parents = mutableSetOf(),
                    children = mutableSetOf(),
                    branches = mutableSetOf(),
                    committer = null,
                    author = null,
                )

            // register in the identity map
            ctx.domain.commit[domain.sha] = domain

            // schedule neighbors
            queue += entity.parents
            queue += entity.children
        }

        // local helper — now an O(1) hash lookup
        fun findDomainBySha(sha: String): CommitEntity? = entityBySha[sha]

        // --- PHASE 2: wire up all relationships ---

        // 2a) parents & children
        ctx.domain.commit.values.forEach { entity ->
            val dom =
                findDomainBySha(entity.sha)
                    ?: throw IllegalStateException("Missing domain for sha=${entity.sha}")

            entity.parents.addAll(
                dom.parents.map { parentDom -> ctx.domain.commit[parentDom.sha]!! },
            )
            entity.children.addAll(
                dom.children.map { childDom -> ctx.domain.commit[childDom.sha]!! },
            )
        }

        // 2b) branches, committer, author
        ctx.domain.commit.values.forEach { entity ->
            val dom = findDomainBySha(entity.sha)!!

            entity.branches.addAll(
                dom.branches.map { branchMapper.toDomain(it, repository) },
            )
            entity.committer = dom.committer?.let { userMapper.toDomain(it, repository) }
            entity.author = dom.author?.let { userMapper.toDomain(it, repository) }
        }

        // 2c) finally, populate repository.commits
        repository.commits.addAll(ctx.domain.commit.values)

        // return the root node
        return ctx.domain.commit[root.sha]
            ?: throw IllegalStateException("Root was not mapped")
    }

    fun toEntityGraph(
        domains: Collection<Commit>,
        repository: RepositoryEntity,
    ): MutableSet<CommitEntity> {
        // 1) build a sha→entity map (we assume entities already contains *all* commits)
        val domainBySha = domains.associateBy { it.sha }

        // 2) instantiate *all* domain nodes, register in identity map
//        val entityBySha = mutableMapOf<String, CommitEntity>()
        for ((sha, dom) in domainBySha) {
            if (ctx.entity.commit.containsKey(sha)) {
                continue
            }
            val ent =
                CommitEntity(
                    id = dom.id?.toLong(),
                    sha = sha,
                    commitDateTime = dom.commitDateTime,
                    authorDateTime = dom.authorDateTime,
                    message = dom.message,
                    webUrl = dom.webUrl,
                    branch = dom.branch,
                    repository = repository,
                    parents = mutableSetOf(),
                    children = mutableSetOf(),
                    branches = mutableSetOf(),
                    committer = null,
                    author = null,
                )
//            entityBySha[sha] = ent
            ctx.entity.commit[sha] = ent
        }
        for ((sha, dom) in domainBySha) {
            val ent = ctx.entity.commit[sha] ?: throw IllegalStateException("Commit domain must be mapped to map user")
            ent.committer =
                dom.committer?.let {
                    userMapper.toEntity(
                        it,
                        repository,
                    )
                } // .also { u -> u.committedCommits.add(ent) } }
            ent.author =
                dom.author?.let { userMapper.toEntity(it, repository) } // .also { u -> u.authoredCommits.add(ent) } }
        }

        // 3) wire up parent/child links in one sweep
        for ((sha, dom) in domainBySha) {
            val ent = ctx.entity.commit[sha]!!
//            ent.parents.addAll(dom.parents.map { parent -> entityBySha[parent.sha]!! })
//            ent.children.addAll(dom.children.map { child -> entityBySha[child.sha]!! })
            ent.parents.addAll(
                dom.parents.map { parentDom ->
                    val parentEntity = ctx.entity.commit[parentDom.sha]!!
                    parentEntity.children.add(ent)
                    parentEntity
                },
            )
            ent.children.addAll(
                dom.children.map { childDom ->
                    val childEntity = ctx.entity.commit[childDom.sha]!!
                    childEntity.parents.add(ent)
                    childEntity
                },
            )
        }

        // 4) wire up branches
        for ((sha, dom) in domainBySha) {
            val ent = ctx.entity.commit[sha]!!
            ent.branches.addAll(
                dom.branches.map { branchMapper.toEntity(it, repository).also { b -> b.commits.add(ent) } },
            )
        }

        return ctx.entity.commit.values
            .toMutableSet()
    }

    fun toDomainGraph(
        entities: Collection<CommitEntity>,
        repository: Repository,
    ): Set<Commit> {
        // 1) build a sha→entity map (we assume entities already contains *all* commits)
        val entityBySha = entities.associateBy { it.sha }

        // 2) instantiate *all* domain nodes, register in identity map
        val domainBySha = mutableMapOf<String, Commit>()
        for ((sha, ent) in entityBySha) {
            val dom =
                Commit(
                    id = ent.id!!.toString(),
                    sha = sha,
                    commitDateTime = ent.commitDateTime,
                    authorDateTime = ent.authorDateTime,
                    message = ent.message,
                    webUrl = ent.webUrl,
                    branch = ent.branch,
                    repositoryId = repository.id,
                    parents = mutableSetOf(),
                    children = mutableSetOf(),
                    branches = mutableSetOf(),
                    committer = null,
                    author = null,
                )
            domainBySha[sha] = dom
            ctx.domain.commit[sha] = dom
        }
        for ((sha, ent) in entityBySha) {
            val dom = domainBySha[sha] ?: throw IllegalStateException("Commit domain must be mapped to map user")
            dom.committer = ent.committer?.let { userMapper.toDomain(it, repository) }
            dom.author = ent.author?.let { userMapper.toDomain(it, repository) }
        }

        // 3) wire up parent/child links in one sweep
        for ((sha, ent) in entityBySha) {
            val dom = domainBySha[sha]!!
            dom.parents.addAll(ent.parents.map { parent -> domainBySha[parent.sha]!! })
            dom.children.addAll(ent.children.map { child -> domainBySha[child.sha]!! })
        }

        // 4) wire up branches
        for ((sha, ent) in entityBySha) {
            val dom = domainBySha[sha]!!
            dom.branches.addAll(
                ent.branches.map { branchMapper.toDomain(it, repository).also { b -> b.commitShas.add(ent.sha) } },
            )
        }

        return domainBySha.values.toMutableSet()
    }
}
