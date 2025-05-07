package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestDao
import com.inso_world.binocular.web.persistence.repository.arangodb.MergeRequestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class MergeRequestDao(
  @Autowired private val mergeRequestRepository: MergeRequestRepository
) : ArangoDbDao<MergeRequest, String>(), IMergeRequestDao {

  init {
    this.setClazz(MergeRequest::class.java)
    this.setRepository(mergeRequestRepository)
  }
}
