package com.inso_world.binocular.web.persistence.dao.interfaces

import com.inso_world.binocular.web.persistence.model.Page
import org.springframework.data.domain.Pageable
import java.io.Serializable


interface IDao<T, I : Serializable> {
  fun findById(id: I): T?

  fun create(entity: T): T

  fun findAll(): Iterable<T>

  fun findAll(pageable: Pageable): Page<T>

  fun update(entity: T): T

  fun delete(entity: T)

  fun deleteById(id: I)

  fun updateAndFlush(entity: T): T

  // TBD: Batch saveAll


}
