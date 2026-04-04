package com.inso_world.binocular.web.graphql.model

data class FileDto(
    var id: String? = null,
    var path: String? = null,
    var maxLength: Long? = null,
    var webUrl: String? = null,
    var revisions: List<RevisionDto> = emptyList(),
)
