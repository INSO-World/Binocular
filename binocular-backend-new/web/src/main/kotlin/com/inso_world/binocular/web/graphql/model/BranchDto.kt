package com.inso_world.binocular.web.graphql.model

data class BranchDto(
    var id: String? = null,
    var name: String? = null,
    var branch: String? = null,
    var active: Boolean? = null,
    var tracksFileRenames: Boolean? = null,
    var latestCommit: String? = null,
)
