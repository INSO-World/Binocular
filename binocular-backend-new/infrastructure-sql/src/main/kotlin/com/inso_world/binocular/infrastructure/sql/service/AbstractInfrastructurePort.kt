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
import org.springframework.validation.annotation.Validated
import java.io.Serializable
import java.util.stream.Stream
import kotlin.reflect.KClass

@Validated
internal abstract class AbstractInfrastructurePort<D : Any, E : Any, I : Serializable>(
    private val idKClass: KClass<I>,
) : BinocularInfrastructurePort<D> {
    @Autowired
    protected lateinit var entityManager: EntityManager

    protected lateinit var mapper: EntityMapper<D, E>
    protected lateinit var dao: IDao<E, I>

    fun findById(id: I): D? =
        dao.findById(id)?.let {
            mapper.toDomain(it)
        }

    override fun findById(id: String): D? = this.findById(read(id))

    @Transactional
    fun create(domain: D): D = this.dao.create(mapper.toEntity(domain)).let { mapper.toDomain(it) }

    override fun findAll(): Iterable<D> = this.dao.findAll().map { mapper.toDomain(it) }

    override fun findAll(pageable: Pageable): Page<D> {
        TODO("Not yet implemented")
//        return this.dao.findAll(pageable).map {  }
    }

    fun findAllAsStream(): Stream<D> = this.dao.findAllAsStream().map { mapper.toDomain(it) }

    override fun update(domain: D): D = this.dao.update(mapper.toEntity(domain)).let { mapper.toDomain(it) }

    override fun delete(domain: D) {
        val mappedEntity = mapper.toEntity(domain)
        this.dao.delete(mappedEntity)
    }

    override fun deleteById(id: String) {
        val value = read(id)
        this.dao.deleteById(value)
    }

    override fun updateAndFlush(domain: D): D = this.dao.updateAndFlush(mapper.toEntity(domain)).let { mapper.toDomain(it) }

    override fun deleteAll() {
        this.dao.deleteAll()
    }

    @Deprecated("to be deleted")
    @Transactional
    override fun save(domain: D): D {
        try {
            return create(domain)
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
