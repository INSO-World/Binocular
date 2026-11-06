package com.inso_world.binocular.web.graphql.model

import com.inso_world.binocular.model.Commit

/**
 * Wrapper element representing a commit entry in a file's commits connection.
 */
data class CommitInFile(
    val commit: Commit
)
