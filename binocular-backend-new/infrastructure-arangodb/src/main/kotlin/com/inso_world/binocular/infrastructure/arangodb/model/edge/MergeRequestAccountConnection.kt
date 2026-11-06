package com.inso_world.binocular.infrastructure.arangodb.model.edge

import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.MergeRequest

/**
 * Domain model for a connection between a MergeRequest and an Account.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class MergeRequestAccountConnection(
    var id: String? = null,
    var from: MergeRequest,
    var to: Account,
)
