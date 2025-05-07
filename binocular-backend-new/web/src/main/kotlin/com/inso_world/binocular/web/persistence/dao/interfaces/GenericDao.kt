package com.inso_world.binocular.web.persistence.dao.interfaces

import org.springframework.data.domain.Pageable
import java.io.Serializable

interface GenericDao<T, I : Serializable> {
  fun setClazz(clazz: Class<T>)
  fun findById(id: I): T?
  fun findAll(): Iterable<T>
  fun findAll(pageable: Pageable): Iterable<T>
  fun create(entity: T): T
  fun update(entity: T): T
  fun updateAndFlush(entity: T): T
  fun delete(entity: T)
  fun deleteById(id: I)
}
