package com.inso_world.binocular.web.graphql.model

import com.inso_world.binocular.model.Platform

data class AccountDto(
    var id: String? = null,
    var platform: Platform? = null,
    var login: String? = null,
    var name: String? = null,
    var avatarUrl: String? = null,
    var url: String? = null,
)
