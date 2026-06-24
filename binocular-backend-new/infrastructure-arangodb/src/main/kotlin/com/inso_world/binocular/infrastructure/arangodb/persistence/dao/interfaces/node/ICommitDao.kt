package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.node

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.interfaces.IDao
import com.inso_world.binocular.infrastructure.arangodb.persistence.entity.CiRateBucketEntity
import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.metrics.AuthorPeriodCount

internal interface ICommitDao : IDao<Commit, String>{
    fun findAuthorCommitCountsByPeriod(since: Long, until: Long, firstLabel: String, fmt: String): List<AuthorPeriodCount>
}
