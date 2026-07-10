package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.infrastructure.arangodb.InfrastructureConfig
import org.springframework.stereotype.Component

/**
 * Backfills the `project` reference on the `issues` collection.
 *
 * Every issue is tied to exactly one project (a project may have 0..* issues);
 * an issue cannot be stored without a project. The link is the `@Ref(lazy = true)`
 * field on `IssueEntity`, which Spring Data ArangoDB serialises as the referenced
 * document's `_id` (e.g. `"projects/<key>"`) stored in a `project` field on each
 * issue document.
 *
 * Issue documents persisted before this relationship existed (e.g. from a real-data
 * dump) have a null/missing `project` field. This migration wires all such issues
 * to the **default project** — the one created by `V000_AddProject` — so every
 * issue points at a valid project.
 *
 * Only the authoritative `issue.project` reference is backfilled. The reverse
 * `@Ref var issues` list on `ProjectEntity` is intentionally not updated — it is
 * a redundant, secondary reference not required by the read/create paths.
 */
@Component
@Suppress("ktlint:standard:class-naming")
class V013_AddIssueProject(
    private val infraConfig: InfrastructureConfig,
) : Migration {
    companion object {
        private val logger by logger()
    }

    override val version = 1783700200
    override val description = "Backfill default project reference on issues collection"

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
            logger.info("Default project '{}' not found, skipping issue project backfill", defaultProjectName)
            return
        }

        val projectId = projectIdResult[0]

        // Get all issue keys that are missing the project reference
        val keysWithoutProject =
            db
                .query(
                    "FOR i IN issues FILTER i.project == null RETURN i._key",
                    String::class.java,
                ).asListRemaining()

        if (keysWithoutProject.isEmpty()) {
            logger.info("No issues need project backfill")
            return
        }

        // Generate update map with shared project _id
        val updates =
            keysWithoutProject.map { key ->
                mapOf("_key" to key, "project" to projectId)
            }

        // Batch update all issues
        db.query(
            """
            FOR updt IN @updates
            UPDATE updt._key WITH { project: updt.project } IN issues
            """.trimIndent(),
            Void::class.java,
            mapOf("updates" to updates),
        )

        logger.info("Updated {} issues with default project reference", keysWithoutProject.size)
    }
}
