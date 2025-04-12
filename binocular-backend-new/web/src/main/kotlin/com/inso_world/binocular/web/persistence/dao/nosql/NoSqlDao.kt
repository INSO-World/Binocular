package com.inso_world.binocular.web.persistence.dao.nosql

import com.inso_world.binocular.web.persistence.dao.interfaces.GenericDao
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.io.Serializable

@Repository
@Profile("nosql")
abstract class NoSqlDao<T, I : Serializable> : GenericDao<T, I> {

  private lateinit var clazz: Class<T>
//
//  // A simulated in-memory NoSQL store for demonstration.
//  private val store: MutableMap<I, T> = ConcurrentHashMap()
//
  override fun setClazz(clazz: Class<T>) {
    this.clazz = clazz
  }
//
//  override fun findById(id: I): T? =
//    store[id]
//
//  override fun findAll(): List<T> =
//    store.values.toList()
//
//  override fun create(entity: T): T {
//    // In a real NoSQL implementation, use your NoSQL client here.
//    val id = extractId(entity) // customize this extraction logic as needed
//    store[id] = entity
//    return entity
//  }
//
//  override fun update(entity: T): T {
//    val id = extractId(entity)
//    if (store.containsKey(id)) {
//      store[id] = entity
//      return entity
//    } else {
//      throw NotFoundException("Entity not found")
//    }
//  }
//
//  override fun updateAndFlush(entity: T): T {
//    // Flushing may be a no-op in many NoSQL systems.
//    return update(entity)
//  }
//
//  override fun delete(entity: T) {
//    val id = extractId(entity)
//    store.remove(id)
//  }
//
//  override fun deleteById(id: I) {
//    if (store.containsKey(id)) {
//      store.remove(id)
//    } else {
//      throw NotFoundException("Entity not found")
//    }
//  }
//
//  // Dummy function to extract an ID from the entity.
//  // In a real scenario, you might have a different mechanism to obtain the identifier.
//  @Suppress("UNCHECKED_CAST")
//  private fun extractId(entity: T): I {
//    try {
//      val method = clazz.getMethod("getId")
//      return method.invoke(entity) as I
//    } catch (e: Exception) {
//      throw IllegalArgumentException("Could not extract ID from entity", e)
//    }
//  }
}

