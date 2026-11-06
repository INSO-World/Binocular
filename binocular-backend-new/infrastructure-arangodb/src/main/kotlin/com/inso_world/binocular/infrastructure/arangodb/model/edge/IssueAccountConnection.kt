package com.inso_world.binocular.infrastructure.arangodb.model.edge

import com.inso_world.binocular.model.Account
import com.inso_world.binocular.model.Issue
import com.inso_world.binocular.model.enums.IssueAccountRole

/**
 * Domain model for a connection between an Issue and an Account.
 * This class is database-agnostic and contains no persistence-specific annotations.
 */
data class IssueAccountConnection(
    var id: String? = null,
    var from: Issue,
    var to: Account,
    var role: IssueAccountRole? = null,
)
