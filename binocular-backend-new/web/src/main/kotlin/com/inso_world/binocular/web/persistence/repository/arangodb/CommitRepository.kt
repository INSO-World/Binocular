package com.inso_world.binocular.web.persistence.repository.arangodb

import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.entity.Commit
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Repository
interface CommitRepository: ArangoRepository<Commit, String> {
}
