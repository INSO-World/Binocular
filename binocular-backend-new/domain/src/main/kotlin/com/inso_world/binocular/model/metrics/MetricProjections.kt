package com.inso_world.binocular.model.metrics

import org.hibernate.validator.constraints.Range

/*
 * Small database-agnostic value objects that carry the results of the metric aggregation
 * queries from the persistence layer up to the service. They hold plain numbers only -
 * no identity and no relationships to other domain objects.
 */

/**
 * CI build counts for one time bucket (timeline CI error rate).
 *
 * @property period    time bucket label, e.g. "06/2026"
 * @property failed    number of failed builds in this period
 * @property completed number of finished builds in this period (failed + success)
 */
data class CiRateBucket(
    val period: String,
    val failed: Long,
    val completed: Long
)

/**
 * Commit count for one author in one time bucket (input for the timeline bus factor).
 *
 * @property period       time bucket label, e.g. "06/2026"
 * @property gitSignature the author's git signature ("Name <email>")
 * @property count        commits this author made in this period
 */
data class AuthorPeriodCount(
    val period: String,
    val gitSignature: String,
    val count: Long
)

/**
 * Commit count for one author in one module (input for the per-module bus factor).
 *
 * @property module       the module path, e.g. "src/api"
 * @property gitSignature the author's git signature ("Name <email>")
 * @property count        commits this author made in this module
 */
data class AuthorCountPerModule(
    val module: String,
    val gitSignature: String,
    val count: Long
)

/**
 * CI build counts aggregated for one module.
 *
 * @property module    the module path, e.g. "src/api"
 * @property failed    number of failed builds linked to this module
 * @property completed number of finished builds linked to this module (failed + success)
 */
data class CiRatePerModule(
    val module: String,
    val failed: Long,
    val completed: Long,
)

/**
 * One author's share of the commits, used inside a bus factor result.
 *
 * @property gitSignature the author's git signature ("Name <email>")
 * @property percentage   this author's share of the total commits, between 0.0 and 1.0
 *                        (e.g. 0.6 = 60%); relative to the full total, including excluded authors
 */
data class AuthorContribution(
    val gitSignature: String,
    @field:Range(min = 0, max = 1) val percentage: Double,
)

data class ModuleSizeCount(
    val module: String,
    val loc: Long,
    val changeFrequency: Long,
)
