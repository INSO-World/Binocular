package com.inso_world.binocular.web.entity.edge.domain

import com.inso_world.binocular.web.entity.Module

/**
 * Domain model for a connection between two Modules.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class ModuleModuleConnection(
  var id: String? = null,
  var from: Module,
  var to: Module
)
