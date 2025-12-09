package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.mapper.context.MappingSession
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.IDao
import org.springframework.data.domain.Pageable
import java.io.Serializable
import java.util.stream.Stream

/**
 * Base class for ArangoDB DAO implementations that use entity mapping.
 *
 * This class provides a generic implementation for DAOs that need to map between
 * domain models (D) and database entities (E). It delegates all database operations
 * to an ArangoRepository and uses a mapper to convert between domain models and entities.
 *
 * Usage example:
 * ```
 * @Repository
 * @Profile("nosql", "arangodb")
 * class AccountDao(
 *   @Autowired accountRepository: AccountRepository,
 *   @Autowired accountMapper: AccountMapper
 * ) : MappedArangoDbDao<Account, AccountEntity, String>(accountRepository, accountMapper), IAccountDao {
 * }
 * ```
 *
 * Note: The parameter names in this class match those in the IDao interface to avoid
 * conflicts when implementing both this class and an interface that extends IDao.
 * For example, both this class and IDao use "entity" as the parameter name for create(),
 * update(), and delete() methods.
 *
 * @param D The domain model type
 * @param E The database entity type
 * @param I The ID type (must be Serializable)
 * @param repository The ArangoDB repository for the entity type
 * @param mapper The mapper for converting between domain models and entities
 */

open class MappedArangoDbDao<D : Any, E : Any, I : Serializable>(
    protected val repository: ArangoRepository<E, I>,
    protected val mapper: EntityMapper<D, E>,
) : IDao<D, I> {
    /**
     * Converts a list of database entities to a list of domain models
     */
    protected fun toDomainList(entities: Iterable<E>): List<D> = mapper.toDomainList(entities)

    /**
     * Finds an entity by its ID and converts it to a domain model
     */
    override fun findById(id: I): D? {
        val entity = repository.findById(id).orElse(null) ?: return null
        return mapper.toDomain(entity)
    }

    /**
     * Finds all entities and converts them to domain models
     */
    override fun findAll(): Iterable<D> {
        val entities = repository.findAll()
        return toDomainList(entities)
    }

    /**
     * Finds a page of entities and converts them to domain models
     */
    @MappingSession
    override fun findAll(pageable: Pageable): Page<D> {
        val result = repository.findAll(pageable)
        val content = toDomainList(result.content)
        val totalElements = result.totalElements

        return Page(content, totalElements, pageable)
    }

    /**
     * Creates a new entity from a domain model
     * @param entity The domain model to create an entity from
     * @return The created domain model
     */
    @MappingSession
    override fun create(entity: D): D {
        val mappedEntity = mapper.toEntity(entity)
        val savedEntity = repository.save(mappedEntity)
        return mapper.toDomain(savedEntity)
    }

    /**
     * Updates an existing entity from a domain model
     * @param entity The domain model to update an entity from
     * @return The updated domain model
     */
    @MappingSession
    override fun update(entity: D): D {
        val mappedEntity = mapper.toEntity(entity)
        val savedEntity = repository.save(mappedEntity)
        return mapper.toDomain(savedEntity)
    }

    /**
     * Updates an existing entity from a domain model and flushes changes
     * (ArangoDB doesn't have a concept of "flush", so this is the same as update)
     * @param entity The domain model to update an entity from
     * @return The updated domain model
     */
    override fun updateAndFlush(entity: D): D = update(entity)

    /**
     * Deletes an entity
     * @param entity The domain model to delete
     */
    override fun delete(entity: D) {
        val mappedEntity = mapper.toEntity(entity)
        repository.delete(mappedEntity)
    }

    /**
     * Deletes an entity by its ID
     */
    override fun deleteById(id: I) {
        repository.deleteById(id)
    }

    /**
     * Deletes all entities
     */
    override fun deleteAll() {
        repository.deleteAll()
    }

    /**
     * Save an entity (create or update)
     * For ArangoDB, this is the same as create or update
     */
    @Deprecated("should be replaced with create")
    override fun save(entity: D): D = create(entity)

    /**
     * Save multiple entities
     */
    @MappingSession
    override fun saveAll(entities: Collection<D>): Iterable<D> = entities.map { create(it) }

    override fun findAllAsStream(): Stream<D> {
        TODO("Not yet implemented")
    }
}
