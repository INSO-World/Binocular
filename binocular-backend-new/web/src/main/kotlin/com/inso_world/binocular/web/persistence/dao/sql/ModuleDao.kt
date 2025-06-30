package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.persistence.dao.interfaces.IModuleDao
import com.inso_world.binocular.web.persistence.entity.sql.ModuleEntity
import com.inso_world.binocular.web.persistence.mapper.sql.ModuleMapper
import com.inso_world.binocular.web.persistence.model.Page
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Profile("sql")
@Transactional
class ModuleDao(
  @Autowired private val moduleMapper: ModuleMapper
) : IModuleDao {

  @PersistenceContext
  private lateinit var entityManager: EntityManager

  override fun findById(id: String): Module? {
    val entity = entityManager.find(ModuleEntity::class.java, id) ?: return null
    return moduleMapper.toDomain(entity)
  }

  override fun findAll(): Iterable<Module> {
    val query = entityManager.createQuery("FROM ModuleEntity", ModuleEntity::class.java)
    val entities = query.resultList
    return moduleMapper.toDomainList(entities)
  }

  override fun findAll(pageable: Pageable): Page<Module> {
    val query = entityManager.createQuery("FROM ModuleEntity", ModuleEntity::class.java)
    val countQuery = entityManager.createQuery("SELECT COUNT(m) FROM ModuleEntity m", Long::class.java)
    val totalElements = countQuery.singleResult

    val entities = query
      .setFirstResult(pageable.pageNumber * pageable.pageSize)
      .setMaxResults(pageable.pageSize)
      .resultList

    val content = moduleMapper.toDomainList(entities)
    return Page(content, totalElements, pageable)
  }

  override fun create(entity: Module): Module {
    val moduleEntity = moduleMapper.toEntity(entity)
    entityManager.persist(moduleEntity)
    return moduleMapper.toDomain(moduleEntity)
  }

  override fun update(entity: Module): Module {
    val moduleEntity = moduleMapper.toEntity(entity)
    val mergedEntity = entityManager.merge(moduleEntity)
    return moduleMapper.toDomain(mergedEntity)
  }

  override fun updateAndFlush(entity: Module): Module {
    val result = update(entity)
    entityManager.flush()
    return result
  }

  override fun delete(entity: Module) {
    val moduleEntity = moduleMapper.toEntity(entity)
    val managedEntity = entityManager.merge(moduleEntity)
    entityManager.remove(managedEntity)
  }

  override fun deleteById(id: String) {
    val entity = entityManager.find(ModuleEntity::class.java, id) ?: return
    entityManager.remove(entity)
  }

  /**
   * Delete all entities
   */
  override fun deleteAll() {
    val query = entityManager.createQuery("DELETE FROM ModuleEntity")
    query.executeUpdate()
  }

  /**
   * Save an entity (create or update)
   * For SQL, this is the same as create or update depending on whether the entity exists
   */
  override fun save(entity: Module): Module {
    val moduleEntity = moduleMapper.toEntity(entity)
    return if (entityManager.find(ModuleEntity::class.java, moduleEntity.id) != null) {
      update(entity)
    } else {
      create(entity)
    }
  }

  /**
   * Save multiple entities
   */
  override fun saveAll(entities: List<Module>): Iterable<Module> {
    return entities.map { save(it) }
  }
}
