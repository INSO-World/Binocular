// package com.inso_world.binocular.infrastructure.sql.persistence.mapper
//
// import com.inso_world.binocular.core.persistence.mapper.EntityMapper
// import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
// import com.inso_world.binocular.infrastructure.sql.persistence.entity.BuildEntity
// import com.inso_world.binocular.model.Build
// import org.springframework.beans.factory.annotation.Autowired
// import org.springframework.context.annotation.Lazy
// import org.springframework.context.annotation.Profile
// import org.springframework.stereotype.Component
// import org.springframework.transaction.annotation.Transactional
//
// @Component
// class BuildMapper
//    @Autowired
//    constructor(
//        private val proxyFactory: RelationshipProxyFactory,
//        @Lazy private val commitMapper: CommitMapper,
//    ) : EntityMapper<Build, BuildEntity> {
//        /**
//         * Converts a domain Build to a SQL BuildEntity
//         */
//        override fun toEntity(domain: Build): BuildEntity {
//            val entity =
//                BuildEntity(
//                    id = domain.id,
//                    sha = domain.sha,
//                    ref = domain.ref,
//                    status = domain.status,
//                    tag = domain.tag,
//                    user = domain.user,
//                    userFullName = domain.userFullName,
//                    createdAt = domain.createdAt,
//                    updatedAt = domain.updatedAt,
//                    startedAt = domain.startedAt,
//                    finishedAt = domain.finishedAt,
//                    committedAt = domain.committedAt,
//                    duration = domain.duration,
//                    webUrl = domain.webUrl,
//                    // Note: Relationships are not directly mapped in SQL entity
//                )
//
//            // Set jobs
//            domain.jobs?.let { jobsList ->
//                entity.setDomainJobs(jobsList)
//            }
//
//            return entity
//        }
//
//        /**
//         * Converts a SQL BuildEntity to a domain Build
//         *
//         * Uses lazy loading proxies for relationships, which will only be loaded
//         * when accessed. This provides a consistent API regardless of the database
//         * implementation and avoids the N+1 query problem.
//         */
//        @Transactional(readOnly = true)
//        override fun toDomain(entity: BuildEntity): Build {
//            val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")
//
//            // Get jobs from entity
//            val jobs = entity.getDomainJobs()
//
//            return Build(
//                id = id,
//                sha = entity.sha,
//                ref = entity.ref,
//                status = entity.status,
//                tag = entity.tag,
//                user = entity.user,
//                userFullName = entity.userFullName,
//                createdAt = entity.createdAt,
//                updatedAt = entity.updatedAt,
//                startedAt = entity.startedAt,
//                finishedAt = entity.finishedAt,
//                committedAt = entity.committedAt,
//                duration = entity.duration,
//                jobs = jobs,
//                webUrl = entity.webUrl,
//                // Use direct entity relationships and map them to domain objects using the new createLazyMappedList method
//                commits =
//                    proxyFactory.createLazyMappedList(
//                        { entity.commits },
//                        { commitMapper.toDomain(it) },
//                    ),
//            )
//        }
//    }
