package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.persistence.entity.sql.FileEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class FileMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    @Lazy private val commitMapper: CommitMapper,
    @Lazy private val branchMapper: BranchMapper,
    @Lazy private val moduleMapper: ModuleMapper,
    @Lazy private val userMapper: UserMapper
) : EntityMapper<File, FileEntity> {

    /**
     * Converts a domain File to a SQL FileEntity
     */
    override fun toEntity(domain: File): FileEntity {
        return FileEntity(
            id = domain.id,
            path = domain.path,
            webUrl = domain.webUrl,
            maxLength = domain.maxLength
            // Note: Relationships are not directly mapped in SQL entity
        )
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
            id = id,
            path = entity.path,
            webUrl = entity.webUrl,
            maxLength = entity.maxLength,
            // Use direct entity relationships and map them to domain objects using the createLazyMappedList method
            commits = proxyFactory.createLazyMappedList(
                { entity.commits },
                { commitMapper.toDomain(it) }
            ),
            branches = proxyFactory.createLazyMappedList(
                { entity.branches },
                { branchMapper.toDomain(it) }
            ),
            modules = proxyFactory.createLazyMappedList(
                { entity.modules },
                { moduleMapper.toDomain(it) }
            ),
            relatedFiles = proxyFactory.createLazyMappedList(
                { entity.outgoingFiles },
                { toDomain(it) }
            ),
            users = proxyFactory.createLazyMappedList(
                { entity.commitUserConnections.mapNotNull { it.user } },
                { userMapper.toDomain(it) }
            )
        )
    }
}
