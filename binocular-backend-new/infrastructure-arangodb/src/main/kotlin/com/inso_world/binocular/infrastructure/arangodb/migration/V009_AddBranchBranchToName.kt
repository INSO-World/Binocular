package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import org.springframework.stereotype.Component

/**
 * Resolves the `head` reference on branch documents from the legacy `latestCommit` SHA.
 *
 * Legacy branch documents store the latest commit SHA in a `latestCommit` field
 * but lack a `head` document reference. The current
 * [BranchEntity][com.inso_world.binocular.infrastructure.arangodb.persistence.entity.BranchEntity]
 * requires a non-null `@Ref head` field. This migration:
 * 1. Finds branches with `head == null` and a non-null `latestCommit` SHA
 * 2. Looks up the matching commit document in the `commits` collection
 * 3. Sets `head` to that commit's document ID
 *
 * This migration depends on the commit migrations (`V004`, `V005`) running first
 * via the monotonic version ordering, ensuring the `commits` collection has valid
 * `sha` fields for lookups.
 *
 * Branches with `latestCommit == null` or no matching commit are skipped — the
 * migration logs how many were updated vs. skipped, which is the only signal
 * that those branches will still fail deserialization.
 *
 * This migration is idempotent.
 */
@Component
@Suppress("ktlint:standard:class-naming")
class V009_AddBranchBranchToName : Migration {
    companion object {
        private val logger by logger()
    }

    override val version = 1780312174
    override val description = "Add branch.name from branch.branch property"

    override fun migrate(db: ArangoDatabase) {
        val result =
            db
                .query(
                    """
                    FOR b IN branches
                    FILTER b.name == null
                    UPDATE b WITH { name: b.branch } IN branches
                    RETURN 1
                    """.trimIndent(),
                    Int::class.java,
                ).asListRemaining()

        val updatedCount = result.size
        if (updatedCount > 0) {
            logger.info("Updated {} branch(es) with head reference", updatedCount)
        } else {
            logger.info("No branches need head reference backfill")
        }
    }
}
