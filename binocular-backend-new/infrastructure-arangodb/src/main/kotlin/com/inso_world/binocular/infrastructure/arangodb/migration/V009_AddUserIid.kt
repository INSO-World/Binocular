package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import org.springframework.stereotype.Component
import kotlin.uuid.Uuid

/**
 * Adds the `iid` field to existing users that don't have one.
 */
@Component
@Suppress("ktlint:standard:class-naming")
class V009_AddUserIid : Migration {
    companion object {
        private val logger by logger()
    }

    override val version = 1770713500
    override val description = "Add iid (UUID) field to users collection"

    override fun migrate(db: ArangoDatabase) {
        if (!db.collection("users").exists()) {
            return
        }

        val keysWithoutIid =
            db
                .query(
                    "FOR u IN users FILTER u.iid == null RETURN u._key",
                    String::class.java,
                ).asListRemaining()

        if (keysWithoutIid.isEmpty()) {
            logger.info("No users need iid backfill")
            return
        }

        val updates =
            keysWithoutIid.map { key ->
                mapOf("_key" to key, "iid" to Uuid.random().toString())
            }

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
