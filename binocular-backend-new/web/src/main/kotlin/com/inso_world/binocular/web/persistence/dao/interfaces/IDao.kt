package com.inso_world.binocular.web.persistence.dao.interfaces

import java.io.Serializable


interface IDao<T, I : Serializable> {
  fun findById(id: I): T?

  fun create(entity: T): T

  fun findAll(): Iterable<T>

  fun update(entity: T): T

  fun delete(entity: T)

  fun deleteById(id: I)

  fun updateAndFlush(entity: T): T

  // TBD: Batch saveAll


}
