package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestDao
import com.inso_world.binocular.web.persistence.entity.sql.MergeRequestEntity
import com.inso_world.binocular.web.persistence.mapper.sql.MergeRequestMapper
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
class MergeRequestDao(
  @Autowired private val mergeRequestMapper: MergeRequestMapper
) : IMergeRequestDao {

  @PersistenceContext
  private lateinit var entityManager: EntityManager

  override fun findById(id: String): MergeRequest? {
    val entity = entityManager.find(MergeRequestEntity::class.java, id) ?: return null
    return mergeRequestMapper.toDomain(entity)
  }

  override fun findAll(): Iterable<MergeRequest> {
    val query = entityManager.createQuery("FROM MergeRequestEntity", MergeRequestEntity::class.java)
    val entities = query.resultList
    return mergeRequestMapper.toDomainList(entities)
  }

  override fun findAll(pageable: Pageable): Page<MergeRequest> {
    val query = entityManager.createQuery("FROM MergeRequestEntity", MergeRequestEntity::class.java)
    val countQuery = entityManager.createQuery("SELECT COUNT(m) FROM MergeRequestEntity m", Long::class.java)
    val totalElements = countQuery.singleResult

    val entities = query
      .setFirstResult(pageable.pageNumber * pageable.pageSize)
      .setMaxResults(pageable.pageSize)
      .resultList

    val content = mergeRequestMapper.toDomainList(entities)
    return Page(content, totalElements, pageable)
  }

  override fun create(entity: MergeRequest): MergeRequest {
    val mergeRequestEntity = mergeRequestMapper.toEntity(entity)
    entityManager.persist(mergeRequestEntity)
    return mergeRequestMapper.toDomain(mergeRequestEntity)
  }

  override fun update(entity: MergeRequest): MergeRequest {
    val mergeRequestEntity = mergeRequestMapper.toEntity(entity)
    val mergedEntity = entityManager.merge(mergeRequestEntity)
    return mergeRequestMapper.toDomain(mergedEntity)
  }

  override fun updateAndFlush(entity: MergeRequest): MergeRequest {
    val result = update(entity)
    entityManager.flush()
    return result
  }

  override fun delete(entity: MergeRequest) {
    val mergeRequestEntity = mergeRequestMapper.toEntity(entity)
    val managedEntity = entityManager.merge(mergeRequestEntity)
    entityManager.remove(managedEntity)
  }

  override fun deleteById(id: String) {
    val entity = entityManager.find(MergeRequestEntity::class.java, id) ?: return
    entityManager.remove(entity)
  }

  /**
   * Delete all entities
   */
  override fun deleteAll() {
    val mergeRequests = entityManager.createQuery("SELECT m FROM MergeRequestEntity m", MergeRequestEntity::class.java)
      .resultList
    mergeRequests.forEach { entityManager.remove(it) }
  }

  /**
   * Save an entity (create or update)
   * For SQL, this is the same as create or update depending on whether the entity exists
   */
  override fun save(entity: MergeRequest): MergeRequest {
    val mergeRequestEntity = mergeRequestMapper.toEntity(entity)
    return if (entityManager.find(MergeRequestEntity::class.java, mergeRequestEntity.id) != null) {
      update(entity)
    } else {
      create(entity)
    }
  }

  /**
   * Save multiple entities
   */
  override fun saveAll(entities: List<MergeRequest>): Iterable<MergeRequest> {
    return entities.map { save(it) }
  }
}
