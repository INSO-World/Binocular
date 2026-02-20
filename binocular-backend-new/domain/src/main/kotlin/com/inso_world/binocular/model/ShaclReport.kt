package com.inso_world.binocular.model

data class ShaclReport(
    val conforms: Boolean,
    val criticalErrors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val rawRdf: String = ""
)
