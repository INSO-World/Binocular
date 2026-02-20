package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import org.springframework.stereotype.Component
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Adds the `iid` field to existing users that don't have one.
 *
 * The `iid` (immutable identifier) is a UUID that provides a stable identity
 * for users independent of the ArangoDB document key. This migration
 * backfills the field for any users created before this field was added.
 *
 * Uses Kotlin's [Uuid] class to generate UUIDs for consistency with domain model.
 */
@OptIn(ExperimentalUuidApi::class)
@Component
class V002_AddUserIid : Migration {
    companion object {
        private val logger by logger()
    }

    override val version = 1771595853
    override val description = "Add iid (UUID) field to users collection"

    override fun migrate(db: ArangoDatabase) {
        // Get all commit keys that don't have an iid
        val keysWithoutIid = db.query(
            "FOR c IN users FILTER c.iid == null RETURN c._key",
            String::class.java,
        ).asListRemaining()

        if (keysWithoutIid.isEmpty()) {
            logger.info("No users need iid backfill")
            return
        }

        // Generate Kotlin UUIDs and create update map
        val updates = keysWithoutIid.map { key ->
            mapOf("_key" to key, "iid" to Uuid.random().toString())
        }

        // Batch update all users
        db.query(
            """
            FOR updt IN @updates
            UPDATE updt._key WITH { iid: updt.iid } IN users
            """.trimIndent(),
            Void::class.java,
            mapOf("updates" to updates),
        )

        logger.info("Updated {} users with new iid", keysWithoutIid.size)
    }
}
