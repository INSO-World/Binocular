package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase

/**
 * Interface for ArangoDB migrations.
 *
 * Migrations are executed in order based on their [version] number.
 * Each migration runs exactly once - the framework tracks executed migrations
 * in a `_migrations` collection.
 *
 * ### Implementation
 * - Use a versioned naming convention: `V001_Description`, `V002_Description`
 * - Keep migrations idempotent where possible
 * - Use AQL queries for bulk operations
 *
 * ### Example
 * ```kotlin
 * @Component
 * class V001_AddUserEmail : Migration {
 *     override val version = 1
 *     override val description = "Add email field to users"
 *
 *     override fun migrate(db: ArangoDatabase) {
 *         db.query(
 *             "FOR u IN users FILTER u.email == null UPDATE u WITH { email: '' } IN users",
 *             Void::class.java
 *         )
 *     }
 * }
 * ```
 */
interface Migration {
    /**
     * Unique version number for ordering migrations.
     * Must be positive and unique across all migrations.
     */
    val version: Int

    /**
     * Human-readable description of what this migration does.
     */
    val description: String

    /**
     * Executes the migration against the given database.
     *
     * @param db The ArangoDB database instance
     */
    fun migrate(db: ArangoDatabase)
}