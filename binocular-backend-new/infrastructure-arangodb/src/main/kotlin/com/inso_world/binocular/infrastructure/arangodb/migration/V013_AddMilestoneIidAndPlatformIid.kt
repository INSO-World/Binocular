package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import org.springframework.stereotype.Component
import kotlin.uuid.Uuid

/**
 * Normalizes `iid` and `platformIid` for existing milestones.
 */
@Component
@Suppress("ktlint:standard:class-naming")
class V013_AddMilestoneIidAndPlatformIid : Migration {
    companion object {
        private val logger by logger()
    }

    override val version = 1770713900
    override val description = "Add UUID iid and platformIid to milestones collection"

    override fun migrate(db: ArangoDatabase) {
        if (!db.collection("milestones").exists()) {
            return
        }

        val docs =
            db
                .query(
                    """
                    FOR m IN milestones
                    FILTER m.platformIid == null || m.iid == null || IS_NUMBER(m.iid)
                    RETURN { _key: m._key, iid: m.iid, platformIid: m.platformIid }
                    """.trimIndent(),
                    Map::class.java,
                ).asListRemaining()

        if (docs.isEmpty()) {
            logger.info("No milestones need iid/platformIid backfill")
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
            } IN milestones
            """.trimIndent(),
            Void::class.java,
            mapOf("updates" to updates),
        )

        logger.info("Updated {} milestones with iid/platformIid", docs.size)
    }
}
