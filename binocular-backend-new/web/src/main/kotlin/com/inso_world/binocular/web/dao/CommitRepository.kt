package com.inso_world.binocular.web.dao

import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.entity.Commit

interface CommitRepository: ArangoRepository<Commit, String> {
}
