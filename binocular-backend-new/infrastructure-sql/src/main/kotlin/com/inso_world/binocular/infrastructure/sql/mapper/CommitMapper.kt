package com.inso_world.binocular.infrastructure.sql.mapper

import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.sql.exception.IllegalMappingStateException
import com.inso_world.binocular.infrastructure.sql.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.toEntity
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import jakarta.validation.constraints.NotEmpty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
internal class CommitMapper {
    private val logger: Logger = LoggerFactory.getLogger(CommitMapper::class.java)

    @Autowired
    private lateinit var ctx: MappingContext

    @Autowired
    @Lazy
    private lateinit var userMapper: UserMapper

    @Autowired
    @Lazy
    private lateinit var proxyFactory: RelationshipProxyFactory

    @Autowired
    @Lazy
    private lateinit var branchMapper: BranchMapper

    /**
     * Converts a domain Commit to a SQL CommitEntity
     */
    fun toEntity(root: Commit): CommitEntity {
        toEntityGraph(sequenceOf(root))

        // return the root node
        return ctx.entity.commit[root.sha]
            ?: throw IllegalMappingStateException("Root was not mapped")
    }

    fun toEntityFull(
        values: Set<Commit>,
        repository: RepositoryEntity,
    ): Set<CommitEntity> {
        val mappedEntities = toEntityGraph(values.asSequence())

        (values + values.flatMap { it.children } + values.flatMap { it.parents })
            .toSet()
            .forEach { cmt ->
                val entity =
                    ctx.entity.commit[cmt.sha]
                        ?: throw IllegalStateException("Cannot map Commit$cmt with its entity ${cmt.sha}")
                repository.addCommit(entity)
                cmt.committer?.let {
                    val u = userMapper.toEntity(it)
                    repository.addUser(u)
                    u.addCommittedCommit(entity)
                }
                cmt.author?.let {
                    val u = userMapper.toEntity(it)
                    repository.addUser(u)
                    u.addAuthoredCommit(entity)
                }
                cmt.branches.forEach {
                    val b = branchMapper.toEntity(it)
                    repository.addBranch(b)
                    b.addCommit(entity)
                }
            }

        return mappedEntities
    }

    fun toEntityFull(
        value: Commit,
        repository: RepositoryEntity,
    ): CommitEntity = toEntityFull(setOf(value), repository).toList()[0]

    fun toDomain(root: CommitEntity): Commit {
        toDomainGraph(sequenceOf(root))

        // return the root node
        return ctx.domain.commit[root.sha]
            ?: throw IllegalMappingStateException("Root was not mapped")
    }

    fun toEntityGraph(
        @NotEmpty domains: Sequence<Commit>,
    ): MutableSet<CommitEntity> {
        // --- PHASE 1: discover & create all CommitEntity instances ---

        val domainBySha = domains.associateBy { it.sha }.toMutableMap()
        // we'll do a simple BFS (or DFS) from the root Commit
        val queue = ArrayDeque<Commit>()
        queue.addAll(domains)

        // 2) instantiate *all* domain nodes, register in identity map
        while (queue.isNotEmpty()) {
            val dom = queue.removeFirst()
            domainBySha.computeIfAbsent(dom.sha) { dom }
            val sha = dom.sha
            if (ctx.entity.commit.containsKey(sha)) {
                continue
            }
            val ent = dom.toEntity()

            // 1b) register it in our identity–map
            ctx.entity.commit[sha] = ent

            // 1c) schedule its neighbours
            queue += dom.parents
            queue += dom.children
        }
        // now every CommitEntity is sitting in ctx.entity.commit

        // 2) wire up parent/child links in one sweep
        for ((sha, dom) in domainBySha) {
            val ent = ctx.entity.commit[sha]!!
            dom.parents.map { parentDom ->
                val parentEntity =
                    ctx.entity.commit[parentDom.sha] ?: throw IllegalMappingStateException(
                        "Parent ${parentDom.sha} must be present when wire up parent",
                    )
                parentEntity.addChild(ent)
            }
            dom.children.map { childDom ->
                val childEntity =
                    ctx.entity.commit[childDom.sha] ?: throw IllegalMappingStateException(
                        "Child ${childDom.sha} must be present when wire up child",
                    )
                childEntity.addParent(ent)
            }
        }

        val requiredCommits = domainBySha.values.map { it.sha }.toSet()
        return ctx.entity.commit.values
            .filter { it.sha in requiredCommits }
            .toMutableSet()
    }

    fun toDomainGraph(
        @NotEmpty entities: Sequence<CommitEntity>,
    ): Set<Commit> {
        // 1) build a sha→entity map (we assume entities already contains *all* commits)
        val entityBySha = entities.associateBy { it.sha }

        // we'll do a simple BFS (or DFS) from the root Commit
        val queue = ArrayDeque<CommitEntity>()
        queue.addAll(entityBySha.values)

        // 2) instantiate *all* domain nodes, register in identity map
        while (queue.isNotEmpty()) {
            val ent = queue.removeFirst()
            val sha = ent.sha

            logger.trace("Mapping commit $sha")
            if (ctx.domain.commit.containsKey(sha)) {
                continue
            }
            val dom = ent.toDomain()
            ctx.domain.commit.computeIfAbsent(sha) { dom }
            queue +=
                ent.children.filter { child ->
                    entityBySha[sha]?.children?.map { it.sha }?.contains(child.sha) == true
                }
        }

        for ((sha, ent) in entityBySha) {
            val dom =
                ctx.domain.commit[sha]
                    ?: throw IllegalMappingStateException("Commit domain $sha must be mapped to map user")
            ent.committer
                ?.let { userMapper.toDomain(it) }
                ?.also { dom.addCommitter(it) }
            ent.author
                ?.let { userMapper.toDomain(it) }
                ?.also { dom.addAuthor(it) }
        }

        // 3) wire up parent/child links in one sweep
        for ((sha, ent) in entityBySha) {
            val dom =
                ctx.domain.commit[sha]
                    ?: throw IllegalMappingStateException("Commit domain $sha must be mapped to wire up user")
            ent.parents
                .map { parentDom ->
                    ctx.domain.commit[parentDom.sha]
                        ?: throw IllegalMappingStateException("Parent Commit domain ${parentDom.sha} must be mapped to wire up parent")
                }.forEach {
                    dom.addParent(it)
                }
            ent.children
                .map { childDom ->
                    ctx.domain.commit[childDom.sha]
                        ?: throw IllegalMappingStateException("Child Commit domain ${childDom.sha} must be mapped to wire up child")
                }.forEach { dom.addChild(it) }
        }

        val requiredCommits = entityBySha.values.map { it.sha }.toSet()
        return ctx.domain.commit.values
            .filter { it.sha in requiredCommits }
            .toSet()
    }

    fun toDomainFull(
        values: Set<CommitEntity>,
        repository: Repository,
    ): Set<Commit> {
        val mappedValues = toDomainGraph(values.asSequence())

        (values + values.flatMap { it.children } + values.flatMap { it.parents })
            .toSet()
            .forEach { cmt ->
                val domain =
                    ctx.domain.commit[cmt.sha]
                        ?: throw IllegalStateException("Cannot map Commit$cmt with its entity ${cmt.sha}")
                repository.addCommit(domain)
                cmt.committer?.let {
                    val u = userMapper.toDomain(it)
                    repository.addUser(u)
                    u.addCommittedCommit(domain)
                }
                cmt.author?.let {
                    val u = userMapper.toDomain(it)
                    repository.addUser(u)
                    u.addAuthoredCommit(domain)
                }
                cmt.branches.forEach {
                    val b = branchMapper.toDomain(it)
                    repository.addBranch(b)
                    b.addCommit(domain)
                }
            }

        return mappedValues
    }

    fun toDomainFull(
        value: CommitEntity,
        repository: Repository,
    ): Commit = toDomainFull(setOf(value), repository).toList()[0]
}
