package com.inso_world.binocular.web.persistence.repository.arangodb

import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.entity.File
import org.springframework.stereotype.Repository

@Repository
interface FileRepository: ArangoRepository<File, String> {
}
