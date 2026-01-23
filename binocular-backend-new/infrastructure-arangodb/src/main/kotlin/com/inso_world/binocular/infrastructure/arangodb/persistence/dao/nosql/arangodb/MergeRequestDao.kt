package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node.IMergeRequestDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.MergeRequestEntity
import com.inso_world.binocular.infrastructure.arangodb.persistence.mapper.MergeRequestMapper
import com.inso_world.binocular.infrastructure.arangodb.persistence.repository.MergeRequestRepository
import com.inso_world.binocular.model.MergeRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
internal class MergeRequestDao
    @Autowired
    constructor(
        mergeRequestRepository: MergeRequestRepository,
        mergeRequestMapper: MergeRequestMapper,
    ) : MappedArangoDbDao<MergeRequest, MergeRequestEntity, String>(mergeRequestRepository, mergeRequestMapper),
        IMergeRequestDao
