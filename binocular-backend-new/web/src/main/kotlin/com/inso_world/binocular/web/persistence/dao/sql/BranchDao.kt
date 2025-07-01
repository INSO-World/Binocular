package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.persistence.dao.interfaces.IBranchDao
import com.inso_world.binocular.web.persistence.entity.sql.BranchEntity
import com.inso_world.binocular.web.persistence.mapper.sql.BranchMapper
import com.inso_world.binocular.web.persistence.model.Page
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * SQL implementation of IBranchDao.
 */
@Repository
@Profile("sql")
@Transactional
class BranchDao(
  @Autowired private val branchMapper: BranchMapper
) : IBranchDao {

  @PersistenceContext
  private lateinit var entityManager: EntityManager

  override fun findById(id: String): Branch? {
    val entity = entityManager.find(BranchEntity::class.java, id) ?: return null
    return branchMapper.toDomain(entity)
  }

  override fun findAll(): Iterable<Branch> {
    val query = entityManager.createQuery("FROM BranchEntity", BranchEntity::class.java)
    val entities = query.resultList
    return branchMapper.toDomainList(entities)
  }

  override fun findAll(pageable: Pageable): Page<Branch> {
    val query = entityManager.createQuery("FROM BranchEntity", BranchEntity::class.java)
    val countQuery = entityManager.createQuery("SELECT COUNT(b) FROM BranchEntity b", Long::class.java)
    val totalElements = countQuery.singleResult

    val entities = query
      .setFirstResult(pageable.pageNumber * pageable.pageSize)
      .setMaxResults(pageable.pageSize)
      .resultList

    val content = branchMapper.toDomainList(entities)
    return Page(content, totalElements, pageable)
  }

  override fun create(entity: Branch): Branch {
    val branchEntity = branchMapper.toEntity(entity)
    entityManager.persist(branchEntity)
    return branchMapper.toDomain(branchEntity)
  }

  override fun update(entity: Branch): Branch {
    val branchEntity = branchMapper.toEntity(entity)
    val mergedEntity = entityManager.merge(branchEntity)
    return branchMapper.toDomain(mergedEntity)
  }

  override fun updateAndFlush(entity: Branch): Branch {
    val result = update(entity)
    entityManager.flush()
    return result
  }

  override fun delete(entity: Branch) {
    val branchEntity = branchMapper.toEntity(entity)
    val managedEntity = entityManager.merge(branchEntity)
    entityManager.remove(managedEntity)
  }

  override fun deleteById(id: String) {
    val entity = entityManager.find(BranchEntity::class.java, id) ?: return
    entityManager.remove(entity)
  }

  /**
   * Delete all entities
   */
  override fun deleteAll() {
    val branches = entityManager.createQuery("SELECT b FROM BranchEntity b", BranchEntity::class.java)
      .resultList
    branches.forEach { entityManager.remove(it) }
  }

  /**
   * Save an entity (create or update)
   * For SQL, this is the same as create or update depending on whether the entity exists
   */
  override fun save(entity: Branch): Branch {
    val branchEntity = branchMapper.toEntity(entity)
    return if (entityManager.find(BranchEntity::class.java, branchEntity.id) != null) {
      update(entity)
    } else {
      create(entity)
    }
  }

  /**
   * Save multiple entities
   */
  override fun saveAll(entities: List<Branch>): Iterable<Branch> {
    return entities.map { save(it) }
  }
}
