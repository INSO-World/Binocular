package com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces

import com.inso_world.binocular.core.persistence.model.Page
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import java.io.Serializable
import java.util.stream.Stream

interface IDao<T, I : Serializable> {
    fun findById(id: I): T?

    fun create(
        @Valid entity: T,
    ): T

    fun findAll(): Iterable<T>

    fun findAllAsStream(): Stream<T>

    fun findAll(pageable: Pageable): Page<T>

    fun update(
        @Valid entity: T,
    ): T

    fun delete(
        @Valid entity: T,
    )

    fun deleteById(id: I)

    fun updateAndFlush(
        @Valid entity: T,
    ): T

    /**
     * Delete all entities
     */
    fun deleteAll()

    /**
     * Save an entity (create or update)
     */
    @Deprecated("should be replaced with create")
    fun save(entity: @Valid T): @Valid T

    /**
     * Save multiple entities
     */
    fun saveAll(
        @Valid entities: Collection<T>,
    ): Iterable<T>
}
