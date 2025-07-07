package com.inso_world.binocular.infrastructure.arangodb.model.edge

import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Note

/**
 * Domain model for a connection between a Note and an Account.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class NoteAccountConnection(
    var id: String? = null,
    var from: Note,
    var to: Account,
)
