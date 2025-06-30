package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.persistence.entity.sql.ModuleEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.CommitMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.FileMapper
import com.inso_world.binocular.web.persistence.mapper.arangodb.ModuleMapper as ArangoModuleMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.CommitModuleConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.ModuleFileConnectionRepository
import com.inso_world.binocular.web.persistence.repository.arangodb.edges.ModuleModuleConnectionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class ModuleMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    private val commitModuleConnectionRepository: CommitModuleConnectionRepository,
    private val moduleFileConnectionRepository: ModuleFileConnectionRepository,
    private val moduleModuleConnectionRepository: ModuleModuleConnectionRepository,
    private val commitMapper: CommitMapper,
    private val fileMapper: FileMapper,
    private val arangoModuleMapper: ArangoModuleMapper
) : EntityMapper<Module, ModuleEntity> {

    /**
     * Converts a domain Module to a SQL ModuleEntity
     */
    override fun toEntity(domain: Module): ModuleEntity {
        return ModuleEntity(
            id = domain.id,
            path = domain.path
            // Note: Relationships are not directly mapped in SQL entity
        )
    }

    /**
     * Converts a SQL ModuleEntity to a domain Module
     * 
     * Uses lazy loading proxies for relationships, which will only be loaded
     * when accessed. This provides a consistent API regardless of the database
     * implementation and avoids the N+1 query problem.
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: ModuleEntity): Module {
        val id = entity.id ?: throw IllegalStateException("Entity ID cannot be null")

        return Module(
            id = id,
            path = entity.path,
            // Create lazy-loaded proxies for relationships that will load data from repositories when accessed
            commits = proxyFactory.createLazyList { 
                commitModuleConnectionRepository.findCommitsByModule(id).map { commitMapper.toDomain(it) } 
            },
            files = proxyFactory.createLazyList { 
                moduleFileConnectionRepository.findFilesByModule(id).map { fileMapper.toDomain(it) } 
            },
            childModules = proxyFactory.createLazyList { 
                moduleModuleConnectionRepository.findChildModulesByModule(id).map { arangoModuleMapper.toDomain(it) } 
            },
            parentModules = proxyFactory.createLazyList { 
                moduleModuleConnectionRepository.findParentModulesByModule(id).map { arangoModuleMapper.toDomain(it) } 
            }
        )
    }

    /**
     * Converts a list of SQL ModuleEntity objects to a list of domain Module objects
     */
    override fun toDomainList(entities: Iterable<ModuleEntity>): List<Module> {
        return entities.map { toDomain(it) }
    }
}
