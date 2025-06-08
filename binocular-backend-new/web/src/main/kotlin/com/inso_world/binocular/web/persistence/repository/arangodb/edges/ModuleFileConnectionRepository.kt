package com.inso_world.binocular.web.persistence.repository.arangodb.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.entity.edge.ModuleFileConnection
import org.springframework.stereotype.Repository

@Repository
interface ModuleFileConnectionRepository: ArangoRepository<ModuleFileConnection, String> {

  @Query("""
    FOR c IN `modules-files`
        FILTER c._from == CONCAT('modules/', @moduleId)
        FOR f IN files
            FILTER f._id == c._to
            RETURN f
""")
  fun findFilesByModule(moduleId: String): List<File>

  @Query("""
    FOR c IN `modules-files`
        FILTER c._to == CONCAT('files/', @fileId)
        FOR m IN modules
            FILTER m._id == c._from
            RETURN m
""")
  fun findModulesByFile(fileId: String): List<Module>
}
