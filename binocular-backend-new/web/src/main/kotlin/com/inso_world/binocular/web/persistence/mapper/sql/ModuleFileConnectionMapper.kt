package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.edge.domain.ModuleFileConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IFileDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IModuleDao
import com.inso_world.binocular.web.persistence.entity.sql.ModuleFileConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class ModuleFileConnectionMapper @Autowired constructor(
    private val moduleDao: IModuleDao,
    private val fileDao: IFileDao
) : EntityMapper<ModuleFileConnection, ModuleFileConnectionEntity> {

    /**
     * Converts a domain ModuleFileConnection to a SQL ModuleFileConnectionEntity
     */
    override fun toEntity(domain: ModuleFileConnection): ModuleFileConnectionEntity {
        return ModuleFileConnectionEntity(
            id = domain.id,
            moduleId = domain.from.id ?: throw IllegalStateException("Module ID cannot be null"),
            fileId = domain.to.id ?: throw IllegalStateException("File ID cannot be null")
        )
    }

    /**
     * Converts a SQL ModuleFileConnectionEntity to a domain ModuleFileConnection
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: ModuleFileConnectionEntity): ModuleFileConnection {
        val module = moduleDao.findById(entity.moduleId)
            ?: throw IllegalStateException("Module with ID ${entity.moduleId} not found")
        val file = fileDao.findById(entity.fileId)
            ?: throw IllegalStateException("File with ID ${entity.fileId} not found")

        return ModuleFileConnection(
            id = entity.id,
            from = module,
            to = file
        )
    }

    /**
     * Converts a list of SQL ModuleFileConnectionEntity objects to a list of domain ModuleFileConnection objects
     */
    override fun toDomainList(entities: Iterable<ModuleFileConnectionEntity>): List<ModuleFileConnection> {
        return entities.map { toDomain(it) }
    }
}
