package com.inso_world.binocular.web.persistence.mapper.arangodb

import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.persistence.entity.arangodb.FileEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("nosql", "arangodb")
class FileMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    @Lazy private val branchMapper: BranchMapper,
    @Lazy private val moduleMapper: ModuleMapper,
    @Lazy private val commitMapper: CommitMapper,
    @Lazy private val userMapper: UserMapper
) : EntityMapper<File, FileEntity> {

    /**
     * Converts a domain File to an ArangoDB FileEntity
     */
    override fun toEntity(domain: File): FileEntity {
        return FileEntity(
            id = domain.id,
            path = domain.path,
            webUrl = domain.webUrl,
            maxLength = domain.maxLength,
            // Relationships are handled by ArangoDB through edges
            commits = null,
            branches = null,
            modules = null,
            relatedFiles = null,
            users = null
        )
    }

    /**
     * Converts an ArangoDB FileEntity to a domain File
     * 
     * Uses lazy loading proxies for relationships, which will only be loaded
     * when accessed. This provides a consistent API regardless of the database
     * implementation and avoids the N+1 query problem.
     */
    override fun toDomain(entity: FileEntity): File {
        return File(
            id = entity.id,
            path = entity.path,
            webUrl = entity.webUrl,
            maxLength = entity.maxLength,
            commits = proxyFactory.createLazyList {
                (entity.commits ?: emptyList()).map { commitEntity -> 
                    commitMapper.toDomain(commitEntity) 
                } 
            },
            branches = proxyFactory.createLazyList { 
                (entity.branches ?: emptyList()).map { branchEntity -> 
                    branchMapper.toDomain(branchEntity) 
                } 
            },
            modules = proxyFactory.createLazyList { 
                (entity.modules ?: emptyList()).map { moduleEntity -> 
                    moduleMapper.toDomain(moduleEntity) 
                } 
            },
            relatedFiles = proxyFactory.createLazyList { 
                (entity.relatedFiles ?: emptyList()).map { relatedFileEntity -> 
                    toDomain(relatedFileEntity) 
                } 
            },
            users = proxyFactory.createLazyList { 
                (entity.users ?: emptyList()).map { userEntity -> 
                    userMapper.toDomain(userEntity) 
                } 
            }
        )
    }

    /**
     * Converts a list of ArangoDB FileEntity objects to a list of domain File objects
     */
    override fun toDomainList(entities: Iterable<FileEntity>): List<File> {
        return entities.map { toDomain(it) }
    }
}
