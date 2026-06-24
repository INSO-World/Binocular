package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

import com.arangodb.springframework.annotation.Document
import org.springframework.data.annotation.Id

data class CiRateBucketEntity(
    var period: String = "",
    var failed: Long = 0,
    var completed: Long = 0,
)

data class AuthorPeriodCountEntity(
    var period: String = "",
    var gitSignature: String = "",
    var count: Long = 0,
)
