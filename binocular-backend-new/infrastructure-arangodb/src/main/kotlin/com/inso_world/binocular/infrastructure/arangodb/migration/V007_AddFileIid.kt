package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import org.springframework.stereotype.Component
import kotlin.uuid.Uuid

/**
 * Adds the `iid` field to existing files that don't have one.
 */
@Component
@Suppress("ktlint:standard:class-naming")
class V007_AddFileIid : Migration {
    companion object {
        private val logger by logger()
    }

    override val version = 1770713300
    override val description = "Add iid (UUID) field to files collection"

    override fun migrate(db: ArangoDatabase) {
        if (!db.collection("files").exists()) {
            return
        }

        val keysWithoutIid =
            db
                .query(
                    "FOR f IN files FILTER f.iid == null RETURN f._key",
                    String::class.java,
                ).asListRemaining()

        if (keysWithoutIid.isEmpty()) {
            logger.info("No files need iid backfill")
            return
        }

        val updates =
            keysWithoutIid.map { key ->
                mapOf("_key" to key, "iid" to Uuid.random().toString())
            }

        db.query(
            """
            FOR updt IN @updates
            UPDATE updt._key WITH { iid: updt.iid } IN files
            """.trimIndent(),
            Void::class.java,
            mapOf("updates" to updates),
        )

        logger.info("Updated {} files with new iid", keysWithoutIid.size)
    }
}
