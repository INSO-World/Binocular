package com.inso_world.binocular.infrastructure.sql.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
internal class RepositoryMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
        @Lazy private val commitMapper: CommitMapper,
        @Lazy private val projectMapper: ProjectMapper,
    ) : EntityMapper<Repository, RepositoryEntity> {
        @PersistenceContext
        private lateinit var entityManager: EntityManager

        private val context: MutableMap<Long, Project> = mutableMapOf()

        override fun toEntity(domain: Repository): RepositoryEntity {
            val e =
                RepositoryEntity(
                    id = domain.id?.toLong(),
                    name = domain.name,
                    project =
                        domain.projectId?.let {
                            entityManager.getReference(
                                ProjectEntity::class.java,
                                it.toLong(),
                            )
                        } ?: throw IllegalStateException("Expecting valid Project-Entity"),
                )

//            e.commits =
//                proxyFactory.createLazySet {
//                    (domain.commits)
//                        .map { it ->
//                            val c = commitMapper.toEntity(it)
//                            c.repository = e
//                            c
//                        }.toMutableSet()
//                }

            e.project.repo = e
            return e
        }

        override fun toDomain(entity: RepositoryEntity): Repository {
            val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")
//            val pid = entity.project.id ?: throw IllegalStateException("Project-Entity ID cannot be null")
//            val project by lazy { projectMapper.toDomain(entity.project) }

            val r =
                Repository(
                    id = id.toString(),
                    name = entity.name,
//                    project = project,
                    projectId = entity.project.id?.toString(),
                )

            val commits by lazy {
                entity.commits
                    .map { it ->
                        commitMapper.toDomain(it)
                    }.toMutableSet()
            }
            r.commits = commits

            return r
        }
    }
