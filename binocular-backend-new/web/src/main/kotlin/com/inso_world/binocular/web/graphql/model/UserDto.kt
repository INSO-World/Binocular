package com.inso_world.binocular.web.graphql.model

data class UserDto(
    var id: String? = null,
    var name: String? = null,
    var email: String? = null,
    var gitSignature: String? = null,
)
