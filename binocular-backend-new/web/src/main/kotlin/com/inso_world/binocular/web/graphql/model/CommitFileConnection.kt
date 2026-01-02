package com.inso_world.binocular.web.graphql.model

/**
 * Wrapper connection for commit files, exposing a data list as required by legacy schema.
 * Mirrors GraphQL type CommitFileConnection { data: [CommitFile]! }.
 */
data class CommitFileConnection(
    val count: Int,
    val page: Int,
    val perPage: Int,
    val data: List<CommitFile>
)
