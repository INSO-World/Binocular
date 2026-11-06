package com.inso_world.binocular.infrastructure.arangodb.model.edge

import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.User

/**
 * Domain model for a connection between an Account and a User.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class AccountUserConnection(
    var id: String? = null,
    var from: Account,
    var to: User,
)
