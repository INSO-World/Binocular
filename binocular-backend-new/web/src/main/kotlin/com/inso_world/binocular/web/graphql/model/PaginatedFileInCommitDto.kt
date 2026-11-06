package com.inso_world.binocular.web.graphql.model

/**
 * Paginated container for FileInCommit items.
 */
data class PaginatedFileInCommitDto(
    val count: Int,
    val page: Int,
    val perPage: Int,
    val data: List<FileInCommitDto>,
)
