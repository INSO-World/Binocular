package com.inso_world.binocular.web.persistence.mapper.arangodb

import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.persistence.entity.arangodb.ModuleEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import com.inso_world.binocular.web.persistence.proxy.RelationshipProxyFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("nosql", "arangodb")
class ModuleMapper @Autowired constructor(
    private val proxyFactory: RelationshipProxyFactory,
    @Lazy private val commitMapper: CommitMapper,
    @Lazy private val fileMapper: FileMapper
) : EntityMapper<Module, ModuleEntity> {

    /**
     * Converts a domain Module to an ArangoDB ModuleEntity
     */
    override fun toEntity(domain: Module): ModuleEntity {
        return ModuleEntity(
            id = domain.id,
            path = domain.path,
            // Relationships are handled by ArangoDB through edges
            commits = null,
            files = null,
            childModules = null,
            parentModules = null
        )
    }

    /**
     * Converts an ArangoDB ModuleEntity to a domain Module
     * 
     * Uses lazy loading proxies for relationships, which will only be loaded
     * when accessed. This provides a consistent API regardless of the database
     * implementation and avoids the N+1 query problem.
     */
    override fun toDomain(entity: ModuleEntity): Module {
        return Module(
            id = entity.id,
            path = entity.path,
            commits = proxyFactory.createLazyList {
                (entity.commits ?: emptyList()).map { commitEntity -> 
                    commitMapper.toDomain(commitEntity) 
                } 
            },
            files = proxyFactory.createLazyList { 
                (entity.files ?: emptyList()).map { fileEntity -> 
                    fileMapper.toDomain(fileEntity) 
                } 
            },
            childModules = proxyFactory.createLazyList { 
                (entity.childModules ?: emptyList()).map { childModuleEntity -> 
                    toDomain(childModuleEntity) 
                } 
            },
            parentModules = proxyFactory.createLazyList { 
                (entity.parentModules ?: emptyList()).map { parentModuleEntity -> 
                    toDomain(parentModuleEntity) 
                } 
            }
        )
    }

    /**
     * Converts a list of ArangoDB ModuleEntity objects to a list of domain Module objects
     */
    override fun toDomainList(entities: Iterable<ModuleEntity>): List<Module> {
        return entities.map { toDomain(it) }
    }
}
