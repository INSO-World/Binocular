package com.inso_world.binocular.infrastructure.arangodb.persistence.mapper

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.FileEntity
import com.inso_world.binocular.model.File
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class FileMapper
    @Autowired
    constructor(
        private val proxyFactory: RelationshipProxyFactory,
        @Lazy private val branchMapper: BranchMapper,
        @Lazy private val moduleMapper: ModuleMapper,
        @Lazy private val userMapper: UserMapper,
    ) : EntityMapper<File, FileEntity> {
        @Lazy
        @Autowired
        private lateinit var commitMapper: CommitMapper

        /**
         * Converts a domain File to an ArangoDB FileEntity
         */
        override fun toEntity(domain: File): FileEntity =
            FileEntity(
                id = domain.id,
                path = domain.path,
                webUrl = domain.webUrl,
                maxLength = domain.maxLength,
                // Relationships are handled by ArangoDB through edges
            )

        /**
         * Converts an ArangoDB FileEntity to a domain File
         *
         * Uses lazy loading proxies for relationships, which will only be loaded
         * when accessed. This provides a consistent API regardless of the database
         * implementation and avoids the N+1 query problem.
         */
        override fun toDomain(entity: FileEntity): File {
            val file =
                File(
                    id = entity.id,
                    path = entity.path,
                )
            file.webUrl = entity.webUrl
//            file.maxLength = entity.maxLength
//            file.commits =
//                proxyFactory.createLazyList {
//                    (entity.commits ?: emptyList()).map { commitEntity ->
//                        commitMapper.toDomain(commitEntity)
//                    }
//                }
//            file.branches =
//                proxyFactory.createLazyList {
//                    (entity.branches ?: emptyList()).map { branchEntity ->
//                        branchMapper.toDomain(branchEntity)
//                    }
//                }
//            file.modules =
//                proxyFactory.createLazyList {
//                    (entity.modules ?: emptyList()).map { moduleEntity ->
//                        moduleMapper.toDomain(moduleEntity)
//                    }
//                }
//            file.relatedFiles =
//                proxyFactory.createLazyList {
//                    (entity.relatedFiles ?: emptyList()).map { relatedFileEntity ->
//                        toDomain(relatedFileEntity)
//                    }
//                }
//            file.users =
//                proxyFactory.createLazyList {
//                    (entity.users ?: emptyList()).map { userEntity ->
//                        userMapper.toDomain(userEntity)
//                    }
//                }
            return file
        }

        /**
         * Converts a list of ArangoDB FileEntity objects to a list of domain File objects
         */
        override fun toDomainList(entities: Iterable<FileEntity>): List<File> = entities.map { toDomain(it) }
    }
