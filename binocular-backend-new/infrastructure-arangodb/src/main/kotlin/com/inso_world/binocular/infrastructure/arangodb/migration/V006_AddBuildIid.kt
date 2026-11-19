package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import org.springframework.stereotype.Component
import kotlin.uuid.Uuid

/**
 * Adds the `iid` field to existing builds that don't have one.
 */
@Component
@Suppress("ktlint:standard:class-naming")
class V006_AddBuildIid : Migration {
    companion object {
        private val logger by logger()
    }

    override val version = 1770713200
    override val description = "Add iid (UUID) field to builds collection"

    override fun migrate(db: ArangoDatabase) {
        if (!db.collection("builds").exists()) {
            return
        }

        val keysWithoutIid =
            db
                .query(
                    "FOR b IN builds FILTER b.iid == null RETURN b._key",
                    String::class.java,
                ).asListRemaining()

        if (keysWithoutIid.isEmpty()) {
            logger.info("No builds need iid backfill")
            return
        }

        val updates =
            keysWithoutIid.map { key ->
                mapOf("_key" to key, "iid" to Uuid.random().toString())
            }

        db.query(
            """
            FOR updt IN @updates
            UPDATE updt._key WITH { iid: updt.iid } IN builds
            """.trimIndent(),
            Void::class.java,
            mapOf("updates" to updates),
        )

        logger.info("Updated {} builds with new iid", keysWithoutIid.size)
    }
}
