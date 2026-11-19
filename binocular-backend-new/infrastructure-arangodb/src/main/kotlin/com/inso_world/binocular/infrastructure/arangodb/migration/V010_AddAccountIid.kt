package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import org.springframework.stereotype.Component
import kotlin.uuid.Uuid

/**
 * Adds the `iid` and `gid` fields to existing accounts that don't have them.
 */
@Component
@Suppress("ktlint:standard:class-naming")
class V010_AddAccountIid : Migration {
    companion object {
        private val logger by logger()
    }

    override val version = 1770713600
    override val description = "Add iid (UUID) and gid fields to accounts collection"

    override fun migrate(db: ArangoDatabase) {
        if (!db.collection("accounts").exists()) {
            return
        }

        val docs =
            db
                .query(
                    """
                    FOR a IN accounts
                    FILTER a.iid == null || a.gid == null
                    RETURN { _key: a._key, login: a.login, hasIid: a.iid != null, gid: a.gid }
                    """.trimIndent(),
                    Map::class.java,
                ).asListRemaining()

        if (docs.isEmpty()) {
            logger.info("No accounts need iid/gid backfill")
            return
        }

        val updates =
            docs.map { doc ->
                val key = doc["_key"] as String
                val hasIid = doc["hasIid"] as? Boolean ?: false
                val gid = doc["gid"] as? String ?: doc["login"] as? String ?: key
                val map = mutableMapOf<String, Any?>("_key" to key, "gid" to gid)
                if (!hasIid) {
                    map["iid"] = Uuid.random().toString()
                }
                map
            }

        db.query(
            """
            FOR updt IN @updates
            UPDATE updt._key WITH updt IN accounts
            """.trimIndent(),
            Void::class.java,
            mapOf("updates" to updates),
        )

        logger.info("Updated {} accounts with iid/gid", docs.size)
    }
}
