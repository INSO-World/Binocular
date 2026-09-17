package com.inso_world.binocular.infrastructure.sql.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.infrastructure.sql.mapper.CommitMapper
import com.inso_world.binocular.infrastructure.sql.mapper.BranchMapper
//import com.inso_world.binocular.infrastructure.sql.mapper.ModuleMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingContext
import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.sql.persistence.entity.FileEntity
import com.inso_world.binocular.infrastructure.sql.persistence.entity.RepositoryEntity
import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Repository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
internal class FileMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,    
        @Lazy private val ctx: MappingContext,
        @Lazy private val commitMapper: CommitMapper,
        @Lazy private val branchMapper: BranchMapper,
        //@Lazy private val moduleMapper: ModuleMapper,
    ) : EntityMapper<File, FileEntity> {
        /**
         * Converts a domain File to a SQL FileEntity
         */
        override fun toEntity(domain: File): FileEntity {

            ctx.findEntity<File.Key, File, FileEntity>(domain)?.let { return it }

            //val owner =
            //ctx.findEntity<Repository.Key, Repository, RepositoryEntity>(domain.repository)
            //    ?: throw IllegalStateException(
            //        "RepositoryEntity must be mapped before CommitEntity. " +
            //            "Ensure CommitEntity is in MappingContext before calling toDomain().",
            //    )

            val entity = FileEntity(
                iid = domain.iid,
                path = domain.path,
                //webUrl = domain.webUrl,
                //maxLength = domain.maxLength,
            )
            //ctx.remember(domain, entity)

            return entity
        }

        /**
         * Converts a SQL FileEntity to a domain File
         *
         * Uses lazy loading proxies for relationships, which will only be loaded
         * when accessed. This provides a consistent API regardless of the database
         * implementation and avoids the N+1 query problem.
         */
        @Transactional(readOnly = true)
        override fun toDomain(entity: FileEntity): File {
            val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")
 
            return File(
                //id = id,
                path = (entity.path ?: ""),
                //webUrl = entity.webUrl,
                //maxLength = entity.maxLength,
                // Use direct entity relationships and map them to domain objects using the createLazyMappedList method
                //commits =
                //    proxyFactory.createLazyMappedList(
                //        { entity.commits },
                //        { commitMapper.toDomain(it) },
                //    ),
                //branches =
                //    proxyFactory.createLazyMappedList(
                //        { entity.branches },
                //        { branchMapper.toDomain(it) },
                //    ),
//                modules =
//                    proxyFactory.createLazyMappedList(
//                        { entity.modules },
//                        { moduleMapper.toDomain(it) },
//                    ),
                //relatedFiles =
                //    proxyFactory.createLazyMappedList(
                //        { entity.outgoingFiles },
                //        { toDomain(it) },
                //    ),
//                 users =
//                     proxyFactory.createLazyMappedList(
//                        { entity.commitUserConnections.mapNotNull { it.user } },
//                        { userMapper.toDomain(it) },
//                    ),
            )
        }
    }
