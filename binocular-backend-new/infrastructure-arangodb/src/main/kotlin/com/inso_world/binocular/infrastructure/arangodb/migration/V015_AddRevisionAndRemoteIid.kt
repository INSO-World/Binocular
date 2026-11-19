package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import org.springframework.stereotype.Component
import kotlin.uuid.Uuid

/**
 * Adds the `iid` field to existing revisions and remotes that don't have one.
 */
@Component
@Suppress("ktlint:standard:class-naming")
class V015_AddRevisionAndRemoteIid : Migration {
    companion object {
        private val logger by logger()
    }

    override val version = 1770714100
    override val description = "Add iid (UUID) field to revisions and remotes collections"

    override fun migrate(db: ArangoDatabase) {
        backfillCollectionIid(db, "revisions")
        backfillCollectionIid(db, "remotes")
    }

    private fun backfillCollectionIid(db: ArangoDatabase, collectionName: String) {
        if (!db.collection(collectionName).exists()) {
            return
        }

        val keysWithoutIid =
            db
                .query(
                    "FOR doc IN @@collection FILTER doc.iid == null RETURN doc._key",
                    String::class.java,
                    mapOf("@collection" to collectionName),
                ).asListRemaining()

        if (keysWithoutIid.isEmpty()) {
            logger.info("No {} need iid backfill", collectionName)
            return
        }

        val updates =
            keysWithoutIid.map { key ->
                mapOf("_key" to key, "iid" to Uuid.random().toString())
            }

        db.query(
            """
            FOR updt IN @updates
            UPDATE updt._key WITH { iid: updt.iid } IN @@collection
            """.trimIndent(),
            Void::class.java,
            mapOf("updates" to updates, "@collection" to collectionName),
        )

        logger.info("Updated {} {} with new iid", keysWithoutIid.size, collectionName)
    }
}
