package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.web.persistence.entity.sql.CommitEntity
import com.inso_world.binocular.web.persistence.mapper.sql.CommitMapper
import com.inso_world.binocular.web.persistence.model.Page
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * SQL implementation of ICommitDao.
 */
@Repository
@Profile("sql")
@Transactional
class CommitDao(
  @Autowired private val commitMapper: CommitMapper
) : ICommitDao {

  @PersistenceContext
  private lateinit var entityManager: EntityManager

  override fun findById(id: String): Commit? {
    val entity = entityManager.find(CommitEntity::class.java, id) ?: return null
    return commitMapper.toDomain(entity)
  }

  override fun findAll(): Iterable<Commit> {
    val query = entityManager.createQuery("FROM CommitEntity", CommitEntity::class.java)
    val entities = query.resultList
    return commitMapper.toDomainList(entities)
  }

  override fun findAll(pageable: Pageable): Page<Commit> {
    val query = entityManager.createQuery("FROM CommitEntity", CommitEntity::class.java)
    val countQuery = entityManager.createQuery("SELECT COUNT(c) FROM CommitEntity c", Long::class.java)
    val totalElements = countQuery.singleResult

    val entities = query
      .setFirstResult(pageable.pageNumber * pageable.pageSize)
      .setMaxResults(pageable.pageSize)
      .resultList

    val content = commitMapper.toDomainList(entities)
    return Page(content, totalElements, pageable)
  }

  override fun create(entity: Commit): Commit {
    val commitEntity = commitMapper.toEntity(entity)
    entityManager.persist(commitEntity)
    return commitMapper.toDomain(commitEntity)
  }

  override fun update(entity: Commit): Commit {
    val commitEntity = commitMapper.toEntity(entity)
    val mergedEntity = entityManager.merge(commitEntity)
    return commitMapper.toDomain(mergedEntity)
  }

  override fun updateAndFlush(entity: Commit): Commit {
    val result = update(entity)
    entityManager.flush()
    return result
  }

  override fun delete(entity: Commit) {
    val commitEntity = commitMapper.toEntity(entity)
    val managedEntity = entityManager.merge(commitEntity)
    entityManager.remove(managedEntity)
  }

  override fun deleteById(id: String) {
    val entity = entityManager.find(CommitEntity::class.java, id) ?: return
    entityManager.remove(entity)
  }

  /**
   * Delete all entities
   */
  override fun deleteAll() {
    val commits = entityManager.createQuery("SELECT c FROM CommitEntity c", CommitEntity::class.java)
      .resultList
    commits.forEach { entityManager.remove(it) }
  }

  /**
   * Save an entity (create or update)
   * For SQL, this is the same as create or update depending on whether the entity exists
   */
  override fun save(entity: Commit): Commit {
    val commitEntity = commitMapper.toEntity(entity)
    return if (entityManager.find(CommitEntity::class.java, commitEntity.id) != null) {
      update(entity)
    } else {
      create(entity)
    }
  }

  /**
   * Save multiple entities
   */
  override fun saveAll(entities: List<Commit>): Iterable<Commit> {
    return entities.map { save(it) }
  }
}
