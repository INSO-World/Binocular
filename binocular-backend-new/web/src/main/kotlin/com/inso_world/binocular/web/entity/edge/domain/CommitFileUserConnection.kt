package com.inso_world.binocular.web.entity.edge.domain

import com.inso_world.binocular.web.entity.File
import com.inso_world.binocular.web.entity.User

/**
 * Domain model for a connection between a File and a User.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class CommitFileUserConnection(
  var id: String? = null,
  var from: File,
  var to: User
)
