package com.inso_world.binocular.web.entity.edge.domain

import com.inso_world.binocular.web.entity.Module
import com.inso_world.binocular.web.entity.File

/**
 * Domain model for a connection between a Module and a File.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class ModuleFileConnection(
  var id: String? = null,
  var from: Module,
  var to: File
)
