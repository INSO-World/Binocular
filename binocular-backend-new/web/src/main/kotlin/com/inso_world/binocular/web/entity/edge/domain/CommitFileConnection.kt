package com.inso_world.binocular.web.entity.edge.domain

import com.inso_world.binocular.web.entity.Commit
import com.inso_world.binocular.web.entity.File

/**
 * Domain model for a connection between a Commit and a File.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class CommitFileConnection(
  var id: String? = null,
  var from: Commit,
  var to: File,
  var lineCount: Int? = null
)
