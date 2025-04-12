package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.persistence.dao.nosql.NoSqlDao
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.io.Serializable

@Repository
@Profile("nosql", "arangodb")
class ArangoDbDao<T : Any, I : Serializable> : NoSqlDao<T, I>() {

  fun setRepository(repo: ArangoRepository<T, I>) {
    this.arangoRepository = repo
  }

  private lateinit var arangoRepository: ArangoRepository<T, I>

  override fun findById(id: I): T? {
    TODO("Not yet implemented")
  }

  override fun findAll(): Iterable<T> {
    return this.arangoRepository.findAll()
  }

  override fun create(entity: T): T {
    return this.arangoRepository.save(entity)
  }

  override fun update(entity: T): T {
    TODO("Not yet implemented")
  }

  override fun updateAndFlush(entity: T): T {
    TODO("Not yet implemented")
  }

  override fun delete(entity: T) {
    TODO("Not yet implemented")
  }

  override fun deleteById(id: I) {
    TODO("Not yet implemented")
  }
}
