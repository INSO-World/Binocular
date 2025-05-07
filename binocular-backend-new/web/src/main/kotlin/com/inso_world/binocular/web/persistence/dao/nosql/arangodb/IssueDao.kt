package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueDao
import com.inso_world.binocular.web.persistence.repository.arangodb.IssueRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class IssueDao(
  @Autowired private val issueRepository: IssueRepository
) : ArangoDbDao<Issue, String>(), IIssueDao {

  init {
    this.setClazz(Issue::class.java)
    this.setRepository(issueRepository)
  }
}
