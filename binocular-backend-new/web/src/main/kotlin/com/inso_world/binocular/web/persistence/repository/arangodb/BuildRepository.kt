package com.inso_world.binocular.web.persistence.repository.arangodb

import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.entity.Build
import org.springframework.stereotype.Repository

@Repository
interface BuildRepository: ArangoRepository<Build, String> {
}
