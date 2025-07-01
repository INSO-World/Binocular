package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Milestone
import com.inso_world.binocular.web.persistence.dao.interfaces.IMilestoneDao
import com.inso_world.binocular.web.persistence.entity.sql.MilestoneEntity
import com.inso_world.binocular.web.persistence.mapper.sql.MilestoneMapper
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
class MilestoneDao(
  @Autowired private val milestoneMapper: MilestoneMapper
) : IMilestoneDao {

  @PersistenceContext
  private lateinit var entityManager: EntityManager

  override fun findById(id: String): Milestone? {
    val entity = entityManager.find(MilestoneEntity::class.java, id) ?: return null
    return milestoneMapper.toDomain(entity)
  }

  override fun findAll(): Iterable<Milestone> {
    val query = entityManager.createQuery("FROM MilestoneEntity", MilestoneEntity::class.java)
    val entities = query.resultList
    return milestoneMapper.toDomainList(entities)
  }

  override fun findAll(pageable: Pageable): Page<Milestone> {
    val query = entityManager.createQuery("FROM MilestoneEntity", MilestoneEntity::class.java)
    val countQuery = entityManager.createQuery("SELECT COUNT(m) FROM MilestoneEntity m", Long::class.java)
    val totalElements = countQuery.singleResult

    val entities = query
      .setFirstResult(pageable.pageNumber * pageable.pageSize)
      .setMaxResults(pageable.pageSize)
      .resultList

    val content = milestoneMapper.toDomainList(entities)
    return Page(content, totalElements, pageable)
  }

  override fun create(entity: Milestone): Milestone {
    val milestoneEntity = milestoneMapper.toEntity(entity)
    entityManager.persist(milestoneEntity)
    return milestoneMapper.toDomain(milestoneEntity)
  }

  override fun update(entity: Milestone): Milestone {
    val milestoneEntity = milestoneMapper.toEntity(entity)
    val mergedEntity = entityManager.merge(milestoneEntity)
    return milestoneMapper.toDomain(mergedEntity)
  }

  override fun updateAndFlush(entity: Milestone): Milestone {
    val result = update(entity)
    entityManager.flush()
    return result
  }

  override fun delete(entity: Milestone) {
    val milestoneEntity = milestoneMapper.toEntity(entity)
    val managedEntity = entityManager.merge(milestoneEntity)
    entityManager.remove(managedEntity)
  }

  override fun deleteById(id: String) {
    val entity = entityManager.find(MilestoneEntity::class.java, id) ?: return
    entityManager.remove(entity)
  }

  /**
   * Delete all entities
   */
  override fun deleteAll() {
    val milestones = entityManager.createQuery("SELECT m FROM MilestoneEntity m", MilestoneEntity::class.java)
      .resultList
    milestones.forEach { entityManager.remove(it) }
  }

  /**
   * Save an entity (create or update)
   * For SQL, this is the same as create or update depending on whether the entity exists
   */
  override fun save(entity: Milestone): Milestone {
    val milestoneEntity = milestoneMapper.toEntity(entity)
    return if (entityManager.find(MilestoneEntity::class.java, milestoneEntity.id) != null) {
      update(entity)
    } else {
      create(entity)
    }
  }

  /**
   * Save multiple entities
   */
  override fun saveAll(entities: List<Milestone>): Iterable<Milestone> {
    return entities.map { save(it) }
  }
}
