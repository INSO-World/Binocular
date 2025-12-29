package com.inso_world.binocular.web.graphql.model

import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Stats

/**
 * Wrapper element representing a file entry in a commit's files connection.
 * This mirrors the GraphQL type CommitFile { file, stats }.
 */
data class CommitFile(
    val file: File?,
    val stats: Stats?,
    val hunks: List<Hunk> = emptyList(),
)
