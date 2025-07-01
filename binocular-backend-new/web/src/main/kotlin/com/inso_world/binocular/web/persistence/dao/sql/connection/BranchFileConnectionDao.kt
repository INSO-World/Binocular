package com.inso_world.binocular.web.persistence.dao.sql.connection

import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.edge.domain.BranchFileConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IBranchDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IBranchFileConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IFileDao
import com.inso_world.binocular.web.persistence.entity.sql.BranchEntity
import com.inso_world.binocular.web.persistence.entity.sql.FileEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * SQL implementation of BranchFileConnectionDao that uses direct JPA relationships
 * instead of intermediate connection entities.
 */
@Repository
@Profile("sql")
@Transactional
class BranchFileConnectionDao(
    @Autowired private val branchDao: IBranchDao,
    @Autowired private val fileDao: IFileDao
) : IBranchFileConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findFilesByBranch(branchId: String): List<File> {
        // Use the direct relationship between Branch and File
        val query = entityManager.createQuery(
            "SELECT b FROM BranchEntity b WHERE b.id = :branchId",
            BranchEntity::class.java
        )
        query.setParameter("branchId", branchId)
        val branchEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return branchEntity.files.map { fileDao.findById(it.id!!)!! }
    }

    override fun findBranchesByFile(fileId: String): List<Branch> {
        // Use the direct relationship between File and Branch
        val query = entityManager.createQuery(
            "SELECT f FROM FileEntity f WHERE f.id = :fileId",
            FileEntity::class.java
        )
        query.setParameter("fileId", fileId)
        val fileEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return fileEntity.branches.map { branchDao.findById(it.id!!)!! }
    }

    override fun save(connection: BranchFileConnection): BranchFileConnection {
        val branchId = connection.from.id ?: throw IllegalStateException("Branch ID cannot be null")
        val fileId = connection.to.id ?: throw IllegalStateException("File ID cannot be null")

        // Find the entities
        val branchEntity = entityManager.find(BranchEntity::class.java, branchId)
            ?: throw IllegalStateException("Branch with ID $branchId not found")
        val fileEntity = entityManager.find(FileEntity::class.java, fileId)
            ?: throw IllegalStateException("File with ID $fileId not found")

        // Add the relationship if it doesn't exist
        if (!fileEntity.branches.contains(branchEntity)) {
            fileEntity.branches.add(branchEntity)
            entityManager.merge(fileEntity)
        }

        // Generate a connection ID if not provided
        val connectionId = connection.id ?: UUID.randomUUID().toString()

        // Return the connection with the branch and file
        return BranchFileConnection(
            id = connectionId,
            from = branchDao.findById(branchId)!!,
            to = fileDao.findById(fileId)!!
        )
    }

    override fun deleteAll() {
        // Clear all relationships between branches and files
        val files = entityManager.createQuery("SELECT f FROM FileEntity f", FileEntity::class.java)
            .resultList
        files.forEach { file ->
            file.branches.clear()
            entityManager.merge(file)
        }
    }
}
