package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.edge.domain.BranchFileFileConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IFileDao
import com.inso_world.binocular.web.persistence.entity.sql.BranchFileFileConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class BranchFileFileConnectionMapper @Autowired constructor(
    private val fileDao: IFileDao
) : EntityMapper<BranchFileFileConnection, BranchFileFileConnectionEntity> {

    /**
     * Converts a domain BranchFileFileConnection to a SQL BranchFileFileConnectionEntity
     */
    override fun toEntity(domain: BranchFileFileConnection): BranchFileFileConnectionEntity {
        return BranchFileFileConnectionEntity(
            id = domain.id,
            branchFileId = domain.from.id ?: throw IllegalStateException("BranchFile ID cannot be null"),
            fileId = domain.to.id ?: throw IllegalStateException("File ID cannot be null")
        )
    }

    /**
     * Converts a SQL BranchFileFileConnectionEntity to a domain BranchFileFileConnection
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: BranchFileFileConnectionEntity): BranchFileFileConnection {
        val branchFile = fileDao.findById(entity.branchFileId)
            ?: throw IllegalStateException("BranchFile with ID ${entity.branchFileId} not found")
        val file = fileDao.findById(entity.fileId)
            ?: throw IllegalStateException("File with ID ${entity.fileId} not found")

        return BranchFileFileConnection(
            id = entity.id,
            from = branchFile,
            to = file
        )
    }

    /**
     * Converts a list of SQL BranchFileFileConnectionEntity objects to a list of domain BranchFileFileConnection objects
     */
    override fun toDomainList(entities: Iterable<BranchFileFileConnectionEntity>): List<BranchFileFileConnection> {
        return entities.map { toDomain(it) }
    }
}
