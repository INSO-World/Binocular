package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import org.springframework.stereotype.Component
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Backfills a synthetic `gid` on issues that have a null or empty `gid`.
 *
 * `Issue.gid` is the issue's global ID as assigned by the source platform
 * (e.g. GitHub's GraphQL `node.id`). It is a required field on the domain
 * model and mirrors the non-nullable `IssueEntity.gid` in ArangoDB.
 *
 * Issues persisted before `gid` existed (or seeded without one) have no `gid`,
 * which breaks Spring Data ArangoDB deserialization into the Kotlin data class
 * (non-null constructor parameter throws NPE). Since we cannot recover the real
 * platform ID retroactively, this migration generates a synthetic, opaque,
 * obviously Binocular-origin global ID that loosely follows GitHub's node-ID shape.
 *
 * The generated format is `I_bino_<uuid>`:
 * - `I_` = GitHub's Issue-node prefix convention.
 * - `bino_` = unmistakable Binocular-generated marker so it can never be confused
 *   with a real platform ID.
 *
 * This value is opaque and Binocular-generated — it is NOT a real GitHub/GitLab
 * global ID.
 */
@OptIn(ExperimentalUuidApi::class)
@Component
@Suppress("ktlint:standard:class-naming")
class V012_AddIssueGid : Migration {
    companion object {
        private val logger by logger()
    }

    override val version = 1783700100
    override val description = "Backfill synthetic gid on issues collection"

    override fun migrate(db: ArangoDatabase) {
        // Get all issue keys that have null or empty gid
        val keysWithoutGid =
            db
                .query(
                    "FOR i IN issues FILTER i.gid == null || i.gid == '' RETURN i._key",
                    String::class.java,
                ).asListRemaining()

        if (keysWithoutGid.isEmpty()) {
            logger.info("No issues need gid backfill")
            return
        }

        // Generate synthetic gids and create update map
        val updates =
            keysWithoutGid.map { key ->
                mapOf(
                    "_key" to key,
                    "gid" to "I_bino_${Uuid.random()}",
                )
            }

        // Batch update all issues
        db.query(
            """
            FOR updt IN @updates
            UPDATE updt._key WITH { gid: updt.gid } IN issues
            """.trimIndent(),
            Void::class.java,
            mapOf("updates" to updates),
        )

        logger.info("Updated {} issues with new gid", keysWithoutGid.size)
    }
}
