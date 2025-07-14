package com.inso_world.binocular.infrastructure.sql.persistence.dao.interfaces

import com.inso_world.binocular.core.persistence.model.Page
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import java.io.Serializable
import java.util.stream.Stream

internal interface IDao<T, I : Serializable> {
    fun findById(id: I): @Valid T?

    fun create(
        @Valid entity: T,
    ): @Valid T

    fun findAll(): Iterable<@Valid T>

    fun findAllAsStream(): Stream<T>

    fun findAll(pageable: Pageable): Page<@Valid T>

    fun update(
        @Valid entity: T,
    ): @Valid T

    fun delete(
        @Valid entity: T,
    )

    fun deleteById(id: I)

    fun updateAndFlush(
        @Valid entity: T,
    ): @Valid T

    /**
     * Delete all entities
     */
    fun deleteAll()

    /**
     * Save an entity (create or update)
     */
    @Deprecated("should be replaced with create", ReplaceWith("create"))
    fun save(
        @Valid entity: T,
    ): @Valid T

    /**
     * Save multiple entities
     */
    fun saveAll(
        @Valid entities: Collection<@Valid T>,
    ): Iterable<@Valid T>
}
