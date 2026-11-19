package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import org.springframework.stereotype.Component
import kotlin.uuid.Uuid

/**
 * Normalizes `iid` and `platformIid` for existing merge requests.
 *
 * In legacy dumps, `iid` was stored as an integer platform MR number.
 * This migration:
 * - Copies numeric `iid` to `platformIid`
 * - Replaces `iid` with a UUID string
 */
@Component
@Suppress("ktlint:standard:class-naming")
class V012_AddMergeRequestIidAndPlatformIid : Migration {
    companion object {
        private val logger by logger()
    }

    override val version = 1770713800
    override val description = "Add UUID iid and platformIid to mergeRequests collection"

    override fun migrate(db: ArangoDatabase) {
        if (!db.collection("mergeRequests").exists()) {
            return
        }

        val docs =
            db
                .query(
                    """
                    FOR mr IN mergeRequests
                    FILTER mr.platformIid == null || mr.iid == null || IS_NUMBER(mr.iid)
                    RETURN { _key: mr._key, iid: mr.iid, platformIid: mr.platformIid }
                    """.trimIndent(),
                    Map::class.java,
                ).asListRemaining()

        if (docs.isEmpty()) {
            logger.info("No merge requests need iid/platformIid backfill")
            return
        }

        val updates =
            docs.map { doc ->
                val key = doc["_key"] as String
                val rawIid = doc["iid"]
                val existingPlatformIid = doc["platformIid"] as? Number

                val platformIid =
                    existingPlatformIid?.toInt()
                        ?: (rawIid as? Number)?.toInt()
                val iid =
                    if (rawIid is String && runCatching { Uuid.parse(rawIid) }.isSuccess) {
                        rawIid
                    } else {
                        Uuid.random().toString()
                    }

                mapOf(
                    "_key" to key,
                    "iid" to iid,
                    "platformIid" to platformIid,
                )
            }

        db.query(
            """
            FOR updt IN @updates
            UPDATE updt._key WITH {
                iid: updt.iid,
                platformIid: updt.platformIid
            } IN mergeRequests
            """.trimIndent(),
            Void::class.java,
            mapOf("updates" to updates),
        )

        logger.info("Updated {} merge requests with iid/platformIid", docs.size)
    }
}
