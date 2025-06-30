package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.edge.domain.BranchFileFileConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IBranchFileFileConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IFileDao
import com.inso_world.binocular.web.persistence.entity.sql.BranchFileFileConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.sql.BranchFileFileConnectionMapper
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Profile("sql")
@Transactional
class BranchFileFileConnectionDao(
    @Autowired private val branchFileFileConnectionMapper: BranchFileFileConnectionMapper,
    @Autowired private val fileDao: IFileDao
) : IBranchFileFileConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findFilesByBranchFile(branchFileId: String): List<File> {
        val query = entityManager.createQuery(
            "SELECT f FROM FileEntity f JOIN BranchFileFileConnectionEntity c ON f.id = c.fileId WHERE c.branchFileId = :branchFileId",
            com.inso_world.binocular.web.persistence.entity.sql.FileEntity::class.java
        )
        query.setParameter("branchFileId", branchFileId)
        val fileEntities = query.resultList
        
        // Convert SQL entities to domain models
        return fileEntities.map { fileDao.findById(it.id!!)!! }
    }

    override fun findBranchFilesByFile(fileId: String): List<File> {
        val query = entityManager.createQuery(
            "SELECT f FROM FileEntity f JOIN BranchFileFileConnectionEntity c ON f.id = c.branchFileId WHERE c.fileId = :fileId",
            com.inso_world.binocular.web.persistence.entity.sql.FileEntity::class.java
        )
        query.setParameter("fileId", fileId)
        val fileEntities = query.resultList
        
        // Convert SQL entities to domain models
        return fileEntities.map { fileDao.findById(it.id!!)!! }
    }

    override fun deleteAll() {
        entityManager.createQuery("DELETE FROM BranchFileFileConnectionEntity").executeUpdate()
    }
}
