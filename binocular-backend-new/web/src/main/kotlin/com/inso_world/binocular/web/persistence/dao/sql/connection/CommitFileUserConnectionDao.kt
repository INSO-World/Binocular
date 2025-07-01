package com.inso_world.binocular.web.persistence.dao.sql.connection

import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.entity.edge.domain.CommitFileUserConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitFileUserConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IFileDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IUserDao
import com.inso_world.binocular.web.persistence.entity.sql.FileEntity
import com.inso_world.binocular.web.persistence.entity.sql.UserEntity
import com.inso_world.binocular.web.persistence.entity.sql.connections.CommitFileUserConnectionEntity
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
    @Autowired private val fileDao: IFileDao,
    @Autowired private val userDao: IUserDao
) : ICommitFileUserConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findUsersByFile(fileId: String): List<User> {
        val query = entityManager.createQuery(
            "SELECT u FROM UserEntity u JOIN CommitFileUserConnectionEntity c ON u.id = c.userId WHERE c.fileId = :fileId",
            UserEntity::class.java
        )
        query.setParameter("fileId", fileId)
        val userEntities = query.resultList

        // Convert SQL entities to domain models
        return userEntities.map { userDao.findById(it.id!!)!! }
    }

    override fun findFilesByUser(userId: String): List<File> {
        val query = entityManager.createQuery(
            "SELECT f FROM FileEntity f JOIN CommitFileUserConnectionEntity c ON f.id = c.fileId WHERE c.userId = :userId",
            FileEntity::class.java
        )
        query.setParameter("userId", userId)
        val fileEntities = query.resultList

        // Convert SQL entities to domain models
        return fileEntities.map { fileDao.findById(it.id!!)!! }
    }

    override fun save(connection: CommitFileUserConnection): CommitFileUserConnection {
        // Convert domain object to entity
        val entity = CommitFileUserConnectionEntity(
            id = connection.id,
            fileId = connection.from.id ?: throw IllegalStateException("File ID cannot be null"),
            userId = connection.to.id ?: throw IllegalStateException("User ID cannot be null")
        )

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

            // Convert entity to domain object
            val file = fileDao.findById(mergedEntity.fileId)
                ?: throw IllegalStateException("File with ID ${mergedEntity.fileId} not found")
            val user = userDao.findById(mergedEntity.userId)
                ?: throw IllegalStateException("User with ID ${mergedEntity.userId} not found")

            return CommitFileUserConnection(
                id = mergedEntity.id,
                from = file,
                to = user
            )
        } else {
            // Create new entity
            entityManager.persist(entity)

            // Convert entity to domain object
            val file = fileDao.findById(entity.fileId)
                ?: throw IllegalStateException("File with ID ${entity.fileId} not found")
            val user = userDao.findById(entity.userId)
                ?: throw IllegalStateException("User with ID ${entity.userId} not found")

            return CommitFileUserConnection(
                id = entity.id,
                from = file,
                to = user
            )
        }
    }

    override fun deleteAll() {
        val connections = entityManager.createQuery("SELECT c FROM CommitFileUserConnectionEntity c", CommitFileUserConnectionEntity::class.java)
            .resultList
        connections.forEach { entityManager.remove(it) }
    }
}
