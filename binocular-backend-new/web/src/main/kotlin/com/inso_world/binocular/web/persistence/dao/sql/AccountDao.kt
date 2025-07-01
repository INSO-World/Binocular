package com.inso_world.binocular.web.persistence.dao.sql

import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.persistence.dao.interfaces.IAccountDao
import com.inso_world.binocular.web.persistence.entity.sql.AccountEntity
import com.inso_world.binocular.web.persistence.mapper.sql.AccountMapper
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
class AccountDao(
  @Autowired private val accountMapper: AccountMapper
) : IAccountDao {

  @PersistenceContext
  private lateinit var entityManager: EntityManager

  override fun findById(id: String): Account? {
    val entity = entityManager.find(AccountEntity::class.java, id) ?: return null
    return accountMapper.toDomain(entity)
  }

  override fun findAll(): Iterable<Account> {
    val query = entityManager.createQuery("FROM AccountEntity", AccountEntity::class.java)
    val entities = query.resultList
    return accountMapper.toDomainList(entities)
  }

  override fun findAll(pageable: Pageable): Page<Account> {
    val query = entityManager.createQuery("FROM AccountEntity", AccountEntity::class.java)
    val countQuery = entityManager.createQuery("SELECT COUNT(a) FROM AccountEntity a", Long::class.java)
    val totalElements = countQuery.singleResult

    val entities = query
      .setFirstResult(pageable.pageNumber * pageable.pageSize)
      .setMaxResults(pageable.pageSize)
      .resultList

    val content = accountMapper.toDomainList(entities)
    return Page(content, totalElements, pageable)
  }

  override fun create(entity: Account): Account {
    val accountEntity = accountMapper.toEntity(entity)
    entityManager.persist(accountEntity)
    return accountMapper.toDomain(accountEntity)
  }

  override fun update(entity: Account): Account {
    val accountEntity = accountMapper.toEntity(entity)
    val mergedEntity = entityManager.merge(accountEntity)
    return accountMapper.toDomain(mergedEntity)
  }

  override fun updateAndFlush(entity: Account): Account {
    val result = update(entity)
    entityManager.flush()
    return result
  }

  override fun delete(entity: Account) {
    val accountEntity = accountMapper.toEntity(entity)
    val managedEntity = entityManager.merge(accountEntity)
    entityManager.remove(managedEntity)
  }

  override fun deleteById(id: String) {
    val entity = entityManager.find(AccountEntity::class.java, id) ?: return
    entityManager.remove(entity)
  }

  /**
   * Delete all entities
   */
  override fun deleteAll() {
    val accounts = entityManager.createQuery("SELECT a FROM AccountEntity a", AccountEntity::class.java)
      .resultList
    accounts.forEach { entityManager.remove(it) }
  }

  /**
   * Save an entity (create or update)
   * For SQL, this is the same as create or update depending on whether the entity exists
   */
  override fun save(entity: Account): Account {
    val accountEntity = accountMapper.toEntity(entity)
    return if (entityManager.find(AccountEntity::class.java, accountEntity.id) != null) {
      update(entity)
    } else {
      create(entity)
    }
  }

  /**
   * Save multiple entities
   */
  override fun saveAll(entities: List<Account>): Iterable<Account> {
    return entities.map { save(it) }
  }
}
