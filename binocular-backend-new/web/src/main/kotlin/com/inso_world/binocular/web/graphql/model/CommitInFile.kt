package com.inso_world.binocular.web.graphql.model

/**
 * Wrapper element representing a commit entry in a file's commits connection.
 */
data class CommitInFile(
    val commit: CommitDto
)
