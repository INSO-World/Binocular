package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import org.springframework.stereotype.Component
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Backfills a synthetic `gid` on accounts that have a null or empty `gid`.
 *
 * `Account.gid` is the account's global ID as assigned by the source platform
 * (e.g. GitHub's GraphQL `node.id`). It is a required `@field:NotBlank` field on
 * the domain model and forms the natural key `Account.Key(platform, gid)`.
 *
 * Accounts persisted before `gid` existed (or seeded without one) have no `gid`,
 * which breaks mapping/lookup. Since we cannot recover the real platform ID
 * retroactively, this migration generates a synthetic, opaque, obviously
 * Binocular-origin global ID that loosely follows GitHub's node-ID shape.
 *
 * The generated format is `U_bino_<uuid>`:
 * - `U_` = GitHub's User-node prefix convention.
 * - `bino_` = unmistakable Binocular-generated marker so it can never be confused
 *   with a real platform ID.
 *
 * This value is opaque and Binocular-generated — it is NOT a real GitHub/GitLab
 * global ID.
 */
@OptIn(ExperimentalUuidApi::class)
@Component
@Suppress("ktlint:standard:class-naming")
class V011_AddAccountGid : Migration {
    companion object {
        private val logger by logger()
    }

    override val version = 1783700000
    override val description = "Backfill synthetic gid on accounts collection"

    override fun migrate(db: ArangoDatabase) {
        // Get all account keys that have null or empty gid
        val keysWithoutGid =
            db
                .query(
                    "FOR a IN accounts FILTER a.gid == null || a.gid == '' RETURN a._key",
                    String::class.java,
                ).asListRemaining()

        if (keysWithoutGid.isEmpty()) {
            logger.info("No accounts need gid backfill")
            return
        }

        // Generate synthetic gids and create update map
        val updates =
            keysWithoutGid.map { key ->
                mapOf(
                    "_key" to key,
                    "gid" to "U_bino_${Uuid.random()}",
                )
            }

        // Batch update all accounts
        db.query(
            """
            FOR updt IN @updates
            UPDATE updt._key WITH { gid: updt.gid } IN accounts
            """.trimIndent(),
            Void::class.java,
            mapOf("updates" to updates),
        )

        logger.info("Updated {} accounts with new gid", keysWithoutGid.size)
    }
}
