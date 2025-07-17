package com.inso_world.binocular.infrastructure.sql.persistence.mapper

import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.sql.persistence.entity.BranchEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.UserEntity
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
internal class RepositoryMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
        @Lazy private val commitMapper: CommitMapper,
        @Lazy private val branchMapper: BranchMapper,
        @Lazy private val userMapper: UserMapper,
    ) {
        private val logger: Logger = LoggerFactory.getLogger(RepositoryMapper::class.java)

        fun toEntity(
            domain: Repository,
            project: ProjectEntity,
            commitContext: MutableMap<String, CommitEntity> = mutableMapOf(),
            branchContext: MutableMap<String, BranchEntity> = mutableMapOf(),
            userContext: MutableMap<String, UserEntity> = mutableMapOf(),
        ): RepositoryEntity {
            logger.debug("toEntity({})", domain)

            val entity by lazy {
                val e =
                    RepositoryEntity(
                        id = domain.id?.toLong(),
                        name = domain.name,
                        project = project,
                    )
                domain.commits.forEach { it ->
                    commitMapper.toEntity(it, e, commitContext, branchContext, userContext)
                }

                domain.branches.forEach { it ->
                    branchMapper.toEntity(it, e, commitContext, branchContext)
                }

                domain.user.forEach { it ->
                    userMapper.toEntity(it, e, commitContext, userContext)
                }

                e.project.repo = e

                return@lazy e
            }

            return entity
        }

        fun toDomain(
            entity: RepositoryEntity,
            commitContext: MutableMap<String, Commit>,
            branchContext: MutableMap<String, Branch>,
            userContext: MutableMap<String, User>,
        ): Repository {
            val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")

            val domain =
                Repository(
                    id = id.toString(),
                    name = entity.name,
                    projectId = entity.project.id?.toString(),
                )

            entity.commits
                .forEach { it ->
                    commitMapper.toDomain(it, domain, commitContext, branchContext, userContext)
                }

            entity.branches.forEach { branchMapper.toDomain(it, domain, commitContext, branchContext) }

            entity.user.forEach { userMapper.toDomain(it, domain, userContext, commitContext, branchContext) }

            return domain
        }
    }
