package com.inso_world.binocular.web.entity.edge.domain

import com.inso_world.binocular.web.entity.Branch
import com.inso_world.binocular.web.entity.File

/**
 * Domain model for a connection between a Branch and a File.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class BranchFileConnection(
  var id: String? = null,
  var from: Branch,
  var to: File
)
