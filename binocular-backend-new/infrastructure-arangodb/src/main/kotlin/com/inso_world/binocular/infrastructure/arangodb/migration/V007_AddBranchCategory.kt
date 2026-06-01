package com.inso_world.binocular.infrastructure.arangodb.migration

import com.arangodb.ArangoDatabase
import com.inso_world.binocular.core.delegates.logger
import org.springframework.stereotype.Component

/**
 * Sets a default `category` on branch documents that lack one.
 *
 * Legacy branch documents (pre-dating the current
 * [BranchEntity][com.inso_world.binocular.infrastructure.arangodb.persistence.entity.BranchEntity])
 * have no `category` field. The entity requires a non-null value, so this
 * migration assigns `"LOCAL_BRANCH"` — the string constant matching
 * [ReferenceCategory.LOCAL_BRANCH][com.inso_world.binocular.model.vcs.ReferenceCategory.LOCAL_BRANCH]
 * — on every branch where `category == null`.
 *
 * This migration is idempotent.
 *
 * **Note for future maintainers:** If `ReferenceCategory` is renamed or reordered,
 * this literal must stay in sync. Prefer using the `.name` suffix of the enum
 * constant as the source of truth for this string value.
 */
@Component
@Suppress("ktlint:standard:class-naming")
class V007_AddBranchCategory : Migration {
    companion object {
        private val logger by logger()
    }

    override val version = 1770800001
    override val description = "Add default category to branches missing one"

    override fun migrate(db: ArangoDatabase) {
        val result =
            db
                .query(
                    """
                    FOR b IN branches
                    FILTER b.category == null
                    UPDATE b WITH { category: "LOCAL_BRANCH" } IN branches
                    RETURN 1
                    """.trimIndent(),
                    Int::class.java,
                ).asListRemaining()

        val updatedCount = result.size
        if (updatedCount > 0) {
            logger.info("Updated {} branch(es) with category", updatedCount)
        } else {
            logger.info("No branches need category backfill")
        }
    }
}
