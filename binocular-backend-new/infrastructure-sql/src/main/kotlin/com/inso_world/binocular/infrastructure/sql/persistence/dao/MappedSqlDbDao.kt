package com.inso_world.binocular.infrastructure.sql.persistence.dao

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import org.springframework.data.jpa.repository.JpaRepository
import java.io.Serializable

/**
 * Base class for SQL DAO implementations that use entity mapping.
 *
 * This class provides a generic implementation for DAOs that need to map between
 * domain models (D) and database entities (E). It delegates all database operations
 * to an JpaRepository and uses a mapper to convert between domain models and entities.
 *
 * Usage example:
 * ```
 * @Repository
 * class AccountDao(
 *   @Autowired accountRepository: AccountRepository,
 *   @Autowired accountMapper: AccountMapper
 * ) : MappedSqlDbDao<Account, AccountEntity, String>(accountRepository, accountMapper), IAccountDao {
 * }
 * ```
 *
 * Note: The parameter names in this class match those in the IDao interface (resp. MappedSqlDbDao class) to avoid
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
abstract class MappedSqlDbDao<D : Any, E : Any, I : Serializable>(
    protected open val repository: JpaRepository<E, I>,
    protected open val mapper: EntityMapper<D, E>,
) : SqlDao<E, I>() {
//    @PersistenceContext
//    protected lateinit var entityManager: EntityManager

//    override fun findById(id: I): D? =
//        this.repository.findById(id).getOrNull()?.let {
//            mapper.toDomain(it)
//        }
//
//    override fun create(entity: D): D =
//        this.repository
//            .save(
//                mapper.toEntity(entity),
//            ).let { mapper.toDomain(it) }
//
//    override fun findAll(): Iterable<D> = this.repository.findAll().map { mapper.toDomain(it) }
//
//    override fun findAll(pageable: Pageable): Page<D> {
//        TODO("Not yet implemented")
//    }
//
//    override fun findAllAsStream(): Stream<D> {
//        TODO("Not yet implemented")
//    }
//
//    override fun update(entity: D): D = this.repository.save(mapper.toEntity(entity)).let { mapper.toDomain(it) }
//
//    override fun delete(entity: D) {
//        this.repository.delete(mapper.toEntity(entity))
//    }
//
//    override fun deleteById(id: I) {
//        this.repository.deleteById(id)
//    }
//
//    override fun updateAndFlush(entity: D): D {
//        TODO("Not yet implemented")
//    }
//
//    override fun deleteAll() {
//        this.repository.deleteAll()
//    }
//
//    override fun saveAll(entities: Collection<D>): Iterable<D> =
//        this.repository
//            .saveAll(
//                entities.map {
//                    mapper.toEntity(it)
//                },
//            ).map { mapper.toDomain(it) }

    //    init {
//        this.setRepository(repository)
//    }

//    protected fun toDomainList(entities: Iterable<E>): List<D> = mapper.toDomainList(entities)
//
//    protected fun setClazz(clazz: Class<E>) {
//        this.setClazz(clazz)
//    }
//
//    override fun findById(id: I): D? =
//        this.findById(id)?.let {
//            this.mapper.toDomain(it)
//        }
//
//
//
//    override fun create(entity: D): D =
//        this
//            .save(
//                mapper.toEntity(entity),
//            ).let { mapper.toDomain(it) }
//
//    override fun findAll(): Iterable<D> = this.findAll().map { mapper.toDomain(it) }
//
//    override fun findAll(pageable: Pageable): Page<D> {
//        val result = super.findAll(pageable)
//        val content = toDomainList(result.content)
//        val totalElements = result.totalElements
//
//        return Page(content, totalElements, pageable)
//    }

//    override fun deleteAll() {
//        this.repository.deleteAll()
//    }

//    @Deprecated("should be replaced with create")
//    override fun save(entity: D): D = create(entity)

//    override fun saveAll(entities: Collection<D>): Iterable<D> =
//        super
//            .saveAll(
//                entities.map { mapper.toEntity(it) },
//            ).map { mapper.toDomain(it) }
}
