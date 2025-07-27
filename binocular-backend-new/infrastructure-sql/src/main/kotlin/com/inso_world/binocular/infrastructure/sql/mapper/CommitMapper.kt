package com.inso_world.binocular.infrastructure.sql.mapper

import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.sql.exception.IllegalMappingStateException
import com.inso_world.binocular.infrastructure.sql.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
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
    fun toEntity(
        root: Commit,
        repository: RepositoryEntity,
    ): CommitEntity {
        toEntityGraph(setOf(root), repository)

        // return the root node
        return ctx.entity.commit[root.sha]
            ?: throw IllegalMappingStateException("Root was not mapped")
    }

    fun toDomain(
        root: CommitEntity,
        repository: Repository,
    ): Commit {
        toDomainGraph(setOf(root), repository)

        // return the root node
        return ctx.domain.commit[root.sha]
            ?: throw IllegalMappingStateException("Root was not mapped")
    }

    fun toEntityGraph(
        @NotEmpty domains: Collection<Commit>,
        repository: RepositoryEntity,
    ): MutableSet<CommitEntity> {
        // --- PHASE 1: discover & create all CommitEntity instances ---

        val domainBySha = domains.associateBy { it.sha }.toMutableMap()
        // we'll do a simple BFS (or DFS) from the root Commit
        val queue = ArrayDeque(domains)

        // 2) instantiate *all* domain nodes, register in identity map
        while (queue.isNotEmpty()) {
            val dom = queue.removeFirst()
            domainBySha.computeIfAbsent(dom.sha) { dom }
            val sha = dom.sha
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

            // 1b) register it in our identity–map
            ctx.entity.commit[sha] = ent

            // 1c) schedule its neighbours
            queue += dom.parents
            queue += dom.children
        }
        // now every CommitEntity is sitting in ctx.entity.commit

        // 2) wire up user
        for ((sha, dom) in domainBySha) {
            val ent =
                ctx.entity.commit[sha] ?: throw IllegalMappingStateException("Parent $sha must be present when wire up user")
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
            ent.parents.addAll(
                dom.parents.map { parentDom ->
                    val parentEntity =
                        ctx.entity.commit[parentDom.sha] ?: throw IllegalMappingStateException(
                            "Parent ${parentDom.sha} must be present when wire up parent",
                        )
                    parentEntity.children.add(ent)
                    parentEntity
                },
            )
            ent.children.addAll(
                dom.children.map { childDom ->
                    val childEntity =
                        ctx.entity.commit[childDom.sha] ?: throw IllegalMappingStateException(
                            "Child ${childDom.sha} must be present when wire up child",
                        )
                    childEntity.parents.add(ent)
                    childEntity
                },
            )
        }

        // 4) wire up branches
        for ((sha, dom) in domainBySha) {
            val ent =
                ctx.entity.commit[sha] ?: throw IllegalMappingStateException(
                    "Commit $sha must be present wiring up branches",
                )
            ent.branches.addAll(
                dom.branches.map { branchMapper.toEntity(it, repository).also { b -> b.commits.add(ent) } },
            )
        }

        repository.commits.addAll(ctx.entity.commit.values)

        return repository.commits
    }

    fun toDomainGraph(
        @NotEmpty entities: Collection<CommitEntity>,
        repository: Repository,
    ): Set<Commit> {
        // 1) build a sha→entity map (we assume entities already contains *all* commits)
        val entityBySha = entities.associateBy { it.sha }

        // 2) instantiate *all* domain nodes, register in identity map
        for ((sha, ent) in entityBySha) {
            if (ctx.domain.commit.containsKey(sha)) {
                continue
            }
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
            ctx.domain.commit[sha] = dom
        }
        for ((sha, ent) in entityBySha) {
            val dom = ctx.domain.commit[sha] ?: throw IllegalMappingStateException("Commit domain must be mapped to map user")
            dom.committer = ent.committer?.let { userMapper.toDomain(it, repository) }
            dom.author = ent.author?.let { userMapper.toDomain(it, repository) }
        }

        // 3) wire up parent/child links in one sweep
        for ((sha, ent) in entityBySha) {
            val dom = ctx.domain.commit[sha]!!
            dom.parents.addAll(
                ent.parents.map { parentDom ->
                    val parentEntity = ctx.domain.commit[parentDom.sha]!!
                    parentEntity.children.add(dom)
                    parentEntity
                },
            )
            dom.children.addAll(
                ent.children.map { childDom ->
                    val childEntity = ctx.domain.commit[childDom.sha]!!
                    childEntity.parents.add(dom)
                    childEntity
                },
            )
        }

        // 4) wire up branches
        for ((sha, ent) in entityBySha) {
            val dom = ctx.domain.commit[sha]!!
            dom.branches.addAll(
                ent.branches.map { branchMapper.toDomain(it, repository).also { b -> b.commitShas.add(ent.sha) } },
            )
        }

        repository.commits.addAll(ctx.domain.commit.values)

        return repository.commits
    }
}
