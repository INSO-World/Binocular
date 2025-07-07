package com.inso_world.binocular.infrastructure.arangodb.model.edge

import com.inso_world.binocular.model.MergeRequest
import com.inso_world.binocular.model.Note

/**
 * Domain model for a connection between a MergeRequest and a Note.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class MergeRequestNoteConnection(
    var id: String? = null,
    var from: MergeRequest,
    var to: Note,
)
