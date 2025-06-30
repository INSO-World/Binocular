package com.inso_world.binocular.web.persistence.repository.arangodb.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.persistence.entity.arangodb.FileEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.ModuleEntity
import com.inso_world.binocular.web.persistence.entity.arangodb.ModuleFileConnectionEntity
import org.springframework.stereotype.Repository

@Repository
interface ModuleFileConnectionRepository: ArangoRepository<ModuleFileConnectionEntity, String> {

  @Query("""
    FOR c IN `modules-files`
        FILTER c._from == CONCAT('modules/', @moduleId)
        FOR f IN files
            FILTER f._id == c._to
            RETURN f
""")
  fun findFilesByModule(moduleId: String): List<FileEntity>

  @Query("""
    FOR c IN `modules-files`
        FILTER c._to == CONCAT('files/', @fileId)
        FOR m IN modules
            FILTER m._id == c._from
            RETURN m
""")
  fun findModulesByFile(fileId: String): List<ModuleEntity>
}
