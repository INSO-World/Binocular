package com.inso_world.binocular.core.persistence.dao.sql

import com.inso_world.binocular.core.persistence.dao.interfaces.IDao
import com.inso_world.binocular.core.persistence.exception.NotFoundException
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Example
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.io.Serializable


@Repository
@Profile("sql")
class SqlDao<T, I : Serializable> : IDao<T, I> {

  private val batchSize = 500;
  private lateinit var clazz: Class<T>
  private lateinit var repository: JpaRepository<T, I>

  fun setRepository(repo: JpaRepository<T, I>) {
    this.repository = repo
  }


  @PersistenceContext
  protected lateinit var entityManager: EntityManager

  fun setClazz(clazz: Class<T>) {
    this.clazz = clazz
  }

  override fun findById(id: I): T? =
    entityManager.find(clazz, id)

  override fun findAll(): List<T> =
    entityManager.createQuery("FROM ${clazz.name}", clazz).resultList

  override fun findAll(pageable: Pageable): List<T> =
    entityManager.createQuery("FROM ${clazz.name}", clazz)
      .setFirstResult(pageable.pageNumber * pageable.pageSize)
      .setMaxResults(pageable.pageSize)
      .resultList

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

  override fun <S : T> exists(example: Example<S>): Boolean {
    return this.repository.exists(example)
  }

  override fun saveAll(entities: Iterable<T>): Iterable<T> {
    return this.repository.saveAll(entities)
  }
}
