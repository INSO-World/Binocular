package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.persistence.dao.interfaces.ICommitDao
import com.inso_world.binocular.web.persistence.entity.arangodb.CommitEntity
import com.inso_world.binocular.web.persistence.mapper.arangodb.CommitMapper
import com.inso_world.binocular.web.persistence.repository.arangodb.CommitRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

/**
 * ArangoDB implementation of ICommitDao using the MappedArangoDbDao approach.
 * 
 * This class extends MappedArangoDbDao to leverage the entity mapping pattern,
 * which provides a clean separation between domain models (Commit) and 
 * database-specific entities (CommitEntity).
 */
@Repository
@Profile("nosql", "arangodb")
class CommitDao(
  @Autowired commitRepository: CommitRepository,
  @Autowired commitMapper: CommitMapper
) : MappedArangoDbDao<Commit, CommitEntity, String>(commitRepository, commitMapper), ICommitDao {
}
