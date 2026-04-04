package com.inso_world.binocular.web.graphql.model

data class RevisionDto(
    var id: String? = null,
    val content: String? = null,
    val commit: CommitDto,
    val file: FileDto,
)
