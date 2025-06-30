package com.inso_world.binocular.web.persistence.mapper.sql

import com.inso_world.binocular.web.entity.edge.domain.BranchFileConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IBranchDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IFileDao
import com.inso_world.binocular.web.persistence.entity.sql.BranchFileConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.EntityMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("sql")
class BranchFileConnectionMapper @Autowired constructor(
    private val branchDao: IBranchDao,
    private val fileDao: IFileDao
) : EntityMapper<BranchFileConnection, BranchFileConnectionEntity> {

    /**
     * Converts a domain BranchFileConnection to a SQL BranchFileConnectionEntity
     */
    override fun toEntity(domain: BranchFileConnection): BranchFileConnectionEntity {
        return BranchFileConnectionEntity(
            id = domain.id,
            branchId = domain.from.id ?: throw IllegalStateException("Branch ID cannot be null"),
            fileId = domain.to.id ?: throw IllegalStateException("File ID cannot be null")
        )
    }

    /**
     * Converts a SQL BranchFileConnectionEntity to a domain BranchFileConnection
     */
    @Transactional(readOnly = true)
    override fun toDomain(entity: BranchFileConnectionEntity): BranchFileConnection {
        val branch = branchDao.findById(entity.branchId)
            ?: throw IllegalStateException("Branch with ID ${entity.branchId} not found")
        val file = fileDao.findById(entity.fileId)
            ?: throw IllegalStateException("File with ID ${entity.fileId} not found")

        return BranchFileConnection(
            id = entity.id,
            from = branch,
            to = file
        )
    }

    /**
     * Converts a list of SQL BranchFileConnectionEntity objects to a list of domain BranchFileConnection objects
     */
    override fun toDomainList(entities: Iterable<BranchFileConnectionEntity>): List<BranchFileConnection> {
        return entities.map { toDomain(it) }
    }
}
