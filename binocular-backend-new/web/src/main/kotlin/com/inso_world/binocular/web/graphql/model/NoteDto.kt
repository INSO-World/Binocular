package com.inso_world.binocular.web.graphql.model

data class NoteDto(
    var id: String? = null,
    var body: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null,
    var system: Boolean? = null,
    var resolvable: Boolean? = null,
    var resolved: Boolean? = null,
    var type: String? = null,
    var webUrl: String? = null,
    var confidential: Boolean? = null,
    var internal: Boolean? = null,
    var imported: Boolean? = null,
    var importedFrom: String? = null,
)
