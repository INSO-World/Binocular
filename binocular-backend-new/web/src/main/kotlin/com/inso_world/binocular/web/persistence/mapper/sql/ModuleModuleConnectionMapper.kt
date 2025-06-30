package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.edge.domain.ModuleModuleConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IModuleDao
import com.inso_world.binocular.web.persistence.entity.sql.ModuleModuleConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class ModuleModuleConnectionMapper @Autowired constructor(
    private val moduleDao: IModuleDao
) : EntityMapper<ModuleModuleConnection, ModuleModuleConnectionEntity> {

    /**
     * Converts a domain ModuleModuleConnection to a SQL ModuleModuleConnectionEntity
     */
    override fun toEntity(domain: ModuleModuleConnection): ModuleModuleConnectionEntity {
        return ModuleModuleConnectionEntity(
            id = domain.id,
            fromModuleId = domain.from.id ?: throw IllegalStateException("From Module ID cannot be null"),
            toModuleId = domain.to.id ?: throw IllegalStateException("To Module ID cannot be null")
        )
    }

    /**
     * Converts a SQL ModuleModuleConnectionEntity to a domain ModuleModuleConnection
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: ModuleModuleConnectionEntity): ModuleModuleConnection {
        val fromModule = moduleDao.findById(entity.fromModuleId)
            ?: throw IllegalStateException("Module with ID ${entity.fromModuleId} not found")
        val toModule = moduleDao.findById(entity.toModuleId)
            ?: throw IllegalStateException("Module with ID ${entity.toModuleId} not found")

        return ModuleModuleConnection(
            id = entity.id,
            from = fromModule,
            to = toModule
        )
    }

    /**
     * Converts a list of SQL ModuleModuleConnectionEntity objects to a list of domain ModuleModuleConnection objects
     */
    override fun toDomainList(entities: Iterable<ModuleModuleConnectionEntity>): List<ModuleModuleConnection> {
        return entities.map { toDomain(it) }
    }
}
