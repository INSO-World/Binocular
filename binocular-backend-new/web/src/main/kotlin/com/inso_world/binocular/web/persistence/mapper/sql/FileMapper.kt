package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.persistence.entity.sql.FileEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.BranchMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.CommitMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.FileMapper as ArangoFileMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.ModuleMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.UserMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class FileMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    private val commitFileConnectionRepository: CommitFileConnectionRepository,
    private val branchFileConnectionRepository: BranchFileConnectionRepository,
    private val moduleFileConnectionRepository: ModuleFileConnectionRepository,
    private val branchFileFileConnectionRepository: BranchFileFileConnectionRepository,
    private val commitFileUserConnectionRepository: CommitFileUserConnectionRepository,
    private val commitMapper: CommitMapper,
    private val branchMapper: BranchMapper,
    private val moduleMapper: ModuleMapper,
    private val arangoFileMapper: ArangoFileMapper,
    private val userMapper: UserMapper
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
            // Create lazy-loaded proxies for relationships that will load data from repositories when accessed
            commits = proxyFactory.createLazyList { 
                commitFileConnectionRepository.findCommitsByFile(id).map { commitMapper.toDomain(it) } 
            },
            branches = proxyFactory.createLazyList { 
                branchFileConnectionRepository.findBranchesByFile(id).map { branchMapper.toDomain(it) } 
            },
            modules = proxyFactory.createLazyList { 
                moduleFileConnectionRepository.findModulesByFile(id).map { moduleMapper.toDomain(it) } 
            },
            relatedFiles = proxyFactory.createLazyList { 
                branchFileFileConnectionRepository.findFilesByBranchFile(id).map { arangoFileMapper.toDomain(it) } 
            },
            users = proxyFactory.createLazyList { 
                commitFileUserConnectionRepository.findUsersByCommitFile(id).map { userMapper.toDomain(it) } 
            }
        )
    }
}
