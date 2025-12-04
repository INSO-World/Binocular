package com.inso_world.binocular.infrastructure.sql.mapper

 import com.inso_world.binocular.core.delegates.logger
 import com.inso_world.binocular.core.persistence.mapper.EntityMapper
 import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
 import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
 import com.inso_world.binocular.infrastructure.sql.persistence.entity.AccountEntity
 import com.inso_world.binocular.infrastructure.sql.persistence.entity.ProjectEntity
 import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
 import com.inso_world.binocular.infrastructure.sql.persistence.entity.toEntity
 import com.inso_world.binocular.model.Account
 import com.inso_world.binocular.model.Project
 import com.inso_world.binocular.model.Repository
 import org.slf4j.Logger
 import org.slf4j.LoggerFactory
 import org.springframework.beans.factory.annotation.Autowired
 import org.springframework.data.util.ReflectionUtils.setField
 import org.springframework.stereotype.Component
 import org.springframework.transaction.annotation.Transactional
 import kotlin.getValue

@Component
 internal class AccountMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
//        @Lazy private val issueMapper: IssueMapper,
//        @Lazy private val mergeRequestMapper: MergeRequestMapper,
//        @Lazy private val noteMapper: NoteMapper,
    ) : EntityMapper<Account, AccountEntity> {
     @Autowired
     private lateinit var ctx: MappingContext

//        @Autowired
//        private lateinit var ctx: MappingContext

     companion object {
         private val logger by logger()
     }

        /**
         * Converts a domain Account to a SQL AccountEntity
         */
        override fun toEntity(domain: Account): AccountEntity {
            logger.trace("Account toEntity(${domain.login})")
            // Fast-path: if this Repository was already mapped in the current context, return it.
            ctx.findEntity<Account.Key, Account, AccountEntity>(domain)?.let { return it }

//            return AccountEntity(
//                id = domain.id?.toLong(),
//                gid = domain.gid,
//                platform = domain.platform,
//                login = domain.login,
//                name = domain.name,
//                avatarUrl = domain.avatarUrl,
//                url = domain.url,
//                // Note: Relationships are not directly mapped in SQL entity
//            )

            // IMPORTANT: Expect Project already in context (cross-aggregate reference).
            // Do NOT auto-map Project here - that's a separate aggregate.
            val owner: ProjectEntity = ctx.findEntity<Project.Key, Project, ProjectEntity>(domain.project)
                ?: throw IllegalStateException(
                    "ProjectEntity must be mapped before RepositoryEntity. " +
                            "Ensure ProjectEntity is in MappingContext before calling toEntity()."
                )

            // Create entity and remember in context
            val entity = domain.toEntity(owner)
            ctx.remember(domain, entity)

            // Delegate to overload with explicit owner
            return entity
        }


        /**
         * Converts a SQL AccountEntity to a domain Account
         *
         * Uses lazy loading proxies for relationships, which will only be loaded
         * when accessed. This provides a consistent API regardless of the database
         * implementation and avoids the N+1 query problem.
         */
        @Transactional(readOnly = true)
        override fun toDomain(entity: AccountEntity): Account {
            // Fast-path: Check if already mapped
            ctx.findDomain<Account, AccountEntity>(entity)?.let { return it }

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

        /**
         * Converts a list of SQL AccountEntity objects to a list of domain Account objects
         */
        override fun toDomainList(entities: Iterable<AccountEntity>): List<Account> = entities.map { toDomain(it) }
    }
