package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.entity.edge.domain.CommitModuleConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitModuleConnectionDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IModuleDao
import com.inso_world.binocular.web.persistence.entity.sql.CommitEntity
import com.inso_world.binocular.web.persistence.entity.sql.ModuleEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * SQL implementation of CommitModuleConnectionDao that uses direct JPA relationships
 * instead of intermediate connection entities.
 */
@Repository
@Profile("sql")
@Transactional
class CommitModuleConnectionDao(
    @Autowired private val commitDao: ICommitDao,
    @Autowired private val moduleDao: IModuleDao
) : ICommitModuleConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findModulesByCommit(commitId: String): List<Module> {
        // Use the direct relationship between Commit and Module
        val query = entityManager.createQuery(
            "SELECT c FROM CommitEntity c WHERE c.id = :commitId",
            CommitEntity::class.java
        )
        query.setParameter("commitId", commitId)
        val commitEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return commitEntity.modules.map { moduleDao.findById(it.id!!)!! }
    }

    override fun findCommitsByModule(moduleId: String): List<Commit> {
        // Use the direct relationship between Module and Commit
        val query = entityManager.createQuery(
            "SELECT m FROM ModuleEntity m WHERE m.id = :moduleId",
            ModuleEntity::class.java
        )
        query.setParameter("moduleId", moduleId)
        val moduleEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return moduleEntity.commits.map { commitDao.findById(it.id!!)!! }
    }

    override fun save(connection: CommitModuleConnection): CommitModuleConnection {
        val commitId = connection.from.id ?: throw IllegalStateException("Commit ID cannot be null")
        val moduleId = connection.to.id ?: throw IllegalStateException("Module ID cannot be null")

        // Find the entities
        val commitEntity = entityManager.find(CommitEntity::class.java, commitId)
            ?: throw IllegalStateException("Commit with ID $commitId not found")
        val moduleEntity = entityManager.find(ModuleEntity::class.java, moduleId)
            ?: throw IllegalStateException("Module with ID $moduleId not found")

        // Add the relationship if it doesn't exist
        if (!commitEntity.modules.contains(moduleEntity)) {
            commitEntity.modules.add(moduleEntity)
            entityManager.merge(commitEntity)
        }

        // Generate a connection ID if not provided
        val connectionId = connection.id ?: UUID.randomUUID().toString()

        // Return the connection with the commit and module
        return CommitModuleConnection(
            id = connectionId,
            from = commitDao.findById(commitId)!!,
            to = moduleDao.findById(moduleId)!!
        )
    }

    override fun deleteAll() {
        // Clear all relationships between commits and modules
        val commits = entityManager.createQuery("FROM CommitEntity", CommitEntity::class.java).resultList
        for (commit in commits) {
            commit.modules.clear()
            entityManager.merge(commit)
        }
    }
}
