package com.inso_world.binocular.web.graphql.model

/**
 * Wrapper element representing a file entry in a commit's files connection.
 * This mirrors the GraphQL type CommitFile { file, stats }.
 */
data class CommitFile(
    val file: FileDto?,
    val stats: StatsDto?,
    val action: String? = null,
    val hunks: List<Hunk> = emptyList(),
    val commitId: String? = null,
)
