package com.inso_world.binocular.model.metrics

data class CiRateBucket(val period: String, val failed: Long, val completed: Long)
data class AuthorPeriodCount(val period: String, val gitSignature: String, val count: Long)
