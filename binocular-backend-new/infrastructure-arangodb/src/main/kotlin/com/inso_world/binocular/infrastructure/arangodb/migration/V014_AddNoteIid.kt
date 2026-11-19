package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import org.springframework.stereotype.Component
import kotlin.uuid.Uuid

/**
 * Adds the `iid` field to existing notes that don't have one.
 */
@Component
@Suppress("ktlint:standard:class-naming")
class V014_AddNoteIid : Migration {
    companion object {
        private val logger by logger()
    }

    override val version = 1770714000
    override val description = "Add iid (UUID) field to notes collection"

    override fun migrate(db: ArangoDatabase) {
        if (!db.collection("notes").exists()) {
            return
        }

        val keysWithoutIid =
            db
                .query(
                    "FOR n IN notes FILTER n.iid == null RETURN n._key",
                    String::class.java,
                ).asListRemaining()

        if (keysWithoutIid.isEmpty()) {
            logger.info("No notes need iid backfill")
            return
        }

        val updates =
            keysWithoutIid.map { key ->
                mapOf("_key" to key, "iid" to Uuid.random().toString())
            }

        db.query(
            """
            FOR updt IN @updates
            UPDATE updt._key WITH { iid: updt.iid } IN notes
            """.trimIndent(),
            Void::class.java,
            mapOf("updates" to updates),
        )

        logger.info("Updated {} notes with new iid", keysWithoutIid.size)
    }
}
