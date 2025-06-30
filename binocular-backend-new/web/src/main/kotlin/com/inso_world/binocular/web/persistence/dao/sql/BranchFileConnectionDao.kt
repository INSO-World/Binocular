package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.edge.domain.BranchFileConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IBranchDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IBranchFileConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IFileDao
import com.inso_world.binocular.web.persistence.entity.sql.BranchFileConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.sql.BranchFileConnectionMapper
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
@Profile("sql")
@Transactional
class BranchFileConnectionDao(
    @Autowired private val branchFileConnectionMapper: BranchFileConnectionMapper,
    @Autowired private val branchDao: IBranchDao,
    @Autowired private val fileDao: IFileDao
) : IBranchFileConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findFilesByBranch(branchId: String): List<File> {
        val query = entityManager.createQuery(
            "SELECT f FROM FileEntity f JOIN BranchFileConnectionEntity c ON f.id = c.fileId WHERE c.branchId = :branchId",
            com.inso_world.binocular.web.persistence.entity.sql.FileEntity::class.java
        )
        query.setParameter("branchId", branchId)
        val fileEntities = query.resultList
        
        // Convert SQL entities to domain models
        return fileEntities.map { fileDao.findById(it.id!!)!! }
    }

    override fun findBranchesByFile(fileId: String): List<Branch> {
        val query = entityManager.createQuery(
            "SELECT b FROM BranchEntity b JOIN BranchFileConnectionEntity c ON b.id = c.branchId WHERE c.fileId = :fileId",
            com.inso_world.binocular.web.persistence.entity.sql.BranchEntity::class.java
        )
        query.setParameter("fileId", fileId)
        val branchEntities = query.resultList
        
        // Convert SQL entities to domain models
        return branchEntities.map { branchDao.findById(it.id!!)!! }
    }

    override fun save(connection: BranchFileConnection): BranchFileConnection {
        val entity = branchFileConnectionMapper.toEntity(connection)
        
        // Generate ID if not provided
        if (entity.id == null) {
            entity.id = UUID.randomUUID().toString()
        }
        
        // Check if entity already exists
        val existingEntity = entityManager.createQuery(
            "FROM BranchFileConnectionEntity WHERE branchId = :branchId AND fileId = :fileId",
            BranchFileConnectionEntity::class.java
        )
            .setParameter("branchId", entity.branchId)
            .setParameter("fileId", entity.fileId)
            .resultList
            .firstOrNull()
        
        if (existingEntity != null) {
            // Update existing entity
            existingEntity.id = entity.id
            val mergedEntity = entityManager.merge(existingEntity)
            return branchFileConnectionMapper.toDomain(mergedEntity)
        } else {
            // Create new entity
            entityManager.persist(entity)
            return branchFileConnectionMapper.toDomain(entity)
        }
    }

    override fun deleteAll() {
        entityManager.createQuery("DELETE FROM BranchFileConnectionEntity").executeUpdate()
    }
}
