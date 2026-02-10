package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import org.springframework.stereotype.Component
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Adds the `iid` field to existing commits that don't have one.
 *
 * The `iid` (immutable identifier) is a UUID that provides a stable identity
 * for commits independent of the ArangoDB document key. This migration
 * backfills the field for any commits created before this field was added.
 *
 * Uses Kotlin's [Uuid] class to generate UUIDs for consistency with domain model.
 */
@OptIn(ExperimentalUuidApi::class)
@Component
@Suppress("ktlint:standard:class-naming")
class V000_AddProject : Migration {
    companion object {
        private val logger by logger()
    }

    override val version = 1770656575
    override val description = "Create default project for binocular migrations"

    override fun migrate(db: ArangoDatabase) {
        val iid = Uuid.random().toString()

        val query =
            """
            INSERT {
                iid: "$iid",
                name: "INSO-World/Binocular",
                description: "Binocular is a tool for visualizing data from various software-engineering tools."
            }
            INTO projects
            RETURN NEW
            """.trimIndent()
        val result =
            db
                .query(
                    query,
                    Map::class.java,
                ).asListRemaining()

        logger.info("Inserted project: {}", result)
    }
}
