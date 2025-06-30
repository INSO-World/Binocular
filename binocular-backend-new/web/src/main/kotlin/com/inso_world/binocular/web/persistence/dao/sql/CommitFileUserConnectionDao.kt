package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.entity.edge.domain.CommitFileUserConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitFileUserConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IFileDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IUserDao
import com.inso_world.binocular.web.persistence.entity.sql.CommitFileUserConnectionEntity
import com.inso_world.binocular.web.persistence.mapper.sql.CommitFileUserConnectionMapper
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
class CommitFileUserConnectionDao(
    @Autowired private val commitFileUserConnectionMapper: CommitFileUserConnectionMapper,
    @Autowired private val fileDao: IFileDao,
    @Autowired private val userDao: IUserDao
) : ICommitFileUserConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findUsersByFile(fileId: String): List<User> {
        val query = entityManager.createQuery(
            "SELECT u FROM UserEntity u JOIN CommitFileUserConnectionEntity c ON u.id = c.userId WHERE c.fileId = :fileId",
            com.inso_world.binocular.web.persistence.entity.sql.UserEntity::class.java
        )
        query.setParameter("fileId", fileId)
        val userEntities = query.resultList
        
        // Convert SQL entities to domain models
        return userEntities.map { userDao.findById(it.id!!)!! }
    }

    override fun findFilesByUser(userId: String): List<File> {
        val query = entityManager.createQuery(
            "SELECT f FROM FileEntity f JOIN CommitFileUserConnectionEntity c ON f.id = c.fileId WHERE c.userId = :userId",
            com.inso_world.binocular.web.persistence.entity.sql.FileEntity::class.java
        )
        query.setParameter("userId", userId)
        val fileEntities = query.resultList
        
        // Convert SQL entities to domain models
        return fileEntities.map { fileDao.findById(it.id!!)!! }
    }

    override fun save(connection: CommitFileUserConnection): CommitFileUserConnection {
        val entity = commitFileUserConnectionMapper.toEntity(connection)
        
        // Generate ID if not provided
        if (entity.id == null) {
            entity.id = UUID.randomUUID().toString()
        }
        
        // Check if entity already exists
        val existingEntity = entityManager.createQuery(
            "FROM CommitFileUserConnectionEntity WHERE fileId = :fileId AND userId = :userId",
            CommitFileUserConnectionEntity::class.java
        )
            .setParameter("fileId", entity.fileId)
            .setParameter("userId", entity.userId)
            .resultList
            .firstOrNull()
        
        if (existingEntity != null) {
            // Update existing entity
            existingEntity.id = entity.id
            val mergedEntity = entityManager.merge(existingEntity)
            return commitFileUserConnectionMapper.toDomain(mergedEntity)
        } else {
            // Create new entity
            entityManager.persist(entity)
            return commitFileUserConnectionMapper.toDomain(entity)
        }
    }

    override fun deleteAll() {
        entityManager.createQuery("DELETE FROM CommitFileUserConnectionEntity").executeUpdate()
    }
}
