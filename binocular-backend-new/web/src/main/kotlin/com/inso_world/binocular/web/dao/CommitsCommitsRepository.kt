package com.inso_world.binocular.web.dao

import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.CommitParent

interface CommitsCommitsRepository: ArangoRepository<CommitParent, String> {
}
