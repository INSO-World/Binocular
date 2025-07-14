package com.inso_world.binocular.infrastructure.sql.persistence.mapper

import com.inso_world.binocular.core.exception.BinocularValidationException
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.validation.Validator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
internal class ProjectMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
        @Lazy private val repoMapper: RepositoryMapper,
        private val validator: Validator,
    ) : EntityMapper<Project, ProjectEntity> {
        @PersistenceContext
        private lateinit var entityManager: EntityManager

        private val context: MutableMap<Long, Repository> = mutableMapOf()

        override fun toEntity(domain: Project): ProjectEntity {
            val p =
//                domain.id?.let {
//                    entityManager.getReference(ProjectEntity::class.java, it.toLong())
//                } ?:
                ProjectEntity(
                    id = domain.id?.toLong(),
                    name = domain.name,
                    description = domain.description,
                )

            val repo by lazy {
                domain.repo?.let {
                    val repo = repoMapper.toEntity(it)
                    repo.project = p
                    repo
                }
            }
            p.repo = repo
            val violations = validator.validate(p)
            if (violations.isNotEmpty()) throw BinocularValidationException(violations.toString())

            return p
        }

        override fun toDomain(entity: ProjectEntity): Project {
            val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")
//            val rid = entity.repo?.id

            val p =
                Project(
                    id = id.toString(),
                    name = entity.name,
                    description = entity.description,
                )
            val repo by lazy {
                entity.repo?.let { r ->
                    r.id?.let {
//                        LazyProxy { repoMapper.toDomain(r) }.value
//                        context.getOrPut(it) {
                        repoMapper.toDomain(r)
//                        }
                    }
                }
            }

            p.repo = repo
            val violations = validator.validate(p)
            if (violations.isNotEmpty()) throw BinocularValidationException(violations.toString())
            return p
        }
    }
