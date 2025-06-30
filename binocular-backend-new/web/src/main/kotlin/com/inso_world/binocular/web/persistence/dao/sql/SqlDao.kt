package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.exception.NotFoundException
import com.inso_world.binocular.web.persistence.dao.interfaces.GenericDao
import com.inso_world.binocular.web.persistence.model.Page
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.io.Serializable

@Repository
@Profile("sql")
class SqlDao<T, I : Serializable> : GenericDao<T, I> {

  private lateinit var clazz: Class<T>

  @PersistenceContext
  protected lateinit var entityManager: EntityManager

  override fun setClazz(clazz: Class<T>) {
    this.clazz = clazz
  }

  override fun findById(id: I): T? =
    entityManager.find(clazz, id)

  override fun findAll(): List<T> =
    entityManager.createQuery("FROM ${clazz.name}", clazz).resultList

  override fun findAll(pageable: Pageable): Page<T> {
    val query = entityManager.createQuery("FROM ${clazz.name}", clazz)
    val totalElements = entityManager.createQuery("SELECT COUNT(e) FROM ${clazz.name} e", Long::class.java).singleResult

    val content = query
      .setFirstResult(pageable.pageNumber * pageable.pageSize)
      .setMaxResults(pageable.pageSize)
      .resultList

    return Page(content, totalElements, pageable)
  }

  override fun create(entity: T): T {
    entityManager.persist(entity)
    return entity
  }

  override fun update(entity: T): T =
    entityManager.merge(entity)

  override fun updateAndFlush(entity: T): T {
    val updated = entityManager.merge(entity)
    entityManager.flush()
    return updated
  }

  override fun delete(entity: T) {
    entityManager.remove(entity)
  }

  override fun deleteById(id: I) {
    val entity = findById(id) ?: throw NotFoundException("Entity not found")
    delete(entity)
  }

  /**
   * Delete all entities
   */
  override fun deleteAll() {
    entityManager.createQuery("DELETE FROM ${clazz.name}").executeUpdate()
  }

  /**
   * Save an entity (create or update)
   * For SQL, this is the same as create or update depending on whether the entity exists
   */
  override fun save(entity: T): T {
    return if (entityManager.contains(entity)) {
      update(entity)
    } else {
      create(entity)
    }
  }

  /**
   * Save multiple entities
   */
  override fun saveAll(entities: List<T>): Iterable<T> {
    return entities.map { save(it) }
  }
}
