package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.edge.domain.CommitModuleConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IModuleDao
import com.inso_world.binocular.web.persistence.entity.sql.CommitModuleConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class CommitModuleConnectionMapper @Autowired constructor(
    private val commitDao: ICommitDao,
    private val moduleDao: IModuleDao
) : EntityMapper<CommitModuleConnection, CommitModuleConnectionEntity> {

    /**
     * Converts a domain CommitModuleConnection to a SQL CommitModuleConnectionEntity
     */
    override fun toEntity(domain: CommitModuleConnection): CommitModuleConnectionEntity {
        return CommitModuleConnectionEntity(
            id = domain.id,
            commitId = domain.from.id ?: throw IllegalStateException("Commit ID cannot be null"),
            moduleId = domain.to.id ?: throw IllegalStateException("Module ID cannot be null")
        )
    }

    /**
     * Converts a SQL CommitModuleConnectionEntity to a domain CommitModuleConnection
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: CommitModuleConnectionEntity): CommitModuleConnection {
        val commit = commitDao.findById(entity.commitId)
            ?: throw IllegalStateException("Commit with ID ${entity.commitId} not found")
        val module = moduleDao.findById(entity.moduleId)
            ?: throw IllegalStateException("Module with ID ${entity.moduleId} not found")

        return CommitModuleConnection(
            id = entity.id,
            from = commit,
            to = module
        )
    }

    /**
     * Converts a list of SQL CommitModuleConnectionEntity objects to a list of domain CommitModuleConnection objects
     */
    override fun toDomainList(entities: Iterable<CommitModuleConnectionEntity>): List<CommitModuleConnection> {
        return entities.map { toDomain(it) }
    }
}
