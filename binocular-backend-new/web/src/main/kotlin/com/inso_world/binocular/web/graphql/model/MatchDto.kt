package com.inso_world.binocular.web.graphql.model

data class MatchDto(
    val accountLogin: String?,
    val accountName: String?,
    val userGitSignature: String?,
    val matchLayer: String?,
    val confidence: Double?,
    val status: String?,
    val signals: String?,
    val matchId: String? = null,
    val reviewedBy: String? = null,
    val resolvedAt: String? = null,
    )
