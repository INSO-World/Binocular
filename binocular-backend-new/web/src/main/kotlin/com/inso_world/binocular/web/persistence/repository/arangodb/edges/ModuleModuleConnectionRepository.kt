package com.inso_world.binocular.web.persistence.repository.arangodb.edges

import com.arangodb.springframework.annotation.Query
import com.arangodb.springframework.repository.ArangoRepository
import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.entity.edge.ModuleModuleConnection
import org.springframework.stereotype.Repository

@Repository
interface ModuleModuleConnectionRepository: ArangoRepository<ModuleModuleConnection, String> {

  @Query("""
    FOR c IN `modules-modules`
        FILTER c._from == CONCAT('modules/', @moduleId)
        FOR m IN modules
            FILTER m._id == c._to
            RETURN m
""")
  fun findChildModulesByModule(moduleId: String): List<Module>

  @Query("""
    FOR c IN `modules-modules`
        FILTER c._to == CONCAT('modules/', @moduleId)
        FOR m IN modules
            FILTER m._id == c._from
            RETURN m
""")
  fun findParentModulesByModule(moduleId: String): List<Module>
}
