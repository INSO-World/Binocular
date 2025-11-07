package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CommitEntity
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.Project
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date

@Component
internal class CommitMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
        @Lazy private val moduleMapper: ModuleMapper,
        @Lazy private val buildMapper: BuildMapper,
        @Lazy private val fileMapper: FileMapper,
        @Lazy private val userMapper: UserMapper,
        @Lazy private val issueMapper: IssueMapper,
        private val statsMapper: StatsMapper,
    ) : EntityMapper<Commit, CommitEntity> {
        /**
         * Converts a domain Commit to an ArangoDB CommitEntity
         */
        override fun toEntity(domain: Commit): CommitEntity =
            CommitEntity(
                id = domain.id,
                sha = domain.sha,
                date = Date.from(domain.commitDateTime?.toInstant(ZoneOffset.UTC)),
                message = domain.message,
                webUrl = domain.webUrl,
                stats = domain.stats?.let { statsMapper.toEntity(it) },
                branch = domain.branch,
                // Relationships are handled by ArangoDB through edges
            )

        /**
         * Converts an ArangoDB CommitEntity to a domain Commit
         *
         * Uses lazy loading proxies for relationships, which will only be loaded
         * when accessed. This provides a consistent API regardless of the database
         * implementation and avoids the N+1 query problem.
         */
        override fun toDomain(entity: CommitEntity): Commit {
            val normalizedSha = when {
                entity.sha.length < 40 -> entity.sha.padEnd(40, '0')
                entity.sha.length > 40 -> entity.sha.substring(0, 40)
                else -> entity.sha
            }
            val commitDt = entity.date?.let { LocalDateTime.ofInstant(it.toInstant(), ZoneOffset.UTC) } ?: LocalDateTime.now()
            val cmt =
                Commit(
                    id = entity.id,
                    sha = normalizedSha,
                    commitDateTime = commitDt,
                    message = entity.message,
                    webUrl = entity.webUrl,
                    stats = entity.stats?.let { statsMapper.toDomain(it) },
                    branch = entity.branch,
                    repository = Repository(localPath = "unknown", project = Project(name = "unknown")),
                    builds =
                        proxyFactory.createLazyList {
                            (entity.builds).map { buildEntity ->
                                buildMapper.toDomain(buildEntity)
                            }
                        },
                    files =
                        proxyFactory.createLazyList {
                            (entity.files).map { fileEntity ->
                                fileMapper.toDomain(fileEntity)
                            }
                        },
                    modules =
                        proxyFactory.createLazyList {
                            (entity.modules).map { moduleEntity ->
                                moduleMapper.toDomain(moduleEntity)
                            }
                        },
//                TODO this should be fixed by author and committer
//                users =
//                    proxyFactory.createLazyList {
//                        (entity.users).map { userEntity ->
//                            userMapper.toDomain(userEntity)
//                        }
//                    },
                    issues =
                        proxyFactory.createLazyList {
                            (entity.issues).map { issueEntity ->
                                issueMapper.toDomain(issueEntity)
                            }
                        },
                )
//            TODO does not work so
//            cmt.children.addAll(
//                proxyFactory.createLazyMutableSet {
//                    (entity.children).map { childEntity ->
//                        toDomain(childEntity)
//                    }
//                },
//            )
//            cmt.parents.addAll(
//                proxyFactory.createLazyMutableSet {
//                    (entity.parents).map { parentEntity ->
//                        toDomain(parentEntity)
//                    }
//                },
//            )

            return cmt
        }

        /**
         * Converts a list of ArangoDB CommitEntity objects to a list of domain Commit objects
         */
        override fun toDomainList(entities: Iterable<CommitEntity>): List<Commit> = entities.map { toDomain(it) }
    }
