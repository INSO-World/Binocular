package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

data class ModuleSizeCountEntity(
    var module: String = "",
    var loc: Long = 0,
    var changeFrequency: Long = 0,
)
