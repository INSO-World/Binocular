package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.persistence.dao.interfaces.IIssueDao
import com.inso_world.binocular.web.persistence.entity.arangodb.IssueEntity
import com.inso_world.binocular.web.persistence.mapper.arangodb.IssueMapper
import com.inso_world.binocular.web.persistence.repository.arangodb.IssueRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of IIssueDao using the MappedArangoDbDao approach.
 * 
 * This class extends MappedArangoDbDao to leverage the entity mapping pattern,
 * which provides a clean separation between domain models (Issue) and 
 * database-specific entities (IssueEntity).
 */
@Repository
@Profile("nosql", "arangodb")
class IssueDao(
  @Autowired issueRepository: IssueRepository,
  @Autowired issueMapper: IssueMapper
) : MappedArangoDbDao<Issue, IssueEntity, String>(issueRepository, issueMapper), IIssueDao {
}
