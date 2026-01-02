package com.inso_world.binocular.web.graphql.model

import com.inso_world.binocular.model.File
import com.inso_world.binocular.model.Stats

/**
 * Wrapper representing a file within a commit including change statistics and diff hunks.
 */
data class FileInCommitDto(
    val file: File,
    val stats: Stats,
    val hunks: List<Hunk> = emptyList(),
)
