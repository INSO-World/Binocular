package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueDao
import com.inso_world.binocular.web.persistence.entity.sql.IssueEntity
import com.inso_world.binocular.web.persistence.mapper.sql.IssueMapper
import com.inso_world.binocular.web.persistence.model.Page
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * SQL implementation of IIssueDao.
 */
@Repository
@Profile("sql")
@Transactional
class IssueDao(
  @Autowired private val issueMapper: IssueMapper
) : IIssueDao {

  @PersistenceContext
  private lateinit var entityManager: EntityManager

  override fun findById(id: String): Issue? {
    val entity = entityManager.find(IssueEntity::class.java, id) ?: return null
    return issueMapper.toDomain(entity)
  }

  override fun findAll(): Iterable<Issue> {
    val query = entityManager.createQuery("FROM IssueEntity", IssueEntity::class.java)
    val entities = query.resultList
    return issueMapper.toDomainList(entities)
  }

  override fun findAll(pageable: Pageable): Page<Issue> {
    val query = entityManager.createQuery("FROM IssueEntity", IssueEntity::class.java)
    val countQuery = entityManager.createQuery("SELECT COUNT(i) FROM IssueEntity i", Long::class.java)
    val totalElements = countQuery.singleResult

    val entities = query
      .setFirstResult(pageable.pageNumber * pageable.pageSize)
      .setMaxResults(pageable.pageSize)
      .resultList

    val content = issueMapper.toDomainList(entities)
    return Page(content, totalElements, pageable)
  }

  override fun create(entity: Issue): Issue {
    val issueEntity = issueMapper.toEntity(entity)
    entityManager.persist(issueEntity)
    return issueMapper.toDomain(issueEntity)
  }

  override fun update(entity: Issue): Issue {
    val issueEntity = issueMapper.toEntity(entity)
    val mergedEntity = entityManager.merge(issueEntity)
    return issueMapper.toDomain(mergedEntity)
  }

  override fun updateAndFlush(entity: Issue): Issue {
    val result = update(entity)
    entityManager.flush()
    return result
  }

  override fun delete(entity: Issue) {
    val issueEntity = issueMapper.toEntity(entity)
    val managedEntity = entityManager.merge(issueEntity)
    entityManager.remove(managedEntity)
  }

  override fun deleteById(id: String) {
    val entity = entityManager.find(IssueEntity::class.java, id) ?: return
    entityManager.remove(entity)
  }

  /**
   * Delete all entities
   */
  override fun deleteAll() {
    val query = entityManager.createQuery("DELETE FROM IssueEntity")
    query.executeUpdate()
  }

  /**
   * Save an entity (create or update)
   * For SQL, this is the same as create or update depending on whether the entity exists
   */
  override fun save(entity: Issue): Issue {
    val issueEntity = issueMapper.toEntity(entity)
    return if (entityManager.find(IssueEntity::class.java, issueEntity.id) != null) {
      update(entity)
    } else {
      create(entity)
    }
  }

  /**
   * Save multiple entities
   */
  override fun saveAll(entities: List<Issue>): Iterable<Issue> {
    return entities.map { save(it) }
  }
}
