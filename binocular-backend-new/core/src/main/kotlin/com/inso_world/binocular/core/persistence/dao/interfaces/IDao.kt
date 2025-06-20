package com.inso_world.binocular.core.persistence.dao.interfaces

import org.springframework.data.domain.Example
import org.springframework.data.domain.Pageable
import java.io.Serializable


interface IDao<T, I : Serializable> {
  fun findById(id: I): T?

  fun create(entity: T): T

  fun findAll(): Iterable<T>

  fun findAll(pageable: Pageable): Iterable<T>

  fun update(entity: T): T

  fun delete(entity: T)

  fun deleteById(id: I)

  fun updateAndFlush(entity: T): T

  fun <S : T> exists(example: Example<S>): Boolean

  // TBD: Batch saveAll
  fun saveAll(entities: Iterable<T>): Iterable<T>
}
