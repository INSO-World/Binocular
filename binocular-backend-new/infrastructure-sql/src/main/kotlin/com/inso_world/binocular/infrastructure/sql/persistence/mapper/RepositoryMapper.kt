package com.inso_world.binocular.infrastructure.sql.persistence.mapper

import com.inso_world.binocular.core.exception.BinocularValidationException
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.sql.persistence.entity.CommitEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.model.Repository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.validation.Validator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Component
@Validated
internal class RepositoryMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
        @Lazy private val commitMapper: CommitMapper,
        @Lazy private val projectMapper: ProjectMapper,
        private val validator: Validator,
    ) : EntityMapper<Repository, RepositoryEntity> {
        @PersistenceContext
        private lateinit var entityManager: EntityManager

        override fun toEntity(domain: Repository): RepositoryEntity {
            val e =
                RepositoryEntity(
                    id = domain.id?.toLong(),
                    name = domain.name,
                    project = ProjectEntity(id = domain.projectId?.toLong(), name = ""),
                )

            e.commits =
                proxyFactory.createLazySet {
                    val context: MutableMap<String, CommitEntity> = mutableMapOf()
                    (domain.commits)
                        .map { it ->
                            val c = commitMapper.toEntity(it, context)
                            setRepositoryToCommits(e, c)
                            c
                        }.toMutableSet()
                }

            e.project.repo = e
            val violations = validator.validate(e)
            if (violations.isNotEmpty()) throw BinocularValidationException(violations.toString())

            return e
        }

        fun setRepositoryToCommits(
            repository: RepositoryEntity,
            cmt: CommitEntity,
        ) {
            cmt.repository = repository
            cmt.parents.map { setRepositoryToCommits(repository, it) }
        }

        override fun toDomain(entity: RepositoryEntity): Repository {
            val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")

            val r =
                Repository(
                    id = id.toString(),
                    name = entity.name,
                    projectId = entity.project.id?.toString(),
                )

            val commits by lazy {
                entity.commits
                    .map { it ->
                        commitMapper.toDomain(it, mutableMapOf())
                    }.toMutableSet()
            }
            r.commits = commits

            val violations = validator.validate(r)
            if (violations.isNotEmpty()) throw BinocularValidationException(violations.toString())
            return r
        }
    }
