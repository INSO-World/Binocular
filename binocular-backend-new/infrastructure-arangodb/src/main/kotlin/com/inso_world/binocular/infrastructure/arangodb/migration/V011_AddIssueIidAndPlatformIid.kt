package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import org.springframework.stereotype.Component
import kotlin.uuid.Uuid

/**
 * Normalizes `iid`, `platformIid`, and `gid` for existing issues.
 *
 * In legacy dumps, `iid` was stored as an integer platform issue number,
 * and `id` was the global ID (gid). This migration:
 * - Copies numeric `iid` to `platformIid`
 * - Sets `gid` from legacy `id` or stringified `iid`
 * - Replaces `iid` with a UUID string
 */
@Component
@Suppress("ktlint:standard:class-naming")
class V011_AddIssueIidAndPlatformIid : Migration {
    companion object {
        private val logger by logger()
    }

    override val version = 1770713700
    override val description = "Add UUID iid, platformIid, and gid to issues collection"

    override fun migrate(db: ArangoDatabase) {
        if (!db.collection("issues").exists()) {
            return
        }

        val docs =
            db
                .query(
                    """
                    FOR i IN issues
                    FILTER i.platformIid == null || i.gid == null || i.iid == null || IS_NUMBER(i.iid)
                    RETURN { _key: i._key, iid: i.iid, id: i.id, gid: i.gid, platformIid: i.platformIid }
                    """.trimIndent(),
                    Map::class.java,
                ).asListRemaining()

        if (docs.isEmpty()) {
            logger.info("No issues need iid/platformIid/gid backfill")
            return
        }

        val updates =
            docs.map { doc ->
                val key = doc["_key"] as String
                val rawIid = doc["iid"]
                val rawId = doc["id"] as? String
                val existingGid = doc["gid"] as? String
                val existingPlatformIid = doc["platformIid"] as? Number

                val platformIid =
                    existingPlatformIid?.toInt()
                        ?: (rawIid as? Number)?.toInt()
                val gid = existingGid ?: rawId ?: rawIid?.toString() ?: key
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
                    "gid" to gid,
                )
            }

        db.query(
            """
            FOR updt IN @updates
            UPDATE updt._key WITH {
                iid: updt.iid,
                platformIid: updt.platformIid,
                gid: updt.gid
            } IN issues
            """.trimIndent(),
            Void::class.java,
            mapOf("updates" to updates),
        )

        logger.info("Updated {} issues with iid/platformIid/gid", docs.size)
    }
}
