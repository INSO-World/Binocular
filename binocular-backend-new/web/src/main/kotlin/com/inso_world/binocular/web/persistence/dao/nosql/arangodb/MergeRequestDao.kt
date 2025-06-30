package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.inso_world.binocular.web.entity.MergeRequest
import com.inso_world.binocular.web.persistence.dao.interfaces.IMergeRequestDao
import com.inso_world.binocular.web.persistence.entity.arangodb.MergeRequestEntity
import com.inso_world.binocular.web.persistence.mapper.arangodb.MergeRequestMapper
import com.inso_world.binocular.web.persistence.repository.arangodb.MergeRequestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Repository
@Profile("nosql", "arangodb")
class MergeRequestDao @Autowired constructor(
  mergeRequestRepository: MergeRequestRepository,
  mergeRequestMapper: MergeRequestMapper
) : MappedArangoDbDao<MergeRequest, MergeRequestEntity, String>(mergeRequestRepository, mergeRequestMapper), IMergeRequestDao
