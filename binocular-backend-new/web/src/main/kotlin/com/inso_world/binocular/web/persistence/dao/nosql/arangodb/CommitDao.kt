package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.web.persistence.repository.arangodb.CommitRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class CommitDao(
  @Autowired private val commitRepository: CommitRepository
) : ArangoDbDao<Commit, String>(), ICommitDao {

  init {
    this.setClazz(Commit::class.java)
    this.setRepository(commitRepository)
  }


}
