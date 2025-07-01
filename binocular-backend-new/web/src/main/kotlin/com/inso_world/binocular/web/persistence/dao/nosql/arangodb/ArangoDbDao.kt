package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.persistence.dao.nosql.NoSqlDao
import com.inso_world.binocular.web.persistence.model.Page
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.io.Serializable

/**
 * Base class for ArangoDB DAO implementations using the direct entity approach.
 * 
 * There are two approaches for implementing DAOs in this project:
 * 
 * 1. Direct entity usage (using this class):
 *    - Use ArangoDbDao<Entity, ID> where Entity is both the domain model and database entity
 *    - Call setRepository(repository) and setClazz(Entity::class.java) in the init block
 *    - Example: BranchDao extends ArangoDbDao<Branch, String>
 * 
 * 2. With entity mapping (using MappedArangoDbDao):
 *    - Use MappedArangoDbDao<DomainModel, Entity, ID> to separate domain models from entities
 *    - Implement the abstract methods for converting between domain models and entities
 *    - Example: AccountDao extends MappedArangoDbDao<Account, AccountEntity, String>
 * 
 * The second approach is recommended for new code as it provides better separation of concerns
 * between domain models and database-specific entities.
 * 
 * @see MappedArangoDbDao for the entity mapping approach
 */
@Repository
@Profile("nosql", "arangodb")
class ArangoDbDao<T : Any, I : Serializable> : NoSqlDao<T, I>() {

  fun setRepository(repo: ArangoRepository<T, I>) {
    this.arangoRepository = repo
  }

  private lateinit var arangoRepository: ArangoRepository<T, I>

  override fun findById(id: I): T? {
    return arangoRepository.findById(id).orElse(null)
  }

  override fun findAll(): Iterable<T> {
    return this.arangoRepository.findAll()
  }

  override fun findAll(pageable: Pageable): Page<T> {
    val result = this.arangoRepository.findAll(pageable)
    val content = result.content
    val totalElements = result.totalElements

    return Page(content, totalElements, pageable)
  }

  override fun create(entity: T): T {
    return this.arangoRepository.save(entity)
  }

  override fun update(entity: T): T {
    return this.arangoRepository.save(entity)
  }

  override fun updateAndFlush(entity: T): T {
    // ArangoDB doesn't have a concept of "flush", so this is the same as update
    return update(entity)
  }

  override fun delete(entity: T) {
    this.arangoRepository.delete(entity)
  }

  override fun deleteById(id: I) {
    this.arangoRepository.deleteById(id)
  }

  override fun deleteAll() {
    this.arangoRepository.deleteAll()
  }

  override fun save(entity: T): T {
    return this.arangoRepository.save(entity)
  }

  override fun saveAll(entities: List<T>): Iterable<T> {
    return entities.map { save(it) }
  }
}
