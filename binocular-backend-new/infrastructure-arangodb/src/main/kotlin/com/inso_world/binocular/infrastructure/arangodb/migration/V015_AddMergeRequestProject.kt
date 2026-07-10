package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.infrastructure.arangodb.InfrastructureConfig
import org.springframework.stereotype.Component

/**
 * Backfills the `project` reference on the `mergeRequests` collection.
 *
 * Every merge request is tied to exactly one project (a project may have 0..* merge requests);
 * a merge request cannot be stored without a project. The link is the `@Ref(lazy = true)`
 * field on `MergeRequestEntity`, which Spring Data ArangoDB serialises as the referenced
 * document's `_id` (e.g. `"projects/<key>"`) stored in a `project` field on each
 * merge request document.
 *
 * Merge request documents persisted before this relationship existed (e.g. from a real-data
 * dump) have a null/missing `project` field. This migration wires all such merge requests
 * to the **default project** — the one created by `V000_AddProject` — so every
 * merge request points at a valid project.
 *
 * Only the authoritative `mergeRequest.project` reference is backfilled. The reverse
 * `@Ref var mergeRequests` list on `ProjectEntity` is intentionally not updated — it is
 * a redundant, secondary reference not required by the read/create paths.
 */
@Component
@Suppress("ktlint:standard:class-naming")
class V015_AddMergeRequestProject(
    private val infraConfig: InfrastructureConfig,
) : Migration {
    companion object {
        private val logger by logger()
    }

    override val version = 1783700400
    override val description = "Backfill default project reference on mergeRequests collection"

    override fun migrate(db: ArangoDatabase) {
        // Resolve the default project's _id
        val defaultProjectName = infraConfig.arangodb.migration.defaultProjectName
        val projectIdResult =
            db
                .query(
                    "FOR p IN projects FILTER p.name == @name LIMIT 1 RETURN p._id",
                    String::class.java,
                    mapOf("name" to defaultProjectName),
                ).asListRemaining()

        if (projectIdResult.isEmpty()) {
            logger.info("Default project '{}' not found, skipping merge request project backfill", defaultProjectName)
            return
        }

        val projectId = projectIdResult[0]

        // Get all merge request keys that are missing the project reference
        val keysWithoutProject =
            db
                .query(
                    "FOR mr IN mergeRequests FILTER mr.project == null RETURN mr._key",
                    String::class.java,
                ).asListRemaining()

        if (keysWithoutProject.isEmpty()) {
            logger.info("No merge requests need project backfill")
            return
        }

        // Generate update map with shared project _id
        val updates =
            keysWithoutProject.map { key ->
                mapOf("_key" to key, "project" to projectId)
            }

        // Batch update all merge requests
        db.query(
            """
            FOR updt IN @updates
            UPDATE updt._key WITH { project: updt.project } IN mergeRequests
            """.trimIndent(),
            Void::class.java,
            mapOf("updates" to updates),
        )

        logger.info("Updated {} mergeRequests with default project reference", keysWithoutProject.size)
    }
}
