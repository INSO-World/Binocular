package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.entity.edge.domain.ModuleModuleConnection
import com.inso_world.binocular.web.persistence.dao.interfaces.IModuleDao
import com.inso_world.binocular.web.persistence.dao.interfaces.IModuleModuleConnectionDao
import com.inso_world.binocular.web.persistence.entity.sql.ModuleEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * SQL implementation of ModuleModuleConnectionDao that uses direct JPA relationships
 * instead of intermediate connection entities.
 */
@Repository
@Profile("sql")
@Transactional
class ModuleModuleConnectionDao(
    @Autowired private val moduleDao: IModuleDao
) : IModuleModuleConnectionDao {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findChildModules(parentModuleId: String): List<Module> {
        // Use the direct relationship between Module and its child Modules
        val query = entityManager.createQuery(
            "SELECT m FROM ModuleEntity m WHERE m.id = :parentModuleId",
            ModuleEntity::class.java
        )
        query.setParameter("parentModuleId", parentModuleId)
        val moduleEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return moduleEntity.childModules.map { moduleDao.findById(it.id!!)!! }
    }

    override fun findParentModules(childModuleId: String): List<Module> {
        // Use the direct relationship between Module and its parent Modules
        val query = entityManager.createQuery(
            "SELECT m FROM ModuleEntity m WHERE m.id = :childModuleId",
            ModuleEntity::class.java
        )
        query.setParameter("childModuleId", childModuleId)
        val moduleEntity = query.resultList.firstOrNull()
            ?: return emptyList()

        // Convert SQL entities to domain models
        return moduleEntity.parentModules.map { moduleDao.findById(it.id!!)!! }
    }

    override fun save(connection: ModuleModuleConnection): ModuleModuleConnection {
        val fromModuleId = connection.from.id ?: throw IllegalStateException("From Module ID cannot be null")
        val toModuleId = connection.to.id ?: throw IllegalStateException("To Module ID cannot be null")

        // Find the entities
        val fromModuleEntity = entityManager.find(ModuleEntity::class.java, fromModuleId)
            ?: throw IllegalStateException("From Module with ID $fromModuleId not found")
        val toModuleEntity = entityManager.find(ModuleEntity::class.java, toModuleId)
            ?: throw IllegalStateException("To Module with ID $toModuleId not found")

        // Add the relationship if it doesn't exist
        if (!fromModuleEntity.childModules.contains(toModuleEntity)) {
            fromModuleEntity.childModules.add(toModuleEntity)
            entityManager.merge(fromModuleEntity)
        }

        // Generate a connection ID if not provided
        val connectionId = connection.id ?: UUID.randomUUID().toString()

        // Return the connection with the from and to modules
        return ModuleModuleConnection(
            id = connectionId,
            from = moduleDao.findById(fromModuleId)!!,
            to = moduleDao.findById(toModuleId)!!
        )
    }

    override fun deleteAll() {
        // Clear all relationships between modules
        val modules = entityManager.createQuery("FROM ModuleEntity", ModuleEntity::class.java).resultList
        for (module in modules) {
            module.childModules.clear()
            entityManager.merge(module)
        }
    }
}
