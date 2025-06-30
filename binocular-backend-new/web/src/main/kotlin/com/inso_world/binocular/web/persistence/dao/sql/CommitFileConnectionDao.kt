package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.edge.domain.CommitFileConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitFileConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IFileDao
import com.inso_world.binocular.web.persistence.entity.sql.CommitEntity
import com.inso_world.binocular.web.persistence.entity.sql.FileEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * SQL implementation of CommitFileConnectionDao that uses direct JPA relationships
 * instead of intermediate connection entities.
 */
@Repository
@Profile("sql")
@Transactional
class CommitFileConnectionDao(
    @Autowired private val commitDao: ICommitDao,
    @Autowired private val fileDao: IFileDao
) : ICommitFileConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findFilesByCommit(commitId: String): List<File> {
        // Use the direct relationship between Commit and File
        val query = entityManager.createQuery(
            "SELECT c FROM CommitEntity c WHERE c.id = :commitId",
            CommitEntity::class.java
        )
        query.setParameter("commitId", commitId)
        val commitEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return commitEntity.files.map { fileDao.findById(it.id!!)!! }
    }

    override fun findCommitsByFile(fileId: String): List<Commit> {
        // Use the direct relationship between File and Commit
        val query = entityManager.createQuery(
            "SELECT f FROM FileEntity f WHERE f.id = :fileId",
            FileEntity::class.java
        )
        query.setParameter("fileId", fileId)
        val fileEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return fileEntity.commits.map { commitDao.findById(it.id!!)!! }
    }

    override fun save(connection: CommitFileConnection): CommitFileConnection {
        val commitId = connection.from.id ?: throw IllegalStateException("Commit ID cannot be null")
        val fileId = connection.to.id ?: throw IllegalStateException("File ID cannot be null")
        val lineCount = connection.lineCount

        // Find the entities
        val commitEntity = entityManager.find(CommitEntity::class.java, commitId)
            ?: throw IllegalStateException("Commit with ID $commitId not found")
        val fileEntity = entityManager.find(FileEntity::class.java, fileId)
            ?: throw IllegalStateException("File with ID $fileId not found")

        // Add the relationship if it doesn't exist
        if (!commitEntity.files.contains(fileEntity)) {
            commitEntity.files.add(fileEntity)
            entityManager.merge(commitEntity)
        }

        // Note: We're losing the lineCount information here since it's not part of the direct relationship
        // If lineCount is important, we would need to keep the connection entity or add a field to track it

        // Generate a connection ID if not provided
        val connectionId = connection.id ?: UUID.randomUUID().toString()

        // Return the connection with the commit and file
        return CommitFileConnection(
            id = connectionId,
            from = commitDao.findById(commitId)!!,
            to = fileDao.findById(fileId)!!,
            lineCount = lineCount
        )
    }

    override fun deleteAll() {
        // Clear all relationships between commits and files
        val commits = entityManager.createQuery("FROM CommitEntity", CommitEntity::class.java).resultList
        for (commit in commits) {
            commit.files.clear()
            entityManager.merge(commit)
        }
    }
}
