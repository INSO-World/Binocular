package com.inso_world.binocular.model.metrics

data class CiRateBucket(val period: String, val failed: Long, val completed: Long)
data class AuthorPeriodCount(val period: String, val gitSignature: String, val count: Long)
data class AuthorCountPerModule(val module: String, val gitSignature: String, val count: Long)


data class CiRatePerModule(    val module: String,
                               val failed: Long,
                               val completed: Long,)
