package com.inso_world.binocular.infrastructure.sql.service

import com.inso_world.binocular.core.persistence.mapper.EntityMapper
import com.inso_world.binocular.core.persistence.model.Page
import com.inso_world.binocular.core.service.BinocularInfrastructurePort
import com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces.IDao
import jakarta.persistence.EntityManager
import org.hibernate.exception.ConstraintViolationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional
import java.io.Serializable
import java.util.stream.Stream
import kotlin.reflect.KClass

abstract class AbstractInfrastructurePort<D : Any, E : Any, I : Serializable>(
    private val idKClass: KClass<I>,
) : IDao<D, I>,
    BinocularInfrastructurePort<D> {
    @Autowired
    protected lateinit var entityManager: EntityManager

    protected lateinit var mapper: EntityMapper<D, E>
    protected lateinit var dao: IDao<E, I>

    override fun findById(id: I): D? =
        dao.findById(id)?.let {
            mapper.toDomain(it)
        }

    override fun findById(id: String): D? = this.findById(read(id))

    @Transactional
    override fun create(entity: D): D = this.dao.create(mapper.toEntity(entity)).let { mapper.toDomain(it) }

    override fun findAll(): Iterable<D> = this.dao.findAll().map { mapper.toDomain(it) }

    override fun findAll(pageable: Pageable): Page<D> {
        TODO("Not yet implemented")
//        return this.dao.findAll(pageable).map {  }
    }

    override fun findAllAsStream(): Stream<D> = this.dao.findAllAsStream().map { mapper.toDomain(it) }

    override fun update(entity: D): D = this.dao.update(mapper.toEntity(entity)).let { mapper.toDomain(it) }

    override fun delete(entity: D) {
//        this.dao.delete(mapper.toEntity(entity))
        TODO("Not yet implemented")
    }

    override fun deleteById(id: I) {
//        this.dao.deleteById(id)
        TODO("Not yet implemented")
    }

    override fun deleteById(id: String) {
        val value = read(id)
        this.dao.deleteById(value)
    }

    override fun updateAndFlush(entity: D): D = this.dao.updateAndFlush(mapper.toEntity(entity)).let { mapper.toDomain(it) }

    override fun deleteAll() {
        this.dao.deleteAll()
    }

    @Deprecated("to be deleted")
    override fun save(entity: D): D {
        try {
            return this.dao.create(mapper.toEntity(entity)).let { mapper.toDomain(it) }
        } catch (ex: DataIntegrityViolationException) {
            val cause = ex.cause
            if (cause is ConstraintViolationException) {
                throw cause
            }
            throw ex
        }
    }

    override fun saveAll(entities: Collection<D>): Iterable<D> {
        val mapped = entities.map { this.mapper.toEntity(it) }.toList()
        return this.dao.saveAll(mapped).map { this.mapper.toDomain(it) }
    }

    private fun read(value: String): I {
//        val value: String = readLine()!!
        return when (idKClass) {
            Int::class -> value.toInt() as I
            String::class -> value as I
            Long::class -> value.toLong() as I
            // add other types here if need
            else -> throw IllegalStateException("Unknown Generic Type")
        }
    }
}
