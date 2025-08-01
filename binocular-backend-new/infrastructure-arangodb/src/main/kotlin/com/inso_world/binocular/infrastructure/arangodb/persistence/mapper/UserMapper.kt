package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.UserEntity
import com.inso_world.binocular.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class UserMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
        @Lazy private val issueMapper: IssueMapper,
        @Lazy private val fileMapper: FileMapper,
    ) : EntityMapper<User, UserEntity> {
        @Lazy @Autowired
        private lateinit var commitMapper: CommitMapper

        /**
         * Converts a domain User to an ArangoDB UserEntity
         */
        override fun toEntity(domain: User): UserEntity =
            UserEntity(
                id = domain.id,
                gitSignature = domain.gitSignature,
                // Relationships are handled by ArangoDB through edges
            )

        /**
         * Converts an ArangoDB UserEntity to a domain User
         *
         * Uses lazy loading proxies for relationships, which will only be loaded
         * when accessed. This provides a consistent API regardless of the database
         * implementation and avoids the N+1 query problem.
         */
        override fun toDomain(entity: UserEntity): User =
            User(
                id = entity.id,
                name = entity.name,
                email = entity.email,
//                committedCommits = mutableSetOf<Commit>(),
//                TODO
//                    proxyFactory.createLazyList {
//                        (entity.commits ?: emptyList()).map { commitEntity ->
//                            commitMapper.toDomain(commitEntity)
//                        }
//                    },
                issues =
                    proxyFactory.createLazyList {
                        entity.issues.map { issueEntity ->
                            issueMapper.toDomain(issueEntity)
                        }
                    },
                files =
                    proxyFactory.createLazyList {
                        entity.files.map { fileEntity ->
                            fileMapper.toDomain(fileEntity)
                        }
                    },
            )

        /**
         * Converts a list of ArangoDB UserEntity objects to a list of domain User objects
         */
        override fun toDomainList(entities: Iterable<UserEntity>): List<User> = entities.map { toDomain(it) }
    }
