package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces

import com.inso_world.binocular.core.persistence.model.Page
import org.springframework.data.domain.Pageable
import java.io.Serializable
import java.util.stream.Stream

internal interface IDao<T, I : Serializable> {
    fun findById(id: I): T?

    fun create(entity: T): T

    fun findAll(): Iterable<T>

    fun findAllAsStream(): Stream<T>

    fun findAll(pageable: Pageable): Page<T>

    fun update(entity: T): T

    fun delete(entity: T)

    fun deleteById(id: I)

    fun updateAndFlush(entity: T): T

    /**
     * Delete all entities
     */
    fun deleteAll()

    /**
     * Save an entity (create or update)
     */
    @Deprecated("should be replaced with create")
    fun save(entity: T): T

    /**
     * Save multiple entities
     */
    fun saveAll(entities: Collection<T>): Iterable<T>
}
