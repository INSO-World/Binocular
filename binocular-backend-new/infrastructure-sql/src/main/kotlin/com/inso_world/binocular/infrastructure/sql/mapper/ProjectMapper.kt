package com.inso_world.binocular.infrastructure.sql.mapper

import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.toEntity
import com.inso_world.binocular.model.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
internal class ProjectMapper
    @Autowired
    constructor(
        @Lazy private val repoMapper: RepositoryMapper,
    ) {
        var logger: Logger = LoggerFactory.getLogger(ProjectMapper::class.java)

        fun toEntity(domain: Project): ProjectEntity {
            val p = domain.toEntity()

            p.repo =
                domain.repo?.let {
                    repoMapper.toEntity(it, p)
                }

            return p
        }

        fun toDomain(entity: ProjectEntity): Project {
            val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")

            val p = entity.toDomain()

            p.repo =
                entity.repo?.let { r ->
                    r.id?.let {
                        repoMapper.toDomain(r, p)
                    }
                }
            return p
        }
    }
