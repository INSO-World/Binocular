package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import org.springframework.stereotype.Component

/**
 * Backfills the `fullName` field on existing branch documents.
 *
 * Legacy branch documents (pre-dating the current
 * [BranchEntity][com.inso_world.binocular.infrastructure.arangodb.persistence.entity.BranchEntity])
 * store the branch name in a legacy `branch` field and lack a `fullName` field.
 * The current entity requires a non-null `fullName`, so this migration copies
 * the legacy `branch` value into `fullName` where it is missing.
 *
 * This migration is idempotent — it only updates documents where `fullName == null`.
 * If `branch` itself is null (should not happen in the realdata dump), it falls
 * back to the document `_key` so the field stays non-null.
 *
 * **Out of scope:** The legacy `branch` field is conceptually the `name` as well.
 * A separate migration (`V00X_AddBranchName.kt`) or a `@Field("branch")` annotation
 * on `BranchEntity.name` should handle that decision later.
 */
@Component
@Suppress("ktlint:standard:class-naming")
class V006_AddBranchFullName : Migration {
    companion object {
        private val logger by logger()
    }

    override val version = 1770800000
    override val description = "Add fullName field to branches from legacy branch field"

    override fun migrate(db: ArangoDatabase) {
        val result =
            db
                .query(
                    """
                    FOR b IN branches
                    FILTER b.fullName == null
                    UPDATE b WITH { fullName: b.branch != null ? b.branch : b._key } IN branches
                    RETURN 1
                    """.trimIndent(),
                    Int::class.java,
                ).asListRemaining()

        val updatedCount = result.size
        if (updatedCount > 0) {
            logger.info("Updated {} branch(es) with fullName", updatedCount)
        } else {
            logger.info("No branches need fullName backfill")
        }
    }
}
