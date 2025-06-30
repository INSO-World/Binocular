package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Note
import com.inso_world.binocular.web.persistence.dao.interfaces.INoteDao
import com.inso_world.binocular.web.persistence.entity.sql.NoteEntity
import com.inso_world.binocular.web.persistence.mapper.sql.NoteMapper
import com.inso_world.binocular.web.persistence.model.Page
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * SQL implementation of INoteDao.
 */
@Repository
@Profile("sql")
@Transactional
class NoteDao(
  @Autowired private val noteMapper: NoteMapper
) : INoteDao {

  @PersistenceContext
  private lateinit var entityManager: EntityManager

  override fun findById(id: String): Note? {
    val entity = entityManager.find(NoteEntity::class.java, id) ?: return null
    return noteMapper.toDomain(entity)
  }

  override fun findAll(): Iterable<Note> {
    val query = entityManager.createQuery("FROM NoteEntity", NoteEntity::class.java)
    val entities = query.resultList
    return noteMapper.toDomainList(entities)
  }

  override fun findAll(pageable: Pageable): Page<Note> {
    val query = entityManager.createQuery("FROM NoteEntity", NoteEntity::class.java)
    val countQuery = entityManager.createQuery("SELECT COUNT(n) FROM NoteEntity n", Long::class.java)
    val totalElements = countQuery.singleResult

    val entities = query
      .setFirstResult(pageable.pageNumber * pageable.pageSize)
      .setMaxResults(pageable.pageSize)
      .resultList

    val content = noteMapper.toDomainList(entities)
    return Page(content, totalElements, pageable)
  }

  override fun create(entity: Note): Note {
    val noteEntity = noteMapper.toEntity(entity)
    entityManager.persist(noteEntity)
    return noteMapper.toDomain(noteEntity)
  }

  override fun update(entity: Note): Note {
    val noteEntity = noteMapper.toEntity(entity)
    val mergedEntity = entityManager.merge(noteEntity)
    return noteMapper.toDomain(mergedEntity)
  }

  override fun updateAndFlush(entity: Note): Note {
    val result = update(entity)
    entityManager.flush()
    return result
  }

  override fun delete(entity: Note) {
    val noteEntity = noteMapper.toEntity(entity)
    val managedEntity = entityManager.merge(noteEntity)
    entityManager.remove(managedEntity)
  }

  override fun deleteById(id: String) {
    val entity = entityManager.find(NoteEntity::class.java, id) ?: return
    entityManager.remove(entity)
  }

  /**
   * Delete all entities
   */
  override fun deleteAll() {
    val query = entityManager.createQuery("DELETE FROM NoteEntity")
    query.executeUpdate()
  }

  /**
   * Save an entity (create or update)
   * For SQL, this is the same as create or update depending on whether the entity exists
   */
  override fun save(entity: Note): Note {
    val noteEntity = noteMapper.toEntity(entity)
    return if (entityManager.find(NoteEntity::class.java, noteEntity.id) != null) {
      update(entity)
    } else {
      create(entity)
    }
  }

  /**
   * Save multiple entities
   */
  override fun saveAll(entities: List<Note>): Iterable<Note> {
    return entities.map { save(it) }
  }
}
