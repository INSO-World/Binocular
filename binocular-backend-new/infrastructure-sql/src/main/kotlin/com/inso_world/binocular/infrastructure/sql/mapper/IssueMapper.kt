package com.inso_world.binocular.infrastructure.sql.mapper

import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.infrastructure.sql.persistence.entity.IssueEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.toEntity
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.Project
import com.inso_world.binocular.model.Repository
import com.inso_world.binocular.model.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.util.ReflectionUtils.setField
import org.springframework.stereotype.Component

@Component
internal class IssueMapper {
    private val logger: Logger = LoggerFactory.getLogger(IssueMapper::class.java)

    @Autowired
    private lateinit var ctx: MappingContext

    @Autowired
    @Lazy
    private lateinit var userMapper: UserMapper

    /**
     * Converts a domain Issue to a SQL IssueEntity
     */
    fun toEntity(domain: Issue): IssueEntity {
        ctx.findEntity<Issue.Key, Issue, IssueEntity>(domain)?.let { return it }

        // IMPORTANT: Expect Project already in context (cross-aggregate reference).
        // Do NOT auto-map Project here - that's a separate aggregate.
        val owner: ProjectEntity = ctx.findEntity<Project.Key, Project, ProjectEntity>(domain.project)
            ?: throw IllegalStateException(
                "ProjectEntity must be mapped before RepositoryEntity. " +
                        "Ensure ProjectEntity is in MappingContext before calling toEntity()."
            )

        val entity = domain.toEntity(owner)
        ctx.remember(domain, entity)

        return entity
    }

    /**
     * Converts a SQL IssueEntity to a domain Issue
     *
     * Uses lazy loading proxies for relationships, which will only be loaded
     * when accessed. This provides a consistent API regardless of the database
     * implementation and avoids the N+1 query problem.
     */
    fun toDomain(entity: IssueEntity): Issue {
        // Fast-path: Check if already mapped
        ctx.findDomain<Issue, IssueEntity>(entity)?.let { return it }

        // IMPORTANT: Expect Project already in context (cross-aggregate reference).
        // Do NOT auto-map Project here - that's a separate aggregate.
        val owner = ctx.findDomain<Project, ProjectEntity>(entity.project)
            ?: throw IllegalStateException(
                "Project must be mapped before Repository. " +
                        "Ensure Project is in MappingContext before calling toDomain()."
            )

        val domain = entity.toDomain(owner)
        setField(
            domain.javaClass.superclass.getDeclaredField("iid"),
            domain,
            entity.iid
        )

        ctx.remember(domain, entity)

        return domain
    }

    fun refreshDomain(target: Issue, entity: IssueEntity): Issue {
        setField(
            target.javaClass.getDeclaredField("id"),
            target,
            entity.id?.toString()
        )

        return target
    }

    /**
     * Converts a list of SQL IssueEntity objects to a list of domain Issue objects
     */
    fun toDomainList(entities: Iterable<IssueEntity>): List<Issue> = entities.map { toDomain(it) }
}



// package com.inso_world.binocular.infrastructure.sql.persistence.mapper
//
// import com.inso_world.binocular.core.persistence.mapper.EntityMapper
// import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
// import com.inso_world.binocular.infrastructure.sql.persistence.entity.IssueEntity
// import com.inso_world.binocular.model.Issue
// import org.springframework.beans.factory.annotation.Autowired
// import org.springframework.context.annotation.Lazy
// import org.springframework.context.annotation.Profile
// import org.springframework.stereotype.Component
// import org.springframework.transaction.annotation.Transactional
//
// @Component
// class IssueMapper
//    @Autowired
//    constructor(
//        private val proxyFactory: RelationshipProxyFactory,
//        @Lazy private val accountMapper: AccountMapper,
//        @Lazy private val commitMapper: CommitMapper,
//        @Lazy private val milestoneMapper: MilestoneMapper,
//        @Lazy private val noteMapper: NoteMapper,
//        @Lazy private val userMapper: UserMapper,
//    ) : EntityMapper<Issue, IssueEntity> {
//        /**
//         * Converts a domain Issue to a SQL IssueEntity
//         */
//        override fun toEntity(domain: Issue): IssueEntity {
//            val entity =
//                IssueEntity(
//                    id = domain.id,
//                    iid = domain.iid,
//                    title = domain.title,
//                    description = domain.description,
//                    createdAt = domain.createdAt,
//                    closedAt = domain.closedAt,
//                    updatedAt = domain.updatedAt,
//                    state = domain.state,
//                    webUrl = domain.webUrl,
//                    // Note: Relationships are not directly mapped in SQL entity
//                )
//
//            // Set labels and mentions
//            entity.setDomainLabels(domain.labels)
//            entity.setDomainMentions(domain.mentions)
//
//            return entity
//        }
//
//        /**
//         * Converts a SQL IssueEntity to a domain Issue
//         *
//         * Uses lazy loading proxies for relationships, which will only be loaded
//         * when accessed. This provides a consistent API regardless of the database
//         * implementation and avoids the N+1 query problem.
//         */
//        @Transactional(readOnly = true)
//        override fun toDomain(entity: IssueEntity): Issue {
//            val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")
//
//            return Issue(
//                id = id,
//                iid = entity.iid,
//                title = entity.title,
//                description = entity.description,
//                createdAt = entity.createdAt,
//                closedAt = entity.closedAt,
//                updatedAt = entity.updatedAt,
//                labels = entity.getDomainLabels(),
//                state = entity.state,
//                webUrl = entity.webUrl,
//                mentions = entity.getDomainMentions(),
//                // Use direct entity relationships and map them to domain objects using the new createLazyMappedList method
//                accounts =
//                    proxyFactory.createLazyMappedList(
//                        { entity.accounts },
//                        { accountMapper.toDomain(it) },
//                    ),
//                commits =
//                    proxyFactory.createLazyMappedList(
//                        { entity.commits },
//                        { commitMapper.toDomain(it) },
//                    ),
//                milestones =
//                    proxyFactory.createLazyMappedList(
//                        { entity.milestones },
//                        { milestoneMapper.toDomain(it) },
//                    ),
//                notes =
//                    proxyFactory.createLazyMappedList(
//                        { entity.notes },
//                        { noteMapper.toDomain(it) },
//                    ),
//                users =
//                    proxyFactory.createLazyMappedList(
//                        { entity.users },
//                        { userMapper.toDomain(it) },
//                    ),
//            )
//        }
//    }
