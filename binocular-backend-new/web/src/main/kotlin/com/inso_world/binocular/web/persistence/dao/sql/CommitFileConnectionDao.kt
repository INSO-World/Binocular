package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.edge.domain.CommitFileConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitFileConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IFileDao
import com.inso_world.binocular.web.persistence.entity.sql.CommitFileConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.sql.CommitFileConnectionMapper
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
class CommitFileConnectionDao(
    @Autowired private val commitFileConnectionMapper: CommitFileConnectionMapper,
    @Autowired private val commitDao: ICommitDao,
    @Autowired private val fileDao: IFileDao
) : ICommitFileConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findFilesByCommit(commitId: String): List<File> {
        val query = entityManager.createQuery(
            "SELECT f FROM FileEntity f JOIN CommitFileConnectionEntity c ON f.id = c.fileId WHERE c.commitId = :commitId",
            com.inso_world.binocular.web.persistence.entity.sql.FileEntity::class.java
        )
        query.setParameter("commitId", commitId)
        val fileEntities = query.resultList
        
        // Convert SQL entities to domain models
        return fileEntities.map { fileDao.findById(it.id!!)!! }
    }

    override fun findCommitsByFile(fileId: String): List<Commit> {
        val query = entityManager.createQuery(
            "SELECT c FROM CommitEntity c JOIN CommitFileConnectionEntity cf ON c.id = cf.commitId WHERE cf.fileId = :fileId",
            com.inso_world.binocular.web.persistence.entity.sql.CommitEntity::class.java
        )
        query.setParameter("fileId", fileId)
        val commitEntities = query.resultList
        
        // Convert SQL entities to domain models
        return commitEntities.map { commitDao.findById(it.id!!)!! }
    }

    override fun save(connection: CommitFileConnection): CommitFileConnection {
        val entity = commitFileConnectionMapper.toEntity(connection)
        
        // Generate ID if not provided
        if (entity.id == null) {
            entity.id = UUID.randomUUID().toString()
        }
        
        // Check if entity already exists
        val existingEntity = entityManager.createQuery(
            "FROM CommitFileConnectionEntity WHERE commitId = :commitId AND fileId = :fileId",
            CommitFileConnectionEntity::class.java
        )
            .setParameter("commitId", entity.commitId)
            .setParameter("fileId", entity.fileId)
            .resultList
            .firstOrNull()
        
        if (existingEntity != null) {
            // Update existing entity
            existingEntity.id = entity.id
            existingEntity.lineCount = entity.lineCount
            val mergedEntity = entityManager.merge(existingEntity)
            return commitFileConnectionMapper.toDomain(mergedEntity)
        } else {
            // Create new entity
            entityManager.persist(entity)
            return commitFileConnectionMapper.toDomain(entity)
        }
    }

    override fun deleteAll() {
        entityManager.createQuery("DELETE FROM CommitFileConnectionEntity").executeUpdate()
    }
}
