package com.inso_world.binocular.web.entity.edge

import com.arangodb.springframework.annotation.Edge
import com.arangodb.springframework.annotation.From
import com.arangodb.springframework.annotation.To
import com.inso_world.binocular.web.entity.Module
import org.springframework.data.annotation.Id

@Edge(value = "modules-modules")
data class ModuleModuleConnection(
  @Id var id: String? = null,
  @From var from: Module,
  @To var to: Module
)
