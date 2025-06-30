package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Build
import com.inso_world.binocular.web.persistence.dao.interfaces.IBuildDao
import com.inso_world.binocular.web.persistence.entity.sql.BuildEntity
import com.inso_world.binocular.web.persistence.mapper.sql.BuildMapper
import com.inso_world.binocular.web.persistence.model.Page
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * SQL implementation of IBuildDao.
 */
@Repository
@Profile("sql")
@Transactional
class BuildDao(
  @Autowired private val buildMapper: BuildMapper
) : IBuildDao {

  @PersistenceContext
  private lateinit var entityManager: EntityManager

  override fun findById(id: String): Build? {
    val entity = entityManager.find(BuildEntity::class.java, id) ?: return null
    return buildMapper.toDomain(entity)
  }

  override fun findAll(): Iterable<Build> {
    val query = entityManager.createQuery("FROM BuildEntity", BuildEntity::class.java)
    val entities = query.resultList
    return buildMapper.toDomainList(entities)
  }

  override fun findAll(pageable: Pageable): Page<Build> {
    val query = entityManager.createQuery("FROM BuildEntity", BuildEntity::class.java)
    val countQuery = entityManager.createQuery("SELECT COUNT(b) FROM BuildEntity b", Long::class.java)
    val totalElements = countQuery.singleResult

    val entities = query
      .setFirstResult(pageable.pageNumber * pageable.pageSize)
      .setMaxResults(pageable.pageSize)
      .resultList

    val content = buildMapper.toDomainList(entities)
    return Page(content, totalElements, pageable)
  }

  override fun create(entity: Build): Build {
    val buildEntity = buildMapper.toEntity(entity)
    entityManager.persist(buildEntity)
    return buildMapper.toDomain(buildEntity)
  }

  override fun update(entity: Build): Build {
    val buildEntity = buildMapper.toEntity(entity)
    val mergedEntity = entityManager.merge(buildEntity)
    return buildMapper.toDomain(mergedEntity)
  }

  override fun updateAndFlush(entity: Build): Build {
    val result = update(entity)
    entityManager.flush()
    return result
  }

  override fun delete(entity: Build) {
    val buildEntity = buildMapper.toEntity(entity)
    val managedEntity = entityManager.merge(buildEntity)
    entityManager.remove(managedEntity)
  }

  override fun deleteById(id: String) {
    val entity = entityManager.find(BuildEntity::class.java, id) ?: return
    entityManager.remove(entity)
  }

  /**
   * Delete all entities
   */
  override fun deleteAll() {
    val query = entityManager.createQuery("DELETE FROM BuildEntity")
    query.executeUpdate()
  }

  /**
   * Save an entity (create or update)
   * For SQL, this is the same as create or update depending on whether the entity exists
   */
  override fun save(entity: Build): Build {
    val buildEntity = buildMapper.toEntity(entity)
    return if (entityManager.find(BuildEntity::class.java, buildEntity.id) != null) {
      update(entity)
    } else {
      create(entity)
    }
  }

  /**
   * Save multiple entities
   */
  override fun saveAll(entities: List<Build>): Iterable<Build> {
    return entities.map { save(it) }
  }
}
