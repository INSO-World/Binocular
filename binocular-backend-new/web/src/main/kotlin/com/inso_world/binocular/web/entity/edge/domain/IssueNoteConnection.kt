package com.inso_world.binocular.web.entity.edge.domain

import com.inso_world.binocular.web.entity.Issue
import com.inso_world.binocular.web.entity.Note

/**
 * Domain model for a connection between an Issue and a Note.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class IssueNoteConnection(
  var id: String? = null,
  var from: Issue,
  var to: Note
)
