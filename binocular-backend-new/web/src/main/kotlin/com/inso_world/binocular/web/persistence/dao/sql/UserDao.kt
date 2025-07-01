package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.User
import com.inso_world.binocular.web.persistence.dao.interfaces.IUserDao
import com.inso_world.binocular.web.persistence.entity.sql.UserEntity
import com.inso_world.binocular.web.persistence.mapper.sql.UserMapper
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
class UserDao(
  @Autowired private val userMapper: UserMapper
) : IUserDao {

  @PersistenceContext
  private lateinit var entityManager: EntityManager

  override fun findById(id: String): User? {
    val entity = entityManager.find(UserEntity::class.java, id) ?: return null
    return userMapper.toDomain(entity)
  }

  override fun findAll(): Iterable<User> {
    val query = entityManager.createQuery("FROM UserEntity", UserEntity::class.java)
    val entities = query.resultList
    return userMapper.toDomainList(entities)
  }

  override fun findAll(pageable: Pageable): Page<User> {
    val query = entityManager.createQuery("FROM UserEntity", UserEntity::class.java)
    val countQuery = entityManager.createQuery("SELECT COUNT(u) FROM UserEntity u", Long::class.java)
    val totalElements = countQuery.singleResult

    val entities = query
      .setFirstResult(pageable.pageNumber * pageable.pageSize)
      .setMaxResults(pageable.pageSize)
      .resultList

    val content = userMapper.toDomainList(entities)
    return Page(content, totalElements, pageable)
  }

  override fun create(entity: User): User {
    val userEntity = userMapper.toEntity(entity)
    entityManager.persist(userEntity)
    return userMapper.toDomain(userEntity)
  }

  override fun update(entity: User): User {
    val userEntity = userMapper.toEntity(entity)
    val mergedEntity = entityManager.merge(userEntity)
    return userMapper.toDomain(mergedEntity)
  }

  override fun updateAndFlush(entity: User): User {
    val result = update(entity)
    entityManager.flush()
    return result
  }

  override fun delete(entity: User) {
    val userEntity = userMapper.toEntity(entity)
    val managedEntity = entityManager.merge(userEntity)
    entityManager.remove(managedEntity)
  }

  override fun deleteById(id: String) {
    val entity = entityManager.find(UserEntity::class.java, id) ?: return
    entityManager.remove(entity)
  }

  /**
   * Delete all entities
   */
  override fun deleteAll() {
    val users = entityManager.createQuery("SELECT u FROM UserEntity u", UserEntity::class.java)
      .resultList
    users.forEach { entityManager.remove(it) }
  }

  /**
   * Save an entity (create or update)
   * For SQL, this is the same as create or update depending on whether the entity exists
   */
  override fun save(entity: User): User {
    val userEntity = userMapper.toEntity(entity)
    return if (entityManager.find(UserEntity::class.java, userEntity.id) != null) {
      update(entity)
    } else {
      create(entity)
    }
  }

  /**
   * Save multiple entities
   */
  override fun saveAll(entities: List<User>): Iterable<User> {
    return entities.map { save(it) }
  }
}
