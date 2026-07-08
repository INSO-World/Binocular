package com.inso_world.binocular.infrastructure.arangodb.persistence.entity

/*
 * These are NOT stored ArangoDB collections (no @Document annotation).
 * Each class is just the "shape" of a row that an AQL aggregation query returns.
 * ArangoDB/Jackson deserializes the query result into these classes, so:
 *  - the property names must match the field names in the AQL RETURN { ... },
 *  - every property has a default value so the class can be created without arguments.
 * A dedicated projection mapper then turns each entity into its domain value object.
 */

/**
 * One CI error-rate bucket for the TIMELINE metric.
 *
 * @property period    the time bucket label (e.g. "06/2026"), matches the DB DATE_FORMAT output
 * @property failed    number of failed builds in this period
 * @property completed number of finished builds in this period (failed + success)
 */
data class CiRateBucketEntity(
    var period: String = "",
    var failed: Long = 0,
    var completed: Long = 0,
)

/**
 * Commit count for one author in one time bucket (input for the timeline bus factor).
 *
 * @property period       the time bucket label (e.g. "06/2026")
 * @property gitSignature the author's git signature ("Name <email>")
 * @property count        how many commits this author made in this period
 */
data class AuthorPeriodCountEntity(
    var period: String = "",
    var gitSignature: String = "",
    var count: Long = 0,
)

/**
 * Commit count for one author in one module (input for the per-module bus factor).
 *
 * @property module       the module path (e.g. "src/api")
 * @property gitSignature the author's git signature ("Name <email>")
 * @property count        how many commits this author made in this module
 */
data class ModuleAuthorCountEntity(
    var module: String = "",
    var gitSignature: String = "",
    var count: Long = 0,
)

/**
 * CI error-rate numbers aggregated for a single module.
 *
 * @property module    the module path (e.g. "src/api")
 * @property failed    number of failed builds linked to this module
 * @property completed number of finished builds linked to this module (failed + success)
 */
data class CiRatePerModuleEntity(
    var module: String = "",
    var failed: Long = 0,
    var completed: Long = 0,
)
