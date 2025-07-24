package com.inso_world.binocular.infrastructure.sql.mapper

import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.model.Branch
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
internal class ProjectMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
        @Lazy private val repoMapper: RepositoryMapper,
    ) {
        private val context: MutableMap<Long, Repository> = mutableMapOf()

        fun toEntity(domain: Project): ProjectEntity {
            val p =
                ProjectEntity(
                    id = domain.id?.toLong(),
                    name = domain.name,
                    description = domain.description,
                )

            p.repo =
                domain.repo?.let {
                    repoMapper.toEntity(it, p)
                }

            return p
        }

        fun toDomain(
            entity: ProjectEntity,
            commitContext: MutableMap<String, Commit>,
            branchContext: MutableMap<String, Branch>,
            userContext: MutableMap<String, User>,
        ): Project {
            val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")

            val p =
                Project(
                    id = id.toString(),
                    name = entity.name,
                    description = entity.description,
                )
            val repo by lazy {
                entity.repo?.let { r ->
                    r.id?.let {
                        repoMapper.toDomain(r, p, commitContext, branchContext, userContext)
                    }
                }
            }

            p.repo = repo
            return p
        }
    }
