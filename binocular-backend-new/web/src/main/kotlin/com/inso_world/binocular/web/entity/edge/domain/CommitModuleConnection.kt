package com.inso_world.binocular.web.entity.edge.domain

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.Module

/**
 * Domain model for a connection between a Commit and a Module.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class CommitModuleConnection(
  var id: String? = null,
  var from: Commit,
  var to: Module
)
