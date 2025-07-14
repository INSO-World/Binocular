package com.inso_world.binocular.infrastructure.sql.persistence.mapper

import com.inso_world.binocular.core.exception.BinocularValidationException
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.model.Commit
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.validation.Valid
import jakarta.validation.Validator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Validated
@Component
internal class CommitMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
//        @Lazy private val buildMapper: BuildMapper,
//        @Lazy private val fileMapper: FileMapper,
//        @Lazy private val moduleMapper: ModuleMapper,
//        @Lazy private val userMapper: UserMapper,
        @Lazy private val repositoryMapper: RepositoryMapper,
        private val validator: Validator,
    ) : EntityMapper<Commit, CommitEntity> {
        @PersistenceContext
        private lateinit var entityManager: EntityManager

//        private val context: MutableMap<String, CommitEntity> = mutableMapOf()

        @Deprecated(
            "do not use this method",
            ReplaceWith(
                "toEntity(\n" +
                    "            domain: Commit,\n" +
                    "            context: MutableMap<String, CommitEntity>,\n" +
                    "        )",
            ),
        )
        override fun toEntity(domain: Commit): @Valid CommitEntity = toEntity(domain, mutableMapOf())

        /**
         * Converts a domain Commit to a SQL CommitEntity
         */
        fun toEntity(
            @Valid domain: Commit,
            context: MutableMap<String, CommitEntity>,
        ): CommitEntity {
//            { // TODO: conditional check for e.g. repositoryId, going in is ok null, not out
//                val violations = validator.validate(domain)
//                if (violations.isNotEmpty()) {
//                    throw BinocularValidationException(violations.toString())
//                }
//            }()

            if (context.containsKey(domain.sha)) {
                return context[domain.sha]!!
            }

            val repo =
                RepositoryEntity(
                    id = domain.repositoryId?.toLong(),
                    name = "",
                    project = ProjectEntity(name = ""),
                )

            val entity =
                CommitEntity(
                    id = domain.id?.toLong(),
                    sha = domain.sha,
                    date = domain.commitDateTime,
                    message = domain.message,
                    webUrl = domain.webUrl,
                    branch = domain.branch,
                    parents =
                        proxyFactory.createLazyMappedList(
                            { domain.parents },
                            { it ->
                                context.getOrPut(it.sha) {
                                    val e = toEntity(it, context)
                                    e.repository = repo
                                    e
                                }
                            },
                        ),
                    //                        proxyFactory
//                            .createLazySet {
//                                domain.parents
//                                    .map { parent ->
//                                        val c = context.getOrPut(parent.sha) { toEntity(parent, context) }
// //                                        val c = toEntity(parent, context)
//                                        c
//                                    }.toMutableSet()
//                            }.toMutableList(),
                    repository = repo,
                    // Note: Relationships are not directly mapped in SQL entity
                )
            context[entity.sha] = entity
            val violations = validator.validate(entity)
            if (violations.isNotEmpty()) throw BinocularValidationException(violations.toString())

            return entity
        }

//        fun toEntity(
//            domain: CommitEntity,
//            context: MutableMap<String, CommitEntity> = mutableMapOf(),
//        ): CommitEntity {
//            if (context.containsKey(domain.sha)) {
//                return context[domain.sha]!!
//            }
//        }

        /**
         * Converts a SQL CommitEntity to a domain Commit
         *
         * Uses lazy loading proxies for relationships, which will only be loaded
         * when accessed. This provides a consistent API regardless of the database
         * implementation and avoids the N+1 query problem.
         */
        override fun toDomain(entity: CommitEntity): Commit = toDomain(entity, mutableMapOf())

        fun toDomain(
            entity: CommitEntity,
            context: MutableMap<String, Commit>,
        ): Commit {
            if (context.containsKey(entity.sha)) {
                return context[entity.sha]!!
            }

            val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")

            val domain =
                Commit(
                    id = id.toString(),
                    sha = entity.sha,
//                date = entity.date?.let { d -> d.toInstant(ZoneOffset.UTC).let { Date.from(it) } },
                    commitDateTime = entity.date,
                    message = entity.message,
                    webUrl = entity.webUrl,
                    branch = entity.branch,
//                stats = entity.getStats(),
                    repositoryId = entity.repository?.id?.toString(),
                    // Use direct entity relationships and map them to domain objects using the new createLazyMappedList method
                    parents =
                        proxyFactory.createLazyMappedList(
                            { entity.parents },
                            { it ->
                                val d =
                                    context.getOrPut(it.sha) {
                                        val e = toDomain(it, context)
                                        e
                                    }
                                it.repository?.id?.toString().let { id ->
                                    setRepositoryToCommits(id, d)
                                }
                                d
                            },
                        ),
//                children =
//                    proxyFactory.createLazyMappedList(
//                        { entity.childCommits },
//                        { toDomain(it) },
//                    ),
//                builds =
//                    proxyFactory.createLazyMappedList(
//                        { entity.builds },
//                        { buildMapper.toDomain(it) },
//                    ),
//                files =
//                    proxyFactory.createLazyMappedList(
//                        { entity.files },
//                        { fileMapper.toDomain(it) },
//                    ),
//                modules =
//                    proxyFactory.createLazyMappedList(
//                        { entity.modules },
//                        { moduleMapper.toDomain(it) },
//                    ),
//                users =
//                    proxyFactory.createLazyMappedList(
//                        { entity.users },
//                        { userMapper.toDomain(it) },
//                    ),
//                issues =
//                    proxyFactory.createLazyMappedList(
//                        { entity.issues },
//                        { issueMapper.toDomain(it) },
//                    ),
                )

            context[domain.sha] = domain
            val violations = validator.validate(domain)
            if (violations.isNotEmpty()) throw BinocularValidationException(violations.toString())
            return domain
        }

        fun setRepositoryToCommits(
            repositoryId: String?,
            cmt: Commit,
            visited: MutableSet<String> = mutableSetOf(),
        ) {
            if (!visited.add(cmt.sha)) return // already visited, avoid infinite recursion

            if (cmt.repositoryId == null) {
                cmt.repositoryId = repositoryId
            } else if (cmt.repositoryId != null && cmt.repositoryId != repositoryId) {
                throw IllegalArgumentException("cmt.repositoryId=${cmt.repositoryId} must be the same as the repositoryId=$repositoryId")
            }

            cmt.parents.forEach { parent ->
                if (parent.repositoryId == null) {
                    setRepositoryToCommits(repositoryId, parent, visited)
                }
            }
        }
    }
