package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.persistence.dao.interfaces.IBranchFileFileConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IFileDao
import com.inso_world.binocular.web.persistence.entity.sql.FileEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * SQL implementation of BranchFileFileConnectionDao that uses direct JPA relationships
 * instead of intermediate connection entities.
 */
@Repository
@Profile("sql")
@Transactional
class BranchFileFileConnectionDao(
    @Autowired private val fileDao: IFileDao
) : IBranchFileFileConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findFilesByBranchFile(branchFileId: String): List<File> {
        // Use the direct relationship between File entities (outgoingFiles)
        val query = entityManager.createQuery(
            "SELECT f FROM FileEntity f WHERE f.id = :branchFileId",
            FileEntity::class.java
        )
        query.setParameter("branchFileId", branchFileId)
        val branchFileEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return branchFileEntity.outgoingFiles.map { fileDao.findById(it.id!!)!! }
    }

    override fun findBranchFilesByFile(fileId: String): List<File> {
        // Use the direct relationship between File entities (incomingFiles)
        val query = entityManager.createQuery(
            "SELECT f FROM FileEntity f WHERE f.id = :fileId",
            FileEntity::class.java
        )
        query.setParameter("fileId", fileId)
        val fileEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return fileEntity.incomingFiles.map { fileDao.findById(it.id!!)!! }
    }

    override fun deleteAll() {
        // Clear all relationships between files
        val files = entityManager.createQuery("FROM FileEntity", FileEntity::class.java).resultList
        for (file in files) {
            file.outgoingFiles.clear()
            entityManager.merge(file)
        }
    }
}
